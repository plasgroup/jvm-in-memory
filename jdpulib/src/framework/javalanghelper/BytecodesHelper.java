package framework.javalanghelper;

import application.bst.TreeNode;
import framework.pim.dpu.classloader.ClassFileAnalyzer;
import framework.pim.dpu.java_strut.DPUJClass;
import framework.pim.dpu.java_strut.DPUJMethod;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import static framework.pim.utils.ClassLoaderUtils.formalClassName;
import static framework.pim.utils.ClassLoaderUtils.getUTF8;

public class BytecodesHelper {
    public static void main(String[] args){
        byte[] bytecodes = BytecodesHelper.getBytecodes(TreeNode.class, TreeNode.class.getDeclaredMethods()[0]);
        assert bytecodes != null;
        for (byte bytecode : bytecodes) {
            System.out.printf("0x%02X\n", bytecode);
        }
    }

    public static byte[] getBytecodes(Class c, Method jmethod){
               String className = formalClassName(c.getName());
        byte[] classFileBytes;


        /** get bytes of a class file **/
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream((c.getName().replace('.','/') + ".class"));

        if (is == null) {
            // TODO class name with form of "[....;" cannot be load
            System.err.println("class " + (c.getName().replace('.','/') + ".class not found"));
            return null;
        }

        try {
            classFileBytes = is.readAllBytes();
            is.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }



        ClassFileAnalyzer classFileAnalyzer = ClassFileAnalyzer.fromClassBytes(classFileBytes);
        DPUJClass jc = classFileAnalyzer.preResolve();
        String targetDescriptor = jmethod.getName() + ":" + getMethodDescriptor(jmethod);

        for(int i = 0; i < jc.methodTable.length; i++){
            DPUJMethod method = jc.methodTable[i];
            String nameAndType = (getUTF8(jc, method.descriptorIndex));
            String name = (getUTF8(jc, method.nameIndex));
            String fullDescriptor = name + ":" + nameAndType;
            if(fullDescriptor.equals(targetDescriptor)){
                return method.methodAttrCode.code;
            }
        }
        return null;


    }
    static String getDescriptorForClass(final Class c)
    {
        if(c.isPrimitive())
        {
            if(c==byte.class)
                return "B";
            if(c==char.class)
                return "C";
            if(c==double.class)
                return "D";
            if(c==float.class)
                return "F";
            if(c==int.class)
                return "I";
            if(c==long.class)
                return "J";
            if(c==short.class)
                return "S";
            if(c==boolean.class)
                return "Z";
            if(c==void.class)
                return "V";
            throw new RuntimeException("Unrecognized primitive "+c);
        }
        if(c.isArray()) return c.getName().replace('.', '/');
        return ('L'+c.getName()+';').replace('.', '/');
    }

    static String getMethodDescriptor(Method m)
    {
        String s="(";
        for(final Class c: m.getParameterTypes())
            s+=getDescriptorForClass(c);
        s+=')';
        return s+getDescriptorForClass(m.getReturnType());
    }
}


