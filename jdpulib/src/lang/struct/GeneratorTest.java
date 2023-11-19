package lang.struct;


import algorithm.DPUTreeNode;
import algorithm.TreeNode;
import pim.dpu.DPUObjectHandler;
import pim.dpu.ProxyHelper;

import java.lang.reflect.InvocationTargetException;

public class GeneratorTest {
    public static void main(String[] args) throws NoSuchFieldException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        String s = ProxyGenerator.generateProxySourceCode(DPUTreeNode.class);
        System.out.println(s);
        System.out.println(ProxyGenerator.generateProxy(DPUTreeNode.class));
    }
}
