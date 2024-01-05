package simulator;

import java.rmi.Remote;
import java.rmi.RemoteException;



/** This file define the facade for accessing remote services. **/
public interface DPUJVMRemote extends Remote {
    int getID() throws RemoteException; /** get "DPU" ID **/
    void start() throws RemoteException; /** boot **/

    /** set parameter value at tasklet's parameter buffer's pos-th slot **/
    void setParameter(int pos, int value, int tasklet) throws RemoteException;

    /** push a class to meta space **/
    int pushToMetaSpace(Class c) throws RemoteException;

    /** push method of a class to meta space **/
    int pushToMetaSpace(Class c, String methodName, Class<?>... params)  throws RemoteException;

    void pushObject(int pos, Object obj) throws RemoteException;

    /** Get current pointer of a kind of space. Notice: the "pointer" is in a unit of bytes. But "index"
     *  is in a unit of 4 bytes.
     *  The simulator currently simulate the meta/heap space's storage in arrays of 4-byte integers.
     *  **/
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

    /** get the heap's length, in an unit of 4 bytes. **/
    int getHeapLength() throws RemoteException;

    int allocateObject() throws RemoteException;

    int pushArguments(int[] params, int tasklet) throws RemoteException;


    /** read an int32 from a specific address **/
    int getInt32(int addr) throws RemoteException;

    int getMetaSpaceLength() throws RemoteException;
}
