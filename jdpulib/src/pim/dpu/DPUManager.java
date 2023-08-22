package pim.dpu;
import com.upmem.dpu.Dpu;
import com.upmem.dpu.DpuException;
import pim.BatchDispatcher;
import pim.UPMEM;
import pim.dpu.cache.DPUCacheManager;
import pim.dpu.classloader.DPUClassFileManager;
import pim.logger.Logger;
import pim.logger.PIMLoggers;
import pim.utils.BytesUtils;
import pim.IDPUProxyObject;
import java.io.IOException;
import java.io.PrintStream;

import static pim.dpu.java_strut.DPUJVMMemSpaceKind.DPU_HEAPSPACE;

public class DPUManager {
    public final int dpuID;
    public DPUGarbageCollector garbageCollector;
    public DPUClassFileManager dpuClassFileManager;
    public DPUCacheManager classCacheManager;

    public Dpu dpu;

    public byte[] dispatchingBuffer = new byte[1024];

    static Logger dpuManagerLogger = PIMLoggers.dpuManagerLogger;
    int currentTasklet = 0;
    int[] taskletSemaphore = new int[UPMEM.TOTAL_HARDWARE_THREADS_COUNT];
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

    public void dpuExecute(PrintStream printStream) throws DpuException {
        dpu.exec(printStream);
        garbageCollector.readBackHeapSpacePt();
        garbageCollector.readBackMetaSpacePt();
    }


    public void callNonstaticMethod(int classPt, int methodPt, int instanceAddr, Object[] params) throws DpuException {
        if(UPMEM.batchDispatchingRecording) {
            System.out.println("record batch dispatching");
            int t = UPMEM.batchDispatcher.taskletPosition[dpuID];
            int t2 = (t + 1) % 24;
            int size = (1 + 2 + 1 + params.length) * 4;
            BatchDispatcher bd = UPMEM.batchDispatcher;
            while(t2 != t){
                if(bd.paramsBufferPointer[dpuID][t2] + size < DPUGarbageCollector.perDPUBufferSize){
                    break;
                }
                t2 = (t2 + 1) % 24;
                if(t2 == t) throw new RuntimeException("TODO: need dispatch all calls");
            }
            bd.taskletPosition[dpuID] = t2;
            int from = bd.paramsBufferPointer[dpuID][t2] + DPUGarbageCollector.perDPUBufferSize * t2;
            BytesUtils.writeU4LittleEndian(UPMEM.batchDispatcher.paramsBuffer[dpuID], bd.recordedCount++, from);
            BytesUtils.writeU4LittleEndian(UPMEM.batchDispatcher.paramsBuffer[dpuID], classPt, from + 4);
            BytesUtils.writeU4LittleEndian(UPMEM.batchDispatcher.paramsBuffer[dpuID], classPt, from + 8);
            int offset = 12;
            for(Object obj : params){
                int v;
                if(obj instanceof Integer){
                    v = (int) obj;
                }else if(obj instanceof IDPUProxyObject){
                    v = ((IDPUProxyObject)obj).getAddr();
                    if(((IDPUProxyObject)obj).getDpuID() != dpuID){
                        throw new RuntimeException("all objects in the argument list should be at the same place");
                    }
                }
                BytesUtils.writeU4LittleEndian(UPMEM.batchDispatcher.paramsBuffer[dpuID], classPt, from + offset);
                offset += 4;
            }

            System.out.println("write to dpu " + dpuID + " tasklet " + t2 + " buffer from " + UPMEM.batchDispatcher.paramsBufferPointer[dpuID][t2]);
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
        System.out.println("select tasklet = " + tasklet);

        setClassPt(classPt,tasklet);
        setMethodPt(methodPt,tasklet);
        int[] paramsConverted = new int[params.length + 1 + 2 + 1];
        paramsConverted[0] = 0;
        paramsConverted[1] = classPt;
        paramsConverted[2] = methodPt;
        paramsConverted[3] = instanceAddr;

        int i = 1;
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

        garbageCollector.pushParameters(paramsConverted, tasklet);
        dpuExecute(null);

        taskletSemaphore[tasklet] = 0;
        currentTasklet = (currentTasklet + 1) % 24;
    }

    int calcFieldCount(Class c){
        if(c.getSuperclass() == null){
            return c.getDeclaredFields().length;
        }
        return calcFieldCount(c.getSuperclass()) + c.getDeclaredFields().length;
    }

    String generateInitializationDescriptor(Object[] params){
        String desc = "<init>:(";
        for(Object obj : params){
            if(obj instanceof Integer){
                desc += "I";
            }else {
                desc += "L" + obj.getClass().getName().replace(".", "/");
            }
        }

        return desc + ")V";
    }

    public <T> DPUObjectHandler createObject(Class c, Object[] params) throws DpuException, IOException {
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

        int objAddr = garbageCollector.allocate(DPU_HEAPSPACE, instanceSize);
        garbageCollector.transfer(DPU_HEAPSPACE, objectDataStream, objAddr);
        DPUObjectHandler handler = garbageCollector.dpuAddress2ObjHandler(objAddr, dpuID);
        dpuManagerLogger.logln("---> Object Create Finish, handler = " + " (addr: " + handler.address + "," + "dpu: " + handler.dpuID + ") <---");


        // VirtualTable virtualTable = UPMEM.getInstance().getDPUManager(dpuID).classCacheManager.getClassStructure("pim/algorithm/DPUTreeNode").virtualTable;
        // dpuManagerLogger.logln("" + virtualTable);

        // call the init func
        callNonstaticMethod(classAddr, initMethodAddr, handler.address, params);

        return handler;
    }

    public DPUManager(Dpu upmemdpu, int dpuID) throws DpuException {
        this.dpuID = dpuID;
        this.dpu = upmemdpu;
        garbageCollector = new DPUGarbageCollector(dpuID, dpu);
        dpuClassFileManager = new DPUClassFileManager(dpuID, dpu);
        classCacheManager = new DPUCacheManager(dpuID, dpu);
    }


}
