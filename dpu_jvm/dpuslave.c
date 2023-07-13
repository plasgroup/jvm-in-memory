
#include <stdio.h>
#include <string.h>
#include <stdint.h>
  #include <stddef.h>
#ifndef INMEMORY
#include <malloc.h>
#else

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
        printf("Vtable #%d, classref = %p, method ref = %p\n", i, 
        jc->virtual_table[i].classref,
        jc->virtual_table[i].methodref);
    }
}

void print_method(struct j_method __mram_ptr* jm){
    uint8_t __mram_ptr* loc = (uint8_t __mram_ptr*) jm;
    int i = 0;
    printf("-------------------------------------------------------------------\n");
    printf("-- JMethod Addr = %p\n", jm);
    printf("-- (%p) total_size = %d\n", loc,*(u4 __mram_ptr*)loc);
    loc += 4;
    printf("-- (%p) access_flags = 0x%04x\n", loc, *(u2 __mram_ptr*)loc);
    loc += 2;
    printf("-- (%p) params_count = %d\n", loc, *(u2 __mram_ptr*)loc);
    loc += 2;
    printf("-- (%p) name_index = %d\n", loc, *(u2 __mram_ptr*)loc);
    loc += 2;
    printf("-- (%p) max_stack = %d\n", loc, *(u2 __mram_ptr*)loc);
    loc += 2;
    printf("-- (%p) max_locals = %d\n", loc, *(u2 __mram_ptr*)loc);
    loc += 2;
    loc += 2;
    printf("-- (%p) code_length = %d\n", loc, *(u4 __mram_ptr*)loc);
    loc += 4;
    // printf("-- (%p) return_type_ref = %p\n", loc, *(uint32_t __mram_ptr*)loc);
    // loc += 4;
    // printf("-- (%p) params_type_list_ref = %p %p\n", loc, *(uint32_t __mram_ptr*)loc, jm->params);
    // loc += 4;
    printf("-- (%p) bytecodes_list_ref = %p === %p\n", loc, *(uint32_t __mram_ptr*)loc, jm->bytecodes);
    loc += 4;
    
    // params
    // for(i = 0; i < jm->params_count; i++){ 
    //     printf("---- (%p) params_type[%d] = %p\n", loc, i, 
    //             *(uint8_t __mram_ptr**)loc);
    //     loc += sizeof(uint8_t*);
    // }
    
    //loc += 4;
    // bytecodes
    for(i = 0; i < jm->code_length; i++){
        printf("---- (%p) bytecode[%d] = 0x%02x\n", loc, i,
                *(uint8_t __mram_ptr*)loc);
        loc++;
    }
    printf("-------------------------------------------------------------------\n");
}




void exec_task_from_host() {
    struct function_thunk fc;
    int i;
    uint8_t __mram_ptr* cpt = m_heapspace;
    int num = 0;
    struct j_method __mram_ptr *jm = exec_method_pt;
    struct j_class __mram_ptr *jc = exec_class_pt;
    
    if (inited == 0) {
        init_memory();
        inited = 1;
    }
    printf(RED " --------------------- (IN DPU) -----------------------------\n" RESET);

    /* ================================ Object Instance ================================ */
    printf("meta_space_pt = 0x%x, heap_space_pt = 0x%x, param_pt = 0x%x\n"
                , meta_space_pt, mram_heap_pt, params_buffer_pt);
    /* ================================ Write Params ================================ */
#define PUSH_PARAM(X) \ 
                    *(uint32_t*)params_buffer_pt = X; \
                    params_buffer_pt += 4;    
  
    fc.func = jm;
    fc.jc = jc;
    fc.params = params_buffer_pt;
    printf("params_buffer_pt = 0x%x\n", params_buffer_pt);
    
    print_class(jc);
    print_method(jm);

    print_virtual_table(jc);

    
    interp(fc);
    release_global_memory();
    printf(RED " --------------------- (END DPU) -----------------------------\n" RESET);
}


int main() {
    printf("%x\n", params_buffer_pt);
    //printf("class_pt = %p, method_pt = %p\n", exec_class_pt, exec_method_pt);
    exec_task_from_host();
    //test_dpu_side();
    // struct j_class __mram_ptr* jc = (struct j_class __mram_ptr*) 0;
    // printf("total_size = %d\n", m_metaspace[0]);
    return 0;
}
