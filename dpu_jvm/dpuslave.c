
#include <stdio.h>
#include <string.h>
#include <stdint.h>
#include <stddef.h>
#ifndef INMEMORY
#include <malloc.h>  // This part is previously exist because 
#else
#include <defs.h>
#include <alloc.h>
#include <mram.h>
#endif // INMEMORY

#include "core/memory.h"
#include "ir/bytecode.h"
#include "core/frame_helper.h"
#include "core/method.h"
#include "core/jclass.h"
#include "core/vm_loop.h"
#include "utils/jstruct_printer.h"

#define MARAM_METASPACE_MALLOC(size) meta_space_pt; meta_space_pt += size;


char inited = 0;

void print_virtual_table(struct j_class __mram_ptr* jc){
    int len = jc->virtual_table_length;
    for(int i = 0; i < len; i++){
        DEBUG_PRINT("Vtable #%d, classref = %p, method ref = %p\n", i, 
        jc->virtual_table[i].classref,
        jc->virtual_table[i].methodref);
    }
}

void print_method(struct j_method __mram_ptr* jm){
    uint8_t __mram_ptr* loc = (uint8_t __mram_ptr*) jm;
    int i = 0;
    DEBUG_PRINT("-------------------------------------------------------------------\n");
    DEBUG_PRINT("-- JMethod Addr = %p\n", jm);
    DEBUG_PRINT("-- (%p) total_size = %d\n", loc,*(u4 __mram_ptr*)loc);
    loc += 4;
    DEBUG_PRINT("-- (%p) access_flags = 0x%04x\n", loc, *(u2 __mram_ptr*)loc);
    loc += 2;
    DEBUG_PRINT("-- (%p) params_count = %d\n", loc, *(u2 __mram_ptr*)loc);
    loc += 2;
    DEBUG_PRINT("-- (%p) name_index = %d\n", loc, *(u2 __mram_ptr*)loc);
    loc += 2;
    DEBUG_PRINT("-- (%p) max_stack = %d\n", loc, *(u2 __mram_ptr*)loc);
    loc += 2;
    DEBUG_PRINT("-- (%p) max_locals = %d\n", loc, *(u2 __mram_ptr*)loc);
    loc += 2;
    loc += 2;
    DEBUG_PRINT("-- (%p) code_length = %d\n", loc, *(u4 __mram_ptr*)loc);
    loc += 4;
    DEBUG_PRINT("-- (%p) bytecodes_list_ref = %p === %p\n", loc, *(uint32_t __mram_ptr*)loc, jm->bytecodes);
    loc += 4;
    
    // loc += 4;
    // bytecodes
    for(i = 0; i < jm->code_length; i++){
        DEBUG_PRINT("---- (%p) bytecode[%d] = 0x%02x\n", loc, i,
                *(uint8_t __mram_ptr*)loc);
        loc++;
    }
    DEBUG_PRINT("-------------------------------------------------------------------\n");
}

void exec_tasks() {
    struct function_thunk fc;
    int i;
    int num = 0;
    int t  = 0;

    int tasklet_id = me(); // get the id of current tasklet.

    // Each tasklet hold part of the whole parameter buffer. 
    // This statement calculate the size of subparameter buffer that a tasklet hold.
    int this_tasklet_params_buffer_len = (PARAMS_BUFFER_SIZE / TASKLET_CNT);
    
    // Calculate the beginning address of subparameter buffer of current tasklet
    int buffer_begin = params_buffer + tasklet_id * this_tasklet_params_buffer_len;



    // condition judgement to prevent replicative initialization of DPU memory.
    if (inited == 0) {
        init_memory();
        inited = 1;
    }

    // Get the current "parameter buffer head pointer" of parameter buffer of current tasklet
    // the parameter buffer store tasks representation. The tasklet will take task form it one by one, 
    // and each time a task is taken from the buffer, the parameter buffer head pointer of current tasklet will
    // move forward. 
    int tasklet_buffer_pt = params_buffer_pt[tasklet_id];

    // current tasklet's parameter buffer head should > parameter buffer beginning of current tasklet
    if(buffer_begin >= tasklet_buffer_pt){
        return;
    }


    DEBUG_PRINT(RED " --------------------- (IN DPU) -----------------------------\n" RESET);

    /* ================================ Object Instance ================================ */
    DEBUG_PRINT("meta_space_pt = 0x%x, heap_space_pt = 0x%x, param_pt = 0x%x\n"
                , meta_space_pt, mram_heap_pt, params_buffer_pt[me()]);
    /* ================================ Write Params ================================ */
#define PUSH_PARAM(X) \ 
                    *(uint32_t __mram_ptr*)params_buffer_pt = X; \
                    params_buffer_pt[me()] += 4;    
   
    while(buffer_begin < tasklet_buffer_pt){

        /* get a task from parameter buffer */
        /* | task_id (4B) | java class structure pointer (4B) |  java method structure pointer (4B) | parameters (4 * |parameters| B)| */
        int task_id = *(uint32_t __mram_ptr*) buffer_begin;
        buffer_begin += 4; // move buffer_begin pointer toward the head (tasklet_buffer_pt)
        fc.jc = (struct j_class __mram_ptr*)(*(uint32_t __mram_ptr*)buffer_begin);
        buffer_begin += 4; // move buffer_begin pointer toward the head (tasklet_buffer_pt)
        fc.func = (struct j_method __mram_ptr*)(*(uint32_t __mram_ptr*)buffer_begin);
        buffer_begin += 4 + fc.func->params_count * 4; // move buffer_begin pointer toward the head (tasklet_buffer_pt)
        fc.params = buffer_begin;

        // mem.meta_space = buffer_begin;
        

        // init fp (frame pointer) and sp (stack pointer)
        current_fp[tasklet_id] = 0;
        current_sp[tasklet_id] = wram_data_space +  tasklet_id * (WRAM_DATA_SPACE_SIZE / TASKLET_CNT);
       

        // interpretation
        interp(fc);
        

        // write result. The result space hold format | task_id, return_val | task_id, return_val | ...
        return_values[task_id * 2] = task_id;
        return_values[task_id * 2 + 1] = return_val;
        
        // align (8 B)
        buffer_begin = (buffer_begin + 0b111) & (~0b111);
    }

    release_global_memory(); // unused
    params_buffer_pt[tasklet_id] = params_buffer + tasklet_id * this_tasklet_params_buffer_len;

    DEBUG_PRINT(RED " --------------------- (END DPU) -----------------------------\n" RESET);
}

int main() {
    DEBUG_PRINT("meta_space_begin = 0x%x, heap_space_begin = 0x%x, param_begin = 0x%x, wram_space_begin = 0x%x\n"
                , m_metaspace, m_heapspace, params_buffer, wram_data_space);
    exec_tasks();
    return 0;
}
