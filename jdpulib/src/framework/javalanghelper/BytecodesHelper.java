package framework.javalanghelper;

import application.bst.TreeNode;
import framework.pim.dpu.classloader.ClassFileAnalyzer;
import framework.pim.dpu.java_strut.DPUJClass;
import framework.pim.dpu.java_strut.DPUJMethod;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.function.BiFunction;
import java.util.function.Function;

import static framework.pim.utils.ClassLoaderUtils.formalClassName;
import static framework.pim.utils.ClassLoaderUtils.getUTF8;

public class BytecodesHelper {
    @Override
    public boolean equals(Object obj) {
        
        return super.equals(obj);
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
            if(c == byte.class)
                return "B";
            if(c == char.class)
                return "C";
            if(c == double.class)
                return "D";
            if(c == float.class)
                return "F";
            if(c == int.class)
                return "I";
            if(c == long.class)
                return "J";
            if(c == short.class)
                return "S";
            if(c == boolean.class)
                return "Z";
            if(c == void.class)
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
            s += getDescriptorForClass(c);
        s+=')';
        return s+getDescriptorForClass(m.getReturnType());
    }

    static Dictionary<Integer, String> bytecodeMap = new Hashtable<>();
    static Dictionary<String, Integer> bytecodeDescriptionMap = new Hashtable<>();

    static {
        buildBytecodeMap();
        buildBytecodeDescriptionMap();
    }

    public static String getBytecodeDescription(byte bytecode){
        return bytecodeMap.get(bytecode);
    }
    public static int getBytecodeFromDescription(String bytecode){
        return bytecodeDescriptionMap.get(bytecode);
    }


    private static void buildBytecodeDescriptionMap() {
        new DictionaryOpProxy<>(bytecodeDescriptionMap)
                .put("NOP", 0)
                .put("ACONST_NULL", 1)
                .put("ICONST_M1", 2)
                .put("ICONST_0", 3)
                .put("ICONST_1", 4)
                .put("ICONST_2", 5)
                .put("ICONST_3", 6)
                .put("ICONST_4", 7)
                .put("ICONST_5", 8)
                .put("LCONST_0", 9)
                .put("LCONST_1", 10)
                .put("FCONST_0", 11)
                .put("FCONST_1", 12)
                .put("FCONST_2", 13)
                .put("DCONST_0", 14)
                .put("DCONST_1", 15)
                .put("BIPUSH", 16)
                .put("SIPUSH", 17)
                .put("LDC", 18)
                .put("LDC_W", 19)
                .put("LDC2_W", 20)
                .put("ILOAD", 21)
                .put("LLOAD", 22)
                .put("FLOAD", 23)
                .put("DLOAD", 24)
                .put("ALOAD", 25)
                .put("ILOAD_0", 26)
                .put("ILOAD_1", 27)
                .put("ILOAD_2", 28)
                .put("ILOAD_3", 29)
                .put("LLOAD_0", 30)
                .put("LLOAD_1", 31)
                .put("LLOAD_2", 32)
                .put("LLOAD_3", 33)
                .put("FLOAD_0", 34)
                .put("FLOAD_1", 35)
                .put("FLOAD_2", 36)
                .put("FLOAD_3", 37)
                .put("DLOAD_0", 38)
                .put("DLOAD_1", 39)
                .put("DLOAD_2", 40)
                .put("DLOAD_3", 41)
                .put("ALOAD_0", 42)
                .put("ALOAD_1", 43)
                .put("ALOAD_2", 44)
                .put("ALOAD_3", 45)
                .put("IALOAD", 46)
                .put("LALOAD", 47)
                .put("FALOAD", 48)
                .put("DALOAD", 49)
                .put("AALOAD", 50)
                .put("BALOAD", 51)
                .put("CALOAD", 52)
                .put("SALOAD", 53)
                .put("ISTORE", 54)
                .put("LSTORE", 55)
                .put("FSTORE", 56)
                .put("DSTORE", 57)
                .put("ASTORE", 58)
                .put("ISTORE_0", 59)
                .put("ISTORE_1", 60)
                .put("ISTORE_2", 61)
                .put("ISTORE_3", 62)
                .put("LSTORE_0", 63)
                .put("LSTORE_1", 64)
                .put("LSTORE_2", 65)
                .put("LSTORE_3", 66)
                .put("FSTORE_0", 67)
                .put("FSTORE_1", 68)
                .put("FSTORE_2", 69)
                .put("FSTORE_3", 70)
                .put("DSTORE_0", 71)
                .put("DSTORE_1", 72)
                .put("DSTORE_2", 73)
                .put("DSTORE_3", 74)
                .put("ASTORE_0", 75)
                .put("ASTORE_1", 76)
                .put("ASTORE_2", 77)
                .put("ASTORE_3", 78)
                .put("IASTORE", 79)
                .put("LASTORE", 80)
                .put("FASTORE", 81)
                .put("DASTORE", 82)
                .put("AASTORE", 83)
                .put("BASTORE", 84)
                .put("CASTORE", 85)
                .put("SASTORE", 86)
                .put("POP", 87)
                .put("POP2", 88)
                .put("DUP", 89)
                .put("DUP_X1", 90)
                .put("DUP_X2", 91)
                .put("DUP2", 92)
                .put("DUP2_X1", 93)
                .put("DUP2_X2", 94)
                .put("SWAP", 95)
                .put("IADD", 96)
                .put("LADD", 97)
                .put("FADD", 98)
                .put("DADD", 99)
                .put("ISUB", 100)
                .put("LSUB", 101)
                .put("FSUB", 102)
                .put("DSUB", 103)
                .put("IMUL", 104)
                .put("LMUL", 105)
                .put("FMUL", 106)
                .put("DMUL", 107)
                .put("IDIV", 108)
                .put("LDIV", 109)
                .put("FDIV", 110)
                .put("DDIV", 111)
                .put("IREM", 112)
                .put("LREM", 113)
                .put("FREM", 114)
                .put("DREM", 115)
                .put("INEG", 116)
                .put("LNEG", 117)
                .put("FNEG", 118)
                .put("DNEG", 119)
                .put("ISHL", 120)
                .put("LSHL", 121)
                .put("ISHR", 122)
                .put("LSHR", 123)
                .put("IUSHR", 124)
                .put("LUSHR", 125)
                .put("IAND", 126)
                .put("LAND", 127)
                .put("IOR", 128)
                .put("LOR", 129)
                .put("IXOR", 130)
                .put("LXOR", 131)
                .put("IINC", 132)
                .put("I2L", 133)
                .put("I2F", 134)
                .put("I2D", 135)
                .put("L2I", 136)
                .put("L2F", 137)
                .put("L2D", 138)
                .put("F2I", 139)
                .put("F2L", 140)
                .put("F2D", 141)
                .put("D2I", 142)
                .put("D2L", 143)
                .put("D2F", 144)
                .put("I2B", 145)
                .put("I2C", 146)
                .put("I2S", 147)
                .put("LCMP", 148)
                .put("FCMPL", 149)
                .put("FCMPG", 150)
                .put("DCMPL", 151)
                .put("DCMPG", 152)
                .put("IFEQ", 153)
                .put("IFNE", 154)
                .put("IFLT", 155)
                .put("IFGE", 156)
                .put("IFGT", 157)
                .put("IFLE", 158)
                .put("IF_ICMPEQ", 159)
                .put("IF_ICMPNE", 160)
                .put("IF_ICMPLT", 161)
                .put("IF_ICMPGE", 162)
                .put("IF_ICMPGT", 163)
                .put("IF_ICMPLE", 164)
                .put("IF_ACMPEQ", 165)
                .put("IF_ACMPNE", 166)
                .put("GOTO", 167)
                .put("JSR", 168)
                .put("RET", 169)
                .put("TABLESWITCH", 170)
                .put("LOOKUPSWITCH", 171)
                .put("IRETURN", 172)
                .put("LRETURN", 173)
                .put("FRETURN", 174)
                .put("DRETURN", 175)
                .put("ARETURN", 176)
                .put("RETURN", 177)
                .put("GETSTATIC", 178)
                .put("PUTSTATIC", 179)
                .put("GETFIELD", 180)
                .put("PUTFIELD", 181)
                .put("INVOKEVIRTUAL", 182)
                .put("INVOKESPECIAL", 183)
                .put("INVOKESTATIC", 184)
                .put("INVOKEINTERFACE", 185)
                .put("INVOKEDYNAMIC", 186)
                .put("NEW", 187)
                .put("NEWARRAY", 188)
                .put("ANEWARRAY", 189)
                .put("ARRAYLENGTH", 190)
                .put("ATHROW", 191)
                .put("CHECKCAST", 192)
                .put("INSTANCEOF", 193)
                .put("MONITORENTER", 194)
                .put("MONITOREXIT", 195)
                .put("WIDE", 196)
                .put("MULTIANEWARRAY", 197)
                .put("IFNULL", 198)
                .put("IFNONNULL", 199)
                .put("GOTO_W", 200)
                .put("JSR_W", 201)
                .put("BREAKPOINT", 202)
                .put("IMPDEP1", 254)
                .put("IMPDEP2", 255);
    }

    private static void buildBytecodeMap(){
        new DictionaryOpProxy<>(bytecodeMap)
                .put(0, "NOP")
                .put(1, "ACONST_NULL")
                .put(2, "ICONST_M1")
                .put(3, "ICONST_0")
                .put(4, "ICONST_1")
                .put(5, "ICONST_2")
                .put(6, "ICONST_3")
                .put(7, "ICONST_4")
                .put(8, "ICONST_5")
                .put(9, "LCONST_0")
                .put(10, "LCONST_1")
                .put(11, "FCONST_0")
                .put(12, "FCONST_1")
                .put(13, "FCONST_2")
                .put(14, "DCONST_0")
                .put(15, "DCONST_1")
                .put(16, "BIPUSH")
                .put(17, "SIPUSH")
                .put(18, "LDC")
                .put(19, "LDC_W")
                .put(20, "LDC2_W")
                .put(21, "ILOAD")
                .put(22, "LLOAD")
                .put(23, "FLOAD")
                .put(24, "DLOAD")
                .put(25, "ALOAD")
                .put(26, "ILOAD_0")
                .put(27, "ILOAD_1")
                .put(28, "ILOAD_2")
                .put(29, "ILOAD_3")
                .put(30, "LLOAD_0")
                .put(31, "LLOAD_1")
                .put(32, "LLOAD_2")
                .put(33, "LLOAD_3")
                .put(34, "FLOAD_0")
                .put(35, "FLOAD_1")
                .put(36, "FLOAD_2")
                .put(37, "FLOAD_3")
                .put(38, "DLOAD_0")
                .put(39, "DLOAD_1")
                .put(40, "DLOAD_2")
                .put(41, "DLOAD_3")
                .put(42, "ALOAD_0")
                .put(43, "ALOAD_1")
                .put(44, "ALOAD_2")
                .put(45, "ALOAD_3")
                .put(46, "IALOAD")
                .put(47, "LALOAD")
                .put(48, "FALOAD")
                .put(49, "DALOAD")
                .put(50, "AALOAD")
                .put(51, "BALOAD")
                .put(52, "CALOAD")
                .put(53, "SALOAD")
                .put(54, "ISTORE")
                .put(55, "LSTORE")
                .put(56, "FSTORE")
                .put(57, "DSTORE")
                .put(58, "ASTORE")
                .put(59, "ISTORE_0")
                .put(60, "ISTORE_1")
                .put(61, "ISTORE_2")
                .put(62, "ISTORE_3")
                .put(63, "LSTORE_0")
                .put(64, "LSTORE_1")
                .put(65, "LSTORE_2")
                .put(66, "LSTORE_3")
                .put(67, "FSTORE_0")
                .put(68, "FSTORE_1")
                .put(69, "FSTORE_2")
                .put(70, "FSTORE_3")
                .put(71, "DSTORE_0")
                .put(72, "DSTORE_1")
                .put(73, "DSTORE_2")
                .put(74, "DSTORE_3")
                .put(75, "ASTORE_0")
                .put(76, "ASTORE_1")
                .put(77, "ASTORE_2")
                .put(78, "ASTORE_3")
                .put(79, "IASTORE")
                .put(80, "LASTORE")
                .put(81, "FASTORE")
                .put(82, "DASTORE")
                .put(83, "AASTORE")
                .put(84, "BASTORE")
                .put(85, "CASTORE")
                .put(86, "SASTORE")
                .put(87, "POP")
                .put(88, "POP2")
                .put(89, "DUP")
                .put(90, "DUP_X1")
                .put(91, "DUP_X2")
                .put(92, "DUP2")
                .put(93, "DUP2_X1")
                .put(94, "DUP2_X2")
                .put(95, "SWAP")
                .put(96, "IADD")
                .put(97, "LADD")
                .put(98, "FADD")
                .put(99, "DADD")
                .put(100, "ISUB")
                .put(101, "LSUB")
                .put(102, "FSUB")
                .put(103, "DSUB")
                .put(104, "IMUL")
                .put(105, "LMUL")
                .put(106, "FMUL")
                .put(107, "DMUL")
                .put(108, "IDIV")
                .put(109, "LDIV")
                .put(110, "FDIV")
                .put(111, "DDIV")
                .put(112, "IREM")
                .put(113, "LREM")
                .put(114, "FREM")
                .put(115, "DREM")
                .put(116, "INEG")
                .put(117, "LNEG")
                .put(118, "FNEG")
                .put(119, "DNEG")
                .put(120, "ISHL")
                .put(121, "LSHL")
                .put(122, "ISHR")
                .put(123, "LSHR")
                .put(124, "IUSHR")
                .put(125, "LUSHR")
                .put(126, "IAND")
                .put(127, "LAND")
                .put(128, "IOR")
                .put(129, "LOR")
                .put(130, "IXOR")
                .put(131, "LXOR")
                .put(132, "IINC")
                .put(133, "I2L")
                .put(134, "I2F")
                .put(135, "I2D")
                .put(136, "L2I")
                .put(137, "L2F")
                .put(138, "L2D")
                .put(139, "F2I")
                .put(140, "F2L")
                .put(141, "F2D")
                .put(142, "D2I")
                .put(143, "D2L")
                .put(144, "D2F")
                .put(145, "I2B")
                .put(146, "I2C")
                .put(147, "I2S")
                .put(148, "LCMP")
                .put(149, "FCMPL")
                .put(150, "FCMPG")
                .put(151, "DCMPL")
                .put(152, "DCMPG")
                .put(153, "IFEQ")
                .put(154, "IFNE")
                .put(155, "IFLT")
                .put(156, "IFGE")
                .put(157, "IFGT")
                .put(158, "IFLE")
                .put(159, "IF_ICMPEQ")
                .put(160, "IF_ICMPNE")
                .put(161, "IF_ICMPLT")
                .put(162, "IF_ICMPGE")
                .put(163, "IF_ICMPGT")
                .put(164, "IF_ICMPLE")
                .put(165, "IF_ACMPEQ")
                .put(166, "IF_ACMPNE")
                .put(167, "GOTO")
                .put(168, "JSR")
                .put(169, "RET")
                .put(170, "TABLESWITCH")
                .put(171, "LOOKUPSWITCH")
                .put(172, "IRETURN")
                .put(173, "LRETURN")
                .put(174, "FRETURN")
                .put(175, "DRETURN")
                .put(176, "ARETURN")
                .put(177, "RETURN")
                .put(178, "GETSTATIC")
                .put(179, "PUTSTATIC")
                .put(180, "GETFIELD")
                .put(181, "PUTFIELD")
                .put(182, "INVOKEVIRTUAL")
                .put(183, "INVOKESPECIAL")
                .put(184, "INVOKESTATIC")
                .put(185, "INVOKEINTERFACE")
                .put(186, "INVOKEDYNAMIC")
                .put(187, "NEW")
                .put(188, "NEWARRAY")
                .put(189, "ANEWARRAY")
                .put(190, "ARRAYLENGTH")
                .put(191, "ATHROW")
                .put(192, "CHECKCAST")
                .put(193, "INSTANCEOF")
                .put(194, "MONITORENTER")
                .put(195, "MONITOREXIT")
                .put(196, "WIDE")
                .put(197, "MULTIANEWARRAY")
                .put(198, "IFNULL")
                .put(199, "IFNONNULL")
                .put(200, "GOTO_W")
                .put(201, "JSR_W")
                .put(202, "BREAKPOINT")
                .put(254, "IMPDEP1")
                .put(255, "IMPDEP2");



    }



    private static class DictionaryOpProxy<K, V> {
        private final Dictionary<K, V> client;

        public DictionaryOpProxy(Dictionary<K, V> hashtable){
            this.client = hashtable;
        }

        public DictionaryOpProxy<K, V> put(K key, V value){
            client.put(key, value);
            return this;
        }
    }



}




