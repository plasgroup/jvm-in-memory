package simulator;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DPUJVMRemote extends Remote, SimulatorBackend {
    void multiThreadExecution() throws RemoteException;
    int getID() throws RemoteException;

    /* boot */
    void start() throws RemoteException;

    /* memory space operation */
    void setParameter(int pos, int value, int tasklet) throws RemoteException;
    int pushToMetaSpace(Class c) throws RemoteException;
    int pushToMetaSpace(Class c, String methodName, Class<?>... params)  throws RemoteException;
    void pushObject(int pos, Object obj) throws RemoteException;

    int getMetaSpacePointer() throws RemoteException;

    int getHeapPointer() throws RemoteException;

    int getParamsBufferPointer(int tasklet) throws RemoteException;

    void setMetaSpacePointer(int p) throws RemoteException;

    void setHeapPointer(int p) throws RemoteException;

    void setParamsBufferPointer(int p, int tasklet) throws RemoteException;

    int getMetaSpaceIndex() throws RemoteException;

    int getHeapIndex() throws RemoteException;

    int getParamsBufferIndex(int tasklet) throws RemoteException;

    void setMetaSpaceIndex(int p) throws RemoteException;

    void setHeapIndex(int p) throws RemoteException;

    void setParamsBufferIndex(int p, int tasklet) throws RemoteException;

    int getResultValue(int taskID) throws RemoteException;

    int getHeapLength() throws RemoteException;

    int allocateObject() throws RemoteException;

    int pushArguments(int[] params, int tasklet) throws RemoteException;
}
