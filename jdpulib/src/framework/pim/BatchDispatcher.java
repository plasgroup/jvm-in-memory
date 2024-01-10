package framework.pim;

import com.upmem.dpu.DpuException;
import framework.pim.dpu.DPUGarbageCollector;
import framework.pim.dpu.java_strut.DPUJVMMemSpaceKind;
import framework.pim.logger.Logger;
import framework.pim.logger.PIMLoggers;
import framework.pim.utils.BytesUtils;
import simulator.DPUJVMRemote;
import simulator.DPUJVMRemoteImpl;
import simulator.JVMSimulatorResult;

import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.*;

import static framework.pim.dpu.DPUGarbageCollector.*;

public class BatchDispatcher {
    public byte[][] paramsBuffer = new byte[UPMEM.dpuInUse][perTaskletParameterBufferSize];
    public int[][] paramsBufferPointer = new int[UPMEM.dpuInUse][UPMEM.perDPUThreadsInUse];
    public int[] taskletPosition = new int[UPMEM.dpuInUse]; // use for Round-robin scheduling

    Logger dispatchLogger = PIMLoggers.batchDispatchLogger;
    ExecutorService executorService = Executors.newCachedThreadPool();
    int maxResult = 1000000;
    int[] result;
    byte[] resultBytes = new byte[4 * 1024];
    public int[] recordedCount = new int[UPMEM.dpuInUse];
    public HashSet<Integer> dpusInUse = new HashSet<>(); // record the dpu that has more than 1 tasks in record list.
    int dispatchedCount = 0;
    CountDownLatch latch;
    public BatchDispatcher(){
        result = new int[maxResult];
    }
    UPMEM upmem = UPMEM.getInstance();


    {
        dispatchLogger.setEnable(true);
    }

    public int getResult(int tid) {
        return result[tid];
    }

    /** DPU task structure **/
    class DPUExecutionTask implements Runnable{
        private int id;

        public DPUExecutionTask(int dpuId){
            this.id = dpuId;
        }
        public void run(){
            try {
                dispatchLogger.logln("DPU#" + id + "dispatched, " + recordedCount[id] +" tasks. pointer = " + paramsBufferPointer[id]);
                upmem.getDPUManager(id).dpuExecute(null);
            } catch (DpuException e) {
                throw new RuntimeException(e);
            }

            dispatchLogger.logln("retrieve result (DPU#" + id + ")");
            /** result retrieving **/

            if(!UPMEM.getConfigurator().isUseSimulator()){
                try {
                    synchronized (resultBytes){
                        UPMEM.getInstance().getDPUManager(id).dpu.copy(resultBytes, "return_values");
                    }
                } catch (DpuException e) {
                    throw new RuntimeException(e);
                }
            }


            for(int i = 0; i < recordedCount[id]; i++){
                if(!UPMEM.getConfigurator().isUseSimulator()){
                    synchronized (resultBytes){
                        /** | task ID (4 bytes) | value (4 bytes) | task ID (4 bytes) | value (4 bytes) | ... **/
                        int taskID = BytesUtils.readU4LittleEndian(resultBytes, i * 8);
                        int res = BytesUtils.readU4LittleEndian(resultBytes, i * 8 + 4);
                        result[taskID + dispatchedCount] = res;
                    }
                }else{
                    Object dpu = UPMEM.getInstance().getDpu(id);
                    DPUJVMRemote backend = (DPUJVMRemote) dpu;
                    try {
                        JVMSimulatorResult result = backend.getResult(i);
                        // System.out.println("task id = " + result.taskID);
                        BytesUtils.writeU4LittleEndian(resultBytes, (Integer) result.value, 4 * result.taskID);
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }

                }

            }

            dispatchLogger.logln("dpu#" + id + " finished");

            latch.countDown();

        }
    }


    {
        dispatchLogger.setEnable(false);
    }


    /** dispatch all recorded tasks **/
    public void dispatchAll() throws DpuException {
        for (int dpuID: dpusInUse) {
            dispatchLogger.logln(" === write tasks to DPU " + dpuID + " === ");
            byte[] ptBytes = new byte[4];

            // for each tasklet of a DPU
            for(int i = 0; i < UPMEM.perDPUThreadsInUse; i++){
                int pbp = paramsBufferPointer[dpuID][i];
                if(pbp == 0) continue;

                dispatchLogger.logln("dpu id = " + dpuID + " tasklet " + i + ":");
                dispatchLogger.logln("parameters buffer pointer = " + pbp);
                // pbp is relative offset of a tasklet, so, the i'th tasklet's  params_buffer_pt[i] should be parameterBufferBeginAddr + i * perDPUBufferSize + pbp
                // BytesUtils.writeU4LittleEndian(ptBytes, parameterBufferBeginAddr + i * perTaskletParameterBufferSize + pbp,0);
                // write pointer to params_buffer_pt[i]
                BytesUtils.writeU4LittleEndian(ptBytes, i * perTaskletParameterBufferSize + pbp,0);
                UPMEM.getInstance().getDPUManager(dpuID).garbageCollector.transfer(DPUJVMMemSpaceKind.DPU_PARAMETER_BUFFER_POINTERS, ptBytes , + 4 * i);
            }

            // transfer the DPU#i's params buffer
            UPMEM.getInstance().getDPUManager(dpuID).garbageCollector.transfer(DPUJVMMemSpaceKind.DPU_PARAMETER_BUFFER, paramsBuffer[dpuID], parameterBufferBeginAddr);
        }

        int count = 0;

        // O(|DPUs|)
        for(int dpuID : dpusInUse){
          count += recordedCount[dpuID];
        }

        dispatchLogger.logln("=== dispatch all ====");

        latch = new CountDownLatch(UPMEM.dpuInUse);

        for(int dpuID : dpusInUse){
            new DPUExecutionTask(dpuID).run();
            //executorService.submit(new DPUExecutionTask(dpuID));
        }

//        try {
//            latch.await();
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }

        for(int dpuID : dpusInUse){
            recordedCount[dpuID] = 0;
            Arrays.fill(paramsBufferPointer[dpuID], 0);
            Arrays.fill(paramsBuffer[dpuID], (byte)0);
        }
        dispatchLogger.setEnable(true);
        dpusInUse.clear();
        dispatchLogger.logln("All dispatched");
        dispatchedCount += count;

        dispatchLogger.logln("current total count == " + dispatchedCount);
    }
}