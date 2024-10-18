package pim.dpu.java_strut;

public class DPUJMethod {
    public int size;
    public int accessFlag;
    public short paramCount;
    public int nameIndex;
    public int descriptorIndex;
    public int attributeCount;
    public String name;
    public pim.dpu.jvmattr.MethodAttrCode methodAttrCode;

    public class MethodAttrCode{
        public int maxStack;
        public int maxLocals;
        public int codeLength;
        public byte[] code;

    }
}
