package simulator;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DPUJVMRemote extends Remote {
    int getID() throws RemoteException;
    /* boot */
    void start() throws RemoteException;
    /* memory space operation */
    void setParameterRelative(int pos, int value, int tasklet) throws RemoteException;
    void setParameterAbsolutely(int pos, int value) throws RemoteException;

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

    JVMSimulatorResult getResult(int resultIndex) throws RemoteException;

    int getHeapLength() throws RemoteException;

    int allocateObject() throws RemoteException;

    int pushArguments(int[] params, int tasklet) throws RemoteException;

    int getInt32(int addr) throws RemoteException;

    int getMetaSpaceLength() throws RemoteException;
}
