package simulator;

import com.upmem.dpu.DpuException;
import framework.pim.BatchDispatcher;
import framework.lang.struct.IDPUProxyObject;
import framework.pim.ProxyHelper;
import framework.pim.UPMEM;
import framework.pim.dpu.DPUGarbageCollector;
import framework.pim.dpu.DPUManager;
import framework.lang.struct.DPUObjectHandler;
import framework.pim.dpu.cache.DPUMethodCacheItem;
import framework.pim.utils.BytesUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Dictionary;
import java.util.Enumeration;

import static framework.pim.UPMEM.generateProxyObject;
import static framework.pim.UPMEM.packageSearchPath;
import static framework.pim.dpu.java_strut.DPUJVMMemSpaceKind.DPU_HEAPSPACE;

public class DPUManagerSimulator extends DPUManager {

    private DPUJVMRemote dpujvmRemote;
    public DPUManagerSimulator(int dpuID) {
        System.out.println("Init DPU " +  dpuID + "'s JVM");
        this.dpuID = dpuID;
        try {
            this.dpujvmRemote = (DPUJVMRemote) LocateRegistry.getRegistry("localhost", 9239 + dpuID).lookup("DPUJVM" + dpuID);
        } catch (RemoteException | NotBoundException e) {
            throw new RuntimeException(e);
        }
        garbageCollector = new DPUGarbageCollectorSimulator(dpuID, dpujvmRemote);
        dpuClassFileManager = new DPUClassFileManagerSimulator(dpuID, dpujvmRemote);
        classCacheManager = new DPUCacheManagerSimulator(dpuID, dpujvmRemote);
    }

    @Override
    public void dpuExecute(PrintStream printStream) {
        try {
            dpujvmRemote.start();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void callNonstaticMethod(int classPt, int methodPt, int instanceAddress, Object... params) {
        if(UPMEM.batchDispatchingRecording) {
            int lastTaskletId = UPMEM.batchDispatcher.taskletPosition[dpuID];
            int taskletId = (lastTaskletId + 1) % 24;
            int size = (((1 + 2 + 1 + params.length) * 4) + 0b111) & (~0b111);
            BatchDispatcher bd = UPMEM.batchDispatcher;
            if(UPMEM.isSpecifyTasklet(dpuID)){
                taskletId = UPMEM.getSpecifiedTaskletAndCancel(dpuID);
            }else{
                while(taskletId != lastTaskletId){
                    if(bd.paramsBufferPointer[dpuID][taskletId] + size < PIMRemoteJVMConfiguration.heapSize){
                        break;
                    }
                    taskletId = (taskletId + 1) % 24;
                    try {
                        bd.dispatchAll();
                    } catch (DpuException e) {
                        throw new RuntimeException(e);
                    }
                }
            }



            bd.taskletPosition[dpuID] = taskletId; // next time from t2 to find a proper tasklet

            int[] paramPrepared = new int[params.length + 4];
            paramPrepared[0] = bd.recordedCount[dpuID]++;
            paramPrepared[1] = classPt;
            paramPrepared[2] = methodPt;
            paramPrepared[3] = instanceAddress;
            int offset = 4;
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
                params[offset] = v;
                offset++;
            }
            bd.paramsBufferPointer[dpuID][taskletId] += size;
            bd.dpusInUse.add(dpuID);
            try {
                dpujvmRemote.pushArguments(paramPrepared, taskletId);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            return;
        }


        // choose a tasklet
        int tasklet;
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
                    tasklet = (tasklet + 1) % PIMRemoteJVMConfiguration.threadCount;
                }
            }
        }

        int[] paramsConverted = new int[params.length + 4];
        paramsConverted[0] = 0;         // task id
        paramsConverted[1] = classPt;   // class location
        paramsConverted[2] = methodPt;  // method location
        paramsConverted[3] = instanceAddress; // instance address

        int i = 4;
        System.out.println("push params...");

        for (int j = 0; j < params.length; j++){
            Object obj = params[j];
            if(obj.getClass().isAssignableFrom(Integer.class)){
                paramsConverted[i] = (int) obj;
            }else if(IDPUProxyObject.class.isAssignableFrom(obj.getClass())){
                paramsConverted[i] = ((IDPUProxyObject)obj).getAddr();
                if(((IDPUProxyObject)obj).getDpuID() != dpuID){
                    throw new RuntimeException("all objects in the argument list should be at the same place");
                }
            }
            i++;
        }


        try {
            garbageCollector.pushParameters(paramsConverted, tasklet);
            dpujvmRemote.start();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        taskletSemaphore[tasklet] = 0;
        currentTasklet = (currentTasklet + 1) % PIMRemoteJVMConfiguration.threadCount;
    }

    @Override
    public <T> IDPUProxyObject createObjectSpecific(Class c, String descriptor, Object... params) throws IOException {
        int fieldCount = calcFieldCount(c);
        int instanceSize = 8 + fieldCount * 4;
        byte[] objectDataStream = new byte[(instanceSize + 7) & ~7];
        int classAddr;
        int initMethodAddr = -1;
        String className = c.getName().replace(".", "/");
        if(classCacheManager.getClassStrutCacheLine(className) == null){
            dpuClassFileManager.loadClassToDPU(c);
        }

        System.out.println(c.getName().replace(".","/"));
        classAddr = classCacheManager.getClassStrutCacheLine(c.getName().replace(".","/")).marmAddr;
        dpuManagerLogger.logln(" * Get Class Addr = " + classAddr);

        DPUMethodCacheItem methodCacheItem = classCacheManager
                .getMethodCacheItem(className, descriptor);
        if(methodCacheItem != null){
            initMethodAddr = methodCacheItem.mramAddr;
        }else{
            throw new RuntimeException("No appropriate method is found. " + descriptor);
        }


        BytesUtils.writeU4LittleEndian(objectDataStream, classAddr, 4);

        int objAddr;
        try {
            objAddr = dpujvmRemote.allocateObject();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        garbageCollector.transfer(DPU_HEAPSPACE, objectDataStream, objAddr);
        DPUObjectHandler handler = DPUGarbageCollector.dpuAddress2ObjHandler(objAddr, dpuID);
        dpuManagerLogger.logln("---> Object Create Finish, handler = " + " (addr: " + handler.address + "," + "dpu: " + handler.dpuID + ") <---");
        System.out.println("get obj addr = " + objAddr);


        // call the init func
        if(params.length == 0){
            callNonstaticMethod(classAddr, initMethodAddr, handler.address);
        }else if(params.length == 1){
            callNonstaticMethod(classAddr, initMethodAddr, handler.address, params[0]);
        }else if(params.length == 2){
            callNonstaticMethod(classAddr, initMethodAddr, handler.address, params[0], params[1]);
        } else if(params.length == 3){
            callNonstaticMethod(classAddr, initMethodAddr, handler.address, params[0], params[1], params[2]);
        }else if(params.length == 4){
            callNonstaticMethod(classAddr, initMethodAddr, handler.address, params[0], params[1], params[2], params[3]);
        }
        try {
            Class pClass = Class.forName( packageSearchPath + c.getSimpleName() + "Proxy");
            return generateProxyObject(pClass, handler.dpuID, handler.address);
        } catch (NoSuchFieldException | InstantiationException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DPUObjectHandler createObject(Class c, Object... params) {
        // TODO: simplify the createObject in Simulator. (it not need fully simulate the class loading process of DPU JVM,
        //       because in simulator, the format of the same class is the same in each processor. However, in a real
        //       DPU JVM, the JVM use a representation of java class that differ to the host JVM.
        //       But simulator need calculate the DPU class representation's size to simulate the memory space utilization.
        int fieldCount = calcFieldCount(c);
        int instanceSize = 8 + fieldCount * 4;
        byte[] objectDataStream = new byte[(instanceSize + 7) & ~7];
        int classAddr;
        int initMethodAddr = -1;
        String className = c.getName().replace(".", "/");
        if(classCacheManager.getClassStrutCacheLine(className) == null){
            dpuClassFileManager.loadClassToDPU(c);
        }

        System.out.println(c.getName().replace(".","/"));
        classAddr = classCacheManager.getClassStrutCacheLine(c.getName().replace(".","/")).marmAddr;
        dpuManagerLogger.logln(" * Get Class Addr = " + classAddr);
        String initMethodDesc = generateInitializationDescriptor(params);

        DPUMethodCacheItem methodCacheItem = classCacheManager
                .getMethodCacheItem(className, initMethodDesc);
        if(methodCacheItem != null){
            initMethodAddr = methodCacheItem.mramAddr;
        }else {
            Dictionary<String, DPUMethodCacheItem> stringDPUMethodCacheItemDictionary = classCacheManager.methodCache.cache.get(c.getName().replace(".", "/"));
            boolean found = false;
            Enumeration<String> keys = stringDPUMethodCacheItemDictionary.keys();
            while(keys.hasMoreElements()){
                String key = keys.nextElement();
                if(parseParameterList(key, params)){
                    initMethodAddr = stringDPUMethodCacheItemDictionary.get(key).mramAddr;
                    found = true;
                    break;
                }
            }
            if(!found)
                throw new RuntimeException("No appropriate method is found. " + initMethodDesc);
        }


        BytesUtils.writeU4LittleEndian(objectDataStream, classAddr, 4);

        int objAddr;
        try {
            objAddr = dpujvmRemote.allocateObject();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        garbageCollector.transfer(DPU_HEAPSPACE, objectDataStream, objAddr);
        DPUObjectHandler handler = DPUGarbageCollector.dpuAddress2ObjHandler(objAddr, dpuID);
        dpuManagerLogger.logln("---> Object Create Finish, handler = " + " (addr: " + handler.address + "," + "dpu: " + handler.dpuID + ") <---");
        System.out.println("get obj addr = " + objAddr);


        // call the init func
        if(params.length == 0){
            callNonstaticMethod(classAddr, initMethodAddr, handler.address);
        }else if(params.length == 1){
            callNonstaticMethod(classAddr, initMethodAddr, handler.address, params[0]);
        }else if(params.length == 2){
            callNonstaticMethod(classAddr, initMethodAddr, handler.address, params[0], params[1]);
        } else if(params.length == 3){
            callNonstaticMethod(classAddr, initMethodAddr, handler.address, params[0], params[1], params[2]);
        }else if(params.length == 4){
            callNonstaticMethod(classAddr, initMethodAddr, handler.address, params[0], params[1], params[2], params[3]);
        }

        return handler;

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
}
