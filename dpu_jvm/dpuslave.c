
#include <stdio.h>
#include <string.h>
#include <stdint.h>
#include <stddef.h>
#ifndef INMEMORY
#include <malloc.h>
#else

#include <defs.h>
#include <alloc.h>
#include <mram.h>
#endif // INMEMORY


#include "vm_shared_src/memory.h"
#include "ir/bytecode.h"
#include "vm_shared_src/frame_helper.h"
#include "vm_shared_src/method.h"
#include "vm_shared_src/jclass.h"
#include "vm_shared_src/vm_loop.h"
#include "utils/jstruct_printer.h"
#include "sample_ils/test_cases_gen.h"



__host uint32_t checksum;

char inited = 0;

/*
struct constant_table_item{
    u4 key;
    u4 val;
};
struct j_class{
    u4 total_size;
    u2 this_class_name_index;
    u2 super_class_name_index;
    ref_type super_class;
    u2 access_flag;
    u2 cp_2b_offset;
    int cp_item_count;
    struct constant_table_item __mram_ptr* items;
    int fields_count;
    struct j_field __mram_ptr* fields;
    int jmethod_count;
    struct j_methods __mram_ptr* methods;
    int string_constant_pool_length;
    uint8_t* string_constant_pool;
};
*/


/*
    struct j_method{
        u4 total_size;
        u2 access_flags;
        u2 params_count;
        u2 name_index;
        u2 max_stack;
        u2 max_locals;
        u2 retained;
        u4 code_length;
        ref_type return_type;
        ref_type* params;
        ref_type bytecodes;
    };
*/
    

#define MARAM_METASPACE_MALLOC(size) meta_space_pt; meta_space_pt += size;



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
  //  if(me() > 1) return;
    struct function_thunk fc;
    int i;
    uint8_t __mram_ptr* cpt = m_heapspace;
    int num = 0;
    int tasklet_id = me();
    struct j_method __mram_ptr *jm = exec_method_pt[tasklet_id];
    struct j_class __mram_ptr *jc = exec_class_pt[tasklet_id];
    int this_tasklet_params_buffer_len = (PARAMS_BUFFER_SIZE / 24);
    int buffer_begin = params_buffer + tasklet_id * this_tasklet_params_buffer_len;

    if (inited == 0) {
        init_memory();
        inited = 1;
    }
   
    //printf("me = %d, buffer_begin = %p, buffer_pt = %p\n", me(), buffer_begin, params_buffer_pt[tasklet_id]);
    
    int tasklet_buffer_pt = params_buffer_pt[tasklet_id];

    if(buffer_begin >= tasklet_buffer_pt){
      //  printf("return ...\n");
        return;
    }

    //if(tasklet_id != 0) return;

    DEBUG_PRINT(RED " --------------------- (IN DPU) -----------------------------\n" RESET);

    /* ================================ Object Instance ================================ */
    DEBUG_PRINT("meta_space_pt = 0x%x, heap_space_pt = 0x%x, param_pt = 0x%x\n"
                , meta_space_pt, mram_heap_pt, params_buffer_pt[me()]);
    /* ================================ Write Params ================================ */
#define PUSH_PARAM(X) \ 
                    *(uint32_t __mram_ptr*)params_buffer_pt = X; \
                    params_buffer_pt[me()] += 4;    
   
    while(buffer_begin < tasklet_buffer_pt){
       
        //DEBUG_PRINT("tasklet_buffer_begin = 0x%x, me = %d\n", buffer_begin, tasklet_id);
        int task_id = *(uint32_t __mram_ptr*)buffer_begin;
        buffer_begin += 4;
        fc.jc = (struct j_class __mram_ptr*)(*(uint32_t __mram_ptr*)buffer_begin);
        buffer_begin += 4;
        fc.func = (struct j_method __mram_ptr*)(*(uint32_t __mram_ptr*)buffer_begin);
        buffer_begin += 4 + fc.func->params_count * 4;
        fc.params = buffer_begin;
        
        printf("me = %d, task id = %d, func = %p, jc = %p, current_params = %p, top = %p\n", me(), task_id, fc.func, fc.jc, buffer_begin, tasklet_buffer_pt);
        
        //print_class(fc.jc);
        //print_method(fc.func);
        //print_virtual_table(fc.jc);
        
        current_fp[tasklet_id] = 0;
        current_sp[tasklet_id] = wram_data_space +  tasklet_id * (WRAM_DATA_SPACE_SIZE / 24);
        
        interp(fc);
        //printf("write to %d\n", task_id * 2);
       
        return_values[task_id * 2] = task_id;
        return_values[task_id * 2 + 1] = return_val;
        buffer_begin = (buffer_begin + 0b111) & (~0b111);
    }
    release_global_memory();
    params_buffer_pt[tasklet_id] = params_buffer + tasklet_id * this_tasklet_params_buffer_len;
    //printf("reset param buffer pt to %p\n", params_buffer_pt[tasklet_id]);
    
    DEBUG_PRINT(RED " --------------------- (END DPU) -----------------------------\n" RESET);
}

int main() {
     printf("meta_space_begin = 0x%x, heap_space_begin = 0x%x, param_begin = 0x%x, wram_space_begin = 0x%x\n"
                , m_metaspace, m_heapspace, params_buffer, wram_data_space);
    exec_task_from_host();
    return 0;
}
