package framework.primitive.control;

import com.upmem.dpu.DpuException;
import framework.pim.ProxyHelper;
import framework.pim.UPMEM;
import framework.pim.dpu.DPUGarbageCollector;
import framework.pim.dpu.cache.DPUCacheManager;
import framework.pim.dpu.cache.DPUClassFileCacheItem;
import framework.pim.dpu.cache.DPUFieldCacheItem;
import framework.pim.dpu.cache.DPUMethodCacheItem;
import framework.pim.dpu.classloader.ClassFileAnalyzer;
import framework.pim.dpu.classloader.ClassFileAnalyzerConstants;
import framework.pim.dpu.classloader.ClassWriter;
import framework.pim.dpu.classloader.DPUClassFileManager;
import framework.pim.dpu.java_strut.DPUJClass;
import framework.pim.dpu.java_strut.DPUJVMMemSpaceKind;
import framework.pim.dpu.java_strut.VirtualTableItem;
import framework.pim.utils.BytesUtils;
import framework.pim.utils.ClassLoaderUtils;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;

import static framework.pim.ProxyHelper.invokeMethod;
import static framework.pim.ProxyHelper.upmem;
import static framework.pim.dpu.classloader.ClassWriter.pushJClassToDPU;
import static framework.pim.logger.PIMLoggers.classfileLogger;
import static framework.pim.utils.ClassLoaderUtils.formalClassName;
import static framework.pim.utils.ClassLoaderUtils.getUTF8;

public class ControlPrimitives {

    private static int dispatchFunctionHelper(int dpuID, Class anomyousClass, Object... params){
        // TODO: add Class type judgement, it should be one of the 16 interfaces IDPUSingleFunctionParameter

        DPUGarbageCollector garbageCollector = UPMEM.getInstance().getDPUManager(dpuID).garbageCollector;
        DPUClassFileManager classFileManager = UPMEM.getInstance().getDPUManager(dpuID).dpuClassFileManager;
        DPUCacheManager cacheManager = UPMEM.getInstance().getDPUManager(dpuID).classCacheManager;

        // get real class
        anomyousClass = anomyousClass.getInterfaces().length == 0 ? anomyousClass.getSuperclass() : anomyousClass.getInterfaces()[0];
        String cName = anomyousClass.getName().replace(".", "/");

        // java/lang/Object is needed
        classFileManager.loadClassToDPU(Object.class);

        // send anomynous class to JVM temporary
        DPUJClass dpuJClass = classFileManager.loadClassToDPU(anomyousClass);

        int beginAddress = garbageCollector.allocate(DPUJVMMemSpaceKind.DPU_METASPACE, dpuJClass.totalSize);
        byte[] bytes = ClassWriter.cvtDPUClassStrut2Bytes(dpuJClass, beginAddress);
        garbageCollector.transfer(DPUJVMMemSpaceKind.DPU_METASPACE, bytes, beginAddress);

        StringBuilder sb = new StringBuilder();
        sb.append("function:(");
        sb.append("Ljava/lang/Object;".repeat(params.length));
        sb.append(")Ljava/lang/Object;");

        ProxyHelper.invokeMethod(dpuID, -1, cName, String.valueOf(sb), params);

        // clear
        garbageCollector.freeFromBack(DPUJVMMemSpaceKind.DPU_METASPACE, dpuJClass.totalSize);
        DPUClassFileCacheItem dpuClassFileCacheItem = cacheManager.dpuClassCache.cache.get(cName);

        // clear structure storage in lookup table
        cacheManager.dpuClassCache.cache.remove(cName);
        cacheManager.dpuClassCache.dpuClassFileCacheItemList.remove(dpuClassFileCacheItem);
        cacheManager.methodCache.cache.remove(cName);

        throw new RuntimeException("");
    }

    public static Object dispatchFunction(int dpuID, IDPUSingleFunction1Parameter function, Object... obj){
        return dispatchFunctionHelper(dpuID, function.getClass(), obj);
    }
    public static Object dispatchFunction(int dpuID, IDPUSingleFunction2Parameter function, Object... obj){
        return dispatchFunctionHelper(dpuID, function.getClass(), obj);
    }
    public static Object dispatchFunction(int dpuID, IDPUSingleFunction3Parameter function, Object... obj){
        return dispatchFunctionHelper(dpuID, function.getClass(), obj);
    }
    public static Object dispatchFunction(int dpuID, IDPUSingleFunction4Parameter function, Object... obj){
        return dispatchFunctionHelper(dpuID, function.getClass(), obj);
    }
    public static Object dispatchFunction(int dpuID, IDPUSingleFunction5Parameter function, Object... obj){
        return dispatchFunctionHelper(dpuID, function.getClass(), obj);
    }
    public static Object dispatchFunction(int dpuID, IDPUSingleFunction6Parameter function, Object... obj){
        return dispatchFunctionHelper(dpuID, function.getClass(), obj);
    }
    public static Object dispatchFunction(int dpuID, IDPUSingleFunction7Parameter function, Object... obj){
        return dispatchFunctionHelper(dpuID, function.getClass(), obj);
    }
    public static Object dispatchFunction(int dpuID, IDPUSingleFunction8Parameter function, Object... obj){
        return dispatchFunctionHelper(dpuID, function.getClass(), obj);
    }
    public static Object dispatchFunction(int dpuID, IDPUSingleFunction9Parameter function, Object... obj){
        return dispatchFunctionHelper(dpuID, function.getClass(), obj);
    }
    public static Object dispatchFunction(int dpuID, IDPUSingleFunction10Parameter function, Object... obj){
        return dispatchFunctionHelper(dpuID, function.getClass(), obj);
    }
    public static Object dispatchFunction(int dpuID, IDPUSingleFunction11Parameter function, Object... obj){
        return dispatchFunctionHelper(dpuID, function.getClass(), obj);
    }
    public static Object dispatchFunction(int dpuID, IDPUSingleFunction12Parameter function, Object... obj){
        return dispatchFunctionHelper(dpuID, function.getClass(), obj);
    }
    public static Object dispatchFunction(int dpuID, IDPUSingleFunction13Parameter function, Object... obj){
        return dispatchFunctionHelper(dpuID, function.getClass(), obj);
    }
    public static Object dispatchFunction(int dpuID, IDPUSingleFunction14Parameter function, Object... obj){
        return dispatchFunctionHelper(dpuID, function.getClass(), obj);
    }
    public static Object dispatchFunction(int dpuID, IDPUSingleFunction15Parameter function, Object... obj){
        return dispatchFunctionHelper(dpuID, function.getClass(), obj);
    }
    public static Object dispatchFunction(int dpuID, IDPUSingleFunction16Parameter function, Object... obj){
        return dispatchFunctionHelper(dpuID, function.getClass(), obj);
    }


}
