package simulator;

import framework.pim.dpu.DPUGarbageCollector;
import framework.pim.dpu.java_strut.DPUJVMMemSpaceKind;
import framework.pim.utils.BytesUtils;

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
        try {
            dpujvmRemote.setHeapPointer(heapSpacePt);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateMetaSpacePointerToDPU() {
        try {
            dpujvmRemote.setMetaSpacePointer(metaSpacePt);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
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
        try {
            heapSpacePt = dpujvmRemote.getHeapPointer();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void readBackMetaSpacePt() {
        try {
            metaSpacePt = dpujvmRemote.getHeapPointer();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void transfer(DPUJVMMemSpaceKind spaceKind, byte[] data, int pt) {
        try {
            switch (spaceKind){
                case DPU_HEAPSPACE:
                    // dpujvmRemote.setHeapPointer(dpujvmRemote.getHeapPointer() + data.length);

                    break;
                case DPU_METASPACE:
                    //dpujvmRemote.setMetaSpacePointer(dpujvmRemote.getMetaSpacePointer() + data.length);
                    break;
                case DPU_PARAMETER_BUFFER:
                    pt -= parameterBufferBeginAddr;
                    pt /= 4;
                    for(int i = 0; i < data.length / 4; i += 4, pt++){
                        int val = BytesUtils.readU4LittleEndian(data, i);
                        System.out.printf(" set to param index = %d, data = %d\n", pt, BytesUtils.readU4LittleEndian(data, i));
                        dpujvmRemote.setParameterAbsolutely(pt, val);
                    }
                    break;
                case DPU_PARAMETER_BUFFER_POINTERS:
                    pt /= 4; // get slot index
                    int val = BytesUtils.readU4LittleEndian(data, 0);
                    System.out.println("transfer param buffer pinter to index " + pt + "("  + pt + "-th tasklet's "
                            + ") with pointer = "
                            + val
                            + " slot index = "
                            + val / 4);

                    // val is a parameter buffer slot index value. In simulator, a slot have 4 bytes. So when setting pointer
                    // this value should * 4.
                    dpujvmRemote.setParamsBufferPointer(val, pt);
                    dpujvmRemote.setParamsBufferIndex(val / 4, pt);
                    break;


            }
        }catch (RemoteException ignored){

        }
    }


    // TODO: currently ignore transfer byte[] data to simulator heap. Because the heap of simulation
    //       storage Object directly
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
    public int freeFromBack(DPUJVMMemSpaceKind spaceKind, int size) {
        switch (spaceKind){
            case DPU_HEAPSPACE:
                try {
                    dpujvmRemote.setHeapPointer(heapSpacePt - size);
                    heapSpacePt -= size;
                    if(heapSpacePt < 0) throw new RuntimeException();
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                break;
            case DPU_METASPACE:
                try {
                    dpujvmRemote.setMetaSpacePointer(heapSpacePt - size);
                    metaSpacePt -= size;
                    if(metaSpacePt < 0) throw new RuntimeException();
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                heapSpacePt -= size;
                if(heapSpacePt < 0) throw new RuntimeException();
                break;
        }
        return -1;
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
        try {
            return dpujvmRemote.getMetaSpaceLength() - dpujvmRemote.getMetaSpacePointer();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getReturnVal() {
        try {
            Object result = dpujvmRemote.getResult(0).value;
            if(Boolean.class.isAssignableFrom(result.getClass())) return (boolean)result ? 0 : 1;
            return (int) result;
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getInt32(int i) {
        try {
            return dpujvmRemote.getInt32(i);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

}
