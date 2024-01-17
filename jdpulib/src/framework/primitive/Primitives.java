package framework.primitive;

import com.upmem.dpu.DpuException;
import framework.RemainTest;
import framework.lang.struct.DummyProxy;
import framework.lang.struct.IDPUProxyObject;
import framework.pim.BatchDispatcher;
import framework.pim.UPMEM;
import framework.pim.dpu.RPCHelper;
import framework.pim.dpu.java_strut.DPUJVMMemSpaceKind;
import framework.pim.utils.BytesUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Primitives {
    BatchDispatcher bd = new BatchDispatcher();

    @RemainTest
    public IDPUProxyObject deploy(Object o, int partition) throws DeployNoEnoughHeapSpaceException {
        if(o == null) return null;
        int limitSize = UPMEM.getInstance().getDPUManager(partition).garbageCollector.getRemainHeapMemory();
        return deployHelper(o, partition, 0, limitSize).result;
    }

    class DeployIntermediateRecord{
        int size;
        IDPUProxyObject result;
        public DeployIntermediateRecord(int size, IDPUProxyObject result){
            this.size = size;
            this.result = result;
        }
    }

    private DeployIntermediateRecord deployHelper(Object o, int partition, int currentSize, int limitSize)
            throws DeployNoEnoughHeapSpaceException {
        if(o == null) return null;
        Field[] declaredFields = o.getClass().getDeclaredFields();
        int size = 8 + declaredFields.length * 4;
        if(currentSize + size > limitSize)
            throw new DeployNoEnoughHeapSpaceException();

        byte[] instance = new byte[size];
        int fieldPos = 0;
        int currentHeapBegin = UPMEM.getInstance().getDPUManager(partition)
                .garbageCollector.getHeapSpacePt();
        int pt = currentHeapBegin;
        for(Field f : declaredFields){
            f.setAccessible(true);
            try {
                Object value = f.get(o);
                if(value != null) {
                    try {
                        DeployIntermediateRecord deployIntermediateRecord =
                                deployHelper(value, partition, currentSize + size, limitSize);
                        BytesUtils.writeU4LittleEndian(
                                instance
                                , deployIntermediateRecord.result.getAddr()
                                , 8 + 4 * fieldPos
                                );
                        pt = currentHeapBegin + deployIntermediateRecord.size;
                    }catch (DeployNoEnoughHeapSpaceException e){
                        // rollback
                        UPMEM.getInstance().getDPUManager(partition).garbageCollector
                                .transfer(DPUJVMMemSpaceKind.DPU_HEAPSPACE, new byte[pt - currentHeapBegin], currentHeapBegin);
                        throw new DeployNoEnoughHeapSpaceException();
                    }
                }
                fieldPos++;
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        UPMEM.getInstance().getDPUManager(partition).garbageCollector
                .transfer(DPUJVMMemSpaceKind.DPU_HEAPSPACE, instance, currentHeapBegin);
        return new DeployIntermediateRecord(pt - currentHeapBegin, RPCHelper.getAReturnValue(partition, DummyProxy.class));
    }

    public Object deployNewObject(Class c, int partition, Object[] params){
        return UPMEM.getInstance().createObject(partition, c, params);
    }

    public List<IDPUProxyObject> broadcast(Object o) throws DeployNoEnoughHeapSpaceException {
        // TODO: parallel for
        List<IDPUProxyObject> result = new ArrayList<>();
        for(int i = 0; i < UPMEM.dpuInUse; i++){
            result.add(deploy(o, i));
        }
        return result;
    }

    public IDPUProxyObject copyBack(Object o){
        throw new RuntimeException("TODO: need modify In-memory JVM");
    }

    public IDPUProxyObject moveBack(Object o){
        throw new RuntimeException("TODO: need modify In-memory JVM");
    }

    public void batchDispatchBegin(){
        if(UPMEM.batchDispatchingRecording){
            try{
                UPMEM.batchDispatcher.dispatchAll();
            }catch (Exception e){

            }
        }
        UPMEM.beginRecordBatchDispatching(bd);
    }

    public void batchDispatchEnd(){
        UPMEM.endRecordBatchDispatching();
    }

    public void dispatchAll() throws DpuException {
        UPMEM.batchDispatcher.dispatchAll();
    }

    private static class DeployNoEnoughHeapSpaceException extends Throwable {
    }
}
