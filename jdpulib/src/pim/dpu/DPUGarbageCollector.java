package pim.dpu;

import com.upmem.dpu.Dpu;
import com.upmem.dpu.DpuException;
import pim.logger.Logger;
import pim.utils.BytesUtils;

public class DPUGarbageCollector {
    int dpuID;
    Dpu dpu;
    int heapSpacePt;
    int metaSpacePt;
    int parameterBufferPt;
    public final static int heapSpaceBeginAddr = 0x000000;
    public final static int metaSpaceBeginAddr = 48 * 1024 * 1024;
    public final static int parameterBufferBeginAddr = 0x42f8;
    public final static int heapSpaceSize = 48 * 1024 * 1024;
    public final static int metaSpaceSize = 16 * 1024 * 1024;

    Logger gcLogger = Logger.getLogger("pim:gc");

    public DPUGarbageCollector(int dpuID, Dpu dpu) throws DpuException {
        this.dpuID = dpuID;
        this.dpu = dpu;
        this.heapSpacePt = 0x00000008;
        this.metaSpacePt = metaSpaceBeginAddr;
        this.parameterBufferPt = parameterBufferBeginAddr;
        byte[] ptBytes = new byte[4];

        // set up space beginning pointer for meta space and heap space
        BytesUtils.writeU4LittleEndian(ptBytes, this.metaSpacePt, 0);
        dpu.copy("meta_space_pt", ptBytes, 0);
        BytesUtils.writeU4LittleEndian(ptBytes, this.heapSpacePt, 0);
        dpu.copy("mram_heap_pt", ptBytes, 0);
        BytesUtils.writeU4LittleEndian(ptBytes, this.parameterBufferPt, 0);
        dpu.copy("params_buffer_pt", ptBytes, 0);

    }


    public void updateHeapPointerToDPU() throws DpuException {
        byte[] ptBytes = new byte[4];
        BytesUtils.writeU4LittleEndian(ptBytes, this.heapSpacePt, 0);
        dpu.copy("mram_heap_pt", ptBytes, 0);
    }

    public int pushParameters(int[] params) throws DpuException {
        int size = params.length * 4;
        byte[] data = new byte[size];
        int addr = allocate(DPUJVMMemSpaceKind.DPU_PARAMETER_BUFFER, size);
        gcLogger.log(" - allocate " + size + " byte in parameter buffer");
        gcLogger.log(" - push ");
        for(int i = 0; i < params.length; i++){
            gcLogger.log(" -- " + params[i]);
            BytesUtils.writeU4LittleEndian(data, params[i], i * 4);
        }

        transfer(DPUJVMMemSpaceKind.DPU_PARAMETER_BUFFER, data, addr);
        return addr;
    }
    public void readBackHeapSpacePt(){
        byte[] bs = new byte[4];
        try {
            dpu.copy(bs, "mram_heap_pt");
            heapSpacePt = BytesUtils.readU4LittleEndian(bs, 0);
            gcLogger.log("read back heap pt "  + heapSpacePt);
        } catch (DpuException e) {
            throw new RuntimeException(e);
        }

    }
    public void readBackMetaSpacePt(){
        byte[] bs = new byte[4];
        try {
            dpu.copy(bs, "meta_space_pt");
            heapSpacePt = BytesUtils.readU4LittleEndian(bs, 0);
        } catch (DpuException e) {
            throw new RuntimeException(e);
        }
    }
    public void transfer(DPUJVMMemSpaceKind spaceKind, byte[] data, int pt) throws DpuException{
        String spaceVarName = "";
        int beginAddr = -1;
        if(spaceKind == DPUJVMMemSpaceKind.DPU_METASPACE){
            spaceVarName = "m_metaspace";
            beginAddr = metaSpaceBeginAddr;
        } else if (spaceKind == DPUJVMMemSpaceKind.DPU_HEAPSPACE){
            spaceVarName = "m_heapspace";
            beginAddr = heapSpaceBeginAddr;
        } else if (spaceKind == DPUJVMMemSpaceKind.DPU_PARAMETER_BUFFER) {
            spaceVarName = "params_buffer";
            beginAddr = parameterBufferBeginAddr;
        }

        if(!"".equals(spaceVarName) && beginAddr != -1){
            gcLogger.logf("copy %d bytes to MRAM, pt = 0x%x" + " [%s]", data.length, pt, spaceVarName);
            dpu.copy(spaceVarName, data, pt - beginAddr);
        }
    }

    public int allocate(DPUJVMMemSpaceKind spaceKind, byte[] data) throws DpuException {
        int addr = allocate(spaceKind, data.length);
        transfer(spaceKind, data, addr);
        byte[] t = new byte[4];
        dpu.copy(t, "meta_space_pt");
        if(BytesUtils.readU4LittleEndian(t, 0) != this.metaSpacePt){
            throw new RuntimeException("dpu pt = " + BytesUtils.readU4LittleEndian(t, 0) + " != " + this.metaSpacePt);
        }
        return addr;
    }

    public int allocate(DPUJVMMemSpaceKind spaceKind, int size) throws DpuException, RuntimeException {
        int alignmentMask;
        if(spaceKind == DPUJVMMemSpaceKind.DPU_PARAMETER_BUFFER){
            alignmentMask = 0b11;
        }else{
            alignmentMask = 0b111;
        }
        size = (size + alignmentMask) & ~alignmentMask;

        // list contains values for selection, according to the spaceKind
        int[] sourceMemoryPointers = new int[]{metaSpacePt, heapSpacePt, parameterBufferPt};
        String[] pointerVarNames = new String[]{"meta_space_pt",  "mram_heap_pt", "params_buffer_pt"};
        String pointerVarName = pointerVarNames[spaceKind.ordinal()];
        int addr;
        byte[] ptBytes = new byte[4];
        // copy latest pointer from DPU
        dpu.copy(ptBytes, pointerVarName);
        // update latest pointer temporary
        sourceMemoryPointers[spaceKind.ordinal()] = BytesUtils.readU4LittleEndian(ptBytes, 0);;
        // save the latest pointer (It will be the beginning of addr of the space we allocate)
        addr = sourceMemoryPointers[spaceKind.ordinal()];
        // increase the space pointer
        sourceMemoryPointers[spaceKind.ordinal()] += size;
        // write to the source variable
        metaSpacePt = sourceMemoryPointers[0];
        heapSpacePt = sourceMemoryPointers[1];

        gcLogger.logf("new %s = 0x%x\n", pointerVarName, addr + size);


        // write new pointer value to DPU
        BytesUtils.writeU4LittleEndian(ptBytes, sourceMemoryPointers[spaceKind.ordinal()], 0);
        dpu.copy(pointerVarName, ptBytes);

        return addr;
    }

    public static DPUObjectHandler dpuAddress2ObjHandler(int addr, int dpuID) {
        DPUObjectHandler handler = new DPUObjectHandler(dpuID, addr);

        return handler;
    }

    public int getRemainHeapMemory() {
        return heapSpaceSize - (heapSpacePt - heapSpaceBeginAddr);
    }

    public int getRemainMetaMemory() {
        return metaSpaceSize - (metaSpacePt - metaSpaceBeginAddr);
    }

    public int getReturnVal() throws DpuException {
        byte[] returnValBytes = new byte[4];
        dpu.copy(returnValBytes, "return_val");
        return BytesUtils.readU4LittleEndian(returnValBytes, 0);
    }
}
