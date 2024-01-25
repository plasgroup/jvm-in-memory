import com.github.ruediste.lambdaInspector.Lambda;
import com.github.ruediste.lambdaInspector.LambdaExpressionAnalyzer;
import com.github.ruediste.lambdaInspector.LambdaInspector;
import framework.pim.UPMEM;
import framework.pim.dpu.DPUGarbageCollector;
import framework.pim.dpu.RPCHelper;
import framework.pim.dpu.cache.DPUClassFileLookupTableItem;
import framework.pim.dpu.cache.DPULookupTableManager;
import framework.pim.dpu.classloader.ClassFileAnalyzer;
import framework.pim.dpu.classloader.ClassWriter;
import framework.pim.dpu.classloader.DPUClassFileManager;
import framework.pim.dpu.java_strut.DPUJClass;
import framework.pim.dpu.java_strut.DPUJVMMemSpaceKind;
import framework.primitive.control.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.instrument.Instrumentation;

public class TestLambda {
    public static void main(String[] args){
        IDPUSingleFunction2Parameter function = ((a, b) -> (Integer)a + (Integer) b);
        LambdaInspector.setup();
        Lambda inspect = LambdaInspector.inspect(function);
    }

}
