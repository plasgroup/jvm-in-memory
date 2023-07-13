package pim.dpu;

import pim.utils.BytesUtils;
import pim.utils.StringUtils;
import pim.utils.Tester;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class ClassFileAnalyzer {
    private DPUJClass jc;
    private int constantAreaSize = 0;
    private int filledFields = 0;
    int mOffset = 0;
    private byte[] classFileBytes;

    private ClassFileAnalyzer(){}
    public static ClassFileAnalyzer fromClassBytes(byte[] bs){
        ClassFileAnalyzer cfa = new ClassFileAnalyzer();
        cfa.classFileBytes = bs;
        return cfa;
    }


    /* fill utf-8/long/int/double/.. constant to utf-8*/
    public void fillConstantArea(){
        int filled = 0;
        ByteBuffer bb = ByteBuffer.wrap(jc.constantBytes);
        for(int i = 1; i < jc.cpItemCount; i++){
            int pos = jc.itemBytesEntries[i];
            int tag = classFileBytes[pos];

            switch (tag){
                case ClassFileAnalyzerConstants.CT_Utf8:
                    long len = (jc.entryItems[i] & 0xFFFF);
                    System.out.println(">> Entry #" + i + " is UTF8, len = " + len);
                    bb.put(classFileBytes, pos + 3, (int) len);
                    // write offset in constantArea to low 32 bits. Write len to 40~56 bit
                    jc.entryItems[i] &= 0xFFFFFFFF00000000L;
                    jc.entryItems[i] |= filled;
                    jc.entryItems[i] |= (len << 40);
                    System.out.printf("Fill UTF8 string in constantArea offset = %d\n", filled);
                    filled += len;
                    break;
                case ClassFileAnalyzerConstants.CT_Integer:
                    int iv = BytesUtils.readU4BigEndian(classFileBytes, pos + 1);
                    System.out.println("in entry #" + i + " int, val = " + iv);
                    bb.put(classFileBytes, pos + 1, 4);
                    jc.entryItems[i] &= 0xFFFFFFFF00000000L;
                    jc.entryItems[i] |= filled;
                    System.out.printf("Fill int in constantDataArea offset = %d\n", filled);
                    filled += 4;
                    break;
                case ClassFileAnalyzerConstants.CT_Double:
                case ClassFileAnalyzerConstants.CT_Long:
                    long lv = BytesUtils.readU8BigEndian(classFileBytes, pos + 1);
                    System.out.println("in entry #" + i + " double, val = " + lv);
                    bb.put(classFileBytes, pos + 1, 8);
                    jc.entryItems[i] &= 0xFFFFFFFF00000000L;
                    jc.entryItems[i] |= filled;
                    System.out.printf("Fill long/double in constantDataArea offset = %d\n", filled);
                    filled += 8;
                    break;
            }
        }
        Tester.alert(filled == jc.constantBytes.length, "In fillConstantArea(), totaled filled bytes != constantArea length");
        System.out.printf("filled %d/%d bytes\n", filled, jc.constantBytes.length);
    }



    /* Read a constant table item from a given offset of class file bytes, and set information to entry table
    *  The entry table item after read:
    *      - class:            |tag (8 bits) | 00 | 00 | 00 | 00           | 00     | classname-utf8-index (16 bits)            |
    *      - fieldref:         |tag (8 bits) | 00 | 00 | 00 | class-index (16 bits) | field-name-and-type-index (16 bits)       |
    *      - methodref:        |tag (8 bits) | 00 | 00 | 00 | class-index (16 bits) | method-name-and-type-utf8-index (16 bits) |
    *      - String:           |tag (8 bits) | 00 | 00 | 00 | 00           | 00     | utf8-index(16 bits)                       |
           - name-and-type:    |tag (8 bits) | 00 | 00 | 00 | name-utf8-index       | type-desc-utf8-index(16 bits)             |
     *     - utf8:             |tag (8 bits) | 00 | 00 | 00 | 00           | 00     | len (16 bits)                             |
     *
     * */
    public int readConstantTableItem(int offset, int i){
        byte tag = classFileBytes[offset];
        // set tag to high 8 bits
        jc.entryItems[i] = (long)tag << 56;
        switch (tag){
            case ClassFileAnalyzerConstants.CT_Class:
                System.out.println("Class");
                System.out.println("\t -> Mark UTF8 index " + i +": " + BytesUtils.readU2BigEndian(classFileBytes, offset + 1) );
                jc.entryItems[i] &= 0xFFFFFFFF00000000L;
                jc.entryItems[i] |= BytesUtils.readU2BigEndian(classFileBytes, offset + 1);
                jc.entryItems[i] |= ((long) BytesUtils.readU2BigEndian(classFileBytes, offset + 1) & 0x0000FFFF) << 32;
                return 3;
            case ClassFileAnalyzerConstants.CT_Fieldref:
                System.out.println("Fieldref");
                System.out.println("\t -> Mark Class and NameAndType index " + i +": " +
                         BytesUtils.readU2BigEndian(classFileBytes, offset + 1) + "|" +  BytesUtils.readU2BigEndian(classFileBytes, offset + 3));
                jc.entryItems[i] |= (((long)BytesUtils.readU2BigEndian(classFileBytes, offset + 1) << 16) & 0xFFFF0000L)
                        |  BytesUtils.readU2BigEndian(classFileBytes, offset + 3);
                return 5;
            case ClassFileAnalyzerConstants.CT_Methodref:
                System.out.println("Methodref");
                System.out.println("\t -> Mark Class and NameAndType index " + i +": " +
                         BytesUtils.readU2BigEndian(classFileBytes, offset + 1) + "|" +  BytesUtils.readU2BigEndian(classFileBytes, offset + 3));
                jc.entryItems[i] |= (((long)BytesUtils.readU2BigEndian(classFileBytes, offset + 1) << 16) & 0xFFFF0000L) |  BytesUtils.readU2BigEndian(classFileBytes, offset + 3);

                return 5;
            case ClassFileAnalyzerConstants.CT_InterfaceMethodref:
                // TODO
                System.out.println("InterfaceMethodref");
                return 5;
            case ClassFileAnalyzerConstants.CT_String:
                System.out.println("String");
                System.out.println("\t -> Mark UTF8 index " + i + ": " + BytesUtils.readU2BigEndian(classFileBytes, offset + 1) );
                jc.entryItems[i] = BytesUtils.readU2BigEndian(classFileBytes, offset + 1);
                return 3;
            case ClassFileAnalyzerConstants.CT_Integer:
                System.out.println("Integer");
                constantAreaSize += 4;
                return 5;
            case ClassFileAnalyzerConstants.CT_Float:
                System.out.println("Float");
                constantAreaSize += 4;
                return 5;
            case ClassFileAnalyzerConstants.CT_Long:
                System.out.println("Long");
                constantAreaSize += 8;
                return 9;
            case ClassFileAnalyzerConstants.CT_Double:
                System.out.println("Double");
                constantAreaSize += 8;
                return 9;
            case ClassFileAnalyzerConstants.CT_NameAndType:
                System.out.println("NameAndType");
                System.out.println("\t -> Mark Name and Type in index " + i +": " +
                         BytesUtils.readU2BigEndian(classFileBytes, offset + 1) + "|" +  BytesUtils.readU2BigEndian(classFileBytes, offset + 3));
                jc.entryItems[i] |= (BytesUtils.readU2BigEndian(classFileBytes, offset + 1) << 16) |  BytesUtils.readU2BigEndian(classFileBytes, offset + 3);
                return 5;
            case ClassFileAnalyzerConstants.CT_Utf8:
                int len = (((classFileBytes[offset + 1]) & 0xFF) << 8) | ((int)(classFileBytes[offset + 2]) & 0xFF);
                jc.entryItems[i] |= len;
                constantAreaSize += len;
                return 3 + len;
            case ClassFileAnalyzerConstants.CT_MethodHandle:
                System.out.println("MethodHandle");
                return 4;
            case ClassFileAnalyzerConstants.CT_MethodType:
                System.out.println("MethodType");
                return 3;
            case ClassFileAnalyzerConstants.CT_InvokeDynamic:
                System.out.println("InvokeDynamic");
                return 5;
        }
        return 0;
    }

    public int analysisInterfaceItem(int pos){
        int attrNameIndex =  BytesUtils.readU2BigEndian(classFileBytes, pos);
        System.out.println(" - attrNameIndex = " + attrNameIndex);
        return 2;
    }

    public int analysisAttributeItem(int pos, DPUJClass ds){
        int attrNameIndex =  BytesUtils.readU2BigEndian(classFileBytes, pos);
        int len =  BytesUtils.readU4BigEndian(classFileBytes, pos + 2);
        System.out.println(">>>> " + attrNameIndex);
        return 6 + len;
    }

    public int analysisFieldItem(int pos, DPUJClass ds, int indexInFieldList){
        int beginPos = pos;
        int accessFlag =  BytesUtils.readU2BigEndian(classFileBytes, pos);
        int nameIndex  =  BytesUtils.readU2BigEndian(classFileBytes, pos + 2);
        int descIndex  =  BytesUtils.readU2BigEndian(classFileBytes, pos + 4);
        int attrCount  =  BytesUtils.readU2BigEndian(classFileBytes, pos + 6);
        System.out.println(" -- Mark name index " + nameIndex + " as this class's field name");


        if((accessFlag & 0x0008) != 0 && (accessFlag & 0x0010) != 0)
        {
            // TODO, static or final field
        }
        else{
            DPUJField field = new DPUJField();
            field.indexInInstance = filledFields++;
            field.size = 0; // TODO
            field.accessFlag = accessFlag;
            field.descIndex = descIndex;
            field.nameIndex = nameIndex;
            ds.fields[indexInFieldList] = field;
        }

        pos += 8;

        System.out.printf("Field >> access_flag = %x, name_index = %d, desc_index = %d, attr_count = %d\n",
                accessFlag, nameIndex, descIndex, attrCount);

        for(int i = 0; i < attrCount; i++){
            int forward = analysisAttributeItem(pos, ds);
            pos += forward;
        }

        return pos - beginPos;
    }



    public static void printEntryTable(DPUJClass jc){

        for(int i = 1; i < jc.cpItemCount; i++){
            long iEntryVal = jc.entryItems[i];
            System.out.print(" - item " + i + " line = " + " ");
            System.out.printf("%02x %02x %02x %02x|%02x %02x %02x %02x\n",
                    (iEntryVal & 0xFF00000000000000L) >> 56,
                    (iEntryVal & 0x00FF000000000000L) >> 48,
                    (iEntryVal & 0x0000FF0000000000L) >> 40,
                    (iEntryVal & 0x000000FF00000000L) >> 32,
                    (iEntryVal & 0x00000000FF000000L) >> 24,
                    (iEntryVal & 0x0000000000FF0000L) >> 16,
                    (iEntryVal & 0x000000000000FF00L) >> 8,
                    (iEntryVal & 0x00000000000000FFL)
            );
        }
    }



    public static short countTypeCountFromDescriptor(String desc){
        int pt = 0;
        int c = 0;
        int state = 0;
        while(pt < desc.length()){
            char ch = desc.charAt(pt);
            if(state == 0){
                if(ch == 'L'){
                    state = 1;
                }else if(ch == '['){
                }else{
                    c++;
                }
            }else if(state == 1){
                if(ch == ';'){
                    c++;
                    state = 0;
                }
            }
            pt++;
        }
        return (short) c;
    }


    public int analysisMethodItem(int pos, DPUJClass jc, int i){
        int beginPos = pos;
        if(jc.superClassNameIndex == 0){
            System.out.println();
        }
        DPUJMethod dm = new DPUJMethod();
        jc.methodTable[i] = dm;
        jc.methodOffset[i] = mOffset;

        System.out.println("-------------------- Method " + i + " --------------------------");


        int accFlag =  BytesUtils.readU2BigEndian(classFileBytes, pos);
        pos += 2;
        System.out.println(" - acc flag = " + accFlag);

        int nameIndex =  BytesUtils.readU2BigEndian(classFileBytes, pos);
        pos += 2;
        System.out.println(" - name_index = " + nameIndex);

        int descIndex =  BytesUtils.readU2BigEndian(classFileBytes, pos);
        pos += 2;
        System.out.println(" - desc_index = " + descIndex);

        int attrCount =  BytesUtils.readU2BigEndian(classFileBytes, pos);
        pos += 2;
        System.out.println(" - attr count = " + attrCount);

        System.out.println("-------------------- Method " + i + " attr count = " + attrCount + "------------------");

        dm.accessFlag = accFlag;
        dm.attributeCount = attrCount;
        dm.nameIndex = nameIndex;
        dm.descriptorIndex = descIndex;
        String desc = DPUClassFileManager.getUTF8(jc, descIndex);
        System.out.println("desc = " + desc);
        dm.paramCount = (short) (countTypeCountFromDescriptor(desc.substring(1, desc.indexOf(')'))) + 1);
        // parse attr
        for(int j = 0; j < attrCount; j++){
            System.out.printf("Attr From Addr: 0x%x / 0x%x\n", pos, classFileBytes.length);
            int attrNameIndex =  BytesUtils.readU2BigEndian(classFileBytes, pos);
            pos += 2;
            String attrName = StringUtils.getStringFromBuffer(jc.constantBytes,
                    (int) (jc.entryItems[attrNameIndex] & 0xFFFF),
                    (int) (((jc.entryItems[attrNameIndex]) >> 40) & 0xFF)
            );
            System.out.println(" --- attr name in index " + attrNameIndex + " = " + attrName);

            int attrLen =  BytesUtils.readU4BigEndian(classFileBytes, pos);
            pos += 4;
            System.out.printf("Attribute len = %d (0x%x)\n" , attrLen, attrLen);
            if(!"Code".equals(attrName)) {
                pos += attrLen;
                continue;
            }

            int maxStack =  BytesUtils.readU2BigEndian(classFileBytes, pos);
            int maxLocals =  BytesUtils.readU2BigEndian(classFileBytes, pos + 2);
            int codeLen =  BytesUtils.readU4BigEndian(classFileBytes, pos + 4);


            pim.dpu.jvmattr.MethodAttrCode mac = new pim.dpu.jvmattr.MethodAttrCode();
            mac.maxLocals = maxLocals;
            mac.maxStack = maxStack;
            mac.codeLength = codeLen;
            mac.code = new byte[codeLen];

            ByteBuffer.wrap(mac.code).put(classFileBytes, pos + 8, codeLen);
            jc.bytecodeOffset[i] = pos + 8;
            dm.methodAttrCode = mac;

            jc.methodTable[i] = dm;

            System.out.printf("[Code maxStack = %d, maxLocals = %d, codeLen = %d, params_count = %d]\n",
                    maxStack, maxLocals, (long)codeLen, dm.paramCount);

            // print bytecodes
            for(int block = 0; block < (int)Math.ceil(codeLen / 8.0); block ++){
                for(int b = 0; b < 8 && block * 8 + b < codeLen; b++){
                    System.out.printf("%02x\t", classFileBytes[jc.bytecodeOffset[i] + block * 8 + b]);
                }
                System.out.println();
            }
            pos += attrLen;
            dm.size = 24 + ((dm.methodAttrCode.codeLength + 0b111) & ~(0b111));
        }

        if(jc.methodTable[i] == null) {
            jc.methodTable[i] = new DPUJMethod();
        }

        if(jc.methodTable[i].methodAttrCode == null) {
            jc.methodTable[i].methodAttrCode = new pim.dpu.jvmattr.MethodAttrCode();
            jc.methodTable[i].size = 24;
        }
        mOffset += jc.methodTable[i].size;
        return pos - beginPos;
    }

    public DPUJClass preResolve() {
        jc = new DPUJClass();
        int pos = 0;
        constantAreaSize = 0;
        System.out.println("begin preresolve classfile");

        // magic number
        Tester.alert(classFileBytes[pos] == 0xca && classFileBytes[pos + 1] == 0xfe
                && classFileBytes[pos + 2] == 0xba && classFileBytes[pos + 3] == 0xbe, "Magic number not match.");
        pos += 4;

        // skip version check
        pos += 4;

        // get constant table item count
        jc.cpItemCount =  BytesUtils.readU2BigEndian(classFileBytes, pos);
        pos += 2;

        // two array for resolution
        jc.itemBytesEntries = new int[jc.cpItemCount];
        jc.entryItems = new long[jc.cpItemCount];

        System.out.println("constant table item count = " + jc.cpItemCount);
        System.out.println("====================== Begin analyze constant table item ====================");
        for(int i = 1; i < jc.cpItemCount; i++){
            System.out.print(" >> Item " + i + " ");
            jc.itemBytesEntries[i] = pos;
            // Analyze a constant table item, fill information to entry table, and obtain how many bytes should the pos should move forward
            int forwardSteps = readConstantTableItem(pos, i);
            byte tag = classFileBytes[pos];

            // Long and Double value take an extra entry
            if(tag == ClassFileAnalyzerConstants.CT_Long || tag == ClassFileAnalyzerConstants.CT_Double) i++;

            pos += forwardSteps;
        }
        System.out.println("====================== End analyze constant table item ====================");
        System.out.println();


        // Print Entry Table after resolution 1
        System.out.println("==== Entries Table After Resolution Phase 1 - read constant table, and fill information ====\n");
        printEntryTable(jc);
        System.out.println("==== ----------------------------------------------------------------------- ====\n");
        System.out.println();

        // cp2BOffset (unnecessary)
        jc.cp2BOffset = 16;

        // access_flag
        int accessFlag =  BytesUtils.readU2BigEndian(classFileBytes, pos);
        pos += 2;
        jc.accessFlags = (short) accessFlag;
        System.out.printf("access_flag = 0x%04x\n", accessFlag);

        // this_class (the entry should point to a class file structure;
        jc.thisClass = (short)  BytesUtils.readU2BigEndian(classFileBytes, pos);
        System.out.println(" - this class = " + jc.thisClass);
        System.out.println("tEntries Table After Resolution Phase 1 - read CPs, and mark information. This_class ref index = " + jc.thisClassNameIndex);
        pos += 2;


        jc.superClass = (short)  BytesUtils.readU2BigEndian(classFileBytes, pos);
        System.out.println(" - super class = " + jc.superClass);
        System.out.println("super_class ref index = " + jc.superClassNameIndex);
        pos += 2;

        jc.thisClassNameIndex = (short) (jc.entryItems[jc.thisClass] & 0xFFFF);
        jc.superClassNameIndex = (short) (jc.entryItems[jc.superClass] & 0xFFFF);

        System.out.println("rescan for filling utf_8 string and number area, total size = "
                + constantAreaSize + " bytes");

        jc.constantBytes = new byte[constantAreaSize];
        jc.stringINTConstantPoolLength = constantAreaSize;

        fillConstantArea();
        // Print Entry Table after resolution 2
        System.out.println("==== Entries Table After Resolution Phase 2 - fill constant value to constantDataArea ====\n");
        // printEntryTable(jc);
        System.out.println("==== ----------------------------------------------------------------------- ====\n");
        System.out.println();
        constantAreaSize = 0;


        /*
            Now,
                1. constants like string, double, long, int, .. should be already be written to constant Area
                2. tag be marked to the highest 8bit of entry.
                3. non-direct constant item be expended to subindexes, and is saved in low 32bit.
        * */

        // interface count
        int interfaceCount =  BytesUtils.readU2BigEndian(classFileBytes, pos);
        pos += 2;
        System.out.println("interface count = " + interfaceCount);

        // interface
        for(int i = 0; i < interfaceCount; i++){
            int forward = analysisInterfaceItem(pos);
            pos += forward;
        }


        /* field analysis */
        int fieldCount =  BytesUtils.readU2BigEndian(classFileBytes, pos);
        pos += 2;
        System.out.println("field count = " + fieldCount);
        jc.fieldCount = fieldCount;
        jc.fields = new DPUJField[fieldCount];
        for(int i = 0; i < fieldCount; i++){
            System.out.println(">> Field " + i);
            jc.fields[i] = new DPUJField();
            pos += analysisFieldItem(pos, jc, i);
        }


        /* method analysis */
        System.out.println("======================= Begin Method Analysis ===========================");
        int methodCount =  BytesUtils.readU2BigEndian(classFileBytes, pos);
        pos += 2;

        System.out.println("- Method Count = " + methodCount);
        jc.methodCount = methodCount;
        jc.methodTable = new DPUJMethod[methodCount];
        jc.methodOffset = new int[methodCount];
        jc.bytecodeOffset = new int[methodCount];
        for(int i = 0; i < methodCount; i++){
            pos += analysisMethodItem(pos, jc, i);
        }

        System.out.println("======================= End of Method Analysis ===========================");


        /* Calculate total size (bytes) of whole the whole class that need for transferring to DPU*/
        jc.totalSize =
                48 + jc.cpItemCount * 8 + 8 +
                Arrays.stream(jc.fields).map(e -> e.size).reduce((s1, s2) -> s1 + s2).orElseGet(()->0) +
                Arrays.stream(jc.methodTable).map(e -> e.size).reduce((s1, s2) -> s1 + s2).orElseGet(()->0)
                + ((jc.stringINTConstantPoolLength + 0b111) & (~0b111));
        return jc;
    }
}
