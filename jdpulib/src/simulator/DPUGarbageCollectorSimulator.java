package simulator;

import framework.pim.dpu.DPUGarbageCollector;
import framework.pim.dpu.java_strut.DPUJVMMemSpaceKind;

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
        try {
            return dpujvmRemote.pushArguments(params, tasklet);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
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

            }
        }catch (RemoteException ignored){
        }

    }

    @Override
    public int allocate(DPUJVMMemSpaceKind spaceKind, byte[] data) {
        throw new RuntimeException();
    }

    @Override
    public int allocate(DPUJVMMemSpaceKind spaceKind, int size) {
        int alignmentMask;
        alignmentMask = 0b111;
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
                    dpujvmRemote.setParamsBufferPointer(ptBytes,0);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                break;
        }


        return addr;
    }

    @Override
    public int getRemainHeapMemory() {
        try {
            return dpujvmRemote.getHeapLength() - dpujvmRemote.getHeapPointer();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
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

    @Override
    public int getInt32(int i) {
        return dpujvmRemote.getInt32(i);
    }
}
