package simulator;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface JVMRemote extends Remote {
    String welcome(String s) throws RemoteException;
    void multiThreadExecution() throws RemoteException;
    int getID() throws RemoteException;
    void start() throws RemoteException;
}
