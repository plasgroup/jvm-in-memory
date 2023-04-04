#include <stdio.h>

#include "frame_helper.h"
#include "../ir/opcode.h"
#include "function.h"
#include "memory.h"
#include "vm_loop.h"
#include "vmloop_parts/helper_macros.def"

#ifdef HOST
#include "../host_vm/dpu_manage.h"
#include <iostream>
struct MethodTable* array_type;
#endif

__host SLOTVAL ret_val;


void interp(struct function_thunk func_thunk) {

#include "vmloop_parts/registers.def"

    code_buffer = func->bytecodes;
    pc = 0;
    times = 0;
    frame = create_new_vmframe(func_thunk, &stack_top, 0, 0, 0, 0);
    evaluation_stack_pt = FRAME_GET_EVAL_STACK(frame);
    params_buffer_pt -= SLOTSIZE * func->params_count;
#define DEBUG
    DEBUG_PRINT("create frame finished\n");
    print_frame(frame);
   
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
//        printf("pc: %d, %d\n", pc, code_buffer[pc]);
        switch (code_buffer[pc++])
        {
        case NOP:
            DEBUG_OUT_INSN_PARSED("NOP");
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
            printf("-- slow read %d\n", op3);
            

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
                printf("- %p \n",  (*(int __mram_ptr*)op2 >> (8 * i)) & 0b11111111);
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

            printf(" - set static field in index %x with val = %p\n", op1 & 0x00FFFFFF, op2);

            sfields_table->lines[op1 & 0x00FFFFFF].value = op2;
            printf(" -- after set, index %d (addr %p), val = %p\n", op1 & 0x00FFFFFF, &(sfields_table->lines[0]), sfields_table->lines[op1 & 0x00FFFFFF].value);

            pc += 4;
        } break;
        case LDSFLD: 
        {
            DEBUG_OUT_INSN_PARSED("LDSFLD");
            READ_INT32_BIT_BY_BIT(code_buffer + pc, op1);
            op2 = sfields_table->lines[op1 & 0x00FFFFFF].value;
            printf(" - load static field in index %x (addr %p) with val = %p\n", op1 & 0x00FFFFFF, &(sfields_table->lines[0]), op2);
            PUSH_EVAL_STACK(op2);
            pc += 4;
        } break;
        default:
            break;
        }
      
        times++;
        

#ifdef INMEMORY
        //if (times > 500) return;
#else
       // if (times > 1800) return;
#endif

    }




}
