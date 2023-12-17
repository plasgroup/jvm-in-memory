package simulator;

import framework.pim.UPMEM;

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
    public int maxParamSize = DPUGarbageCollectorSimulator.parameterBufferSize;
    public int currentMetaspaceSize = 0;
    public int maxMetaspaceSize = PIMRemoteJVMConfiguration.maxMetaspaceSize;
    public int[] parameterQueue;
    public Object[] metaSpace;
    public Object[] resultQueue;
    public Integer resultQueuePointer = 0;
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

            int pt = threadID * perThreadParameterQueueLength;
            int taskId =  parameterQueue[pt];
            Class c = (Class) metaSpace[parameterQueue[pt + 1]];

            if(metaSpace[parameterQueue[pt + 2]] instanceof Constructor){
                Constructor constructor = (Constructor) metaSpace[parameterQueue[pt + 2]];
                int instanceIndex = parameterQueue[pt + 3];
                int paramCount =  constructor.getParameterCount();
                System.out.println("constructor param count = " + paramCount + ", constructor = " + constructor);

                if(paramCount == 0){
                    try {
                        heap[instanceIndex] = constructor.newInstance();
                        System.out.println(" > call " + constructor);

                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }else{
                    Object[] params = new Object[paramCount];
                    pt += 4;
                    for(int i = 0; i < paramCount; i++){
                        Class parameterType = constructor.getParameterTypes()[i];
                        System.out.println("try read argument " + i + " = " + parameterQueue[i] + ", " + parameterType);
                        if(Integer.class.isAssignableFrom(parameterType) || "int".equals("" + parameterType)){
                            params[i] = parameterQueue[pt];
                            System.out.println(" -- set arg " + i + " = (int) " + parameterQueue[pt]);
                            pt++;
                        }else{
                            params[i] = heap[parameterQueue[pt]];
                            System.out.println(" -- set arg " + i + " = (ref type) " + heap[parameterQueue[pt]]);
                            pt++;
                        }
                    }
                    try {
                        heap[instanceIndex] = constructor.newInstance(params);
                        System.out.println(" > call " + constructor);

                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }
                taskletParameterTop[threadID] = perThreadParameterQueueLength * threadID;
                currentParamPointer[threadID] = perThreadParameterQueueSize * threadID;
                countDownLatch.countDown();
                System.out.println(" > thread " + threadID + " finish.");
                return;
            }
            Method m = (Method) metaSpace[parameterQueue[pt + 2]];
//
            System.out.println("class = " + c.getSimpleName());
            System.out.println("method = " + m.getName());
            System.out.println("instance pos = " + parameterQueue[pt + 3]);
            System.out.println("method params count = " + m.getParameterCount());
            Object instance =  heap[parameterQueue[pt + 3]];
            System.out.println(" -- instance = " + instance);
            pt += 4;
            Object[] params = new Object[m.getParameterCount()];
            for(int i = 0; i < params.length; i++){
                if(m.getParameterTypes()[0].isPrimitive()){
                    params[i] = parameterQueue[pt++];
                }else{
                    System.out.println(" is (reference)");
                    params[i] = heap[parameterQueue[pt++]];
                }
                System.out.println(" --- param " + i + " " + params[i]);

            }
            System.out.println(" -- read params finished.");
            try {
                System.out.println("method =  " + m);
                Object ret = m.invoke(instance, params);
                System.out.println(" get result " + ret);
                synchronized (resultQueuePointer){
                    System.out.println(" set taskid = " + taskId + " to result area index = " + resultQueuePointer);
                    resultQueue[resultQueuePointer] = taskId;
                    resultQueuePointer++;
                    if(ret == null){
                        resultQueue[resultQueuePointer] = 0;
                    }else{

                        if(!m.getReturnType().isPrimitive()){
                            int addr = -1;
                            synchronized (heap){
                                addr = ++heapSpaceIndex;
                                heap[addr] = ret;
                                resultQueue[resultQueuePointer] = addr;
                                System.out.println("write reference of " + ret.getClass() + " to addr: " + addr);
                            }
                        }else{
                            resultQueue[resultQueuePointer] = ret;
                        }
                    }
                    resultQueuePointer++;
                }


            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            taskletParameterTop[threadID] = perThreadParameterQueueLength * threadID;
            currentParamPointer[threadID] = perThreadParameterQueueSize * threadID;
            // System.out.println("reset taskletParameterTop of " + threadID + "to" + taskletParameterTop[threadID]);
            countDownLatch.countDown();
            System.out.println(" > thread " + threadID + " finish.");
            resultQueuePointer = 0;
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
        System.out.println("remote JVM start...");
        countDownLatch = new CountDownLatch(threadCount);
        for(int i = 0; i < threadCount; i++){
            System.out.println("> start thread " + i);
            service.submit(threads[i]);
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("remote JVM finish...");

    }

    @Override
    public void setParameterRelative(int pos, int value, int tasklet) throws RemoteException {
        currentParamPointer[tasklet] += 4;
        parameterQueue[tasklet * perThreadParameterQueueLength + pos] = value;
        if(currentParamPointer[tasklet] + perThreadParameterQueueSize > maxParamSize) throw new RuntimeException("parameter buffer overflow");
    }

    @Override
    public void setParameterAbsolutely(int pos, int value) throws RemoteException {
        System.out.println("set parameter buffer slot " + pos + " with val = " + value);
        parameterQueue[pos] = value;
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
        System.out.println("set tasklet " + tasklet + "'s parameter buffer pointer = " + p);
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
        System.out.println("set tasklet " + tasklet + "'s parameter buffer pointer = " + p);

        this.taskletParameterTop[tasklet] = p;
    }

    @Override
    public JVMSimulatorResult getResult(int resultIndex) throws RemoteException {
        int taskID = (int) resultQueue[resultIndex];
        Object result = resultQueue[resultIndex + 1];
        if(result == null){
            return new JVMSimulatorResult(taskID,0);
        }
//        if(Boolean.class.isAssignableFrom(result.getClass())){
//            val =  (boolean) result ? 1 : 0;
//        }else if(result.getClass().isPrimitive()){
//            val = (int) result;
//        }else{
//            return new JVMSimulatorResult(taskID, val);
//        }
        return new JVMSimulatorResult(taskID, result);
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
        System.out.printf("taskletParameterTop[tasklet] = %d, " +
                "params length = %d, perThreadParameterQueueLength = %d \n", taskletParameterTop[tasklet], params.length, perThreadParameterQueueLength);
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
