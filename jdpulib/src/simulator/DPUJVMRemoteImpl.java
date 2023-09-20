package simulator;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.*;

public class DPUJVMRemoteImpl extends UnicastRemoteObject implements DPUJVMRemote {
    public int id;
    private int threadCount;

    Runnable[] threads;
    ExecutorService service;
    static CountDownLatch countDownLatch;
    public Object[] heap;
    public int currentHeapSize = 0;
    public int maxHeapSize = 48 * 1024 * 1024;

    public int currentParamSize = 0;
    public int maxParamSize = 16 * 1024;

    public int currentMetaspaceSize = 0;
    public int maxMetaspaceSize = 12 * 1024 * 1024;
    public int[] parameterQueue;
    public Object[] metaSpace;
    int metaSpaceIndex = 0;
    int heapSpaceIndex = 0;
    int paramsSpaceIndex = 0;
    class ProcessorBinary implements Runnable {
        int threadID;
        public ProcessorBinary(int threadID){
            this.threadID = threadID;
        }

        @Override
        public void run() {
            System.out.println("run thread, ID = " + threadID);
            for(int i = 0; i < 10; i++){
                System.out.println("param queue " + i + ":" + parameterQueue[i]);
            }
            int taskId =  parameterQueue[0];
            Class c = (Class) metaSpace[parameterQueue[1]];
            int pt = 4;
            if(metaSpace[parameterQueue[2]] instanceof Constructor){
                Constructor constructor = (Constructor) metaSpace[parameterQueue[2]];
                int instanceIndex = parameterQueue[3];
                int paramCount =  constructor.getParameterCount();
                System.out.println("constructor param count = " + paramCount);
                if(paramCount == 0){
                    try {
                        heap[instanceIndex] = constructor.newInstance();
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }else{
                    Object[] params = new Object[paramCount];

                    for(int i = 0; i < paramCount; i++){
                        params[i] = parameterQueue[pt++];
                    }
                    try {
                        heap[instanceIndex] = constructor.newInstance(params);
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }
                countDownLatch.countDown();
                return;
            }
            Method m = (Method) metaSpace[parameterQueue[2]];
            System.out.println("class = " + c.getSimpleName());
            System.out.println("method = " + m.getName());
            System.out.println("instance pos = " + parameterQueue[2]);
            System.out.println("method params count = " + m.getParameterCount());
            Object instance =  heap[parameterQueue[2]];
            Class<?>[] classes = new Class[m.getParameterCount()];
            for(int i = 0; i < classes.length; i++){
                classes[i] = m.getParameterTypes()[i];
            }

            try {

                Object ret = m.invoke(instance, classes);
                System.out.println("ret = " + ret);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            countDownLatch.countDown();

        }
    }
    public DPUJVMRemoteImpl(int id, int threadCount) throws RemoteException {
        this.id = id;
        this.threadCount = threadCount;
        this.threads = new Runnable[threadCount];
        for(int i = 0; i < threadCount; i++){
            this.threads[i] = (Runnable) new ProcessorBinary(i);
        }
        service =  Executors.newFixedThreadPool(threadCount);
        maxHeapSize = PIMRemoteJVMConfiguration.heapSize;
        parameterQueue = new int[maxParamSize / 4];
        heap = new Object[maxHeapSize / 4];
        metaSpace = new Object[maxMetaspaceSize / 4];
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
    public void setParameter(int pos, int value) throws RemoteException {
        currentParamSize += 4;
        parameterQueue[pos] = value;
        if(currentHeapSize > maxParamSize) throw new RuntimeException("parameter buffer overflow");
    }

    @Override
    public int pushToMetaSpace(Class c) throws RemoteException {
        System.out.println(c + "be push to index = " + metaSpaceIndex);
        metaSpace[metaSpaceIndex] = c;
        System.out.println("return addr = " + metaSpaceIndex);
        return metaSpaceIndex++;
    }

    @Override
    public int pushToMetaSpace(Class c, String methodName, Class<?>... params) throws RemoteException {
        try {
            if("<init>".equals(methodName)){
                Constructor constructor = c.getDeclaredConstructor(params);
                metaSpace[metaSpaceIndex] = constructor;
            }else{

                Method m = c.getDeclaredMethod(methodName, params);
                metaSpace[metaSpaceIndex] = m;
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
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
    public int getParamsBufferPointer() throws RemoteException {
        return currentParamSize;
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
    public void setParamsBufferPointer(int p) throws RemoteException {
        currentParamSize = p;
    }

    @Override
    public int getMetaSpaceIndex() throws RemoteException {
        System.out.println("get meta space index = " + metaSpaceIndex);
        return metaSpaceIndex;
    }

    @Override
    public int getHeapIndex() throws RemoteException {
        return heapSpaceIndex;
    }

    @Override
    public int getParamsBufferIndex() throws RemoteException {
        return paramsSpaceIndex;
    }

    @Override
    public void setMetaSpaceIndex(int p) throws RemoteException {
        System.out.println("set meta space index = " + p);

        metaSpaceIndex = p;
    }

    @Override
    public void setHeapIndex(int p) throws RemoteException {
        heapSpaceIndex = p;
    }

    @Override
    public void setParamsBufferIndex(int p) throws RemoteException {
        paramsSpaceIndex = p;
    }



    @Override
    public void multiThreadExecution() throws RemoteException {
        
    }

    @Override
    public int getID() {
        return this.id;
    }
}
