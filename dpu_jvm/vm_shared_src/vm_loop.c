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
        if(times > 19) return;
        switch (code_buffer[pc++])
        {
        case NOP:
            DEBUG_OUT_INSN_PARSED("NOP");
            break;
        case RET:
            DEBUG_OUT_INSN_PARSED("RET");
            break;
        case ILOAD:
            DEBUG_OUT_INSN_PARSED("ILOAD");
            break;
        case LLOAD:
            DEBUG_OUT_INSN_PARSED("LLOAD");
            break;
        case FLOAD:
            DEBUG_OUT_INSN_PARSED("FLOAD");
            break;
        case DLOAD:
            DEBUG_OUT_INSN_PARSED("DLOAD");
            break;
        case ALOAD:
            DEBUG_OUT_INSN_PARSED("ALOAD");
            break;
        case ILOAD_0:
		    DEBUG_OUT_INSN_PARSED("ILOAD_0")
            break;
        case ILOAD_1:
            DEBUG_OUT_INSN_PARSED("ILOAD_1")
            op1 = FRAME_GET_LOCALS(current_fp, 1);
            printf(" - Load INT %d to stack\n", op1);
            PUSH_EVAL_STACK(op1)
            break;
        case ILOAD_2:
            DEBUG_OUT_INSN_PARSED("ILOAD_2")
            break;
        case ILOAD_3:
            DEBUG_OUT_INSN_PARSED("ILOAD_3")
            break;
        case LLOAD_0:
            DEBUG_OUT_INSN_PARSED("LLOAD_0")
            break;
        case LLOAD_1:
            DEBUG_OUT_INSN_PARSED("LLOAD_1")
            break;
        case LLOAD_2:
            DEBUG_OUT_INSN_PARSED("LLOAD_2")
            break;
        case LLOAD_3:
            DEBUG_OUT_INSN_PARSED("LLOAD_3")
            break;
        case FLOAD_0:
            DEBUG_OUT_INSN_PARSED("FLOAD_0")
            break;
        case FLOAD_1:
            DEBUG_OUT_INSN_PARSED("FLOAD_1")
            break;
        case FLOAD_2:
            DEBUG_OUT_INSN_PARSED("FLOAD_2")
            break;
        case FLOAD_3:
            DEBUG_OUT_INSN_PARSED("FLOAD_3")
            break;
        case DLOAD_0:
            DEBUG_OUT_INSN_PARSED("DLOAD_0")
            break;
        case DLOAD_1:
            DEBUG_OUT_INSN_PARSED("DLOAD_1")
            break;
        case DLOAD_2:
            DEBUG_OUT_INSN_PARSED("DLOAD_2")
            break;
        case DLOAD_3:
            DEBUG_OUT_INSN_PARSED("DLOAD_3")
            break;
        case ALOAD_0:
            DEBUG_OUT_INSN_PARSED("ALOAD_0")
            op1 = FRAME_GET_LOCALS(current_fp, 0);
            printf(" - Load ref %p to stack\n", op1);
            PUSH_EVAL_STACK(op1)
            break;
        case ALOAD_1:
            DEBUG_OUT_INSN_PARSED("ALOAD_1")
            break;
        case ALOAD_2:
            DEBUG_OUT_INSN_PARSED("ALOAD_2")
            break;
        case ALOAD_3:
            DEBUG_OUT_INSN_PARSED("ALOAD_3")
            break;
        case IALOAD:
            DEBUG_OUT_INSN_PARSED("IALOAD")
            break;
        case LALOAD:
            DEBUG_OUT_INSN_PARSED("LALOAD")
            break;
        case FALOAD:
            DEBUG_OUT_INSN_PARSED("FALOAD")
            break;
        case DALOAD:
            DEBUG_OUT_INSN_PARSED("DALOAD")
            break;
        case AALOAD:
            DEBUG_OUT_INSN_PARSED("AALOAD")
            break;
        case BALOAD:
            DEBUG_OUT_INSN_PARSED("BALOAD")
            break;
        case CALOAD:
            DEBUG_OUT_INSN_PARSED("CALOAD")
            break;
        case SALOAD:
            DEBUG_OUT_INSN_PARSED("SALOAD")
            break;
        case ICONST_0:
            DEBUG_OUT_INSN_PARSED("ICONST_0")
            printf(" - push const 0 to stack\n");
            PUSH_EVAL_STACK(0);
            break;
        case ICONST_1:
            DEBUG_OUT_INSN_PARSED("ICONST_1")
            printf(" - push const 1 to stack\n");
            printf(" ? current sp = %p\n", current_sp);
            PUSH_EVAL_STACK(1);
            break;
        case IFGE:
            DEBUG_OUT_INSN_PARSED("IFGE")
            op1 = (uint8_t)code_buffer[pc] << 8 | code_buffer[pc + 1];
            POP_EVAL_STACK(op2)
            printf(" - branch addr = %p\n - cmp value = %d\n", op1, op2);
            if(op2 >= 0){
                pc = op1;
                printf(" - branch to pc = %p\n", op1);
            }else{
                pc += 2;
            }
            break;
        case IF_ICMPNE:
            DEBUG_OUT_INSN_PARSED("IF_ICMPNE")
            op1 = (uint8_t)code_buffer[pc] << 8 | code_buffer[pc + 1];
            POP_EVAL_STACK(op2);
            POP_EVAL_STACK(op3);
            printf(" - value 2 = %d\n", op2);
            printf(" - value 1 = %d\n", op3);
            printf(" - branch-target = 0x%02x\n", op1);
            if(op2 != op3){
                pc = op1;
                printf(" - branch to pc = %p\n", op1);
            }else{
                pc += 2;
            }
            break;
        case GETFIELD:
            DEBUG_OUT_INSN_PARSED("GETFIELD")

            // op1 <- constant table index
            op1 = (code_buffer[pc + 1] << 8) | code_buffer[pc + 2];
            pc += 2;

            // op2 <- instance ref
            POP_EVAL_STACK(op2)
            
            // op3 <- class structure's addr, from instance addr = op2
            GET_CLASSSTRUT(op2, op3);


            // locate fieldref
            op3 += op1 * 8;

            READ_INT32_BIT_BY_BIT((uint8_t __mram_ptr*)(op3 + 4), op1); // field offset


            // read field value and push to stack
            PUSH_EVAL_STACK(*(uint32_t __mram_ptr*)(op2 + 4 + op1 * 4))

            break;
        case PUTFIELD:
            DEBUG_OUT_INSN_PARSED("PUTFIELD")
            op1 = (code_buffer[pc + 1] << 8) | code_buffer[pc + 2]; // constant table index
            pc += 2;
            GET_CLASSSTRUT(op2, op3); //class structure's addr. 
            READ_INT32_BIT_BY_BIT((uint8_t __mram_ptr*)(op3), op4); //class structure's addr
            // locate fieldref
            op4 += op1 * 8;
            READ_INT32_BIT_BY_BIT((uint8_t __mram_ptr*)(op4 + 4), op1); // field offset
            WRITE_INT32_BIT_BY_BIT((uint8_t __mram_ptr*)(op3 + 4 + 4 * op1), op2);
            break;

        case INVOKEVIRTUAL: // function call
            DEBUG_OUT_INSN_PARSED("INVOKEVIRTUAL")


            op1 = (code_buffer[pc] << 8) | code_buffer[pc + 1]; // constant table index to methoderef
            printf(" - method-ref-cp-index = %d\n", op1);
            printf(" - jmethod-ref = %p\n", func_thunk.jc->items[op1].direct_value);


            // TODO, need support static
            

            callee.func = func_thunk.jc->items[op1].direct_value;
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
            
            break;
        
        case NEW:
            DEBUG_OUT_INSN_PARSED("NEW")
            break;

        case IRETURN:
            DEBUG_OUT_INSN_PARSED("IRETURN")
            if(func->return_type != 0){
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
                return;
            }
            current_sp = op2;
            printf(" - change sp to %p\n", op2);
            printf(" - push ret val %d\n", op1);
            PUSH_EVAL_STACK(op1)
            printf(" - reset pc to 0x%02x\n", op4);
            
            func = FRAME_GET_METHOD(current_fp);
            current_fp = op3;
            code_buffer = func->bytecodes;
            
            pc = op4;

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
