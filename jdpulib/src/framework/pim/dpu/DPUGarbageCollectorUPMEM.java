package framework.pim.dpu;

import com.upmem.dpu.Dpu;
import com.upmem.dpu.DpuException;
import framework.pim.ExperimentConfigurator;
import framework.pim.UPMEM;
import framework.pim.dpu.java_strut.DPUJVMMemSpaceKind;
import framework.pim.utils.BytesUtils;

/** Memory Manager (UPMEM version) **/
public class DPUGarbageCollectorUPMEM extends DPUGarbageCollector {

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

        /**
         * Each tasklet of a DPU manage part of the parameter buffer.
         * This loop init the beginning address of i-th tasklet's parameter buffer
         * **/
        for(int i = 0; i < MAX_TASKLET; i++){
            BytesUtils.
                    writeU4LittleEndian(bufferPointers, parameterBufferBeginAddr + i * perTaskletParameterBufferSize, i * 4);
        }
        if(!ExperimentConfigurator.useSimulator)
            dpu.copy("params_buffer_pt", bufferPointers, 0);

    }

    @Override
    public int getHeapSpacePt() {
        return heapSpacePt;
    }

    @Override
    public int getMetaSpacePt() {
        return metaSpacePt;
    }

    @Override
    public void updateHeapPointerToDPU() {
        byte[] ptBytes = new byte[4];
        BytesUtils.writeU4LittleEndian(ptBytes, this.heapSpacePt, 0);
        try {
            if(UPMEM.getConfigurator().isEnableProfilingRPCDataMovement()){
                UPMEM.profiler.transferredBytesToDPU += 4 * 64;
            }
            dpu.copy("mram_heap_pt", ptBytes, 0);
        } catch (DpuException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateMetaSpacePointerToDPU() {
        byte[] ptBytes = new byte[4];
        BytesUtils.writeU4LittleEndian(ptBytes, this.heapSpacePt, 0);
        try {
            if(UPMEM.getConfigurator().isEnableProfilingRPCDataMovement()){
                UPMEM.profiler.transferredBytesToDPU += 4 * 64;
            }
            dpu.copy("meta_space_pt", ptBytes, 0);
        } catch (DpuException e) {
            throw new RuntimeException(e);
        }
    }
    /** push parameters to tasklet's parameter buffer (default: tasklet id = 0) **/

    @Override
    public int pushParameters(int[] params) {

        return pushParameters(params,0);
    }


    /** push parameters to tasklet's parameter buffer **/
    @Override
    public int pushParameters(int[] params, int tasklet) {

        int size = (params.length * 4 + 0b111) & ~(0b111);
        byte[] data = new byte[size];
        int addr = parameterBufferBeginAddr + (parameterBufferSize / 24) * tasklet;
        for(int i = 0; i < params.length; i++){
            BytesUtils.writeU4LittleEndian(data, params[i], i * 4);
        }

        transfer(DPUJVMMemSpaceKind.DPU_PARAMETER_BUFFER, data, addr);
        byte[] ptBytes = new byte[4];
        BytesUtils.writeU4LittleEndian(ptBytes, parameterBufferBeginAddr + tasklet * perTaskletParameterBufferSize + size, 0);
        try {
            if(UPMEM.getConfigurator().isEnableProfilingRPCDataMovement()){
                UPMEM.profiler.transferredBytesToDPU += ptBytes.length * 64L;
            }
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
            if(UPMEM.getConfigurator().isEnableProfilingRPCDataMovement()){
                UPMEM.profiler.transferredBytesFromDPU += 4 * 64;
            }
            dpu.copy(bs, "mram_heap_pt");
            heapSpacePt = BytesUtils.readU4LittleEndian(bs, 0);
        } catch (DpuException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void readBackMetaSpacePt() {
        byte[] bs = new byte[4];
        try {
            if(UPMEM.getConfigurator().isEnableProfilingRPCDataMovement()){
                UPMEM.profiler.transferredBytesFromDPU += 4 * 64;
            }
            dpu.copy(bs, "meta_space_pt");
            heapSpacePt = BytesUtils.readU4LittleEndian(bs, 0);
        } catch (DpuException e) {
            throw new RuntimeException(e);
        }
    }

    /** transfer data to DPU from pt **/
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


        if(!spaceVarName.isEmpty()){
            if(!ExperimentConfigurator.useSimulator) {
                try {
                    if(UPMEM.getConfigurator().isEnableProfilingRPCDataMovement()){
                        UPMEM.profiler.transferredBytesToDPU += data.length * 64L;
                    }
                    dpu.copy(spaceVarName, data, pt - beginAddr);
                } catch (DpuException e) {
                    throw new RuntimeException(e);
                }
            }
        }


    }


    /** allocate memory in DPU memory, and fill bytes data **/

    @Override
    public int allocate(DPUJVMMemSpaceKind spaceKind, byte[] data) {
        int addr = 0;
        addr = allocate(spaceKind, data.length);
        transfer(spaceKind, data, addr);
        byte[] t = new byte[4];
        try {
            if(UPMEM.getConfigurator().isEnableProfilingRPCDataMovement()){
                UPMEM.profiler.transferredBytesToDPU += 4 * 64;
            }
            dpu.copy(t, "meta_space_pt");
        } catch (DpuException e) {
            throw new RuntimeException(e);
        }
        if(BytesUtils.readU4LittleEndian(t, 0) != this.metaSpacePt){
            throw new RuntimeException("dpu pt = " + BytesUtils.readU4LittleEndian(t, 0) + " != " + this.metaSpacePt);
        }
        return addr;
    }

    /** allocate memory in DPU memory **/
    @Override
    public int allocate(DPUJVMMemSpaceKind spaceKind, int size) throws  RuntimeException {
        int alignmentMask;
        if(spaceKind == DPUJVMMemSpaceKind.DPU_PARAMETER_BUFFER){
            alignmentMask = 0b111;
        }else{
            alignmentMask = 0b111;
        }
        size = (size + alignmentMask) & ~alignmentMask;

        // This list contains values for selection, according to the spaceKind
        int[] sourceMemoryPointers = new int[]{metaSpacePt, heapSpacePt};
        String[] pointerVarNames = new String[]{"meta_space_pt",  "mram_heap_pt", "params_buffer_pt"};
        String pointerVarName = pointerVarNames[spaceKind.ordinal()];
        int addr;
        byte[] ptBytes = new byte[4];
        // copy latest pointer from DPU
        try {
            if(UPMEM.getConfigurator().isEnableProfilingRPCDataMovement()){
                UPMEM.profiler.transferredBytesToDPU += 4 * 64;
            }
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



        // write new pointer value to DPU
        BytesUtils.writeU4LittleEndian(ptBytes, sourceMemoryPointers[spaceKind.ordinal()], 0);
        try {
            if(UPMEM.getConfigurator().isEnableProfilingRPCDataMovement()){
                UPMEM.profiler.transferredBytesToDPU += ptBytes.length * 64;
            }
            dpu.copy(pointerVarName, ptBytes);
        } catch (DpuException e) {
            throw new RuntimeException(e);
        }

        return addr;
    }

    @Override
    public int freeFromBack(DPUJVMMemSpaceKind spaceKind, int size) {
        switch (spaceKind){
            case DPU_HEAPSPACE:
                heapSpacePt -= size;
                if(heapSpacePt < 0) throw new RuntimeException("Exception in freeing DPU memory");
                updateHeapPointerToDPU();
                return heapSpacePt;
            case DPU_METASPACE:
                metaSpacePt -= size;
                if(metaSpacePt < 0) throw new RuntimeException("Exception in freeing DPU memory");
                updateMetaSpacePointerToDPU();
                return heapSpacePt;
            case DPU_PARAMETER_BUFFER, DPU_STATIC_TEMP:
                break;
        }
        return -1;
    }

    @Override
    public int getRemainHeapMemory() {
        return heapSpaceSize - (heapSpacePt - heapSpaceBeginAddr);
    }

    @Override
    public int getRemainMetaMemory(){
        return metaSpaceSize - (metaSpacePt - metaSpaceBeginAddr);
    }

    /** get return value from DPU **/
    @Override
    public int getReturnVal() {
        byte[] returnValBytes = new byte[4];
        try {
            if(UPMEM.getConfigurator().isEnableProfilingRPCDataMovement()){
                UPMEM.profiler.transferredBytesFromDPU += 4 * 64;
            }
            dpu.copy(returnValBytes, "return_val");
        } catch (DpuException e) {
            throw new RuntimeException(e);
        }
        return BytesUtils.readU4LittleEndian(returnValBytes, 0);
    }


    /** get int32 values from address addr **/
    @Override
    public int getInt32(int addr) {
        byte[] returnValBytes = new byte[4];
        try {
            if(UPMEM.getConfigurator().isEnableProfilingRPCDataMovement()){
                UPMEM.profiler.transferredBytesFromDPU += 4 * 64;
            }
            dpu.copy(returnValBytes, "mram_heap_pt", addr);
        } catch (DpuException e) {
            throw new RuntimeException(e);
        }
        return BytesUtils.readU4LittleEndian(returnValBytes, 0);
    }

    @Override
    public void setInt32(int addr, int val) {
        byte[] valBytes = new byte[4];
        BytesUtils.writeU4LittleEndian(valBytes, val, 0);
        try {
            if(UPMEM.getConfigurator().isEnableProfilingRPCDataMovement()){
                UPMEM.profiler.transferredBytesToDPU += 4 * 64;
            }
            dpu.copy(valBytes, "mram_heap_pt", addr);
        } catch (DpuException e) {
            throw new RuntimeException(e);
        }
    }
}