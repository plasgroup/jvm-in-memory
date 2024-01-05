package framework.lang.struct;


import application.bst.DPUTreeNode;

import java.lang.reflect.InvocationTargetException;

public class GeneratorTest {
    public static void main(String[] args) throws NoSuchFieldException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        String s = ProxyGenerator.generateProxySourceCode(DPUTreeNode.class);
        System.out.println(s);
        System.out.println(ProxyGenerator.generateProxy(DPUTreeNode.class));
    }
}
