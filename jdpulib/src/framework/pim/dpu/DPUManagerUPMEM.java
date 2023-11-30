package framework.pim.dpu;

import com.upmem.dpu.Dpu;
import com.upmem.dpu.DpuException;
import framework.pim.*;
import framework.pim.struct.DPUObjectHandler;
import framework.pim.struct.IDPUProxyObject;
import framework.pim.utils.BytesUtils;

import java.io.PrintStream;

import static framework.pim.dpu.java_strut.DPUJVMMemSpaceKind.DPU_HEAPSPACE;

public class DPUManagerUPMEM extends DPUManager{

    public <T> DPUObjectHandler createObject(Class c, Object[] params) {
        if(ExperimentConfigurator.useSimulator) {
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

            int objAddr = 0;
            objAddr = garbageCollector.allocate(DPU_HEAPSPACE, instanceSize);
            garbageCollector.transfer(DPU_HEAPSPACE, objectDataStream, objAddr);
            DPUObjectHandler handler = garbageCollector.dpuAddress2ObjHandler(objAddr, dpuID);
            dpuManagerLogger.logln("---> Object Create Finish, handler = " + " (addr: " + handler.address + "," + "dpu: " + handler.dpuID + ") <---");
            System.out.println("addr " + objAddr);

            // VirtualTable virtualTable = UPMEM.getInstance().getDPUManager(dpuID).classCacheManager.getClassStructure("framework.pim/algorithm/DPUTreeNode").virtualTable;
            // dpuManagerLogger.logln("" + virtualTable);

            // call the init func
            callNonstaticMethod(classAddr, initMethodAddr, handler.address, params);
            return handler;
        }
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

        int objAddr =
                0;
        objAddr = garbageCollector.allocate(DPU_HEAPSPACE, instanceSize);
        garbageCollector.transfer(DPU_HEAPSPACE, objectDataStream, objAddr);
        DPUObjectHandler handler = garbageCollector.dpuAddress2ObjHandler(objAddr, dpuID);
        dpuManagerLogger.logln("---> Object Create Finish, handler = " + " (addr: " + handler.address + "," + "dpu: " + handler.dpuID + ") <---");


        // VirtualTable virtualTable = UPMEM.getInstance().getDPUManager(dpuID).classCacheManager.getClassStructure("framework.pim/algorithm/DPUTreeNode").virtualTable;
        // dpuManagerLogger.logln("" + virtualTable);

        // call the init func
        callNonstaticMethod(classAddr, initMethodAddr, handler.address, params);
        return handler;
    }

    public DPUManagerUPMEM(Dpu upmemdpu, int dpuID) throws DpuException {
        this.dpuID = dpuID;
        this.dpu = upmemdpu;
        garbageCollector = new DPUGarbageCollectorUPMEM(dpuID, dpu);
        dpuClassFileManager = new DPUClassFileManagerUPMEM(dpuID, dpu);
        classCacheManager = new DPUCacheManagerUPMEM(dpuID, dpu);
    }

    @Override
    public void dpuExecute(PrintStream printStream) throws DpuException {
        dpu.exec(printStream);
        garbageCollector.readBackHeapSpacePt();
        garbageCollector.readBackMetaSpacePt();
    }
    public void setClassPt(int classPt, int tasklet) throws DpuException {
        byte[] data = new byte[4];
        BytesUtils.writeU4LittleEndian(data, classPt, 0);

        dpu.copy("exec_class_pt", data, 4 * tasklet);

    }
    public void setMethodPt(int methodPt, int tasklet) throws DpuException {
        byte[] data = new byte[4];
        BytesUtils.writeU4LittleEndian(data, methodPt, 0);

        dpu.copy("exec_method_pt", data, 4 * tasklet);


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
            if(UPMEM.isSpecifyTasklet(dpuID)){
                t2 = UPMEM.getSpecifiedTaskletAndCancel(dpuID);
            }else{
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
                    tasklet = (tasklet + 1) % 24;
                }
            }
        }

        // System.out.println("select tasklet = " + tasklet);

        int[] paramsConverted = new int[params.length + 1 + 2 + 1];
        paramsConverted[0] = 0;
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

            dpuExecute(null);
        } catch (DpuException e) {
            throw new RuntimeException(e);
        }

        taskletSemaphore[tasklet] = 0;
        currentTasklet = (currentTasklet + 1) % 24;
    }
}