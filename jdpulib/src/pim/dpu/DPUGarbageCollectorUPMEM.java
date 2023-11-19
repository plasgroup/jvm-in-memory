package pim.dpu;

import com.upmem.dpu.Dpu;
import com.upmem.dpu.DpuException;
import pim.ExperimentConfigurator;
import pim.dpu.java_strut.DPUJVMMemSpaceKind;
import pim.utils.BytesUtils;

public class DPUGarbageCollectorUPMEM extends DPUGarbageCollector{

    public DPUGarbageCollectorUPMEM(int dpuID, Dpu dpu) throws DpuException {
        this.dpuID = dpuID;
        this.dpu = dpu;
        this.heapSpacePt = heapSpaceBeginAddr + 0x00000008;
        this.metaSpacePt = metaSpaceBeginAddr;
        byte[] ptBytes = new byte[4];

        // set up space beginning pointer for meta space and heap space
        BytesUtils.writeU4LittleEndian(ptBytes, this.metaSpacePt, 0);
        if(!ExperimentConfigurator.useSimulator)
            dpu.copy("meta_space_pt", ptBytes, 0);
        BytesUtils.writeU4LittleEndian(ptBytes, this.heapSpacePt, 0);
        if(!ExperimentConfigurator.useSimulator)
            dpu.copy("mram_heap_pt", ptBytes, 0);
        byte[] bufferPointers = new byte[24 * 4];
        for(int i = 0; i < 24; i++){
            BytesUtils.writeU4LittleEndian(bufferPointers, parameterBufferBeginAddr + i * perDPUBufferSize, i * 4);
        }
        if(!ExperimentConfigurator.useSimulator)
            dpu.copy("params_buffer_pt", bufferPointers, 0);

    }

    @Override
    public void updateHeapPointerToDPU() {
        byte[] ptBytes = new byte[4];
        BytesUtils.writeU4LittleEndian(ptBytes, this.heapSpacePt, 0);
        try {
            dpu.copy("mram_heap_pt", ptBytes, 0);
        } catch (DpuException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int pushParameters(int[] params) {
        return pushParameters(params,0);
    }

    @Override
    public int pushParameters(int[] params, int tasklet) {
        int size = (params.length * 4 + 0b111) & ~(0b111);
        byte[] data = new byte[size];
        int addr = parameterBufferBeginAddr + (parameterBufferSize / 24) * tasklet;
        gcLogger.log(" - allocate " + size + " byte in parameter buffer");
        gcLogger.log(" - push ");
        for(int i = 0; i < params.length; i++){
            gcLogger.log(" -- " + params[i]);
            BytesUtils.writeU4LittleEndian(data, params[i], i * 4);
        }

        transfer(DPUJVMMemSpaceKind.DPU_PARAMETER_BUFFER, data, addr);
        byte[] ptBytes = new byte[4];
        BytesUtils.writeU4LittleEndian(ptBytes, parameterBufferBeginAddr + tasklet * perDPUBufferSize + size, 0);
        try {
            dpu.copy("params_buffer_pt", ptBytes , 4 * tasklet);
        } catch (DpuException e) {
            throw new RuntimeException(e);
        }
        return addr;
    }
    @Override
    public void readBackHeapSpacePt() {
        byte[] bs = new byte[4];
        try {
            dpu.copy(bs, "mram_heap_pt");
            heapSpacePt = BytesUtils.readU4LittleEndian(bs, 0);
            gcLogger.log("read back heap pt "  + heapSpacePt);
        } catch (DpuException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void readBackMetaSpacePt() {
        byte[] bs = new byte[4];
        try {
            dpu.copy(bs, "meta_space_pt");
            heapSpacePt = BytesUtils.readU4LittleEndian(bs, 0);
        } catch (DpuException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void transfer(DPUJVMMemSpaceKind spaceKind, byte[] data, int pt) {
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
            if(!ExperimentConfigurator.useSimulator) {
                try {
                    dpu.copy(spaceVarName, data, pt - beginAddr);
                } catch (DpuException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


    @Override
    public int allocate(DPUJVMMemSpaceKind spaceKind, byte[] data) {
        int addr = 0;
        addr = allocate(spaceKind, data.length);
        transfer(spaceKind, data, addr);
        byte[] t = new byte[4];
        try {
            dpu.copy(t, "meta_space_pt");
        } catch (DpuException e) {
            throw new RuntimeException(e);
        }
        if(BytesUtils.readU4LittleEndian(t, 0) != this.metaSpacePt){
            throw new RuntimeException("dpu pt = " + BytesUtils.readU4LittleEndian(t, 0) + " != " + this.metaSpacePt);
        }
        return addr;
    }

    @Override
    public int allocate(DPUJVMMemSpaceKind spaceKind, int size) throws  RuntimeException {
        int alignmentMask;
        if(spaceKind == DPUJVMMemSpaceKind.DPU_PARAMETER_BUFFER){
            alignmentMask = 0b111;
        }else{
            alignmentMask = 0b111;
        }
        size = (size + alignmentMask) & ~alignmentMask;

        // list contains values for selection, according to the spaceKind
        int[] sourceMemoryPointers = new int[]{metaSpacePt, heapSpacePt};
        String[] pointerVarNames = new String[]{"meta_space_pt",  "mram_heap_pt", "params_buffer_pt"};
        String pointerVarName = pointerVarNames[spaceKind.ordinal()];
        int addr;
        byte[] ptBytes = new byte[4];
        // copy latest pointer from DPU
        try {
            dpu.copy(ptBytes, pointerVarName);
        } catch (DpuException e) {
            throw new RuntimeException(e);
        }
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
        try {
            dpu.copy(pointerVarName, ptBytes);
        } catch (DpuException e) {
            throw new RuntimeException(e);
        }

        return addr;
    }

    @Override
    public int getRemainHeapMemory() {
        return heapSpaceSize - (heapSpacePt - heapSpaceBeginAddr);
    }

    @Override
    public int getRemainMetaMemory(){
        return metaSpaceSize - (metaSpacePt - metaSpaceBeginAddr);
    }

    @Override
    public int getReturnVal() {
        byte[] returnValBytes = new byte[4];
        try {
            dpu.copy(returnValBytes, "return_val");
        } catch (DpuException e) {
            throw new RuntimeException(e);
        }
        return BytesUtils.readU4LittleEndian(returnValBytes, 0);
    }

    @Override
    public int getInt32(int i) {
        byte[] returnValBytes = new byte[4];
        try {
            dpu.copy(returnValBytes, "mram_heap_pt", i);
        } catch (DpuException e) {
            throw new RuntimeException(e);
        }
        return BytesUtils.readU4LittleEndian(returnValBytes, 0);
    }
}
