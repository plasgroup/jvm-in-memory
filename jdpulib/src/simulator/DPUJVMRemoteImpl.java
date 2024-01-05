package simulator;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.*;

public class DPUJVMRemoteImpl extends UnicastRemoteObject implements DPUJVMRemote {
    private int perThreadParameterQueueLength;
    private int perThreadParameterQueueSize;
    public int id;
    private int threadCount;
    Runnable[] threads;
    ExecutorService service;
    static CountDownLatch countDownLatch;
    public Object[] heap;
    public int currentHeapSize = 0;
    public int maxHeapSize = PIMRemoteJVMConfiguration.heapSize;
    public int[] currentParamPointer;
    public int maxParamSize = PIMRemoteJVMConfiguration.maxParameterSpaceSize;
    public int currentMetaspaceSize = 0;
    public int maxMetaspaceSize = PIMRemoteJVMConfiguration.maxMetaspaceSize;
    public int[] parameterQueue;
    public Object[] metaSpace;
    public Object[] resultQueue;
    int metaSpaceIndex = 0;
    int heapSpaceIndex = 0;
    int[] taskletParameterTop;

    class ProcessorBinary implements Runnable {
        int threadID;
        public ProcessorBinary(int threadID){
            this.threadID = threadID;
        }

        @Override
        public void run() {
            //System.out.println("run thread, ID = " + threadID);
            for(int i = 0; i < 10; i++){
                //System.out.println("param queue " + i + ":" + parameterQueue[i]);
            }
            int pt = threadID * perThreadParameterQueueLength;
            int taskId =  parameterQueue[pt];
            Class c = (Class) metaSpace[parameterQueue[pt + 1]];

            if(metaSpace[parameterQueue[pt + 2]] instanceof Constructor){
                Constructor constructor = (Constructor) metaSpace[parameterQueue[pt + 2]];
                int instanceIndex = parameterQueue[pt + 3];
                int paramCount =  constructor.getParameterCount();
                //System.out.println("constructor param count = " + paramCount);
                if(paramCount == 0){
                    try {
                        heap[instanceIndex] = constructor.newInstance();
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }else{
                    Object[] params = new Object[paramCount];
                    pt += 4;
                    for(int i = 0; i < paramCount; i++){
                        params[i] = parameterQueue[pt++];
                    }
                    try {
                        heap[instanceIndex] = constructor.newInstance(params);
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }
                taskletParameterTop[threadID] = perThreadParameterQueueLength * threadID;
                currentParamPointer[threadID] = perThreadParameterQueueSize * threadID;
                countDownLatch.countDown();
                return;
            }
            Method m = (Method) metaSpace[parameterQueue[pt + 2]];
//
//            System.out.println("class = " + c.getSimpleName());
//            System.out.println("method = " + m.getName());
//            System.out.println("instance pos = " + parameterQueue[pt + 3]);
//            System.out.println("method params count = " + m.getParameterCount());
            Object instance =  heap[parameterQueue[pt + 3]];
            pt += 4;
            Object[] params = new Object[m.getParameterCount()];
            for(int i = 0; i < params.length; i++){
                if(m.getParameterTypes()[0].isPrimitive()){
                    params[i] = parameterQueue[pt++];
                }else{
                    params[i] = heap[parameterQueue[pt++]];
                }

            }

            try {
                Object ret = m.invoke(instance, params);
                // System.out.println("ret = " + ret);
                resultQueue[0] = ret;
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            taskletParameterTop[threadID] = perThreadParameterQueueLength * threadID;
            currentParamPointer[threadID] = perThreadParameterQueueSize * threadID;
            // System.out.println("reset taskletParameterTop of " + threadID + "to" + taskletParameterTop[threadID]);
            countDownLatch.countDown();
        }
    }
    public DPUJVMRemoteImpl(int id, int threadCount) throws RemoteException {
        this.id = id;
        this.threadCount = threadCount;
        this.threads = new Runnable[threadCount];
        this.perThreadParameterQueueSize = maxParamSize / PIMRemoteJVMConfiguration.threadCount;
        this.currentParamPointer = new int[PIMRemoteJVMConfiguration.threadCount];
        this.resultQueue = new Object[PIMRemoteJVMConfiguration.threadCount * perThreadParameterQueueSize / 16];
        this.taskletParameterTop = new int[PIMRemoteJVMConfiguration.threadCount];
        for(int i = 0; i < threadCount; i++){
            this.threads[i] = new ProcessorBinary(i);
        }
        service =  Executors.newFixedThreadPool(threadCount);
        maxHeapSize = PIMRemoteJVMConfiguration.heapSize;
        parameterQueue = new int[maxParamSize / 4];
        heap = new Object[maxHeapSize / 4];
        metaSpace = new Object[maxMetaspaceSize / 4];
        this.perThreadParameterQueueLength = parameterQueue.length / PIMRemoteJVMConfiguration.threadCount;

    }



    @Override
    public void start() throws RemoteException{
        countDownLatch = new CountDownLatch(threadCount);
        for(int i = 0; i < threadCount; i++){
            service.submit(threads[i]);
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Finish");

    }

    @Override
    public void setParameter(int pos, int value, int tasklet) throws RemoteException {
        currentParamPointer[tasklet] += 4;
        parameterQueue[pos] = value;
        if(currentParamPointer[tasklet] + perThreadParameterQueueSize > maxParamSize) throw new RuntimeException("parameter buffer overflow");
    }

    @Override
    public int pushToMetaSpace(Class c) throws RemoteException {
        //System.out.println(c + "be push to index = " + metaSpaceIndex);
        if(metaSpaceIndex > metaSpace.length) throw new RuntimeException("metaspace overflow");
        metaSpace[metaSpaceIndex] = c;
        //System.out.println("return addr = " + metaSpaceIndex);

        return metaSpaceIndex++;
    }

    @Override
    public int pushToMetaSpace(Class c, String methodName, Class<?>... params) throws RemoteException {
        if(metaSpaceIndex > metaSpace.length) throw new RuntimeException("metaspace overflow");
        try {

            if("<init>".equals(methodName) || "<clinit>".equals(methodName)){
                c.getConstructor(params);
                Constructor constructor = c.getDeclaredConstructor(params);

                metaSpace[metaSpaceIndex] = constructor;
            }else{

                Method m = c.getDeclaredMethod(methodName, params);
                metaSpace[metaSpaceIndex] = m;
            }
        } catch (NoSuchMethodException e) {
            return -1;
        }
        return metaSpaceIndex++;
    }

    @Override
    public void pushObject(int pos, Object obj) throws RemoteException {
        heap[pos] = obj;
    }

    @Override
    public int getMetaSpacePointer() throws RemoteException {
        return currentMetaspaceSize;
    }

    @Override
    public int getHeapPointer() throws RemoteException {
        return currentHeapSize;
    }

    @Override
    public int getParamsBufferPointer(int tasklet) throws RemoteException {
        return currentParamPointer[tasklet];
    }


    @Override
    public void setMetaSpacePointer(int p) throws RemoteException {
        currentMetaspaceSize = p;
    }

    @Override
    public void setHeapPointer(int p) throws RemoteException {
        currentHeapSize = p;
    }

    @Override
    public void setParamsBufferPointer(int p, int tasklet) throws RemoteException {
        currentParamPointer[tasklet] = p;
    }

    @Override
    public int getMetaSpaceIndex() throws RemoteException {
        //System.out.println("get meta space index = " + metaSpaceIndex);
        return metaSpaceIndex;
    }

    @Override
    public int getHeapIndex() throws RemoteException {
        return heapSpaceIndex;
    }

    @Override
    public int getParamsBufferIndex(int tasklet) throws RemoteException {
        return taskletParameterTop[tasklet];
    }

    @Override
    public void setMetaSpaceIndex(int p) throws RemoteException {
        //System.out.println("set meta space index = " + p);
        metaSpaceIndex = p;
    }

    @Override
    public void setHeapIndex(int p) throws RemoteException {
        heapSpaceIndex = p;
    }

    @Override
    public void setParamsBufferIndex(int p, int tasklet) throws RemoteException {
        this.taskletParameterTop[tasklet] = p;
    }

    @Override
    public int getResultValue(int taskID) throws RemoteException {
        return (int) resultQueue[taskID];
    }

    @Override
    public int getHeapLength() throws RemoteException {
        return maxHeapSize;
    }

    @Override
    public int allocateObject() throws RemoteException {
        int pt = heapSpaceIndex;
        if(heapSpaceIndex >= heap.length) throw new RuntimeException("heap space overflow");
        heapSpaceIndex++;
        return pt;
    }

    @Override
    public int pushArguments(int[] params, int tasklet) throws RemoteException {
        if(taskletParameterTop[tasklet] + params.length >= perThreadParameterQueueLength * tasklet + perThreadParameterQueueLength)
            throw new RuntimeException("parameter buffer overflow");
        for(int i = 0; i < params.length; i++){
            parameterQueue[taskletParameterTop[tasklet]++] = params[i];
        }
        return taskletParameterTop[tasklet];
    }

    @Override
    public int getInt32(int addr) {
        return (int) heap[addr];
    }

    @Override
    public int getMetaSpaceLength() throws RemoteException {
        return maxMetaspaceSize;
    }


    @Override
    public int getID() {
        return this.id;
    }
}
