package simulator;

import com.upmem.dpu.DpuException;
import framework.pim.BatchDispatcher;
import framework.lang.struct.IDPUProxyObject;
import framework.pim.UPMEM;
import framework.pim.dpu.DPUGarbageCollector;
import framework.pim.dpu.DPUManager;
import framework.lang.struct.DPUObjectHandler;
import framework.pim.utils.BytesUtils;

import java.io.PrintStream;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

import static framework.pim.dpu.java_strut.DPUJVMMemSpaceKind.DPU_HEAPSPACE;

public class DPUManagerSimulator extends DPUManager {

    private DPUJVMRemote dpujvmRemote;
    public DPUManagerSimulator(int dpuID) {
        System.out.println("Init DPU " +  dpuID + "'s JVM");
        this.dpuID = dpuID;
        try {
            this.dpujvmRemote = (DPUJVMRemote) LocateRegistry.getRegistry("localhost", 9239 + dpuID).lookup("DPUJVM" + dpuID);
        } catch (RemoteException | NotBoundException e) {
            throw new RuntimeException(e);
        }
        garbageCollector = new DPUGarbageCollectorSimulator(dpuID, dpujvmRemote);
        dpuClassFileManager = new DPUClassFileManagerSimulator(dpuID, dpujvmRemote);
        classCacheManager = new DPUCacheManagerSimulator(dpuID, dpujvmRemote);
    }

    @Override
    public void dpuExecute(PrintStream printStream) {
        try {
            dpujvmRemote.start();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void callNonstaticMethod(int classPt, int methodPt, int instanceAddress, Object[] params) {
        if(UPMEM.batchDispatchingRecording) {
            int lastTaskletId = UPMEM.batchDispatcher.taskletPosition[dpuID];
            int taskletId = (lastTaskletId + 1) % 24;
            int size = (((1 + 2 + 1 + params.length) * 4) + 0b111) & (~0b111);
            BatchDispatcher bd = UPMEM.batchDispatcher;
            if(UPMEM.isSpecifyTasklet(dpuID)){
                taskletId = UPMEM.getSpecifiedTaskletAndCancel(dpuID);
            }else{
                while(taskletId != lastTaskletId){
                    if(bd.paramsBufferPointer[dpuID][taskletId] + size < PIMRemoteJVMConfiguration.heapSize){
                        break;
                    }
                    taskletId = (taskletId + 1) % 24;
                    try {
                        bd.dispatchAll();
                    } catch (DpuException e) {
                        throw new RuntimeException(e);
                    }
                }
            }



            bd.taskletPosition[dpuID] = taskletId; // next time from t2 to find a proper tasklet

            int[] paramPrepared = new int[params.length + 4];
            paramPrepared[0] = bd.recordedCount[dpuID]++;
            paramPrepared[1] = classPt;
            paramPrepared[2] = methodPt;
            paramPrepared[3] = instanceAddress;
            int offset = 4;
            for(Object obj : params){
                int v;
                if(obj instanceof Integer){
                    v = (int) obj;
                }else if(obj instanceof IDPUProxyObject){
                    v = ((IDPUProxyObject)obj).getAddr();
                    if(((IDPUProxyObject)obj).getDpuID() != dpuID){
                        throw new RuntimeException("all objects in the argument list should be at the same place");
                    }
                }else{
                    throw new RuntimeException("can not send CPU object to DPU");
                }
                params[offset] = v;
                offset++;
            }
            bd.paramsBufferPointer[dpuID][taskletId] += size;
            bd.dpusInUse.add(dpuID);
            try {
                dpujvmRemote.pushArguments(paramPrepared, taskletId);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            return;
        }


        // choose a tasklet
        int tasklet;
        if(UPMEM.isSpecifyTasklet(dpuID)){
            tasklet = UPMEM.getSpecifiedTaskletAndCancel(dpuID);
        }else{
            tasklet = currentTasklet;
            while(true){
                if(taskletSemaphore[tasklet] == 0){
                    synchronized (taskletSemaphore){
                        if(taskletSemaphore[tasklet] == 0){
                            taskletSemaphore[tasklet] = 1;
                            currentTasklet = tasklet;
                            break;
                        }
                    }
                }else{
                    tasklet = (tasklet + 1) % PIMRemoteJVMConfiguration.threadCount;
                }
            }
        }

        int[] paramsConverted = new int[params.length + 4];
        paramsConverted[0] = 0;         // task id
        paramsConverted[1] = classPt;   // class location
        paramsConverted[2] = methodPt;  // method location
        paramsConverted[3] = instanceAddress; // instance address

        int i = 4;
        for(Object obj : params){
            if(obj instanceof Integer){
                paramsConverted[i] = (int) obj;
            }else if(obj instanceof IDPUProxyObject){
                paramsConverted[i] = ((IDPUProxyObject)obj).getAddr();
                if(((IDPUProxyObject)obj).getDpuID() != dpuID){
                    throw new RuntimeException("all objects in the argument list should be at the same place");
                }
            }
            i++;
        }

        try {
            garbageCollector.pushParameters(paramsConverted, tasklet);
            dpujvmRemote.start();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        taskletSemaphore[tasklet] = 0;
        currentTasklet = (currentTasklet + 1) % PIMRemoteJVMConfiguration.threadCount;
    }

    @Override
    public DPUObjectHandler createObject(Class c, Object[] params) {
        int fieldCount = calcFieldCount(c);
        int instanceSize = 8 + fieldCount * 4;
        byte[] objectDataStream = new byte[(instanceSize + 7) & ~7];
        int classAddr;
        int initMethodAddr;
        if(classCacheManager.getClassStrutCacheLine(c.getName().replace(".","/")) == null){
            dpuClassFileManager.loadClassToDPU(c);
        }

        classAddr = classCacheManager.getClassStrutCacheLine(c.getName().replace(".","/")).marmAddr;
        dpuManagerLogger.logln(" * Get Class Addr = " + classAddr);
        String initMethodDesc = generateInitializationDescriptor(params);

        initMethodAddr = classCacheManager
                    .getMethodCacheItem(c.getName().replace(".", "/"), initMethodDesc).mramAddr;

        BytesUtils.writeU4LittleEndian(objectDataStream, classAddr, 4);

        int objAddr;
        try {
            objAddr = dpujvmRemote.allocateObject();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        garbageCollector.transfer(DPU_HEAPSPACE, objectDataStream, objAddr);
        DPUObjectHandler handler = DPUGarbageCollector.dpuAddress2ObjHandler(objAddr, dpuID);
        dpuManagerLogger.logln("---> Object Create Finish, handler = " + " (addr: " + handler.address + "," + "dpu: " + handler.dpuID + ") <---");
        System.out.println("get obj addr = " + objAddr);


        // call the init func
        callNonstaticMethod(classAddr, initMethodAddr, handler.address, params);

        return handler;

    }
}
