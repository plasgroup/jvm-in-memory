package framework.pim.dpu;

import com.upmem.dpu.Dpu;
import com.upmem.dpu.DpuException;
import framework.lang.struct.DummyProxy;
import framework.pim.*;
import framework.lang.struct.DPUObjectHandler;
import framework.lang.struct.IDPUProxyObject;
import framework.pim.dpu.cache.DPUClassFileLookupTableItem;
import framework.pim.dpu.cache.DPULookupTableManager;
import framework.pim.dpu.cache.DPUMethodLookupTableItem;
import framework.pim.dpu.java_strut.DPUJClass;
import framework.pim.dpu.java_strut.VirtualTableItem;
import framework.pim.utils.BytesUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Iterator;

import static framework.pim.dpu.java_strut.DPUJVMMemSpaceKind.DPU_HEAPSPACE;

public class DPUManagerUPMEM extends DPUManager{


    public <T> DummyProxy createObject(Class c, Object[] params) {
        if(UPMEM.getConfigurator().isUseSimulator()) {
            int fieldCount = calcFieldCount(c);
            int instanceSize = 8 + fieldCount * 4;
            byte[] objectDataStream = new byte[(instanceSize + 7) & ~7];
            int classAddr;
            int initMethodAddr;
            if(classCacheManager.getClassLookupTableItem(c.getName().replace(".","/")) == null){
                dpuClassFileManager.loadClassToDPU(c);
            }
            classAddr = classCacheManager.getClassLookupTableItem(c.getName().replace(".","/")).marmAddr;
            String initMethodDesc = generateInitializationDescriptor(params);

            initMethodAddr = classCacheManager
                    .getMethodLookupTableItem(c.getName().replace(".", "/"), initMethodDesc).mramAddr;

            BytesUtils.writeU4LittleEndian(objectDataStream, classAddr, 4);

            int objAddr = 0;
            objAddr = garbageCollector.allocate(DPU_HEAPSPACE, instanceSize);
            garbageCollector.transfer(DPU_HEAPSPACE, objectDataStream, objAddr);
            DPUObjectHandler handler = garbageCollector.dpuAddress2ObjHandler(objAddr, dpuID);

            // VirtualTable virtualTable = UPMEM.getInstance().getDPUManager(dpuID).classCacheManager.getClassStructure("framework.pim/algorithm/DPUTreeNode").virtualTable;

            // dpuManagerLogger.logln("" + virtualTable);

            // call the init func
            callNonstaticMethod(classAddr, initMethodAddr, handler.address, params);
            return new DummyProxy(handler.dpuID, handler.address);
        }
        int fieldCount = calcFieldCount(c);
        int instanceSize = 8 + fieldCount * 4;
        byte[] objectDataStream = new byte[(instanceSize + 7) & ~7];
        int classAddr;
        int initMethodAddr = 0;
        if(classCacheManager.getClassLookupTableItem(c.getName().replace(".","/")) == null){
            dpuClassFileManager.loadClassToDPU(c);
        }
        classAddr = classCacheManager.getClassLookupTableItem(c.getName().replace(".","/")).marmAddr;
        String initMethodDesc = generateInitializationDescriptor(params);


        // TODO: to do method signature matching here. when classCacheManager
        //                .getMethodCacheItem(c.getName().replace(".", "/"), initMethodDesc) is null,
        //       we need iterate the class's method table to check whether there exists a method that with the
        //       same name of the method we want to call with a parameter list that each parameter is assignable
        //       from the correspondent given argument.
        if(classCacheManager
                .getMethodLookupTableItem(c.getName().replace(".", "/"), initMethodDesc) != null){

            initMethodAddr = classCacheManager
                    .getMethodLookupTableItem(c.getName().replace(".", "/"), initMethodDesc).mramAddr;
        }else{
            Dictionary<String, DPUMethodLookupTableItem> stringDPUMethodCacheItemDictionary = classCacheManager.methodCache.cache.get(c.getName().replace(".", "/"));

            Enumeration<String> keys = stringDPUMethodCacheItemDictionary.keys();
            while(keys.hasMoreElements()){
                String key = keys.nextElement();
                if(parseParameterList(key, params)){
                    initMethodAddr = stringDPUMethodCacheItemDictionary.get(key).mramAddr;
                }
            }
            throw new RuntimeException("No appropriate method is found.");
        }
        BytesUtils.writeU4LittleEndian(objectDataStream, classAddr, 4);

        int objAddr =
                0;
        objAddr = garbageCollector.allocate(DPU_HEAPSPACE, instanceSize);
        garbageCollector.transfer(DPU_HEAPSPACE, objectDataStream, objAddr);
        DPUObjectHandler handler = garbageCollector.dpuAddress2ObjHandler(objAddr, dpuID);


        // VirtualTable virtualTable = UPMEM.getInstance().getDPUManager(dpuID).classCacheManager.getClassStructure("framework.pim/algorithm/DPUTreeNode").virtualTable;

        // dpuManagerLogger.logln("" + virtualTable);

        // call the init func
        callNonstaticMethod(classAddr, initMethodAddr, handler.address, params);
        return new DummyProxy(handler.dpuID, handler.address);
    }

    @Override
    public <T> IDPUProxyObject createObjectSpecific(Class c, String descriptor, Object... params) throws IOException {
        return null;
    }

    private boolean parseParameterList(String key, Object[] params) {
        StringBuilder sb = new StringBuilder(key);
        int pos =  0;

        while (pos < sb.length()){
            if (sb.charAt(pos) == '(') {
                pos++;
                break;
            } else {
                pos++;
            }
        }
        int state = 0;
        int paramIndex = 0;
        StringBuilder matched = new StringBuilder();
        while (pos < sb.length()){
            if (sb.charAt(pos) == ')') {
                pos++;
                break;
            } else {
                char ch = sb.charAt(pos);
                if(state == 0){
                    switch (ch){
                        case 'B':
                            if(paramIndex >= params.length) return false;
                            if(Byte.class.isAssignableFrom(params[paramIndex].getClass())){
                                paramIndex++;
                                pos++;
                                break;
                            }else{
                                return false;
                            }
                        case 'C':
                            if(paramIndex >= params.length) return false;
                            if(Character.class.isAssignableFrom(params[paramIndex].getClass())){
                                paramIndex++;
                                pos++;
                                break;
                            }else{
                                return false;
                            }
                        case 'D':
                            if(paramIndex >= params.length) return false;
                            if(Double.class.isAssignableFrom(params[paramIndex].getClass())){
                                paramIndex++;
                                pos++;
                                break;
                            }else{
                                return false;
                            }
                        case 'F':
                            if(paramIndex >= params.length) return false;
                            if(Float.class.isAssignableFrom(params[paramIndex].getClass())){
                                paramIndex++;
                                pos++;
                                break;
                            }else{
                                return false;
                            }
                        case 'I':
                            if(paramIndex >= params.length) return false;
                            if(Integer.class.isAssignableFrom(params[paramIndex].getClass())){
                                paramIndex++;
                                pos++;
                                break;
                            }else{
                                return false;
                            }
                        case 'J':
                            if(paramIndex >= params.length) return false;
                            if(Long.class.isAssignableFrom(params[paramIndex].getClass())){
                                paramIndex++;
                                pos++;
                                break;
                            }else{
                                return false;
                            }
                        case 'S':
                            if(paramIndex >= params.length) return false;
                            if(Short.class.isAssignableFrom(params[paramIndex].getClass())){
                                paramIndex++;
                                pos++;
                                break;
                            }else{
                                return false;
                            }
                        case 'Z':
                            if(paramIndex >= params.length) return false;
                            if(Boolean.class.isAssignableFrom(params[paramIndex].getClass())){
                                paramIndex++;
                                pos++;
                                break;
                            }else{
                                return false;
                            }
                        case 'L':
                            if(paramIndex >= params.length) return false;
                            state = 1;
                            pos++;
                            break;
                        case '[':
                            if(paramIndex >= params.length) return false;
                            break;
                    }
                }else if(state == 1){
                    if(paramIndex >= params.length) return false;
                    if(ch != ';'){
                        matched.append(ch);
                        pos++;
                    }else{
                        try {
                            if(Class.forName(matched.toString().replace("/",".")).isAssignableFrom(params[paramIndex].getClass())){
                                paramIndex++;
                                pos++;
                            }
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                        matched.delete(0, matched.length());
                        state = 0;
                    }
                }
            }
        }

        return true;
    }

    public DPUManagerUPMEM(Dpu upmemdpu, int dpuID) throws DpuException {
        this.dpuID = dpuID;
        this.dpu = upmemdpu;
        garbageCollector = new DPUGarbageCollectorUPMEM(dpuID, dpu);
        dpuClassFileManager = new DPUClassFileManagerUPMEM(dpuID, dpu);
        classCacheManager = new DPULookupTableManagerUPMEM(dpuID, dpu);
    }

    @Override
    public void dpuExecute(PrintStream printStream) throws DpuException {
        dpu.exec(printStream);
        garbageCollector.readBackHeapSpacePt();
    }
    public void setClassPt(int classPt, int tasklet) throws DpuException {
        byte[] data = new byte[4];
        BytesUtils.writeU4LittleEndian(data, classPt, 0);

        dpu.copy("exec_class_pt", data, 4 * tasklet);

    }
    public void setMethodPt(int methodPt, int tasklet) throws DpuException {
        byte[] data = new byte[4];
        BytesUtils.writeU4LittleEndian(data, methodPt, 0);

        dpu.copy("exec_method_pt", data, 4 * tasklet);


    }
    @Override
    public void callNonstaticMethod(int classPt, int methodPt, int instanceAddr, Object[] params) {
        if(UPMEM.batchDispatchingRecording) {
            int t = UPMEM.batchDispatcher.taskletPosition[dpuID];
            int t2 = (t + 1) % UPMEM.perDPUThreadsInUse;
            int size = (((1 + 2 + 1 + params.length) * 4) + 0b111) & (~0b111);
            BatchDispatcher bd = UPMEM.batchDispatcher;
            if(UPMEM.isSpecifyTasklet(dpuID)){
                t2 = UPMEM.getSpecifiedTaskletAndCancel(dpuID);
            }else{
                while(t2 != t){
                    if(bd.paramsBufferPointer[dpuID][t2] + size < DPUGarbageCollector.perTaskletParameterBufferSize){
                        break;
                    }
                    t2 = (t2 + 1) % UPMEM.perDPUThreadsInUse;
                    try {
                        bd.dispatchAll();
                    }
                    catch (DpuException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            bd.taskletPosition[dpuID] = t2; // next time from t2 to find a proper tasklet
            // beginning of params_buffer[t2]
            int from = bd.paramsBufferPointer[dpuID][t2] + DPUGarbageCollector.perTaskletParameterBufferSize * t2;
            // id
            BytesUtils.writeU4LittleEndian(UPMEM.batchDispatcher.paramsBuffer[dpuID], bd.recordedCount[dpuID]++, from);
            // class address
            BytesUtils.writeU4LittleEndian(UPMEM.batchDispatcher.paramsBuffer[dpuID], classPt, from + 4);
            // method address
            BytesUtils.writeU4LittleEndian(UPMEM.batchDispatcher.paramsBuffer[dpuID], methodPt, from + 8);
            // instance address
            BytesUtils.writeU4LittleEndian(UPMEM.batchDispatcher.paramsBuffer[dpuID], instanceAddr, from + 12);
            int offset = 16;
            for(Object obj : params){
                int v;
                if(obj instanceof Integer){
                    v = (int) obj;
                }else if(obj instanceof IDPUProxyObject){
                    v = ((IDPUProxyObject)obj).getAddr();
                    if(((IDPUProxyObject)obj).getDpuID() != dpuID){
                        throw new RuntimeException("all objects in the argument list should be at the same place");
                    }
                }else{
                    throw new RuntimeException("can not send CPU object to DPU");
                }
                BytesUtils.writeU4LittleEndian(UPMEM.batchDispatcher.paramsBuffer[dpuID], v, from + offset);
                offset += 4;
            }

            // System.out.println("write to dpu " + dpuID + " tasklet " + t2 + " buffer from " + UPMEM.batchDispatcher.paramsBufferPointer[dpuID][t2]);
            bd.paramsBufferPointer[dpuID][t2] += size;
            bd.dpusInUse.add(dpuID);
            return;
        }
        int tasklet = 0;


        if(UPMEM.perDPUThreadsInUse != 1){
            // choose a tasklet
            if(UPMEM.isSpecifyTasklet(dpuID)){
                tasklet = UPMEM.getSpecifiedTaskletAndCancel(dpuID);
            }else{
                tasklet = currentTasklet;
                while(true){
                    if(taskletSemaphore[tasklet] == 0){
                        synchronized (taskletSemaphore){
                            if(taskletSemaphore[tasklet] == 0){
                                taskletSemaphore[tasklet] = 1;
                                currentTasklet = tasklet;
                                break;
                            }
                        }
                    }else{
                        tasklet = (tasklet + 1) % UPMEM.perDPUThreadsInUse;
                    }
                }
            }

            taskletSemaphore[tasklet] = 0;
            currentTasklet = (currentTasklet + 1) % UPMEM.perDPUThreadsInUse;

        }

        int[] paramsConverted = new int[params.length + 4];
        paramsConverted[0] = 0;
        paramsConverted[1] = classPt;
        paramsConverted[2] = methodPt;
        paramsConverted[3] = instanceAddr;



        int i = 4;
        for(Object obj : params){
            if(obj instanceof Integer){
                paramsConverted[i] = (int) obj;
            }else if(obj instanceof IDPUProxyObject){
                paramsConverted[i] = ((IDPUProxyObject)obj).getAddr();

                if(((IDPUProxyObject)obj).getDpuID() != dpuID){
                    throw new RuntimeException("all objects in the argument list should be at the same place");
                }
            }
            i++;
        }

        try {
            garbageCollector.pushParameters(paramsConverted, tasklet);

            dpuExecute(null);
        } catch (DpuException e) {
            throw new RuntimeException(e);
        }

    }

    private void printClassCache(int dpuID) {
        DPULookupTableManager.DPUClassLookupTable dpuClassLookupTable =
                UPMEM.getInstance().getDPUManager(dpuID).classCacheManager.dpuClassLookupTable;
        for (Iterator<String> it = dpuClassLookupTable.cache.keys().asIterator(); it.hasNext(); ) {
            String key = it.next();
            DPUClassFileLookupTableItem dpuClassFileLookupTableItem = dpuClassLookupTable.cache.get(key);
            System.out.println("----- " + key + " ------");
            System.out.println("classId: " + dpuClassFileLookupTableItem.classId);
            System.out.printf("mram addr: %x\n",dpuClassFileLookupTableItem.marmAddr);
            printVTable(dpuClassFileLookupTableItem.dpuClassStructure);
        }
    }

    private void printMethodCache(int dpuID) {
        DPULookupTableManager.DPUMethodLookupTable dpuMethodLookupTable =
                UPMEM.getInstance().getDPUManager(dpuID).classCacheManager.methodCache;
        System.out.println(" print method cache, class count = " + dpuMethodLookupTable.cache.size()
        );
        for (Iterator<String> it = dpuMethodLookupTable.cache.keys().asIterator(); it.hasNext(); ) {
            String key = it.next();
            System.out.println(" ------- class " + key + "-------");

            Dictionary<String, DPUMethodLookupTableItem> dpuMethodLookupTableItem =
                    dpuMethodLookupTable.cache.get(key);

            for (Iterator<String> mit = dpuMethodLookupTableItem.keys().asIterator(); mit.hasNext();){
                String mName = mit.next();
                System.out.println(" ----- method " + mName);
                DPUMethodLookupTableItem dpuMethodLookupTableItem1 = dpuMethodLookupTableItem.get(mName);
                System.out.printf("------- mram = %x\n", dpuMethodLookupTableItem1.mramAddr);
            }

        }

    }

    private void printVTable(DPUJClass dpujClass) {
        for(int i = 0; i < dpujClass.virtualTable.items.size(); i++){
            VirtualTableItem virtualTableItem = dpujClass.virtualTable.items.get(i);

            System.out.printf(" >>>>> class name = %s   desc = %s " +
                            " class ref = %x   " +
                    "method ref = %x\n"
            , virtualTableItem.className, virtualTableItem.descriptor,
                    virtualTableItem.classReferenceAddress, virtualTableItem.methodReferenceAddress);
        }
    }
}