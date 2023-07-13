#include <stdio.h>

#include "frame_helper.h"

#ifndef JAVA_BYTECODE
#include "../ir/opcode.h"
#else
#include "../ir/bytecode.h"
#endif

#include "method.h"
#include "memory.h"
#include "vm_loop.h"
#include "vmloop_parts/helper_macros.def"

#ifdef HOST
#include "../host_vm/dpu_manage.h"
#include <iostream>
#include <chrono>
struct MethodTable* array_type;
#endif





void interp(struct function_thunk func_thunk) {
#include "vmloop_parts/registers.def"
    func = func_thunk.func;
    func2 = func;
    jc = func_thunk.jc;
    code_buffer = func->bytecodes;

    pc = 0;
    times = 0;
    #ifdef INMEMORY
    printf(RED);
    #endif



    current_fp = create_new_vmframe(func_thunk, NULL);
    printf("code_buffer = %p\n", code_buffer);

#define DEBUG
    DEBUG_PRINT("create frame finished\n");





#ifdef HOST
    array_type = MRAM_METASPACE_ALLOC_STRUT(struct MethodTable);
    array_type->metadata_token = 0x1;

    auto system = DpuSet::allocate(1);
    auto dpu = system.dpus()[0];
    dpu->load("dpuslave");
#endif

    printf("FP = (%p)\n", current_fp);
    while (1) {
        if((func2 ==  0x1000bf0) && times > 100) return;
        switch (code_buffer[pc++])
        {
        case NOP:
            DEBUG_OUT_INSN_PARSED("NOP");
            break;
     
        case ILOAD_1:
            DEBUG_OUT_INSN_PARSED("ILOAD_1")
            op1 = FRAME_GET_LOCALS(current_fp, func->max_locals, 1);
            printf(" - Load INT %d to stack\n", op1);
            PUSH_EVAL_STACK(op1)
            break;
        case ILOAD_2:
            DEBUG_OUT_INSN_PARSED("ILOAD_2")
            op1 = FRAME_GET_LOCALS(current_fp, func->max_locals, 2);
            printf(" - Load INT %d to stack\n", op1);
            PUSH_EVAL_STACK(op1)
            break;
       
     
        
        case ALOAD_0:
            DEBUG_OUT_INSN_PARSED("ALOAD_0")
            op1 = FRAME_GET_LOCALS(current_fp, func->params_count, 0);
            printf(" - Load ref %p to stack\n", op1);
            PUSH_EVAL_STACK(op1)
            break;
        case ALOAD_1:
            DEBUG_OUT_INSN_PARSED("ALOAD_1")
            break;
        case ALOAD_2:
            DEBUG_OUT_INSN_PARSED("ALOAD_2")
            break;
        // case ALOAD_3:
        //     DEBUG_OUT_INSN_PARSED("ALOAD_3")
        //     break;
        case IALOAD:
            DEBUG_OUT_INSN_PARSED("IALOAD")
            break;
   
       
        case ICONST_0:
            DEBUG_OUT_INSN_PARSED("ICONST_0")
            printf(" - push const 0 to stack\n");
            PUSH_EVAL_STACK(0);
            break;
        case ICONST_1:
            DEBUG_OUT_INSN_PARSED("ICONST_1")
            printf(" - push const 1 to stack\n");;
            PUSH_EVAL_STACK(1);
            break;
        case ICONST_M1:
            DEBUG_OUT_INSN_PARSED("ICONST_M1")
            printf(" - push const -1 to stack\n");;
            PUSH_EVAL_STACK(-1);
            break;
        case IFGE:
            DEBUG_OUT_INSN_PARSED("IFGE")
            op1 = (uint8_t)code_buffer[pc] << 8 | code_buffer[pc + 1];
            POP_EVAL_STACK(op2)
            op1 = pc + (short)op1 - 1;
            printf(" - branch addr = %p\n - cmp value = %d\n", op1, op2);
            pc += 2;
            if(op2 >= 0){
                pc = op1;
                printf(" - branch to pc = %p\n", op1);
            }
            break;
        case IF_ICMPNE:
            DEBUG_OUT_INSN_PARSED("IF_ICMPNE")
            op1 = ((uint8_t)code_buffer[pc] << 8 | code_buffer[pc + 1]);
           
            POP_EVAL_STACK(op2);
            POP_EVAL_STACK(op3);
            printf(" - value 2 = %d\n", op2);
            printf(" - value 1 = %d\n", op3);
            op1 = pc + (short)op1 - 1;
            printf(" - branch-target-offset = 0x%02x\n", op1);
            pc += 2;
            if(op2 != op3){
                pc = op1;
                printf(" - branch to pc = %p\n", op1);
            }
            break;
        case IF_ICMPGE:
            DEBUG_OUT_INSN_PARSED("IF_ICMPGE")
            op1 = (uint8_t)code_buffer[pc] << 8 | code_buffer[pc + 1];
            POP_EVAL_STACK(op2);
            POP_EVAL_STACK(op3);
            printf(" - value 2 = %d\n", op2);
            printf(" - value 1 = %d\n", op3);
            op1 = pc + (short)op1 - 1;
            printf(" - branch-target = 0x%02x\n", op1);
            pc += 2;
            if(op2 >= op3){
                pc = op1;
                printf(" - branch to pc = %p\n", op1);
            }
            break;
        case IF_ICMPLT:
            DEBUG_OUT_INSN_PARSED("IF_ICMPLT")
            op1 = (uint8_t)code_buffer[pc] << 8 | code_buffer[pc + 1];
            POP_EVAL_STACK(op2);
            POP_EVAL_STACK(op3);
            printf(" - value 2 = %d\n", op2);
            printf(" - value 1 = %d\n", op3);
            op1 = pc + (short)op1 - 1;
            printf(" - branch-target = 0x%02x\n", op1);
            pc += 2;
            if(op2 < op3){
                pc = op1;
                printf(" - branch to pc = %p\n", op1);
            }
            break;
        case IFNONNULL:
            DEBUG_OUT_INSN_PARSED("IFNONNULL")
            op1 = (uint8_t)code_buffer[pc] << 8 | code_buffer[pc + 1];
            POP_EVAL_STACK(op2); //ref
            printf(" - value 1 = %d\n", op2);
            op1 = pc + (short)op1 - 1;
            printf(" - branch-target = 0x%02x\n", op1);
            pc += 2;
            if(op2 != 0){
                pc = op1;
                printf(" - branch to pc = %p\n", op1);
            }
            break;
        case IFNULL:
            DEBUG_OUT_INSN_PARSED("IFNULL")
            op1 = (uint8_t)code_buffer[pc] << 8 | code_buffer[pc + 1];
            POP_EVAL_STACK(op2); //ref
            printf(" - value 1 = %d\n", op2);
            op1 = pc + (short)op1 - 1;
            printf(" - branch-target = 0x%02x\n", op1);
            pc += 2;
            if(op2 == 0){
                pc = op1;
                printf(" - branch to pc = %p\n", op1);
            }
            break;
        case GETFIELD:
            DEBUG_OUT_INSN_PARSED("GETFIELD")

            // op1 <- constant table index
            op1 = (code_buffer[pc] << 8) | code_buffer[pc + 1];
            pc += 2;
            printf(" - fieldref in cp index = %d\n", op1);
            printf(" - cp val = 0x%08x | 0x%08x\n", func_thunk.jc->items[op1].info, func_thunk.jc->items[op1].direct_value);
            op2 = func_thunk.jc->items[op1].direct_value & 0xFFFF;
            printf(" - field index in instance = %d\n", op2);
            POP_EVAL_STACK(op3)
            printf(" - instance addr(m) = %p, field index = %d, addr(m) = %p\n",
                op3, op2, op3 + 8 + 4 * op2);


            // read field
            READ_INT32_BIT_BY_BIT((uint8_t __mram_ptr*)(op3 + 8 + 4 * op2), op4);
            printf("field val = 0x%x\n", 
                op4
            );
            PUSH_EVAL_STACK(op4);
            
            break;
        case PUTFIELD:
            DEBUG_OUT_INSN_PARSED("PUTFIELD")
            op1 = (code_buffer[pc] << 8) | code_buffer[pc + 1]; // constant table index
            printf(" - constant table index = %d\n", op1);
            pc += 2;
            op2 = (jc->items[op1].direct_value >> 16 & 0xFFFF);
            printf(" - field index in instance = %d\n", (jc->items[op1].direct_value & 0xFFFF));
            op1 = jc->items[op1].direct_value & 0xFFFF; // index in instance fields
            POP_EVAL_STACK(op3); // val
            POP_EVAL_STACK(op4); // instance addr
            printf("set val = %d (hex:0x%08x), at addr(m):0x%08x, instance addr: 0x%08x\n", op3, op3, op4 + 8 + op1 * 4, op4);
            WRITE_INT32_BIT_BY_BIT((uint8_t __mram_ptr*)(op4 + 8 + op1 * 4), op3);
            break;

        case INVOKEVIRTUAL: // function call
            DEBUG_OUT_INSN_PARSED("INVOKEVIRTUAL")

        
            op1 = (uint8_t)(code_buffer[pc] << 8) | code_buffer[pc + 1]; // constant table index to methoderef

            printf(" - current-class-ref = %p\n", func_thunk.jc);
            printf(" - method-ref-cp-index = %d\n", op1);
            printf(" - jmethod-v-index = %p\n", func_thunk.jc->items[op1].direct_value);
            op2 = func_thunk.jc->items[op1].direct_value;
            callee.func = func_thunk.jc->virtual_table[op2];
            op4 = (uint8_t*)(current_sp - 4 * (callee.func->params_count - 1));
          
            printf(" - instance-address = %p, %p\n", *(uint32_t*)op4, op4);
            op3 = *(uint32_t*)op4 + 4;
            READ_INT32_BIT_BY_BIT((uint8_t __mram_ptr*)(op3), op1);
            printf(" - instance-class-address = %p\n", op1); 
            callee.jc = op1;
            printf(" - jmethod-ref = %p\n", callee.jc->virtual_table[op2]);
            callee.func = callee.jc->virtual_table[op2];
            callee.params = current_sp;
           
            //op2 = (func_thunk.jc->items[op1].info >> 16) & 0xFFFF; // class ref index

            current_sp -= 4 * callee.func->params_count;
            printf(" - pop %d elements from operand stack\n", callee.func->params_count);
            printf(" -- new sp = %p\n", current_sp);
            printf(" -- params-pt = %p\n", callee.params);
            printf(" -- return pc = %d\n", pc + 2);


            current_fp = create_new_vmframe(callee,  pc + 2);
            
            pc = 0;
            func = callee.func;
            code_buffer = func->bytecodes;
            jc = callee.jc;
            last_func = func_thunk;
            func_thunk = callee;
            break;
        
        case NEW:
            DEBUG_OUT_INSN_PARSED("NEW")
            op1 = (code_buffer[pc] << 8) | code_buffer[pc + 1]; // index in constant table (classref)
            pc += 2;
            op2 = (func_thunk.jc->items[op1].direct_value);
            op1 = op2;
            op3 = 0;
            printf(" - count field count\n");
            

            // |4 bytes ()| 4 byte (class ref) |   fields (4 * field_count bytes) |
            while(op2 != NULL){
                printf(" - + %d\n",  ((struct j_class __mram_ptr*)op2)->fields_count);
                op3 += ((struct j_class __mram_ptr*)op2)->fields_count;
                printf(" -- super class index = %d\n", (int)((struct j_class __mram_ptr*)op2)->super_class);
                op4 = (int)((struct j_class __mram_ptr*)op2)->super_class;
                printf(" -- super class addr = %p\n", ((struct j_class __mram_ptr*)op2)->items[op4].direct_value);
                op2 = ((struct j_class __mram_ptr*)op2)->items[op4].direct_value;
            }
            printf(" - field count = %d, instance size = %d\n", op3, 8 + op3 * 4);
            printf(" - allocate instance in mram %p\n", mram_heap_pt);
            PUSH_EVAL_STACK(mram_heap_pt);
            printf(" - write class addr: %p\n", op1);
          
            for(op4 = 0; op4 < 8 + op3 * 4; op4++){
                *(uint8_t __mram_ptr*)(mram_heap_pt + op4) = 0;
            }
            WRITE_INT32_BIT_BY_BIT((uint8_t __mram_ptr*)(mram_heap_pt + 4), op1);
            mram_heap_pt += 8 + op3 * 4;

            break;
        case DUP:
            DEBUG_OUT_INSN_PARSED("DUP")
            op1 = EVAL_STACK_TOPSLOT_VALUE;
            printf(" - dup %d(hex: %x)\n", op1, op1);
            PUSH_EVAL_STACK(op1);
            break;
        case RETURN:
            DEBUG_OUT_INSN_PARSED("RETURN")
            printf(" - last-sp = %p\n", FRAME_GET_OLDSP(current_fp));
            printf(" - last-fp = %p\n", FRAME_GET_OLDFP(current_fp));
            printf(" - return-pc = %p\n", FRAME_GET_RETPC(current_fp));
            op2 = FRAME_GET_OLDSP(current_fp);
            op3 = FRAME_GET_OLDFP(current_fp);
            op4 = FRAME_GET_RETPC(current_fp);
            if(op3 == NULL){
                printf(" - >> final frame\n");
                return_val = op1;
                current_fp = 0;
                current_sp = wram_data_space - 4;
                params_buffer_pt = params_buffer;
                return;
            }
            current_sp = op2;
            printf(" - change sp to %p\n", op2);
            printf(" - reset pc to 0x%02x\n", op4);

            func = FRAME_GET_METHOD(op3);
            printf(" - reset func pt to 0x%08x\n", func);
            current_fp = op3;
            code_buffer = func->bytecodes;
            jc = FRAME_GET_CLASS(op3);
            pc = op4;
            printf(" - bytecodes addr: %08x\n", func->bytecodes);
            func_thunk.func = func;
            func_thunk.jc = jc;

            break;
        case ARETURN:
            DEBUG_OUT_INSN_PARSED("ARETURN")
            if(FRAME_GET_OPERAND_STACK_SIZE(current_fp, current_sp) >= 0){
                POP_EVAL_STACK(op1);
                printf(" - ret val = %d\n", op1);
            }
            printf(" - last-sp = %p\n", FRAME_GET_OLDSP(current_fp));
            printf(" - last-fp = %p\n", FRAME_GET_OLDFP(current_fp));
            printf(" - return-pc = %p\n", FRAME_GET_RETPC(current_fp));
            op2 = FRAME_GET_OLDSP(current_fp);
            op3 = FRAME_GET_OLDFP(current_fp);
            op4 =  FRAME_GET_RETPC(current_fp);
            if(op3 == NULL){
                printf(" - >> final frame\n");
                return_val = op1;
                current_fp = 0;
                current_sp = wram_data_space - 4;
                params_buffer_pt = params_buffer;
                return;
            }
            current_sp = op2;
            printf(" - change sp to %p\n", op2);
            printf(" - push ret val %d\n", op1);
            PUSH_EVAL_STACK(op1)
            printf(" - reset pc to 0x%02x\n", op4);
            
            
            func = FRAME_GET_METHOD(op3);
            printf(" - reset func pt to 0x%08x\n", func);
            current_fp = op3;
            code_buffer = func->bytecodes;
            jc = FRAME_GET_CLASS(op3);
            pc = op4;
            printf(" - bytecodes addr: %08x\n", func->bytecodes);
            func_thunk.func = func;
            func_thunk.jc = jc;
            break;
        case IRETURN:
            DEBUG_OUT_INSN_PARSED("IRETURN")
            if(FRAME_GET_OPERAND_STACK_SIZE(current_fp, current_sp) >= 0){
                POP_EVAL_STACK(op1);
                printf(" - ret val = %d\n", op1);
            }
            printf(" - last-sp = %p\n", FRAME_GET_OLDSP(current_fp));
            printf(" - last-fp = %p\n", FRAME_GET_OLDFP(current_fp));
            printf(" - return-pc = %p\n", FRAME_GET_RETPC(current_fp));
            op2 = FRAME_GET_OLDSP(current_fp);
            op3 = FRAME_GET_OLDFP(current_fp);
            op4 =  FRAME_GET_RETPC(current_fp);
            if(op3 == NULL){
                printf(" - >> final frame\n");
                return_val = op1;
                
                current_fp = 0;
                current_sp = wram_data_space - 4;
                params_buffer_pt = params_buffer;
                return;
            }
            current_sp = op2;
            printf(" - change sp to %p\n", op2);
            printf(" - push ret val %d\n", op1);
            PUSH_EVAL_STACK(op1)
            printf(" - reset pc to 0x%02x\n", op4);
            
            
            func = FRAME_GET_METHOD(op3);
            printf(" - reset func pt to 0x%08x\n", func);
            current_fp = op3;
            code_buffer = func->bytecodes;
            jc = FRAME_GET_CLASS(op3);
            pc = op4;
            printf(" - bytecodes addr: %08x\n", func->bytecodes);
            func_thunk.func = func;
            func_thunk.jc = jc;
            break;

        case ISUB:
            DEBUG_OUT_INSN_PARSED("ISUB")
            POP_EVAL_STACK(op1);
            POP_EVAL_STACK(op2);
            printf(" - value 2 = %d\n", op1);
            printf(" - value 1 = %d\n", op2);
            printf(" - sub result = %d\n", op2 - op1);
            PUSH_EVAL_STACK(op2 - op1);
            break;

        case IMUL:
            DEBUG_OUT_INSN_PARSED("IMUL")
            POP_EVAL_STACK(op1);
            POP_EVAL_STACK(op2);
            printf(" - value 2 = %d\n", op1);
            printf(" - value 1 = %d\n", op2);
            printf(" - mul result = %d\n", op2 * op1);
            PUSH_EVAL_STACK(op2 * op1);
            break;
        
        case INVOKESPECIAL:
            DEBUG_OUT_INSN_PARSED("INVOKESPECIAL")
            
            op1 = (code_buffer[pc] << 8) | code_buffer[pc + 1]; // constant table index to methoderef
            printf(" - method-ref-cp-index = %d\n", op1);
            printf(" - jmethod-v-index = %p\n", func_thunk.jc->items[op1].direct_value);
            op4 = func_thunk.jc->items[op1].direct_value;
            printf(" - jmethod-ref = %p\n", func_thunk.jc->virtual_table[op4]);
            
            callee.func = func_thunk.jc->virtual_table[op4];
            op2 = (func_thunk.jc->items[op1].info >> 16) & 0xFFFF;
            printf(" - class-ref-cp-index = %d\n", op2);
            printf(" - jclass-ref = %p\n", func_thunk.jc->items[op2].direct_value);
            callee.jc = func_thunk.jc->items[op2].direct_value;
            callee.params = current_sp;
            current_sp -= 4 * callee.func->params_count;
            printf(" - pop %d elements from operand stack\n", callee.func->params_count);
            printf(" -- new sp = %p\n", current_sp);
            printf(" -- params-pt = %p\n", callee.params);
            printf(" -- return pc = %d\n", pc + 2);
           
            current_fp = create_new_vmframe(callee,  pc + 2);

            pc = 0;
            func = callee.func;
            code_buffer = func->bytecodes;
            jc = callee.jc;
            last_func = func_thunk;
            func_thunk = callee;
            

            break;
        case GOTO:
            DEBUG_OUT_INSN_PARSED("GOTO")
            op1 = (uint8_t)(code_buffer[pc] << 8) | code_buffer[pc + 1];
            op1 = pc + (short)op1 - 1;
            pc += 2;
            printf(" - goto %d\n", op1);
            pc = op1;
            break;
        default:
            DEBUG_OUT_INSN_PARSED("UNKNOW")
            printf(code_buffer[pc]);
            break;


        }
      
        times++;
        


#ifdef INMEMORY
        //if (times > 500) return;
#else
       // if (times > 1800) return;
#endif //INMEMORY





    }




}
