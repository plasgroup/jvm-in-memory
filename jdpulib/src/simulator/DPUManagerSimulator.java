package simulator;

import com.upmem.dpu.DpuException;
import pim.BatchDispatcher;
import pim.IDPUProxyObject;
import pim.UPMEM;
import pim.dpu.DPUGarbageCollector;
import pim.dpu.DPUManager;
import pim.dpu.DPUObjectHandler;
import pim.utils.BytesUtils;

import java.io.PrintStream;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

import static pim.dpu.java_strut.DPUJVMMemSpaceKind.DPU_HEAPSPACE;

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
    public void dpuExecute(PrintStream printStream) throws DpuException {
        throw new RuntimeException();

    }

    @Override
    public void callNonstaticMethod(int classPt, int methodPt, int instanceAddr, Object[] params) {
        if(UPMEM.batchDispatchingRecording) {
            //System.out.println("record batch dispatching");
            //System.out.printf("method pt %x\n", methodPt);
            int t = UPMEM.batchDispatcher.taskletPosition[dpuID];
            int t2 = (t + 1) % 24;
            int size = (((1 + 2 + 1 + params.length) * 4) + 0b111) & (~0b111);
            BatchDispatcher bd = UPMEM.batchDispatcher;
            while(t2 != t){
                if(bd.paramsBufferPointer[dpuID][t2] + size < DPUGarbageCollector.perDPUBufferSize){
                    break;
                }
                t2 = (t2 + 1) % 24;
                try {
                    bd.dispatchAll();
                } catch (DpuException e) {
                    throw new RuntimeException(e);
                }
            }
            bd.taskletPosition[dpuID] = t2; // next time from t2 to find a proper tasklet
            // beginning of params_buffer[t2]
            int from = bd.paramsBufferPointer[dpuID][t2] + DPUGarbageCollector.perDPUBufferSize * t2;
            // id
            BytesUtils.writeU4LittleEndian(UPMEM.batchDispatcher.paramsBuffer[dpuID], bd.recordedCount[dpuID]++, from);
            // class address
            BytesUtils.writeU4LittleEndian(UPMEM.batchDispatcher.paramsBuffer[dpuID], classPt, from + 4);
            // method address
            BytesUtils.writeU4LittleEndian(UPMEM.batchDispatcher.paramsBuffer[dpuID], methodPt, from + 8);
            // instance address
            BytesUtils.writeU4LittleEndian(UPMEM.batchDispatcher.paramsBuffer[dpuID], instanceAddr, from + 12);
            int offset = 16;
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
                BytesUtils.writeU4LittleEndian(UPMEM.batchDispatcher.paramsBuffer[dpuID], v, from + offset);
                offset += 4;
            }

            // System.out.println("write to dpu " + dpuID + " tasklet " + t2 + " buffer from " + UPMEM.batchDispatcher.paramsBufferPointer[dpuID][t2]);
            bd.paramsBufferPointer[dpuID][t2] += size;
            bd.dpusInUse.add(dpuID);
            return;
        }

        // choose a tasklet
        int tasklet = currentTasklet;
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
                tasklet = (tasklet + 1) % 24;
            }
        }
        // System.out.println("select tasklet = " + tasklet);

        int[] paramsConverted = new int[params.length + 1 + 2 + 1];
        paramsConverted[0] = 0; // task id
        paramsConverted[1] = classPt;
        paramsConverted[2] = methodPt;
        paramsConverted[3] = instanceAddr;

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
        currentTasklet = (currentTasklet + 1) % 24;
    }

    @Override
    public <T> DPUObjectHandler createObject(Class c, Object[] params) {
        int fieldCount = calcFieldCount(c);
        int instanceSize = 8 + fieldCount * 4;
        byte[] objectDataStream = new byte[(instanceSize + 7) & ~7];
        int classAddr;
        int initMethodAddr;
        if(classCacheManager.getClassStrutCacheLine(c.getName().replace(".","/")) == null){
            dpuClassFileManager.loadClassForDPU(c);
        }

        classAddr = classCacheManager.getClassStrutCacheLine(c.getName().replace(".","/")).marmAddr;
        dpuManagerLogger.logln(" * Get Class Addr = " + classAddr);
        String initMethodDesc = generateInitializationDescriptor(params);

        initMethodAddr = classCacheManager
                    .getMethodCacheItem(c.getName().replace(".", "/"), initMethodDesc).mramAddr;

        BytesUtils.writeU4LittleEndian(objectDataStream, classAddr, 4);

        int objAddr;
        objAddr = garbageCollector.allocate(DPU_HEAPSPACE, instanceSize);
        garbageCollector.transfer(DPU_HEAPSPACE, objectDataStream, objAddr);
        DPUObjectHandler handler = garbageCollector.dpuAddress2ObjHandler(objAddr, dpuID);
        dpuManagerLogger.logln("---> Object Create Finish, handler = " + " (addr: " + handler.address + "," + "dpu: " + handler.dpuID + ") <---");
        System.out.println("addr " + objAddr);


        // call the init func
        callNonstaticMethod(classAddr, initMethodAddr, handler.address, params);
        return handler;

    }
}
