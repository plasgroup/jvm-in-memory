package pim.dpu;
import com.upmem.dpu.Dpu;

import com.upmem.dpu.DpuException;
import pim.BytesUtils;
import pim.IDPUProxyObject;

import java.io.IOException;
import java.io.PrintStream;

import static pim.dpu.DPUJVMMemSpaceKind.DPU_HEAPSPACE;


public class DPUManager {
    public final int dpuID;
    public DPUGarbageCollector garbageCollector;
    public DPUClassFileManager dpuClassFileManager;
    public DPUCacheManager classCacheManager;

    public Dpu dpu;


    public void setClassPt(int classPt) throws DpuException {
        byte[] data = new byte[4];
        BytesUtils.writeU4LittleEndian(data, classPt, 0);
        dpu.copy("exec_class_pt", data);
    }
    public void setMethodPt(int methodPt) throws DpuException {
        byte[] data = new byte[4];
        BytesUtils.writeU4LittleEndian(data, methodPt, 0);
        dpu.copy("exec_method_pt", data);
    }

    public void dpuExec(PrintStream printStream) throws DpuException {
        dpu.exec(printStream);
        garbageCollector.readBackHeapSpacePt();
        garbageCollector.readBackMetaSpacePt();
    }
    public void callNonstaticMethod(int classPt, int methodPt, int instanceAddr, Object[] params) throws DpuException {
        setClassPt(classPt);
        setMethodPt(methodPt);
        int[] paramsConverted = new int[params.length + 1];
        paramsConverted[0] = instanceAddr;
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
        garbageCollector.pushParameters(paramsConverted);
        dpuExec(System.out);
    }

    int calcFieldCount(Class c){
        if(c.getSuperclass() == null){
            return c.getDeclaredFields().length;
        }
        return calcFieldCount(c.getSuperclass()) + c.getDeclaredFields().length;
    }

    String genInitDesc(Class c, Object[] params){
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
    public <T> PIMObjectHandler createObject(Class c, Object[] params) throws DpuException, IOException {
        int fieldCount = calcFieldCount(c);
        int instanceSize = 8 + fieldCount * 4;
        byte[] objectDataStream = new byte[(instanceSize + 7) & ~7];
        int classAddr;
        int initMethodAddr;
        if(classCacheManager.getClassStrutCacheLine(c.getName().replace(".","/")) == null){
            dpuClassFileManager.loadClassForDPU(c);
        }
        classAddr = classCacheManager.getClassStrutCacheLine(c.getName().replace(".","/")).marmAddr;
        System.out.println(" * Get Class Addr = " + classAddr);
        String initMethodDesc = genInitDesc(c, params);

        initMethodAddr = classCacheManager
                .getMethodCacheItem(c.getName().replace(".", "/"), initMethodDesc).mramAddr;

        BytesUtils.writeU4LittleEndian(objectDataStream, classAddr, 4);

        int objAddr = garbageCollector.allocate(DPU_HEAPSPACE, objectDataStream);
        PIMObjectHandler handler = garbageCollector.dpuAddress2ObjHandler(objAddr, dpuID);
        System.out.println("---> Object Create Finish, handler = " + " (addr: " + handler.address + "," + "dpu: " + handler.dpuID + ") <---");

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
