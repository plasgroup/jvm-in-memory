package simulator;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

public class JVMRemoteImpl extends UnicastRemoteObject implements JVMRemote {
    public int id;
    private int threadCount;

    Runnable[] threads;
    ExecutorService service;
    static CountDownLatch countDownLatch;


    class ProcessorBinary implements Runnable {
        int threadID;
        public ProcessorBinary(int threadID){
            this.threadID = threadID;
        }


        @Override
        public void run() {
            System.out.println("run thread, ID = " + threadID);
            countDownLatch.countDown();
        }
    }
    public JVMRemoteImpl(int id, int threadCount) throws RemoteException {
        this.id = id;
        this.threadCount = threadCount;
        this.threads = new Runnable[threadCount];
        for(int i = 0; i < threadCount; i++){
            this.threads[i] = (Runnable) new ProcessorBinary(i);
        }
        service =  Executors.newFixedThreadPool(threadCount);
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
    public String welcome(String s) throws RemoteException {
        return "Hello " + s;
    }

    @Override
    public void multiThreadExecution() throws RemoteException {
        
    }

    @Override
    public int getID() {
        return this.id;
    }
}
