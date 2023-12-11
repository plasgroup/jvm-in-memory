
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
#include "sample_ils/test_cases_gen.h"

char inited = 0;
   

#define MARAM_METASPACE_MALLOC(size) meta_space_pt; meta_space_pt += size;

#define TASKLET_CNT 24

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

void exec_task_from_host() {
    struct function_thunk fc;
    int i;
    int num = 0;
    int t  = 0;

    int tasklet_id = me(); // get the id of current tasklet.
    struct j_method __mram_ptr *jm = exec_method_pt[tasklet_id];
    struct j_class __mram_ptr *jc = exec_class_pt[tasklet_id];

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
        // DEBUG_PRINT("tasklet_buffer_begin = 0x%x, me = %d\n", buffer_begin, tasklet_id);

        /* get a task from parameter buffer */
        int task_id = *(uint32_t __mram_ptr*)buffer_begin;
        buffer_begin += 4;
        fc.jc = (struct j_class __mram_ptr*)(*(uint32_t __mram_ptr*)buffer_begin);
        buffer_begin += 4;
        fc.func = (struct j_method __mram_ptr*)(*(uint32_t __mram_ptr*)buffer_begin);
        buffer_begin += 4 + fc.func->params_count * 4;
        fc.params = buffer_begin;

        mem.meta_space = buffer_begin;
        
        current_fp[tasklet_id] = 0;
        current_sp[tasklet_id] = wram_data_space +  tasklet_id * (WRAM_DATA_SPACE_SIZE / 24);
       
        interp(fc);

        
        return_values[task_id * 2] = task_id;
        return_values[task_id * 2 + 1] = return_val;
        buffer_begin = (buffer_begin + 0b111) & (~0b111);
        
    }
    release_global_memory();
    params_buffer_pt[tasklet_id] = params_buffer + tasklet_id * this_tasklet_params_buffer_len;

    DEBUG_PRINT(RED " --------------------- (END DPU) -----------------------------\n" RESET);
}

int main() {
    DEBUG_PRINT("meta_space_begin = 0x%x, heap_space_begin = 0x%x, param_begin = 0x%x, wram_space_begin = 0x%x\n"
                , m_metaspace, m_heapspace, params_buffer, wram_data_space);
    exec_task_from_host();
    return 0;
}
