package framework.lang.struct.dist.redis;


import framework.pim.BatchDispatcher;
import framework.pim.UPMEM;

import java.util.Hashtable;

public class RStringController {
    private static final int MAX_ITEMS = 1000;
    Hashtable<String, Object>[] globalMappingTable = new Hashtable[UPMEM.dpuInUse];
    BatchDispatcher bd = new BatchDispatcher();
    public int[] recordCount = new int[UPMEM.dpuInUse];
    public int lastDPU = 0;
    public void SET(String key, String value){
        UPMEM.beginRecordBatchDispatching(bd);
        int dpuID;
        if(recordCount[lastDPU] < MAX_ITEMS) {
            dpuID = lastDPU;
        }else{
            dpuID = ++lastDPU;
        }
        if(globalMappingTable[dpuID].containsKey(key)){
            // TODO: clear BatchDispatcher
            UPMEM.endRecordBatchDispatching();
            return;
        }


        // TODO: Important: depentent work

        // TODO: 使用IR, 可以优化掉多次调用

        // 顺序地调用，出现一个失效时返回，这种情况如何办



        recordCount[lastDPU]++;
        UPMEM.endRecordBatchDispatching();
    }

    public void GET(){

    }

    public void MSET(){

    }

    public void MGET(){

    }

    public void INCR(){

    }

    public void DECR(){

    }

    public void INCRBY(){

    }

    public void DECRBY(){

    }

    public void STRLEN(){

    }

    public void APPEND(){

    }

    public void GETRANGE(){

    }

    public void SETRANGE(){

    }

    public void SETEX(){

    }

    public void SETNX(){

    }

    public void GETSET(){

    }

    public void PSETEX(){

    }

    public void BITCOUNT(){

    }

    public void BITOP(){

    }

    public void BITPOS(){

    }

}
