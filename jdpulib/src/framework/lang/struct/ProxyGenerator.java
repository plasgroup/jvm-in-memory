package framework.lang.struct;

import framework.pim.dpu.RPCHelper;
import sun.misc.Unsafe;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.*;
import java.net.URI;
import java.util.*;

import static java.util.Collections.singletonList;

public class ProxyGenerator{
    static String generateProxySourceCode(Class c){

        StringBuilder result = new StringBuilder();
        result.append(buildImport(c));
        result.append(buildClassHeader(c));
        result.append("{\r\n");
        result.append(buildClassBody(c));
        result.append("\r\n}\r\n");
        return result.toString();
    }

    final static String t2 = "\t\t";
    final static String t4 = "\t\t\t\t";

    static boolean isAbstract(int x){
        return (x & 0x0400) > 0;
    }
    static boolean isEnum(int x){
        return (x & 0x4000) > 0;
    }
    static boolean isModule(int x){
        return (x & 0x8000) > 0;
    }
    static boolean isInterface(int x){
        return (x & 0x0200) > 0;
    }
    static boolean isFinal(int x){
        return (x & 0x0010) > 0;
    }
    static boolean isPublic(int x){
        return (x & 0x0001) > 0;
    }
    static boolean isPrivate(int x){
        return (x & 0x0002) > 0;
    }
    static boolean isProtected(int x){
        return (x & 0x0004) > 0;
    }
    static boolean isStatic(int x){
        return (x & 0x0008) > 0;
    }

    private static String buildClassHeader(Class c) {
        System.out.println("create proxy for " + c.getName());
        StringBuilder sb = new StringBuilder();
        int modifiers = c.getModifiers();
        if(isAbstract(modifiers)) throw new RuntimeException("Cannot create proxy for abstract class");
        if(isEnum(modifiers)) throw new RuntimeException("Cannot create proxy for enum type");
        if(isModule(modifiers)) throw new RuntimeException("Not a class");
        if(isInterface(modifiers)) throw new RuntimeException("Cannot create proxy for interface");
        if(isFinal(modifiers)) return "";

        String accessFlag = "";
        String staticWord = "";

        if(isPublic(modifiers)){
            accessFlag = "public";
        }else if (isPrivate(modifiers)){
            accessFlag = "private";
        }else if (isProtected(modifiers)){
            accessFlag = "protected";
        }
        if (isStatic(modifiers)){
            staticWord = "static";
        }

        sb.append("\r\n" + accessFlag + " " + staticWord + "class " + c.getSimpleName() + "Proxy extends " +
                c.getName().replace("/",".") + " " + "implements " + "IDPUProxyObject");
        return sb.toString();
    }

    private static String buildClassBody(Class c) {
        StringBuilder sb = new StringBuilder();
        sb.append(t4 + "Integer dpuID;\r\n");
        sb.append(t4 + "Integer address;\r\n");
        sb.append("    @Override\n" +
                "    public int getDpuID() {\n" +
                "        return dpuID;\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public int getAddr() {\n" +
                "        return address;\n" +
                "    }");


        Constructor[] declaredConstructors = c.getDeclaredConstructors();
        for(int i = 0; i < declaredConstructors.length; i++){
            Constructor method = declaredConstructors[i];
            sb.append("\r\n" + buildConstractorMethod(method) + "\r\n");

        }
        while (c != null){
            Method[] methods = c.getDeclaredMethods();
            for(int i = 0; i < methods.length; i++){
                Method method = methods[i];

                sb.append("\r\n" + buildMethod(method) + "\r\n");
            }
            c = c.getSuperclass();
        }



        return sb.toString();
    }

    private static String buildConstractorMethod(Constructor method) {
        int modifier = method.getModifiers();
        if(isAbstract(modifier)) return "";
        StringBuilder sb = new StringBuilder();

        sb.append(t2);

        if(isPublic(modifier)){
            sb.append("public ");
        }else if(isPrivate(modifier)){
            sb.append("private ");
        }else if(isProtected(modifier)){
            sb.append("protected ");
        }

        if(isStatic(modifier)) {
            sb.append("static ");
        }

        String[] splitedMathodName = method.getName().split("\\.");
        String methodName = splitedMathodName[splitedMathodName.length - 1];
        sb.append(" " + methodName +"Proxy(");
        Class<?>[] parameterTypes = method.getParameterTypes();
        for(int i = 0; i < parameterTypes.length; i++){
            sb.append(parameterTypes[i].getName() + " " + "x" + i);
            if(i != parameterTypes.length - 1){
                sb.append(", ");
            }
        }
        sb.append("){\r\n");


        sb.append(t4 + "super(");
        for(int i = 0; i < parameterTypes.length; i++){
            sb.append("x" + i);
            if(i != parameterTypes.length - 1){
                sb.append(", ");
            }
        }
        sb.append(");\r\n");
//        // dispatching body
//        sb.append(t4 + "ProxyHelper.invokeMethod(dpuID, address,\"" +
//                method.getDeclaringClass().getName().replace(".","/") + "\"," +
//                " \"" + method.getName() + ":" + getSignature(method) + "\");\r\n");
//
//
//
//        if(!method.getReturnType().equals(Void.TYPE)){
//            if(method.getReturnType().isPrimitive()){
//                if(method.getReturnType().equals(Integer.TYPE)){
//                    sb.append(t4 + "return ProxyHelper.getIReturnValue(dpuID);\r\n");
//                }else if(method.getReturnType().equals(Boolean.TYPE)){
//                    sb.append(t4 + "return ProxyHelper.getIReturnValue(dpuID) == 0 ? false : true;\r\n");
//                }
//            }else{
//                sb.append(t4 + "return (" + method.getReturnType().getName() + ") ProxyHelper.getAReturnValue(dpuID);\r\n");
//            }
//        }


        sb.append(t2 + "}");
        return sb.toString();
    }

    private static String getSignature(Constructor m) {
        String sig;
        try {
            Field gSig = Method.class.getDeclaredField("signature");
            gSig.setAccessible(true);
            sig = (String) gSig.get(m);
            if(sig!=null) return sig;
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }

        StringBuilder sb = new StringBuilder("(");
        for(Class<?> c : m.getParameterTypes())
            sb.append((sig=Array.newInstance(c, 0).toString())
                    .substring(1, sig.indexOf('@')));
        return sb.append(')').toString();
//                .append(
//                        m.getReturnType()==void.class?"V":
//                                (sig= Array.newInstance(m.getReturnType(), 0).toString()).substring(1, sig.indexOf('@'))
//                )
//                .toString();
    }

    public static String getSignature(Method m){
        String sig;
        try {
            Field gSig = Method.class.getDeclaredField("signature");
            gSig.setAccessible(true);
            sig = (String) gSig.get(m);
            if(sig!=null) return sig;
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }

        StringBuilder sb = new StringBuilder("(");
        for(Class<?> c : m.getParameterTypes())
            sb.append((sig=Array.newInstance(c, 0).toString())
                    .substring(1, sig.indexOf('@')));
        return sb.append(')')
                .append(
                        m.getReturnType()==void.class?"V":
                                (sig= Array.newInstance(m.getReturnType(), 0).toString()).substring(1, sig.indexOf('@'))
                )
                .toString();
    }

    private static String buildMethod(Method method) {
        int modifier = method.getModifiers();
        if(isAbstract(modifier)) return "";
        StringBuilder sb = new StringBuilder();
        if(!isFinal(modifier)){
            sb.append(t2 + "@Override\r\n");
        }else{
            return "";
        }
        if(isStatic(modifier)) return "";
        sb.append(t2);

        if(isPublic(modifier)){
            sb.append("public ");
        }else if(isPrivate(modifier)){
            sb.append("private ");
        }else if(isProtected(modifier)){
            sb.append("protected ");
        }

        if(isStatic(modifier)) {
            sb.append("static ");
        }

        sb.append(method.getReturnType().getName() + " " + method.getName() +"(");
        Class<?>[] parameterTypes = method.getParameterTypes();
        for(int i = 0; i < parameterTypes.length; i++){
            sb.append(parameterTypes[i].getName() + " " + "x" + i);
            if(i != parameterTypes.length - 1){
                sb.append(", ");
            }
        }
        sb.append("){\r\n");

        // dispatching body
        sb.append(t4 + "ProxyHelper.invokeMethod(dpuID, address,\"" +
                method.getDeclaringClass().getName().replace(".","/") + "\"," +
                " \"" + method.getName() + ":" + getSignature(method) + "\");\r\n");

        if(!method.getReturnType().equals(Void.TYPE)){
            if(method.getReturnType().isPrimitive()){
                if(method.getReturnType().equals(Integer.TYPE)){
                    sb.append(t4 + "return ProxyHelper.getIReturnValue(dpuID);\r\n");
                }else if(method.getReturnType().equals(Boolean.TYPE)){
                    sb.append(t4 + "return ProxyHelper.getIReturnValue(dpuID) == 0 ? false : true;\r\n");
                }
            }else{
                if(method.getReturnType().isAssignableFrom(method.getDeclaringClass())){
                    sb.append(t4 + "return (" + method.getReturnType().getName() + ") ProxyHelper.getAReturnValue(dpuID);\r\n");
                }else{
                    String typeName = "";
                    try {
                        Field f = ClassLoader.class.getDeclaredField("classes");
                        f.setAccessible(true);
                        Vector<Class> vec = (Vector<Class>) f.get(Thread.currentThread().getContextClassLoader());



                            for(int j = 0; j < vec.size(); j++){
                                if(vec.get(j).getName().contains(method.getReturnType().getSimpleName() + "Proxy")){
                                    typeName = vec.get(j).getName();
                                    break;
                                }
                            }
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }

                    if("".equals(typeName)) {
                        try {
                            typeName = generateProxy(method.getReturnType()).getName();
                        } catch (NoSuchFieldException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
                            throw new RuntimeException(e);
                        }
                    }else{
                        sb.append(t4 + "return (" + typeName + ") ProxyHelper.getAReturnValue(dpuID);\r\n");
                    }
                }
            }
        }


        sb.append(t2 + "}");
        return sb.toString();
    }

    private static String buildImport(Class c) {
        StringBuilder sb = new StringBuilder();
        sb.append("import " + IDPUProxyObject.class.getPackageName() + ".*;\r\n");
        sb.append("import " + RPCHelper.class.getPackageName() + ".*;\r\n");
        sb.append("import " + c.getPackageName() + "." + c.getSimpleName() + ";\r\n");
        return sb.toString();
    }

    static Class generateProxy(Class c) throws NoSuchFieldException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        return buildClass(generateProxySourceCode(c), c.getName());
    }

    static Class buildClass(String sourceCode, String fullClassName) throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        System.out.println(fullClassName + "Proxy.java");
        final SimpleJavaFileObject simpleJavaFileObject =
                new SimpleJavaFileObject(URI.create(fullClassName.split("\\.")[1] + "Proxy.java") , JavaFileObject.Kind.SOURCE){
                    @Override
                    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
                        return sourceCode;
                    }

                    @Override
                    public OutputStream openOutputStream() throws IOException {
                        return byteArrayOutputStream;
                    }
                };
        final JavaFileManager javaFileManager = new ForwardingJavaFileManager(
                ToolProvider.getSystemJavaCompiler()
                        .getStandardFileManager(null, null, null)){
                    @Override
                    public JavaFileObject getJavaFileForOutput(
                            Location location, String className, JavaFileObject.Kind kind, FileObject sibling){
                        return simpleJavaFileObject;
                    }
                };

        ToolProvider.getSystemJavaCompiler()
                .getTask(null, javaFileManager, null, null, null, singletonList(simpleJavaFileObject)).call();

        final byte[] bytes = byteArrayOutputStream.toByteArray();
        final Field
                f = Unsafe.class.getDeclaredField("theUnsafe"),
                f1 = Unsafe.class.getDeclaredField("theUnsafe");

        f.setAccessible(true);
        f1.setAccessible(true);
        final Unsafe unsafe = (Unsafe) f.get(null);
        int i;
        for(i = 0; unsafe.getBoolean(f, i) == unsafe.getBoolean(f1, i); i++);
        Field f2 = Unsafe.class.getDeclaredField("theInternalUnsafe");
        unsafe.putBoolean(f2, i, true);
        Object internalUnsafe = f2.get(null);

        Method defineClass = internalUnsafe.getClass().getDeclaredMethod("defineClass",
                String.class, byte[].class, int.class, int.class);

        unsafe.putBoolean(defineClass, i, true);
        Class<?> newClass = (Class<?>) defineClass.invoke(internalUnsafe, fullClassName, bytes, 0, bytes.length);
        return newClass;
    }

}
