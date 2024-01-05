package simulator;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class SimulatorServer {
    public static void parseParameters(String[] args){
        for(int i = 0; i < args.length; i++){
            String arg = args[i];
            String[] items = arg.split("=");
            String argumentName = items[0];
            System.out.println(argumentName + " " + args[i]);
            if("DPU_COUNT".equals(argumentName)){
                System.out.println("set DPU count = " + items[1]);
                PIMRemoteJVMConfiguration.JVMCount = Integer.parseInt(items[1]) ;
            }else if("THREAD_PER_DPU".equals(argumentName)){
                System.out.println("set threads per dpu = " + items[1]);
                PIMRemoteJVMConfiguration.threadCount = Integer.parseInt(items[1]);
            }

        }
    }


    public static void main(String[] args) throws RemoteException {
        parseParameters(args);

        String deviceName = "DPU";
        for(int i = 0; i < PIMRemoteJVMConfiguration.JVMCount; i++){
            DPUJVMRemoteImpl jvmRemote = new DPUJVMRemoteImpl(i, PIMRemoteJVMConfiguration.threadCount);
            System.out.println("[device:" + deviceName + "(" + i +")] bind server at port = " + (9239 + i));
            Registry registry = LocateRegistry.createRegistry(9239 + i);
            registry.rebind(deviceName + "JVM" + i, jvmRemote);
        }
    }
}


