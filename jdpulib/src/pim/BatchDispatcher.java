package pim;

import com.upmem.dpu.DpuException;
import com.upmem.dpu.DpuSet;
import pim.dpu.DPUGarbageCollector;
import pim.dpu.PIMManager;
import pim.dpu.java_strut.DPUJVMMemSpaceKind;
import pim.utils.BytesUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static pim.dpu.DPUGarbageCollector.parameterBufferBeginAddr;
import static pim.dpu.DPUGarbageCollector.perDPUBufferSize;

public class BatchDispatcher {
    public byte[][] paramsBuffer = new byte[UPMEM.dpuInUse][24 * perDPUBufferSize];
    public int[][] paramsBufferPointer = new int[UPMEM.dpuInUse][24];
    public int[] taskletPosition = new int[UPMEM.dpuInUse];

    int[] result;
    byte[] resultBytes = new byte[2048];
    public int[] recordedCount = new int[UPMEM.dpuInUse];
    public HashSet<Integer> dpusInUse = new HashSet<>();
    public void dispatchAll() throws DpuException {
        for (int dpuID: dpusInUse) {
            System.out.println(" === write tasks to DPU " + dpuID + " === ");
            byte[] ptBytes = new byte[4];
            for(int i = 0; i < 24; i++){
                int pbp = paramsBufferPointer[dpuID][i];
                if(pbp == 0) continue;

                System.out.println("dpu id = " + dpuID + " tasklet " + i + ":");
                System.out.println("parameters buffer pointer = " + pbp);
                BytesUtils.writeU4LittleEndian(ptBytes, parameterBufferBeginAddr + i * perDPUBufferSize + pbp,0);
                UPMEM.getInstance().getDPUManager(dpuID).dpu.copy("params_buffer_pt", ptBytes , 4 * i);
            }
            UPMEM.getInstance().getDPUManager(dpuID).garbageCollector.transfer(DPUJVMMemSpaceKind.DPU_PARAMETER_BUFFER,paramsBuffer[dpuID], parameterBufferBeginAddr );
        }

        int count = 0;

        // O(|DPUs|)
        for(int dpuID : dpusInUse){
          count += recordedCount[dpuID];
        }
        result = new int[count];

        // O(N)
        for(int dpuID : dpusInUse){
            UPMEM.getInstance().getDPUManager(dpuID).dpuExecute(System.out);
            UPMEM.getInstance().getDPUManager(dpuID).dpu.copy(resultBytes, "return_values");
            for(int i = 0; i < recordedCount[dpuID]; i++){
                int taskID = BytesUtils.readU4LittleEndian(resultBytes, (i * 2) * 4);
                int res = BytesUtils.readU4LittleEndian(resultBytes, (i * 2 + 1) * 4);
                result[taskID] = res;
                System.out.println(res);

            }
        }

        dpusInUse.clear();
    }
}
