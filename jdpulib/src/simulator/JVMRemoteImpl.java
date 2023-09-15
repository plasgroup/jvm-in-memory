package simulator;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;


class ProcessorBinary implements Runnable{
    public ProcessorBinary(int threadID){

    }
    @Override
    public void run() {

    }
}
public class JVMRemoteImpl extends UnicastRemoteObject implements JVMRemote {
    public int id;
    private int threadCount;
    Runnable[] threads;
    public JVMRemoteImpl(int id, int threadCount) throws RemoteException {
        this.id = id;
        this.threadCount = threadCount;
        this.threads = new Runnable[threadCount];
        for(int i = 0; i < threadCount; i++){
            this.threads[i] = new ProcessorBinary(i);
        }
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
