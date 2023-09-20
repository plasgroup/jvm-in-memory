package simulator;

import pim.dpu.DPUGarbageCollector;
import pim.dpu.java_strut.DPUJVMMemSpaceKind;

import java.rmi.RemoteException;

public class DPUGarbageCollectorSimulator extends DPUGarbageCollector {
    DPUJVMRemote dpujvmRemote;

    public DPUGarbageCollectorSimulator(int dpuID, DPUJVMRemote dpujvmRemote) {
        this.dpuID = dpuID;
        this.dpujvmRemote = dpujvmRemote;
        this.heapSpacePt = 0;
        this.metaSpacePt = 0;
    }

    @Override
    public void updateHeapPointerToDPU() {
        throw new RuntimeException();
    }

    @Override
    public int pushParameters(int[] params) {
        return pushParameters(params,0);
    }

    @Override
    public int pushParameters(int[] params, int tasklet)       {
        int size = (params.length * 4 + 0b111) & ~(0b111);
        byte[] data = new byte[size];
        int addr = parameterBufferBeginAddr + (parameterBufferSize / 24) * tasklet;
        for(int i = 0; i < params.length; i++) {
            try {
                System.out.println("write param " + i + " to " + ((parameterBufferSize / 24) * tasklet + i));
                dpujvmRemote.setParameter((parameterBufferSize / 24) * tasklet + i, params[i]);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }

//        }
//        gcLogger.log(" - allocate " + size + " byte in parameter buffer");
//        gcLogger.log(" - push ");
//        for(int i = 0; i < params.length; i++){
//            gcLogger.log(" -- " + params[i]);
//            BytesUtils.writeU4LittleEndian(data, params[i], i * 4);
//        }
//
//        transfer(DPUJVMMemSpaceKind.DPU_PARAMETER_BUFFER, data, addr);
//        byte[] ptBytes = new byte[4];
//        BytesUtils.writeU4LittleEndian(ptBytes, parameterBufferBeginAddr + tasklet * perDPUBufferSize + size, 0);
//        try {
//            dpu.copy("params_buffer_pt", ptBytes , 4 * tasklet);
//        } catch (DpuException e) {
//            throw new RuntimeException(e);
//        }
//        return addr;

        return addr;
    }

    @Override
    public void readBackHeapSpacePt() {
        throw new RuntimeException();

    }

    @Override
    public void readBackMetaSpacePt() {
        throw new RuntimeException();

    }

    @Override
    public void transfer(DPUJVMMemSpaceKind spaceKind, byte[] data, int pt) {
        try {

            switch (spaceKind){
                case DPU_HEAPSPACE:
                    dpujvmRemote.setHeapPointer(dpujvmRemote.getHeapPointer() + data.length);
                    break;
                case DPU_METASPACE:
                    dpujvmRemote.setMetaSpacePointer(dpujvmRemote.getMetaSpacePointer() + data.length);
                    break;
                case DPU_PARAMETER_BUFFER:
                    dpujvmRemote.setParamsBufferPointer(dpujvmRemote.getParamsBufferPointer() + data.length);
                    break;
            }
        }catch (RemoteException e){

        }

    }

    @Override
    public int allocate(DPUJVMMemSpaceKind spaceKind, byte[] data) {
        throw new RuntimeException();
    }

    @Override
    public int allocate(DPUJVMMemSpaceKind spaceKind, int size) {
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
        int ptBytes = -1;


        // copy latest pointer from DPU

        switch (pointerVarName){
            case "meta_space_pt":
                try {
                    ptBytes = dpujvmRemote.getMetaSpacePointer();
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "mram_heap_pt":
                try {
                    ptBytes = dpujvmRemote.getHeapPointer();
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "params_buffer_pt":
                try {
                    ptBytes = dpujvmRemote.getParamsBufferPointer();
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                break;
        }

        // update latest pointer temporary
        sourceMemoryPointers[spaceKind.ordinal()] = ptBytes;
        // save the latest pointer (It will be the beginning of addr of the space we allocate)
        addr = sourceMemoryPointers[spaceKind.ordinal()];
        // increase the space pointer
        sourceMemoryPointers[spaceKind.ordinal()] += size;
        // write to the source variable
        metaSpacePt = sourceMemoryPointers[0];
        heapSpacePt = sourceMemoryPointers[1];

        gcLogger.logf("new %s = 0x%x\n", pointerVarName, addr + size);


        // write new pointer value to DPU
        switch (pointerVarName){
            case "meta_space_pt":
                try {
                    dpujvmRemote.setMetaSpacePointer(ptBytes);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "mram_heap_pt":
                try {
                    dpujvmRemote.setHeapPointer(ptBytes);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "params_buffer_pt":
                try {
                    dpujvmRemote.setParamsBufferPointer(ptBytes);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                break;
        }


        return addr;
    }

    @Override
    public int getRemainHeapMemory() {
        throw new RuntimeException();
    }

    @Override
    public int getRemainMetaMemory() {
        throw new RuntimeException();
    }

    @Override
    public int getReturnVal() {
        try {
            return dpujvmRemote.getResultValue(0);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
}
