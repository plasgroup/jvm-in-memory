package pim.dpu;

public class DPUJClass {
    int totalSize;
    short thisClassNameIndex;
    short superClassNameIndex;
    int superClass;
    int thisClass;
    short accessFlags;
    short cp2BOffset;
    int cpItemCount;
    long[] entryItems;
    int fieldCount;
    DPUJField[] fields;
    int methodCount;
    DPUJMethod[] methodTable;
    int stringINTConstantPoolLength;
    byte[] constantBytes;
    public int[] itemBytesEntries;
    public int[] methodOffset;
    public int[] bytecodeOffset;

    public byte[] staticArea;
}
