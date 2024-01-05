package framework.primitive.linq;

import framework.pim.UPMEM;
import framework.pim.dpu.DPUGarbageCollector;
import framework.primitive.control.ControlPrimitives;
import framework.primitive.control.IDPUSingleFunction1Parameter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LinqFacade {

    public static void join(){

    }
    public static List<DPURecordRef> select(int dpuID, IDPUSingleFunction1Parameter selectFunction){
        // send anonymous class to DPU
        // set DPUs' "PC" pointer
        // execute (use all tasklets to scan heap)
        // retrieve result from each DPU
        // merge result

        int resultBeginAddress = (int) ControlPrimitives.dispatchFunction(dpuID, selectFunction);
        DPUGarbageCollector garbageCollector = UPMEM.getInstance().getDPUManager(dpuID).garbageCollector;
        int count = garbageCollector.getInt32(resultBeginAddress);
        List<DPURecordRef> result = new ArrayList<>();
        for(int i = 0; i < count; i++){
            resultBeginAddress += 4;
            result.add(new DPURecordRef(dpuID, garbageCollector.getInt32(resultBeginAddress)));
        }
        return result;
    }

    public static List<DPURecordRef> select(IDPUSingleFunction1Parameter selectFunction){
        // could be parallelized
        List<DPURecordRef>[] results = new List[UPMEM.dpuInUse];
        for(int i = 0; i < UPMEM.dpuInUse; i++){
            results[i] = select(i, selectFunction);
        }

        // merge
        return Stream.of(results).flatMap(Collection::stream).collect(Collectors.toList());
    }

    public static List<DPURecordRef> where(int dpuID, IDPUSingleFunction1Parameter filterFunction){

        int resultBeginAddress = (int) ControlPrimitives.dispatchFunction(dpuID, filterFunction);
        DPUGarbageCollector garbageCollector = UPMEM.getInstance().getDPUManager(dpuID).garbageCollector;
        int count = garbageCollector.getInt32(resultBeginAddress);
        List<DPURecordRef> result = new ArrayList<>();
        for(int i = 0; i < count; i++){
            resultBeginAddress += 4;
            result.add(new DPURecordRef(dpuID, garbageCollector.getInt32(resultBeginAddress)));
        }
        return result;
    }

    public static void selectMany(){

    }

    public static void orderBy(){

    }

    public static void filter(){

    }
    public static void main(String[] args){

        ICondition i = new ICondition() {
            @Override
            public boolean condition(Object object) {
                return false;
            }
        };
        System.out.println(i.getClass());



    }
}
