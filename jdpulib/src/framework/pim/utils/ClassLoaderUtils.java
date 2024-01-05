package framework.pim.utils;

import framework.pim.dpu.java_strut.DPUJClass;

public class ClassLoaderUtils {

    public static String formalClassName(String className){
        return className.replace(".", "/").split("\\$")[0];
    }

    public static String getUTF8(DPUJClass ds, int utf8Index)
    {
        return StringUtils.getStringFromBuffer(ds.constantBytes, (int) (ds.entryItems[utf8Index] & 0xFFFF), (int) (((ds.entryItems[utf8Index]) >> 40) & 0xFFFF));
    }
    public static String getMethodDescriptor(DPUJClass jc, byte[] classBytes, int methodRefIndex){
        int classIndex = BytesUtils.readU2BigEndian(classBytes, jc.itemBytesEntries[methodRefIndex] + 1);
        int nameAndTypeIndex =  BytesUtils.readU2BigEndian(classBytes, jc.itemBytesEntries[methodRefIndex] + 3);
        int classUTF8Index = BytesUtils.readU2BigEndian(classBytes, jc.itemBytesEntries[classIndex] + 1);
        int nameUTF8Index = BytesUtils.readU2BigEndian(classBytes, jc.itemBytesEntries[nameAndTypeIndex] + 1);
        int typeUTF8Index = BytesUtils.readU2BigEndian(classBytes, jc.itemBytesEntries[nameAndTypeIndex] + 3);

        String classUTF8 = getUTF8(jc, classUTF8Index);
        String nameUTF8 = getUTF8(jc, nameUTF8Index);
        String typeUTF8 = getUTF8(jc, typeUTF8Index);
        String descriptor = classUTF8 + "." + nameUTF8 + ":" + typeUTF8;
        return descriptor;
    }


}
