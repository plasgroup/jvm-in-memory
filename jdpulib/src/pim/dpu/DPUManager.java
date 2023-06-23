package pim.dpu;
import com.upmem.dpu.Dpu;

import com.upmem.dpu.DpuException;
import pim.BytesUtils;

import java.io.IOException;

import static pim.dpu.DPUJVMMemSpaceKind.DPU_HEAP;


public class DPUManager {
    public final int dpuID;
    public DPUGarbageCollector garbageCollector;
    public DPUClassFileManager dpuClassFileManager;

    public DPUCacheManager classCacheManager;

    public Dpu dpu;



    public <T> PIMObjectHandler createObject(Class c) throws DpuException, IOException {
        return createObject(c, new Object[]{});
    }

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

    int calcFieldCount(Class c){
        if(c.getSuperclass() == null){
            return c.getDeclaredFields().length;
        }
        return calcFieldCount(c.getSuperclass()) + c.getDeclaredFields().length;
    }
    public <T> PIMObjectHandler createObject(Class c, Object[] params) throws DpuException, IOException {
        int fieldCount = calcFieldCount(c);
        int instanceSize = 8 + fieldCount * 4;
        byte[] objectDataStream = new byte[(instanceSize + 7) & ~7];
        int classAddr = 0;
        if(classCacheManager.getClassStrutCacheLine(c.getName().replace(".","/")) == null){
            dpuClassFileManager.loadClassForDPU(c);
        }
        classAddr = classCacheManager.getClassStrutCacheLine(c.getName().replace(".","/")).marmAddr;

        System.out.println(" * Get Class Addr = " + classAddr);

        BytesUtils.writeU4LittleEndian(objectDataStream, classAddr, 4);

        int objAddr = garbageCollector.allocate(DPU_HEAP, objectDataStream);
        PIMObjectHandler handler = garbageCollector.dpuAddress2ObjHandler(objAddr, dpuID);
        System.out.println("---> Object Create Finish, handler = " + " (addr: " + handler.address + "," + "dpu: " + handler.dpuID + ") <---");
        return handler;
    }

    

    // DPUGarbageCollector.allocate(dpuID, DPU_METASPACE, dpuClassData.length);
    public DPUManager(Dpu upmemdpu, int dpuID) throws DpuException {
        this.dpuID = dpuID;
        this.dpu = upmemdpu;
        garbageCollector = new DPUGarbageCollector(dpuID, dpu);
        dpuClassFileManager = new DPUClassFileManager(dpuID, dpu);
        classCacheManager = new DPUCacheManager(dpuID, dpu);
    }


}
