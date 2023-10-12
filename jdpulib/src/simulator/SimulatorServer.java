package simulator;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class SimulatorServer {
    public static void main(String[] args) throws RemoteException {
        /* TODO: Read configuration from file */
        String deviceName = "DPU";
        for(int i = 0; i < PIMRemoteJVMConfiguration.JVMCount; i++){
            DPUJVMRemoteImpl jvmRemote = new DPUJVMRemoteImpl(i, PIMRemoteJVMConfiguration.threadCount);
            System.out.println("[device:" + deviceName + "(" + i +")] bind server at port = " + (9239 + i));
            Registry registry = LocateRegistry.createRegistry(9239 + i);
            registry.rebind(deviceName + "JVM" + i, jvmRemote);
        }
    }
}


