package pim.dpu.classloader;

import com.upmem.dpu.DpuException;
import pim.UPMEM;
import pim.dpu.java_strut.DPUJClass;
import pim.dpu.java_strut.DPUJMethod;
import pim.dpu.java_strut.DPUJVMMemSpaceKind;
import pim.dpu.java_strut.VirtualTableItem;
import pim.logger.Logger;
import pim.logger.PIMLoggers;
import pim.utils.BytesUtils;

import java.util.Arrays;

public class ClassWriter {
    static UPMEM upmem = UPMEM.getInstance();
    static Logger classfileLogger = PIMLoggers.classfileLogger;
    public static void pushJClassToDPU(DPUJClass jc, int addr, int dpuID) throws DpuException {

        byte[] classBytes = cvtDPUClassStrut2Bytes(jc, addr);
        upmem.getDPUManager(dpuID).garbageCollector.transfer(DPUJVMMemSpaceKind.DPU_METASPACE, classBytes, addr);
    }

    public static byte[] cvtDPUClassStrut2Bytes(DPUJClass ds, int classAddr){
        byte[] bs = new byte[(ds.totalSize + 0b111) & ~(0b111)];
        int pos = 0;
        int entryTablePointer;
        int fieldPointer;
        int methodPointer;
        int constantAreaPointer;
        int entryTablePointerPos;
        int fieldPointerPos;
        int methodPointerPos;
        int constantAreaPointerPos;
        BytesUtils.writeU4LittleEndian(bs, ds.totalSize, pos);
        pos += 4;
        BytesUtils.writeU2LittleEndian(bs, ds.thisClassNameIndex, pos);
        pos += 2;
        BytesUtils.writeU2LittleEndian(bs, ds.superClassNameIndex, pos);
        pos += 2;
        BytesUtils.writeU4LittleEndian(bs, ds.superClass, pos);
        pos += 4;
        BytesUtils.writeU2LittleEndian(bs, ds.accessFlags, pos);
        pos += 2;
        BytesUtils.writeU2LittleEndian(bs, ds.cp2BOffset, pos);
        pos += 2;

        BytesUtils.writeU4LittleEndian(bs, ds.cpItemCount, pos);
        pos += 4;
        // entry table pointer
        entryTablePointerPos = pos;
        pos += 4;

        BytesUtils.writeU4LittleEndian(bs, ds.fieldCount, pos);
        pos += 4;
        fieldPointerPos = pos;
        pos += 4;


        BytesUtils.writeU4LittleEndian(bs, ds.methodCount, pos);
        pos += 4;

        methodPointerPos = pos;
        pos += 4;

        BytesUtils.writeU4LittleEndian(bs, ds.stringINTConstantPoolLength, pos);
        classfileLogger.logf("print 0x%x to %x\n", ds.stringINTConstantPoolLength, pos);
        pos += 4;
        constantAreaPointerPos = pos;
        pos += 4;

        BytesUtils.writeU4LittleEndian(bs, ds.virtualTable.items.size(), pos);
        pos += 4;
        int virtualTablePointerPos = pos;
        pos += 4;

        entryTablePointer = classAddr + pos;
        BytesUtils.writeU4LittleEndian(bs, entryTablePointer, entryTablePointerPos);
        classfileLogger.logf("print %x in %x\n", entryTablePointer, entryTablePointerPos);
        for(int i = 0; i < ds.cpItemCount; i++){
            long v = ds.entryItems[i];
            BytesUtils.writeU4LittleEndian(bs, (int) (((long)v >> 32) & 0xFFFFFFFF),pos);
            pos += 4;
            BytesUtils.writeU4LittleEndian(bs, (int) (((long)v) & 0xFFFFFFFF),pos);
            pos += 4;
        }

        //fields
        fieldPointer = classAddr + pos;
        BytesUtils.writeU4LittleEndian(bs, fieldPointer, fieldPointerPos);
        // TODO: use field
        pos += Arrays.stream(ds.fields).map(f -> f.size).reduce((s1, s2) -> s1 + s2).orElseGet( ()->0);

        //method
        methodPointer = classAddr + pos;
        BytesUtils.writeU4LittleEndian(bs, methodPointer, methodPointerPos);
        classfileLogger.logf("print 0x%x to %x\n", methodPointer, methodPointerPos);

        for(int i = 0; i < ds.methodCount; i++){
            classfileLogger.logf("method %d from 0x%x === 0x%x\n", i, pos, ds.methodOffset[i]);
            DPUJMethod dm = ds.methodTable[i];

            BytesUtils.writeU4LittleEndian(bs, dm.size, pos);
            pos += 4;
            BytesUtils.writeU2LittleEndian(bs, dm.accessFlag, pos);
            pos += 2;
            BytesUtils.writeU2LittleEndian(bs, dm.paramCount, pos);
            pos += 2;
            BytesUtils.writeU2LittleEndian(bs, dm.nameIndex, pos);
            pos += 2;
            BytesUtils.writeU2LittleEndian(bs, dm.methodAttrCode.maxStack, pos);
            pos += 2;
            BytesUtils.writeU2LittleEndian(bs, dm.methodAttrCode.maxLocals, pos);
            pos += 2;
            pos += 2; //retained

            BytesUtils.writeU4LittleEndian(bs, dm.methodAttrCode.codeLength, pos);
            pos += 4;

            // bytecode pt
            BytesUtils.writeU4LittleEndian(bs, classAddr + pos + 4, pos);
            pos += 4;

            for(int k = 0; k < dm.methodAttrCode.codeLength; k++){
                bs[pos + k] = dm.methodAttrCode.code[k];
            }

            pos += (dm.methodAttrCode.codeLength + 0b111) & (~0b111);

        }

        constantAreaPointer = classAddr + pos;
        BytesUtils.writeU4LittleEndian(bs, constantAreaPointer, constantAreaPointerPos);

        for(int offset = 0; offset < ds.stringINTConstantPoolLength; offset ++){
            bs[pos + offset] = ds.constantBytes[offset];
        }
        pos += (ds.stringINTConstantPoolLength + 0b111) & (~0b111);

        // vtable
        int virtualTablePointer = classAddr + pos;
        BytesUtils.writeU4LittleEndian(bs, virtualTablePointer, virtualTablePointerPos);

        // items
        for(int i = 0; i < ds.virtualTable.items.size(); i++){
            VirtualTableItem item = ds.virtualTable.items.get(i);
            BytesUtils.writeU4LittleEndian(bs, item.classReferenceAddress , pos);
            BytesUtils.writeU4LittleEndian(bs, item.methodReferenceAddress , pos + 4);
            pos += 8;
        }
        pos = (pos + 0b111) & (~0b111);


        classfileLogger.logf("=============== !Alert pos = %d === total-size = %d ================\n", pos, ds.totalSize);
        if(pos != ds.totalSize) throw new RuntimeException();
        return bs;
    }
}
