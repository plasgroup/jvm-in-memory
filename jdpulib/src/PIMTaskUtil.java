import com.upmem.dpu.DpuException;
import framework.pim.UPMEM;
import framework.pim.dpu.DPUGarbageCollector;
import framework.pim.dpu.RPCHelper;
import framework.pim.dpu.cache.DPUClassFileLookupTableItem;
import framework.pim.dpu.cache.DPULookupTableManager;
import framework.pim.dpu.cache.DPUMethodLookupTableItem;
import framework.pim.dpu.classloader.ClassFileAnalyzer;
import framework.pim.dpu.classloader.ClassFileAnalyzerConstants;
import framework.pim.dpu.classloader.ClassWriter;
import framework.pim.dpu.classloader.DPUClassFileManager;
import framework.pim.dpu.java_strut.DPUJClass;
import framework.pim.dpu.java_strut.DPUJVMMemSpaceKind;
import framework.primitive.control.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.rmi.RemoteException;

import static framework.pim.UPMEM.allowSet;
import static framework.pim.dpu.classloader.ClassFileAnalyzer.printEntryTable;
import static framework.pim.dpu.classloader.ClassWriter.pushJClassToDPU;
import static framework.pim.utils.ClassLoaderUtils.formalClassName;
import static framework.pim.utils.ClassLoaderUtils.getUTF8;

public class PIMTaskUtil {
    public static byte[] getDPUFunctionBytecodeHelper(Class<?> anomyousClass){

//        Method fieldAnonymousClass;
//        // get real class
//        fieldAnonymousClass = anomyousClass.getDeclaredMethods()[0];
//        Object instanceAnonymousClass = null;
//        try {
//            instanceAnonymousClass = fieldAnonymousClass.getClass(anomyousClass);
//        } catch (IllegalAccessException e) {
//            throw new RuntimeException(e);
//        }
//
//        Class anonymousClassType = instanceAnonymousClass.getClass();
//
//        String className = anomyousClass.getName().replace(".", "/");
//
//
//
//        byte[] classFileBytes;
//
//
//        /** get bytes of a class file **/
//        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream((anomyousClass.getName().replace('.','/') + ".class"));
//
//        if (is == null) {
//            // TODO class name with form of "[....;" cannot be load
//            System.err.println("class " + (anomyousClass.getName().replace('.','/') + ".class not found"));
//            return null;
//        }
//
//
//        try {
//            classFileBytes = is.readAllBytes();
//            is.close();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//
//        /**
//         *
//         *    preliminary analyze the class
//         *    place basis information to class structure
//         *    generate unresolved constant table and entry table
//         *    place constant data in the constant area of class structure
//         *    calculate size
//         *
//         * **/
//        ClassFileAnalyzer classFileAnalyzer = ClassFileAnalyzer.fromClassBytes(classFileBytes);
//        DPUJClass jc = classFileAnalyzer.preResolve();
//
//        printEntryTable(jc);
//
//        /** load super class **/
//        String superClassName = jc.superClassNameIndex == 0 ? "" : getUTF8(jc, jc.superClassNameIndex);
//        className = formalClassName(superClassName);
//
//
//
//        for (int i = 0; i < jc.cpItemCount; i++) {
//            int tag = (int) ((jc.entryItems[i] >> 56) & 0xFF);
//            int classIndex;
//            int nameAndTypeIndex;
//            switch (tag) {
//                case ClassFileAnalyzerConstants.CT_Class:
//
//
//                    break;
//                case ClassFileAnalyzerConstants.CT_Fieldref:
//
//                    break;
//                case ClassFileAnalyzerConstants.CT_Methodref:
//                    int classCPIndex = (int) ((jc.entryItems[i] >> 16) & 0xFFFF);
//                    int nameAndTypeCPIndex = (int) ((jc.entryItems[i]) & 0xFFFF);
//                    int nameCPIndex = (int) ((jc.entryItems[nameAndTypeCPIndex] >> 16) & 0xFFFF);
//                    int typeCPIndex = (int) ((jc.entryItems[nameAndTypeCPIndex]) & 0xFFFF);
//                    int classNameUTF8CPIndex = (int) ((jc.entryItems[classCPIndex]) & 0xFFFF);
//                    String methodClassNameUTF8 = getUTF8(jc, (int) (classNameUTF8CPIndex));
//                    String methodNameUTF8 = getUTF8(jc, (int) (nameCPIndex));
//                    String methodTypeUTF8 = getUTF8(jc, (int) (typeCPIndex));
//
//                    System.out.println("description = " + methodClassNameUTF8 + "." + methodNameUTF8 + ":" + methodTypeUTF8 + ":::" +anomyousClass.getName());
//                    String descriptor = methodNameUTF8 + ":" + methodTypeUTF8;
//
//
//
//
//
//
//            }
//
//
//        }
//



        return null;

    }
    public static byte[] getDPUFunctionBytecode(IDPUSingleFunction1Parameter function){
        return getDPUFunctionBytecodeHelper(function.getClass());
    }
    public static byte[] getDPUFunctionBytecode(IDPUSingleFunction2Parameter function){
        return getDPUFunctionBytecodeHelper(function.getClass());
    }

    public static byte[] getDPUFunctionBytecode(IDPUSingleFunction3Parameter function){
        return getDPUFunctionBytecodeHelper(function.getClass());
    }

    public static byte[] getDPUFunctionBytecode(IDPUSingleFunction4Parameter function){
        return getDPUFunctionBytecodeHelper(function.getClass());
    }

    public static byte[] getDPUFunctionBytecode(IDPUSingleFunction5Parameter function){
        return getDPUFunctionBytecodeHelper(function.getClass());
    }
    public static byte[] getDPUFunctionBytecode(IDPUSingleFunction6Parameter function){
        return getDPUFunctionBytecodeHelper(function.getClass());
    }

    public static byte[] getDPUFunctionBytecode(IDPUSingleFunction7Parameter function){
        return getDPUFunctionBytecodeHelper(function.getClass());
    }

    public static byte[] getDPUFunctionBytecode(IDPUSingleFunction8Parameter function){
        return getDPUFunctionBytecodeHelper(function.getClass());
    }

    public static byte[] getDPUFunctionBytecode(IDPUSingleFunction9Parameter function){
        return getDPUFunctionBytecodeHelper(function.getClass());
    }
    public static byte[] getDPUFunctionBytecode(IDPUSingleFunction10Parameter function){
        return getDPUFunctionBytecodeHelper(function.getClass());
    }

    public static byte[] getDPUFunctionBytecode(IDPUSingleFunction11Parameter function){
        return getDPUFunctionBytecodeHelper(function.getClass());
    }

    public static byte[] getDPUFunctionBytecode(IDPUSingleFunction12Parameter function){
        return getDPUFunctionBytecodeHelper(function.getClass());
    }

    public static byte[] getDPUFunctionBytecode(IDPUSingleFunction13Parameter function){
        return getDPUFunctionBytecodeHelper(function.getClass());
    }
    public static byte[] getDPUFunctionBytecode(IDPUSingleFunction14Parameter function){
        return getDPUFunctionBytecodeHelper(function.getClass());
    }

    public static byte[] getDPUFunctionBytecode(IDPUSingleFunction15Parameter function){
        return getDPUFunctionBytecodeHelper(function.getClass());
    }

    public static byte[] getDPUFunctionBytecode(IDPUSingleFunction16Parameter function){
        return getDPUFunctionBytecodeHelper(function.getClass());
    }

}
