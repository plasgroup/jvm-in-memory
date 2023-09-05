package pim;

import com.upmem.dpu.DpuCallback;
import com.upmem.dpu.DpuException;
import com.upmem.dpu.DpuSet;
import pim.dpu.PIMManager;
import pim.dpu.java_strut.DPUJVMMemSpaceKind;
import pim.logger.Logger;
import pim.logger.PIMLoggers;
import pim.utils.BytesUtils;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static pim.dpu.DPUGarbageCollector.parameterBufferBeginAddr;
import static pim.dpu.DPUGarbageCollector.perDPUBufferSize;

public class BatchDispatcher {

    public byte[][] paramsBuffer = new byte[UPMEM.dpuInUse][24 * perDPUBufferSize];
    public int[][] paramsBufferPointer = new int[UPMEM.dpuInUse][24];
    public int[] taskletPosition = new int[UPMEM.dpuInUse];

    Logger dispatchLogger = PIMLoggers.batchDispatchLogger;
    ExecutorService executorService = Executors.newCachedThreadPool();

    int[] result;
    byte[] resultBytes = new byte[4 * 1024];
    public int[] recordedCount = new int[UPMEM.dpuInUse];
    public HashSet<Integer> dpusInUse = new HashSet<>();
    volatile int dpucount = dpusInUse.size();

    class DPUExecutionTask implements Runnable{
        private int id;
        public DPUExecutionTask(int dpuId){
            this.id = dpuId;
        }
        public void run(){
            try {
                System.out.println("DPU#" + id + "dispatched");
                UPMEM.getInstance().getDPUManager(id).dpuExecute(null);
            } catch (DpuException e) {
                throw new RuntimeException(e);
            }

            Arrays.fill(paramsBufferPointer[id], 0);
            Arrays.fill(paramsBuffer[id], (byte)0);
            System.out.println("dpu#" + id + " finished");
            dpucount--;
        }
    }
    {
        dispatchLogger.setEnable(false);
    }
    public void dispatchAll() throws DpuException {
        for (int dpuID: dpusInUse) {
            dispatchLogger.logln(" === write tasks to DPU " + dpuID + " === ");
            dispatchLogger.logln(" === write tasks to DPU " + dpuID + " === ");
            byte[] ptBytes = new byte[4];

            // for each tasklet of a DPU
            for(int i = 0; i < 24; i++){
                int pbp = paramsBufferPointer[dpuID][i];
                if(pbp == 0) continue;

                dispatchLogger.logln("dpu id = " + dpuID + " tasklet " + i + ":");
                dispatchLogger.logln("parameters buffer pointer = " + pbp);
                // pbp is relative offset of a tasklet, so, the i'th tasklet's  params_buffer_pt[i] should be parameterBufferBeginAddr + i * perDPUBufferSize + pbp
                BytesUtils.writeU4LittleEndian(ptBytes, parameterBufferBeginAddr + i * perDPUBufferSize + pbp,0);
                // write pointer to params_buffer_pt[i]
                UPMEM.getInstance().getDPUManager(dpuID).dpu.copy("params_buffer_pt", ptBytes , 4 * i);
            }
            // transfer the DPU#i's params buffer
            UPMEM.getInstance().getDPUManager(dpuID).garbageCollector.transfer(DPUJVMMemSpaceKind.DPU_PARAMETER_BUFFER,paramsBuffer[dpuID], parameterBufferBeginAddr );
        }

        // calculate the result array size
        int count = 0;


        // O(|DPUs|)
        for(int dpuID : dpusInUse){
          count += recordedCount[dpuID];
        }
        System.out.println("current total count == " + count);
        result = new int[count];
        System.out.println("=== dispatch all ====");


        dpucount = dpusInUse.size();
        for(int dpuID : dpusInUse){
            executorService.submit(new DPUExecutionTask(dpuID));
//            UPMEM.getInstance().getDPUManager(dpuID).dpuExecute(null);
//
//            UPMEM.getInstance().getDPUManager(dpuID).dpu.async().call(new DpuCallback() {
//                @Override
//                public void call(DpuSet dpuSet, int i) throws DpuException {
//                    System.out.println(i + "finished");
//                    dpucount--;
//                }
//            });
//            //UPMEM.getInstance().getDPUManager(dpuID).dpu.copy(resultBytes, "return_values");
//
////            for(int i = 0; i < recordedCount[dpuID]; i++){
////                int taskID = BytesUtils.readU4LittleEndian(resultBytes, (i * 2) * 4);
////                int res = BytesUtils.readU4LittleEndian(resultBytes, (i * 2 + 1) * 4);
////                result[taskID] = res;
////                System.out.println(res);
////            }
//
//            Arrays.fill(paramsBufferPointer[dpuID], 0);
//            Arrays.fill(paramsBuffer[dpuID], (byte)0);
//            System.out.println("dpu#" + dpuID + "dispatched");
        }

        while (dpucount > 0) {
            Thread.onSpinWait();
        }
        dpusInUse.clear();
        System.out.println("all dispatched");
    }
}
