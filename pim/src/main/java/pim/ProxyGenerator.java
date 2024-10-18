package pim;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ProxyGenerator {
    public static void generateProxy(Class c){
        String proxyName = c.getName() + "Proxy";
        Method[] methods = c.getDeclaredMethods();
        Field[] declaredFields = c.getDeclaredFields();
        String packageName = c.getPackageName();
        File f = new File("./" + proxyName + ".java");
        StringBuilder sourceContent = new StringBuilder();
        sourceContent.append("package " + packageName + "." + proxyName);
        sourceContent.append("class " + proxyName + "{ \r\n");
        for (Method m : methods){
            
        }
    }
}
