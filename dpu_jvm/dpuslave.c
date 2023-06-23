
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
return;
    printf("-------------------------------------------------------------------\n");
}

void test_case1(){

    struct j_class __mram_ptr* jc = (struct j_class __mram_ptr*)MARAM_METASPACE_MALLOC(sizeof(struct j_class));
    struct j_method __mram_ptr* jm;
    int size = sizeof(struct j_class);
    struct function_thunk fc;

    /* ================================ Fill Java Class Manually ================================ */
    jc->this_class_name_index = 10;
    jc->super_class_name_index = 4;
    jc->super_class = NULL;
    jc->access_flag = 0x0021;
    jc->cp_2b_offset = 8 + sizeof(uint8_t __mram_ptr*) + 4;
    jc->cp_item_count = 29 + 1;
    jc->items = NULL;
    jc->fields_count = 3;
    jc->fields = NULL;
    jc->jmethod_count = 5;
    jc->methods = NULL;
    jc->constant_area_length = 1024; // temporary
    

    // items part
    jc->items = (struct constant_table_item __mram_ptr*)MARAM_METASPACE_MALLOC(sizeof(struct constant_table_item) * jc->cp_item_count);
    // fields part
    jc->fields = (struct j_field __mram_ptr*)MARAM_METASPACE_MALLOC(sizeof(struct j_field*) * jc->fields_count);
    // methods part
    jc->methods = (struct j_method __mram_ptr*)MARAM_METASPACE_MALLOC(sizeof(struct j_method*) * jc->jmethod_count)
    jc->constant_area = (struct uint8_t __mram_ptr*)MARAM_METASPACE_MALLOC(jc->constant_area_length);

    size += sizeof(struct constant_table_item __mram_ptr*) * jc->cp_item_count
          + sizeof(struct j_field __mram_ptr*) * jc->fields_count
          + sizeof(struct j_method __mram_ptr*) * jc->jmethod_count;(uint8_t*)jm + 3;
    jc->total_size = size;

    /* ================================ Fill Java Method Manually ================================ */
    jm = (struct j_method __mram_ptr*)MARAM_METASPACE_MALLOC(sizeof(struct j_method));
    printf("alloc java method in %p\n", jm);
    size = sizeof(struct j_method);
    // jm->access_flags = 0x0001;
    // jm->params_count = 2;
    // jm->name_index = 26;
    // jm->max_stack = 4;
    // jm->max_locals = 2;
    // jm->code_length = 23;
    // jm->return_type = NULL;
    // jm->params = NULL;
    // jm->bytecodes = NULL;
    // jm->params = (struct j_class __mram_ptr*) MARAM_METASPACE_MALLOC(sizeof(struct j_class __mram_ptr*) * jm->params_count);
    // jm->bytecodes = (uint8_t __mram_ptr*) MARAM_METASPACE_MALLOC(jm->code_length);

    jm->access_flags = 0x0001;
    jm->params_count = 1;
    jm->name_index = 27;
    jm->max_stack = 2;
    jm->max_locals = 1;
    jm->code_length = 10;
    //jm->return_type = NULL;
    //jm->params = NULL;
    jm->bytecodes = NULL;
    //jm->params = (struct j_class __mram_ptr*) MARAM_METASPACE_MALLOC(sizeof(struct j_class __mram_ptr*) * jm->params_count);
    jm->bytecodes = (uint8_t __mram_ptr*) MARAM_METASPACE_MALLOC(jm->code_length);
    
    char bcodes[10] = {
        ALOAD_0,
        GETFIELD,
        0x00,
        0x02,
        ALOAD_0,
        GETFIELD,
        0x00,
        0x03,
        IMUL,
        IRETURN
        // ILOAD_1,
        // IFGE,
        // 0x00,
        // 0x06,
        // ICONST_0,
        // IRETURN,
        // ILOAD_1,
        // ICONST_1,
        // IF_ICMPNE,
        // 0x00,
        // 0x0D,
        // ICONST_1,
        // IRETURN,
        // ILOAD_1,
        // ALOAD_0,
        // ILOAD_1,
        // ICONST_1,
        // ISUB,
        // INVOKEVIRTUAL,
        // 0x00,
        // 0x07,
        // IMUL,
        // IRETURN
    };
    for(int i = 0; i < jm->code_length; i++){
        jm->bytecodes[i] = bcodes[i];
    }

    //jm->return_type = 1;
    size += sizeof(uint8_t __mram_ptr*) * jm->params_count + jm->code_length;
    printf("method total size = %d\n", size);
    jm->total_size = size;
    
 

    /* ================================ Setting Constant Table ================================ */
    jc->items[2].direct_value = 0;
    jc->items[4].info = 30;
    jc->items[4].direct_value = jc;
    jc->items[8].info = (3 << 16) | 33;
    jc->items[8].direct_value = (uint32_t)jm;
  

    /* ================================ Object Instance ================================ */
    uint8_t __mram_ptr* obj = mram_heap_pt;
    *(uint8_t __mram_ptr**) obj = jc;
    mram_heap_pt += 8 + jc->fields_count * 4;
    printf("alloc instance, new mram_heap_pt = %p \n", mram_heap_pt);


    /* ================================ Write Params ================================ */
#define PUSH_PARAM(X) \ 
                    *(uint32_t*)params_buffer_pt = X; \
                    params_buffer_pt += 4;
    
    PUSH_PARAM(obj)
    //PUSH_PARAM(2)

  
    fc.func = jm;
    fc.jc = jc;
    fc.params = params_buffer_pt;
    
    
    print_method(jm);
     return;
    print_class(jc);
    
    interp(fc);
  

}



void test_dpu_side() {
    struct function_thunk fc;
    int i;
    uint8_t __mram_ptr* pt = m_heapspace;
    int num = 0;
    if (inited == 0) {

        init_memory();
        inited = 1;
    }
    printf(RED " --------------------- (IN DPU) -----------------------------\n" RESET);

    test_case1();
    

    release_global_memory();
    printf(RED " --------------------- (END DPU) -----------------------------\n" RESET);
}



void read_class(uint8_t __mram_ptr* data){

}
void test_dpu_read_class(){
    uint8_t __mram_ptr* cpt = exec_class_pt;
    int i = 0;
    __dma_aligned struct j_class __mram_ptr* jc = (struct j_class __mram_ptr*) cpt;
    int num = 0;
    uint8_t __mram_ptr* mpt;
    if (inited == 0) {

        init_memory();
        inited = 1;
    }
    printf(RED " --------------------- (IN DPU) -----------------------------\n" RESET);

    printf("%p\n", cpt);
  
    
    printf("total_size = %d %d %p %p\n", jc->total_size, offsetof(struct j_class, total_size),
        m_metaspace, jc);
    
    for(i = 0; i < 20; i++){
        for(int j = 0; j < 10; j++){
            printf("%02x\t", *((uint8_t __mram_ptr*)(jc) + i * 10 + j));
        }
        printf("\n");
    }
    
    jc->items = (u4)jc->items;
    jc->fields = (u4)jc->fields;
    print_class(jc);


    for(i = 0, mpt = (uint8_t __mram_ptr*)jc->methods; i < jc->jmethod_count; i++){
        printf(" --- print method %d ---\n", i);
        print_method((struct j_method __mram_ptr*)mpt);
        mpt = mpt + ((struct j_method __mram_ptr*)mpt)->total_size;
    }
    release_global_memory();
    printf(RED " --------------------- (END DPU) -----------------------------\n" RESET);
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
    printf("current meta_space_pt = 0x%x, heap_space_pt = 0x%x, param_pt = 0x%x\n"
                , meta_space_pt, mram_heap_pt, params_buffer_pt);
    /* ================================ Write Params ================================ */
#define PUSH_PARAM(X) \ 
                    *(uint32_t*)params_buffer_pt = X; \
                    params_buffer_pt += 4;
    
    
  
    fc.func = jm;
    fc.jc = jc;
    fc.params = params_buffer_pt;
    printf("params_buffer_pt = 0x%x\n", params_buffer_pt);
    
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
