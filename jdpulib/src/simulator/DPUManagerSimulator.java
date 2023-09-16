package simulator;

import com.upmem.dpu.DpuException;
import pim.dpu.DPUGarbageCollector;
import pim.dpu.DPUManager;
import pim.dpu.DPUObjectHandler;
import pim.dpu.cache.DPUCacheManager;
import pim.dpu.classloader.DPUClassFileManager;

import java.io.IOException;
import java.io.PrintStream;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class DPUManagerSimulator extends DPUManager {

    private DPUJVMRemote DPUJVMRemote;
    public DPUManagerSimulator(int dpuID) {
        System.out.println("Init DPU " +  dpuID + "'s JVM");
        this.dpuID = dpuID;
        try {
            this.DPUJVMRemote = (DPUJVMRemote) LocateRegistry.getRegistry("localhost", 9239 + dpuID).lookup("DPUJVM" + dpuID);
        } catch (RemoteException | NotBoundException e) {
            throw new RuntimeException(e);
        }
        garbageCollector = new DPUGarbageCollectorSimulator(dpuID, DPUJVMRemote);
        dpuClassFileManager = new DPUClassFileManager(dpuID, DPUJVMRemote);
        classCacheManager = new DPUCacheManager(dpuID, DPUJVMRemote);
    }

    @Override
    public void dpuExecute(PrintStream printStream) throws DpuException {

    }

    @Override
    public void callNonstaticMethod(int classPt, int methodPt, int instanceAddr, Object[] params) {

    }

    @Override
    public <T> DPUObjectHandler createObject(Class c, Object[] params) throws IOException {
        return null;
    }
}
