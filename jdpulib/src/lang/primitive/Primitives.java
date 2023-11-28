package lang.primitive;

import com.upmem.dpu.DpuException;
import pim.BatchDispatcher;
import pim.IDPUProxyObject;
import pim.UPMEM;

import java.lang.module.Configuration;
import java.util.ArrayList;
import java.util.List;

public class Primitives {
    BatchDispatcher bd = new BatchDispatcher();
    public IDPUProxyObject deploy(Object o, int partition){
        return null;
    }

    public IDPUProxyObject deployNew(Class c, int partition, Object[] params){
        return UPMEM.getInstance().createObject(partition, c, params);
    }

    public List<IDPUProxyObject> broadcast(Object o){
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
}
