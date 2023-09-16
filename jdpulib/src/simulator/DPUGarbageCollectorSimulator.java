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

    }

    @Override
    public int pushParameters(int[] params) {
        return 0;
    }

    @Override
    public int pushParameters(int[] params, int tasklet) {
        return 0;
    }

    @Override
    public void readBackHeapSpacePt() {

    }

    @Override
    public void readBackMetaSpacePt() {

    }

    @Override
    public void transfer(DPUJVMMemSpaceKind spaceKind, byte[] data, int pt) {

    }

    @Override
    public int allocate(DPUJVMMemSpaceKind spaceKind, byte[] data) {
        return 0;
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
                    addr = dpujvmRemote.getMetaSpaceIndex();
                    dpujvmRemote.setMetaSpaceIndex(addr + 1);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "mram_heap_pt":
                try {
                    dpujvmRemote.setHeapPointer(ptBytes);
                    addr = dpujvmRemote.getHeapIndex();
                    dpujvmRemote.setHeapIndex(addr + 1);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "params_buffer_pt":
                try {
                    dpujvmRemote.setParamsBufferPointer(ptBytes);
                    addr = dpujvmRemote.getParamsBufferIndex();
                    dpujvmRemote.setParamsBufferIndex(addr + 1);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                break;
        }


        return addr;
    }

    @Override
    public int getRemainHeapMemory() {
        return 0;
    }

    @Override
    public int getRemainMetaMemory() {
        return 0;
    }

    @Override
    public int getReturnVal() {
        return 0;
    }
}
