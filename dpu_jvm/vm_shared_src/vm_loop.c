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
    int tasklet_id = me();
    int buffer_begin = params_buffer + tasklet_id * (PARAMS_BUFFER_SIZE / 24);
#include "vmloop_parts/registers.def"
    func = func_thunk.func;
    func2 = func;
    jc = func_thunk.jc;
    code_buffer = func->bytecodes;
    pc = 0;
    times = 0;
    #ifdef INMEMORY
    DEBUG_PRINT(RED);
    #endif

    current_fp[tasklet_id] = create_new_vmframe(func_thunk, NULL);
  
    DEBUG_PRINT("code_buffer = %p\n", code_buffer);

#define DEBUG
    DEBUG_PRINT("create frame finished\n");
    DEBUG_PRINT("FP = (%p)\n", current_fp[tasklet_id]);

    
    while (1) {
    
    if(func2 == 0x3000c50 && times > 30) return;
        switch (code_buffer[pc++])
        {
        case NOP:
            DEBUG_OUT_INSN_PARSED("NOP");
            break;
     
        case ILOAD_1:
            DEBUG_OUT_INSN_PARSED("ILOAD_1")
            op1 = FRAME_GET_LOCALS(current_fp[tasklet_id], func->max_locals, 1);
            DEBUG_PRINT(" - Load INT %d to stack\n", op1);
            PUSH_EVAL_STACK(op1)
            break;
        case ILOAD_2:
            DEBUG_OUT_INSN_PARSED("ILOAD_2")
            op1 = FRAME_GET_LOCALS(current_fp[tasklet_id], func->max_locals, 2);
            DEBUG_PRINT(" - Load INT %d to stack\n", op1);
            PUSH_EVAL_STACK(op1)
            break;
       
     
        
        case ALOAD_0:
            DEBUG_OUT_INSN_PARSED("ALOAD_0")
            
            op1 = FRAME_GET_LOCALS(current_fp[tasklet_id], func->params_count, 0);
            DEBUG_PRINT(" - Load ref %p to stack\n", op1);
            
            PUSH_EVAL_STACK(op1)
            break;
        case ALOAD_1:
            DEBUG_OUT_INSN_PARSED("ALOAD_1")
            op1 = FRAME_GET_LOCALS(current_fp[tasklet_id], func->params_count, 1);
            DEBUG_PRINT(" - Load ref %p to stack\n", op1);
            PUSH_EVAL_STACK(op1)
            break;

        case ICONST_0:
            DEBUG_OUT_INSN_PARSED("ICONST_0")
            DEBUG_PRINT(" - push const 0 to stack\n");
            PUSH_EVAL_STACK(0);
            break;
        case ICONST_1:
            DEBUG_OUT_INSN_PARSED("ICONST_1")
            DEBUG_PRINT(" - push const 1 to stack\n");;
            PUSH_EVAL_STACK(1);
            break;
        case ICONST_M1:
            DEBUG_OUT_INSN_PARSED("ICONST_M1")
            DEBUG_PRINT(" - push const -1 to stack\n");;
            PUSH_EVAL_STACK(-1);
            break;
        case IFGE:
            DEBUG_OUT_INSN_PARSED("IFGE")
            op1 = (uint8_t)code_buffer[pc] << 8 | code_buffer[pc + 1];
            POP_EVAL_STACK(op2)
            op1 = pc + (short)op1 - 1;
            DEBUG_PRINT(" - branch addr = %p\n - cmp value = %d\n", op1, op2);
            pc += 2;
            if(op2 >= 0){
                pc = op1;
                DEBUG_PRINT(" - branch to pc = %p\n", op1);
            }
            break;
        case IF_ICMPNE:
            DEBUG_OUT_INSN_PARSED("IF_ICMPNE")
            op1 = ((uint8_t)code_buffer[pc] << 8 | code_buffer[pc + 1]);
           
            POP_EVAL_STACK(op2);
            POP_EVAL_STACK(op3);
            DEBUG_PRINT(" - value 2 = %d\n", op2);
            DEBUG_PRINT(" - value 1 = %d\n", op3);
            op1 = pc + (short)op1 - 1;
            DEBUG_PRINT(" - branch-target-offset = 0x%02x\n", op1);
            pc += 2;
            if(op3 != op2){
                pc = op1;
                DEBUG_PRINT(" - branch to pc = %p\n", op1);
            }
            break;
        case IF_ICMPGE:
            DEBUG_OUT_INSN_PARSED("IF_ICMPGE")
            op1 = (uint8_t)code_buffer[pc] << 8 | code_buffer[pc + 1];
            POP_EVAL_STACK(op2);
            POP_EVAL_STACK(op3);
            DEBUG_PRINT(" - value 2 = %d\n", op2);
            DEBUG_PRINT(" - value 1 = %d\n", op3);
            op1 = pc + (short)op1 - 1;
            DEBUG_PRINT(" - branch-target = 0x%02x\n", op1);
            pc += 2;
            if(op3 >= op2){
                pc = op1;
                DEBUG_PRINT(" - branch to pc = %p\n", op1);
            }
            break;
        case IF_ICMPLT:
            DEBUG_OUT_INSN_PARSED("IF_ICMPLT")
            op1 = (uint8_t)code_buffer[pc] << 8 | code_buffer[pc + 1];
            POP_EVAL_STACK(op2);
            POP_EVAL_STACK(op3);
            DEBUG_PRINT(" - value 2 = %d\n", op2);
            DEBUG_PRINT(" - value 1 = %d\n", op3);
            op1 = pc + (short)op1 - 1;
            DEBUG_PRINT(" - branch-target = 0x%02x\n", op1);
            pc += 2;
            if(op3 < op2){
                pc = op1;
                DEBUG_PRINT(" - branch to pc = %p\n", op1);
            }
            break;
        case IFNONNULL:
            DEBUG_OUT_INSN_PARSED("IFNONNULL")
            op1 = (uint8_t)code_buffer[pc] << 8 | code_buffer[pc + 1];
            POP_EVAL_STACK(op2); //ref
            DEBUG_PRINT(" - value 1 = %d\n", op2);
            op1 = pc + (short)op1 - 1;
            DEBUG_PRINT(" - branch-target = 0x%02x\n", op1);
            pc += 2;
            if(op2 != 0){
                pc = op1;
                DEBUG_PRINT(" - branch to pc = %p\n", op1);
            }
            break;
        case IFNULL:
            DEBUG_OUT_INSN_PARSED("IFNULL")
            op1 = (uint8_t)code_buffer[pc] << 8 | code_buffer[pc + 1];
            POP_EVAL_STACK(op2); //ref
            DEBUG_PRINT(" - value 1 = %d\n", op2);
            op1 = pc + (short)op1 - 1;
            DEBUG_PRINT(" - branch-target = 0x%02x\n", op1);
            pc += 2;
            if(op2 == 0){
                pc = op1;
                DEBUG_PRINT(" - branch to pc = %p\n", op1);
            }
            break;
        case GETFIELD:
            DEBUG_OUT_INSN_PARSED("GETFIELD")

            // op1 <- constant table index
            op1 = (code_buffer[pc] << 8) | code_buffer[pc + 1];
            pc += 2;
            DEBUG_PRINT(" - fieldref in cp index = %d\n", op1);
            DEBUG_PRINT(" - cp val = 0x%08x | 0x%08x\n", func_thunk.jc->items[op1].info, func_thunk.jc->items[op1].direct_value);
            op2 = func_thunk.jc->items[op1].direct_value & 0xFFFF;
            DEBUG_PRINT(" - field index in instance = %d\n", op2);
            POP_EVAL_STACK(op3)
            DEBUG_PRINT(" - instance addr(m) = %p, field index = %d, addr(m) = %p\n",
                op3, op2, op3 + 8 + 4 * op2);

            // read field
            op4 = *(uint32_t __mram_ptr*)(op3 + 8 + 4 * op2);
            DEBUG_PRINT("field val = 0x%x\n", op4);
            PUSH_EVAL_STACK(op4);
            
            break;
        case PUTFIELD:
            DEBUG_OUT_INSN_PARSED("PUTFIELD")
            op1 = (code_buffer[pc] << 8) | code_buffer[pc + 1]; // constant table index
            DEBUG_PRINT(" - constant table index = %d\n", op1);
            pc += 2;
            op2 = (jc->items[op1].direct_value >> 16 & 0xFFFF);
            DEBUG_PRINT(" - field index in instance = %d\n", (jc->items[op1].direct_value & 0xFFFF));
            op1 = jc->items[op1].direct_value & 0xFFFF; // index in instance fields
            POP_EVAL_STACK(op3); // val
            POP_EVAL_STACK(op4); // instance addr
            DEBUG_PRINT("set val = %d (hex:0x%08x), at addr(m):0x%08x, instance addr: 0x%08x\n", op3, op3, op4 + 8 + op1 * 4, op4);
            *(uint32_t __mram_ptr*)(op4 + 8 + op1 * 4) = op3;
            break;

        case INVOKEVIRTUAL: // function call
            DEBUG_OUT_INSN_PARSED("INVOKEVIRTUAL")

        
            op1 = (uint8_t)(code_buffer[pc] << 8) | code_buffer[pc + 1]; // constant table index to methoderef

            DEBUG_PRINT(" - current-class-ref = %p\n", func_thunk.jc);
            DEBUG_PRINT(" - method-ref-cp-index = %d\n", op1);
            
            op2 = func_thunk.jc->items[op1].direct_value;
            DEBUG_PRINT(" - v-index = %p\n", op2);

            callee.func = func_thunk.jc->virtual_table[op2].methodref;
            op4 = (uint8_t*)(current_sp[tasklet_id] - 4 * (callee.func->params_count - 1));

            DEBUG_PRINT(" - instance-address [me()]= %p, %p\n", *(uint32_t*)op4, op4);

            op3 = *(uint32_t*)op4 + 4;

            op1 = *(uint32_t __mram_ptr*)(op3);
            DEBUG_PRINT(" - instance-class-address = %p\n", op1); 

            
            DEBUG_PRINT(" - jclass-ref = %p\n", ((struct j_class __mram_ptr*)(op1))->virtual_table[op2].classref);
            DEBUG_PRINT(" - jmethod-ref = %p\n", ((struct j_class __mram_ptr*)(op1))->virtual_table[op2].methodref);
            callee.jc = ((struct j_class __mram_ptr*)(op1))->virtual_table[op2].classref;
            callee.func = ((struct j_class __mram_ptr*)(op1))->virtual_table[op2].methodref;
            callee.params = current_sp[tasklet_id];
           

            current_sp[tasklet_id] -= 4 * callee.func->params_count;
            DEBUG_PRINT(" - pop %d elements from operand stack\n", callee.func->params_count);
            DEBUG_PRINT(" -- new sp = %p\n", current_sp[tasklet_id]);
            DEBUG_PRINT(" -- params-pt = %p\n", callee.params);
            DEBUG_PRINT(" -- return pc = %d\n", pc + 2);


            current_fp[tasklet_id] = create_new_vmframe(callee,  pc + 2);
            
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
            DEBUG_PRINT(" - count field count\n");
            

            // |4 bytes ()| 4 byte (class ref) |   fields (4 * field_count bytes) |
            while(op2 != NULL){
                DEBUG_PRINT(" - + %d\n",  ((struct j_class __mram_ptr*)op2)->fields_count);
                op3 += ((struct j_class __mram_ptr*)op2)->fields_count;
                DEBUG_PRINT(" -- super class index = %d\n", (int)((struct j_class __mram_ptr*)op2)->super_class);
                op4 = (int)((struct j_class __mram_ptr*)op2)->super_class;
                DEBUG_PRINT(" -- super class addr = %p\n", ((struct j_class __mram_ptr*)op2)->items[op4].direct_value);
                op2 = ((struct j_class __mram_ptr*)op2)->items[op4].direct_value;
            }
            DEBUG_PRINT(" - field count = %d, instance size = %d\n", op3, 8 + op3 * 4);
            DEBUG_PRINT(" - allocate instance in mram %p\n", mram_heap_pt);
            PUSH_EVAL_STACK(mram_heap_pt);
            DEBUG_PRINT(" - write class addr: %p\n", op1);
          
            for(op4 = 0; op4 < 8 + op3 * 4; op4++){
                *(uint8_t __mram_ptr*)(mram_heap_pt + op4) = 0;
            }
            *(uint32_t __mram_ptr*)(mram_heap_pt + 4) = op1;
            mram_heap_pt += 8 + op3 * 4;

            break;
        case DUP:
            DEBUG_OUT_INSN_PARSED("DUP")
            op1 = EVAL_STACK_TOPSLOT_VALUE;
            DEBUG_PRINT(" - dup %d(hex: %x)\n", op1, op1);
            PUSH_EVAL_STACK(op1);
            break;
        case RETURN:
            DEBUG_OUT_INSN_PARSED("RETURN")
            printf("return");
            DEBUG_PRINT(" - last-sp = %p\n", FRAME_GET_OLDSP(current_fp[tasklet_id]));
            DEBUG_PRINT(" - last-fp = %p\n", FRAME_GET_OLDFP(current_fp[tasklet_id]));
            DEBUG_PRINT(" - return-pc = %p\n", FRAME_GET_RETPC(current_fp[tasklet_id]));
            
            op2 = FRAME_GET_OLDSP(current_fp[tasklet_id]);
            op3 = FRAME_GET_OLDFP(current_fp[tasklet_id]);
            op4 = FRAME_GET_RETPC(current_fp[tasklet_id]);
            if(op3 == NULL){
                DEBUG_PRINT(" - >> final frame\n");
                return_val = 0;
                current_fp[tasklet_id] = 0;
                current_sp[tasklet_id] = wram_data_space +  tasklet_id * (WRAM_DATA_SPACE_SIZE / 24) - 4;
                params_buffer_pt[tasklet_id] = buffer_begin;
                return;
            }
            current_sp[tasklet_id] = op2;
            DEBUG_PRINT(" - change sp to %p\n", op2);
            DEBUG_PRINT(" - reset pc to 0x%02x\n", op4);

            func = FRAME_GET_METHOD(op3);
            DEBUG_PRINT(" - reset func pt to 0x%08x\n", func);
            current_fp[tasklet_id] = op3;
            code_buffer = func->bytecodes;
            jc = FRAME_GET_CLASS(op3);
            pc = op4;
            DEBUG_PRINT(" - bytecodes addr: %08x\n", func->bytecodes);
            func_thunk.func = func;
            func_thunk.jc = jc;

            break;
        case ARETURN:
            DEBUG_OUT_INSN_PARSED("ARETURN")
            if(FRAME_GET_OPERAND_STACK_SIZE(current_fp[tasklet_id], current_sp[tasklet_id]) >= 0){
                POP_EVAL_STACK(op1);
                DEBUG_PRINT(" - ret val = %d\n", op1);
            }
            DEBUG_PRINT(" - last-sp = %p\n", FRAME_GET_OLDSP(current_fp[tasklet_id]));
            DEBUG_PRINT(" - last-fp = %p\n", FRAME_GET_OLDFP(current_fp[tasklet_id]));
            DEBUG_PRINT(" - return-pc = %p\n", FRAME_GET_RETPC(current_fp[tasklet_id]));
            op2 = FRAME_GET_OLDSP(current_fp[tasklet_id]);
            op3 = FRAME_GET_OLDFP(current_fp[tasklet_id]);
            op4 =  FRAME_GET_RETPC(current_fp[tasklet_id]);
            if(op3 == NULL){
                DEBUG_PRINT(" - >> final frame\n");
                return_val = op1;
                current_fp[tasklet_id] = 0;
                current_sp[tasklet_id] = wram_data_space +  tasklet_id * (WRAM_DATA_SPACE_SIZE / 24) - 4;
                params_buffer_pt[tasklet_id] = buffer_begin;
                return;
            }
            current_sp[tasklet_id] = op2;
            DEBUG_PRINT(" - change sp to %p\n", op2);
            DEBUG_PRINT(" - push ret val %d\n", op1);
            PUSH_EVAL_STACK(op1)
            DEBUG_PRINT(" - reset pc to 0x%02x\n", op4);
            
            
            func = FRAME_GET_METHOD(op3);
            DEBUG_PRINT(" - reset func pt to 0x%08x\n", func);
            current_fp[tasklet_id] = op3;
            code_buffer = func->bytecodes;
            jc = FRAME_GET_CLASS(op3);
            pc = op4;
            DEBUG_PRINT(" - bytecodes addr: %08x\n", func->bytecodes);
            func_thunk.func = func;
            func_thunk.jc = jc;
            break;
        case IRETURN:
            DEBUG_OUT_INSN_PARSED("IRETURN")
            if(FRAME_GET_OPERAND_STACK_SIZE(current_fp[tasklet_id], current_sp[tasklet_id]) >= 0){
                POP_EVAL_STACK(op1);
                DEBUG_PRINT(" - ret val = %d\n", op1);
            }
            DEBUG_PRINT(" - last-sp = %p\n", FRAME_GET_OLDSP(current_fp[tasklet_id]));
            DEBUG_PRINT(" - last-fp = %p\n", FRAME_GET_OLDFP(current_fp[tasklet_id]));
            DEBUG_PRINT(" - return-pc = %p\n", FRAME_GET_RETPC(current_fp[tasklet_id]));
            op2 = FRAME_GET_OLDSP(current_fp[tasklet_id]);
            op3 = FRAME_GET_OLDFP(current_fp[tasklet_id]);
            op4 =  FRAME_GET_RETPC(current_fp[tasklet_id]);
            if(op3 == NULL){
                DEBUG_PRINT(" - >> final frame\n");
                return_val = op1;
                current_fp[tasklet_id] = 0;
                current_sp[tasklet_id] = wram_data_space + tasklet_id * (WRAM_DATA_SPACE_SIZE / 24) - 4;
                params_buffer_pt[tasklet_id] = buffer_begin;
                return;
            }
            current_sp[tasklet_id] = op2;
            DEBUG_PRINT(" - change sp to %p\n", op2);
            DEBUG_PRINT(" - push ret val %d\n", op1);
            PUSH_EVAL_STACK(op1)
            DEBUG_PRINT(" - reset pc to 0x%02x\n", op4);
            
            
            func = FRAME_GET_METHOD(op3);
            DEBUG_PRINT(" - reset func pt to 0x%08x\n", func);
            current_fp[tasklet_id] = op3;
            code_buffer = func->bytecodes;
            jc = FRAME_GET_CLASS(op3);
            pc = op4;
            DEBUG_PRINT(" - bytecodes addr: %08x\n", func->bytecodes);
            func_thunk.func = func;
            func_thunk.jc = jc;
            break;

        case ISUB:
            DEBUG_OUT_INSN_PARSED("ISUB")
            POP_EVAL_STACK(op1);
            POP_EVAL_STACK(op2);
            DEBUG_PRINT(" - value 2 = %d\n", op1);
            DEBUG_PRINT(" - value 1 = %d\n", op2);
            DEBUG_PRINT(" - sub result = %d\n", op2 - op1);
            PUSH_EVAL_STACK(op2 - op1);
            break;

        case IMUL:
            DEBUG_OUT_INSN_PARSED("IMUL")
            POP_EVAL_STACK(op1);
            POP_EVAL_STACK(op2);
            DEBUG_PRINT(" - value 2 = %d\n", op1);
            DEBUG_PRINT(" - value 1 = %d\n", op2);
            DEBUG_PRINT(" - mul result = %d\n", op2 * op1);
            PUSH_EVAL_STACK(op2 * op1);
            break;
        
        case INVOKESPECIAL:
            DEBUG_OUT_INSN_PARSED("INVOKESPECIAL")
            
            op1 = (code_buffer[pc] << 8) | code_buffer[pc + 1]; // constant table index to methoderef
            DEBUG_PRINT(" - method-ref-cp-index = %d\n", op1);
            DEBUG_PRINT(" - jmethod-v-index = %p\n", func_thunk.jc->items[op1].direct_value);
            op4 = func_thunk.jc->items[op1].direct_value;
            DEBUG_PRINT(" - jmethod-ref = %p\n", func_thunk.jc->virtual_table[op4].methodref);
       
            callee.func = func_thunk.jc->virtual_table[op4].methodref;
            op2 = (func_thunk.jc->items[op1].info >> 16) & 0xFFFF;
            DEBUG_PRINT(" - class-ref-cp-index = %d\n", op2);
            DEBUG_PRINT(" - jclass-ref = %p\n", func_thunk.jc->items[op2].direct_value);
            callee.jc = func_thunk.jc->items[op2].direct_value;
            callee.params = current_sp[tasklet_id];
            current_sp[tasklet_id] -= 4 * callee.func->params_count;
            DEBUG_PRINT(" - pop %d elements from operand stack\n", callee.func->params_count);
            DEBUG_PRINT(" -- new sp = %p\n", current_sp[tasklet_id]);
            DEBUG_PRINT(" -- params-pt = %p\n", callee.params);
            DEBUG_PRINT(" -- return pc = %d\n", pc + 2);
               
            current_fp[tasklet_id] = create_new_vmframe(callee,  pc + 2);

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
            DEBUG_PRINT(" - goto %d\n", op1);
            pc = op1;
            break;
        default:
            DEBUG_OUT_INSN_PARSED("UNKNOW")
            DEBUG_PRINT(code_buffer[pc]);
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
