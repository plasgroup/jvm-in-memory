package framework.pim.dpu.java_strut;

public class DPUJClass {
    public int totalSize;
    public short thisClassNameIndex;
    public short superClassNameIndex;
    public int superClass;
    public int thisClass;
    public short accessFlags;
    public short cp2BOffset;
    private int cpItemCount = -1;
    public long[] entryItems;
    public int fieldCount;
    public DPUJField[] fields;
    public int methodCount;
    public DPUJMethod[] methodTable;
    public int stringINTConstantPoolLength;
    public byte[] constantBytes;
    public int[] itemBytesEntries;
    public int[] methodOffset;
    public int[] bytecodeOffset;
    public byte[] staticArea;
    public VirtualTable virtualTable;

    public void setCpItemCount(int cpItemCount) {
        if (this.getCpItemCount() != -1) {
            throw new RuntimeException("cpItemCount already set");
        }
        this.cpItemCount = cpItemCount;
    }

    public int getCpItemCount() {
        return cpItemCount;
    }

}
