package pim;

import com.upmem.dpu.DpuException;
import pim.dpu.java_strut.DPUJVMMemSpaceKind;
import pim.logger.Logger;
import pim.logger.PIMLoggers;
import pim.utils.BytesUtils;

import java.util.*;
import java.util.concurrent.*;

import static pim.dpu.DPUGarbageCollector.parameterBufferBeginAddr;
import static pim.dpu.DPUGarbageCollector.perDPUBufferSize;

public class BatchDispatcher {

    public byte[][] paramsBuffer = new byte[UPMEM.dpuInUse][24 * perDPUBufferSize];
    public int[][] paramsBufferPointer = new int[UPMEM.dpuInUse][24];
    public int[] taskletPosition = new int[UPMEM.dpuInUse];

    Logger dispatchLogger = PIMLoggers.batchDispatchLogger;
    ExecutorService executorService = Executors.newCachedThreadPool();
    int maxResult = 1000000;
    int[] result;
    byte[] resultBytes = new byte[4 * 1024];
    public int[] recordedCount = new int[UPMEM.dpuInUse];
    public HashSet<Integer> dpusInUse = new HashSet<>();
    int dispatchedCount = 0;
    CountDownLatch latch;
    public BatchDispatcher(){
        result = new int[maxResult];
    }
    UPMEM upmem = UPMEM.getInstance();


    class DPUExecutionTask implements Runnable{
        private int id;

        public DPUExecutionTask(int dpuId){
            this.id = dpuId;
        }
        public void run(){
            try {
              //  System.out.println("DPU#" + id + "dispatched");
                upmem.getDPUManager(id).dpuExecute(null);
            } catch (DpuException e) {
                throw new RuntimeException(e);
            }
            // ignore result retrieving

//            try {
//                synchronized (resultBytes){
//                    UPMEM.getInstance().getDPUManager(id).dpu.copy(resultBytes, "return_values");
//                }
//            } catch (DpuException e) {
//                throw new RuntimeException(e);
//            }
//
//            for(int i = 0; i < recordedCount[id]; i++){
//                synchronized (resultBytes){
//                    int taskID = BytesUtils.readU4LittleEndian(resultBytes, 0);
//                    int res = BytesUtils.readU4LittleEndian(resultBytes, 0);
//                    //result[taskID + dispatchedCount] = res;
//                }
//            }
            System.out.println("dpu#" + id + " finished");

            latch.countDown();

        }
    }
    {
        dispatchLogger.setEnable(false);
    }


    public void dispatchAll() throws DpuException {
        for (int dpuID: dpusInUse) {
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
            UPMEM.getInstance().getDPUManager(dpuID).garbageCollector.transfer(DPUJVMMemSpaceKind.DPU_PARAMETER_BUFFER,paramsBuffer[dpuID], parameterBufferBeginAddr);
        }

        int count = 0;

        // O(|DPUs|)
        for(int dpuID : dpusInUse){
          count += recordedCount[dpuID];
        }

        System.out.println("=== dispatch all ====");

        latch = new CountDownLatch(dpusInUse.size());

        for(int dpuID : dpusInUse){
            executorService.submit(new DPUExecutionTask(dpuID));
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        for(int dpuID : dpusInUse){
            recordedCount[dpuID] = 0;
            Arrays.fill(paramsBufferPointer[dpuID], 0);
            Arrays.fill(paramsBuffer[dpuID], (byte)0);
        }

        dpusInUse.clear();
        System.out.println("all dispatched");
        dispatchedCount += count;

        System.out.println("current total count == " + dispatchedCount);
    }
}
