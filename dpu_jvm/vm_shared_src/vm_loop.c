#include <stdio.h>

#include "frame_helper.h"

#ifndef JAVA_BYTECODE
#include "../ir/opcode.h"
#else
#include "../ir/bytecode.h"
#endif

#include "function.h"
#include "memory.h"
#include "vm_loop.h"
#include "vmloop_parts/helper_macros.def"

#ifdef HOST
#include "../host_vm/dpu_manage.h"
#include <iostream>
#include <chrono>
struct MethodTable* array_type;
#endif

__host SLOTVAL ret_val;



struct method {
    uint16_t access_flag;
    uint32_t class_ref;
    uint32_t return_type;
    uint16_t params_count;
    uint8_t* params;
    uint16_t name_index;
    uint16_t attribute_count;
    uint16_t max_stack;
    uint16_t max_locals;
    uint16_t code_len;
    uint8_t* code;
};

void interp(struct function_thunk func_thunk) {

#include "vmloop_parts/registers.def"

    code_buffer = func->bytecodes;
    pc = 0;
    times = 0;
    // TODO
    frame = create_new_vmframe(func_thunk, &stack_top, 0, 0, 0, 0);
    //evaluation_stack_pt = FRAME_GET_EVAL_STACK(frame);
    //params_buffer_pt -= SLOTSIZE * func->params_count;
#define DEBUG
    DEBUG_PRINT("create frame finished\n");
    //print_frame(frame);
   
#ifdef INMEMORY
    printf(RED);
#endif


#ifdef HOST
    array_type = MRAM_METASPACE_ALLOC_STRUT(struct MethodTable);
    array_type->metadata_token = 0x1;

    auto system = DpuSet::allocate(1);
    auto dpu = system.dpus()[0];
    dpu->load("dpuslave");
#endif


    while (1) {
        switch (code_buffer[pc++])
        {
#ifndef JAVA_BYTECODE
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
        case GETFIELD:
            DEBUG_OUT_INSN_PARSED("GETFIELD")

            // op1 <- constant table index
            op1 = (code_buffer[pc + 1] << 8) | code_buffer[pc + 2];
            pc += 2

            // op2 <- instance ref
            POP_EVAL_STACK(op2)
            
            // op3 <- class structure's addr, from instance addr = op2
            GET_CLASSSTRUT(op2, op3)


            // locate fieldref
            op3 += op1 * 8;

            READ_INT32_BIT_BY_BIT((uint8_t __mram_ptr*)(op3 + 4), op1); // field offset


            // read field value and push to stack
            PUSH_EVAL_STACK(*(uint32_t __mram_ptr*)(op2 + 4 + op1 * 4))

            break;
        case PUTFIELD:
            DEBUG_OUT_INSN_PARSED("PUTFIELD")
            op1 = (code_buffer[pc + 1] << 8) | code_buffer[pc + 2]; // constant table index
            pc += 2
            GET_CLASSSTRUT(op2, op3) //class structure's addr. 
            READ_INT32_BIT_BY_BIT((uint8_t __mram_ptr*)(op3), op4); //class structure's addr
            // locate fieldref
            op4 += op1 * 8;
            READ_INT32_BIT_BY_BIT((uint8_t __mram_ptr*)(op4 + 4), op1); // field offset
            WRITE_INT32_BIT_BY_BIT((uint8_t __mram_ptr*)(op3 + 4 + 4 * op1), op2);
            break;

        case INVOKEVIRTUAL: // function call
            DEBUG_OUT_INSN_PARSED("INVOKEVIRTUAL")
            op1 = (code_buffer[pc + 1] << 8) | code_buffer[pc + 2]; // constant table index to methoderef
            pc += 2;
            method_ptr = FRAME_GET_FUNC_PT(frame)
            jmethod = *(struct j_method __mram_ptr*)(method_ptr->class_ref + 8 * op1 + 4;) // the address of target method
            callee.func = jmethod;
            callee.params = evaluation_stack_pt;

            // create frame, and put all params to locals
            printf("new func size = %p\n", jmethod->code_len);
            
            
            
            mp_frame_begin = create_new_vmframe(callee, &stack_top,  // (uint8_t*)func->bytecodes + 
                pc + 4, evaluation_stack_pt - SLOTSIZE * callee.func->params_count, pc, frame);
            print_frame(tmp_frame_begin);
            evaluation_stack_pt = FRAME_GET_EVAL_STACK(tmp_frame_begin);
            
            //reset some register and process return vals
            pc = 0;
            #ifdef HOST
                code_buffer = (module1->funcs[op3]).ft->func->bytecodes;
            #endif
            frame = tmp_frame_begin;
            
        
        case NEW:
            DEBUG_OUT_INSN_PARSED("NEW")





            break;



                    


#include "vmloop_parts/load_store_loc.inc"
#include "vmloop_parts/load_store_constant.inc"
#include "vmloop_parts/argument.inc"
#include "vmloop_parts/arithmetic.inc"
#include "vmloop_parts/branch.inc"
#include "vmloop_parts/function.inc"


        case NEWARR:
#ifdef INMEMORY
            DEBUG_OUT_INSN_PARSED("NEWARR")
            POP_EVAL_STACK(op1);
            //currently array is dynamically created in MRAM heap
            printf(" - create size = %p, i32 array, pt = %p\n", op1, mram_heap_pt);
            PUSH_EVAL_STACK(mram_heap_pt);

            *MSLOTP  mram_heap_pt = op1;

            MRAM_HEAP_ALLOC_S(SLOTSIZE + 4 * op1);

#else
            {
                DEBUG_OUT_INSN_PARSED("NEWARR")
                POP_EVAL_STACK(op1);
                printf(" - create size = %p, i32 array (object), pt = %p\n", op1, mram_heap_pt);

                struct ObjectInstance* m = MRAM_HEAP_ALLOC_STRUT(struct ObjectInstance);
                
                // |synchronize_pointer  |(ref) typehandler | ((len e1 e2 ... en) : strut SZArray) : ObjInstance::fields|
                ref = OBJ_INSTANCE_REF(m);

                PUSH_EVAL_STACK(ref);
                m->sync_block_pointer = 0;
                m->method_table_pointer = array_type;

                //write szarray to fields area of object instance
                struct SZArray* szarr = (struct SZArray*)OBJ_INSTANCE_FIELDS_PTR(ref); 
                szarr->length = op1; //write array length

                //allocate array elements space
                MRAM_HEAP_ALLOC_S(sizeof(int) * op1);
                
                //write array length
                *(uint8_t**)(OBJ_INSTANCE_FIELDS_PTR(ref)) = op1;

                auto t = &(m->fields);
                auto len = *(uint8_t**)(t);
                printf("create int32 array len = %p %p\n", len, op1);
                

               
        }
#endif
            break;
        case STELEM_I4:

#ifdef INMEMORY
            DEBUG_OUT_INSN_PARSED("STELEM_I4");
            POP_EVAL_STACK(op3);
            POP_EVAL_STACK(op2);
            POP_EVAL_STACK(op1);
#define ELEMENT_MADDR (op1 + 4 * op2 + SLOTSIZE)
#define ELEMENT_OFFSET (4 * op2 + SLOTSIZE)
#define ELEMENT_INNER_PAGE_OFFSET (ELEMENT_OFFSET & DMA_INNER_BLOCK_MASK)

           

            if (1) {
                DEBUG_PRINT("-- slow write\n");
                DEBUG_PRINT(" - set %d to m addr %p [index %d of int32 array addr from %p].\n", op3, ELEMENT_MADDR, op2, op1);
                WRITE_INT32_BIT_BY_BIT((uint8_t __mram_ptr*)(op1 + 4 * op2 + SLOTSIZE), op3);
            }
#else
            {
                
                DEBUG_OUT_INSN_PARSED("STELEM_I4");
                POP_EVAL_STACK(op3); //val
                POP_EVAL_STACK(op2); //element
                POP_EVAL_STACK(op1); //ref

                DEBUG_PRINT(" - set %d to array object ref %p [index %d of int32 array, element addr = %p].\n", op3, op1, 
                    op2, (int*)(op1 + sizeof(struct MethodTable*) + sizeof(uint8_t*) + op2 * sizeof(int)));

                *(int*)(OBJ_INSTANCE_FIELDS_PTR(op1) + sizeof(uint8_t*) + op2 * sizeof(int)) = op3;

            }
          

#endif
                break;
        case LDELEM_I4:
#ifdef INMEMORY
            DEBUG_OUT_INSN_PARSED("LDELEM_I4");
            POP_EVAL_STACK(op2);
            POP_EVAL_STACK(op1);
            DEBUG_PRINT(" - get int32 val in addr %p [index %d of int32 array addr from %p].\n", ELEMENT_MADDR, op2, op1);
            op3 = 0;

            //slow
            READ_INT32_BIT_BY_BIT((uint8_t __mram_ptr*)(op1 + 4 * op2 + SLOTSIZE), op3);
            DEBUG_PRINT("-- slow read %d\n", op3);
            

            DEBUG_PRINT(" - val = %d\n", op3);
            PUSH_EVAL_STACK(op3);
#else
            {
                DEBUG_OUT_INSN_PARSED("LDELEM_I4");
                POP_EVAL_STACK(op2);
                POP_EVAL_STACK(op1);
                DEBUG_PRINT(" - get int32 val in ref %p [index %d of int32 array, elem addr = ].\n",
                    op1, op2, op1 + sizeof(struct MethodTable*) + sizeof(uint8_t*) + sizeof(int) * op2);

                op3 = *(int*)(OBJ_INSTANCE_FIELDS_PTR(op1) + sizeof(uint8_t*) + sizeof(int) * op2);
                DEBUG_PRINT(" - val = %d\n", op3);
                PUSH_EVAL_STACK(op3);
            }

#endif
            break;
        case LDLEN:
#ifdef INMEMORY
            DEBUG_OUT_INSN_PARSED("LDLEN");
         
            POP_EVAL_STACK(op2);
            
            op1 = 0;
            for(i = 0; i < 4; i++){
                DEBUG_PRINT("- %p \n",  (*(int __mram_ptr*)op2 >> (8 * i)) & 0b11111111);
                op1 <<= 8;
                op1 |= ((*(int __mram_ptr*)op2 >> (8 * i)) & 0b11111111);
            }
            DEBUG_PRINT(" - get arr (addr)%p len = %p\n", op2, (uint32_t)op1);
            PUSH_EVAL_STACK(op1);
#else

            DEBUG_OUT_INSN_PARSED("LDLEN");

            POP_EVAL_STACK(op2); //ref of an array object

            op1 = *(uint8_t**)(OBJ_INSTANCE_FIELDS_PTR(op2)); //length field
            DEBUG_PRINT(" - get arr (addr)%p len = %p\n", op2, op1);
            PUSH_EVAL_STACK(op1);

#endif
            break;
        case EXT_PREFIX0:
        {
            switch (code_buffer[pc++]) {
#include "vmloop_parts/ext_comparasion.inc"
            }
        }break;

        case STSFLD: 
        {
            DEBUG_OUT_INSN_PARSED("STSFLD");
            READ_INT32_BIT_BY_BIT(code_buffer + pc, op1);
            POP_EVAL_STACK(op2);

            DEBUG_PRINT(" - set static field in index %x with val = %p\n", op1 & 0x00FFFFFF, op2);

            sfields_table->lines[op1 & 0x00FFFFFF].value = op2;
            DEBUG_PRINT(" -- after set, index %d (addr %p), val = %p\n", op1 & 0x00FFFFFF, &(sfields_table->lines[0]), sfields_table->lines[op1 & 0x00FFFFFF].value);

            pc += 4;
        } break;
        case LDSFLD: 
        {
            DEBUG_OUT_INSN_PARSED("LDSFLD");
            READ_INT32_BIT_BY_BIT(code_buffer + pc, op1);
            op2 = sfields_table->lines[op1 & 0x00FFFFFF].value;
            DEBUG_PRINT(" - load static field in index %x (addr %p) with val = %p\n", op1 & 0x00FFFFFF, &(sfields_table->lines[0]), op2);
            PUSH_EVAL_STACK(op2);
            pc += 4;
        } break;

#else
        case NOP:
            DEBUG_OUT_INSN_PARSED("NOP");
            break;

        
        case IRETURN:
            
            // DEBUG_OUT_INSN_PARSED("IRETURN");
            // DEBUG_PRINT(" - try ret to %p\n", FRAME_GET_RET_ADDR(frame));

            // pc = FRAME_GET_RET_ADDR(frame);
            // DEBUG_PRINT(" - eval_stack_begin: %p, end: %p\n", FRAME_GET_EVAL_STACK(frame), evaluation_stack_pt);

            // //recover
            // tmp_frame_begin = FRAME_GET_LAST_FRAME_PT(frame);

            // if (FRAME_GET_LAST_FRAME_PT(frame) == 0) {
            //     DEBUG_PRINT(" - end of program....\n");
            //     if (FRAME_GET_EVAL_STACK(frame) < evaluation_stack_pt) {
            //         POP_EVAL_STACK(ret_val);
            //         DEBUG_PRINT(" - get ret_val: %p\n", ret_val);
            //     }
            //     DEBUG_PRINT("print local vars: \n");
            //     for (i = 0; i < func->local_slots_count; i++) {
            //         DEBUG_PRINT("slot %d: - %p\n", i, *SLOTPT (FRAME_GET_LOCAL_VARS(frame) + SLOTSIZE * i));
            //     }

            //     printf("final frame\n");
            //     current_frame_top = 0;
            
            //     return;
            // }

            // if (FRAME_GET_EVAL_STACK(frame) < evaluation_stack_pt) {
            //     POP_EVAL_STACK(ret_val);
            //     DEBUG_PRINT(" - get ret_val: %p\n", ret_val);
            //     //write to last frame stack

            //     DEBUG_PRINT("%p\n", FRAME_GET_LAST_FRAME_EVAL_STACK_TOP_PT(frame));
            //     *SLOTPT FRAME_GET_LAST_FRAME_EVAL_STACK_TOP_PT(frame) = ret_val;
            //     DEBUG_PRINT(" - write ret value \n");
            //     evaluation_stack_pt = FRAME_GET_LAST_FRAME_EVAL_STACK_TOP_PT(frame) + sizeof(uint8_t*);
            // }
            // else {
            //     evaluation_stack_pt = FRAME_GET_LAST_FRAME_EVAL_STACK_TOP_PT(frame);
            // }


            // current_frame_end = frame + FRAME_GET_OFFSET_EXT_FIELD2_FRAME_SIZE(frame);

            // frame = FRAME_GET_LAST_FRAME_PT(frame);
            // printf("return to frame addr: %p\n", frame);
            // current_frame_top = frame;

            // //frame = tmp_frame_begin;
            // DEBUG_PRINT("jump to last func codes's %x\n", pc);
            // code_buffer = (FRAME_GET_FUNC_PT(frame))->bytecodes;
            break;

#endif // JAVA_BYTOCODE
        default:
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
