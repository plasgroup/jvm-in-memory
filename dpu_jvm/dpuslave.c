
#include "ir/opcode.h"
#include "ir/bytecode.h"

#include <stdio.h>
#include <string.h>
#include <stdint.h>
#include "vm_shared_src/frame_helper.h"
#include "vm_shared_src/function.h"
#include "vm_shared_src/jclass.h"
#include "vm_shared_src/memory.h"
#include "vm_shared_src/vm_loop.h"
#include "sample_ils/test_cases_gen.h"


////////////////////////////////////////////// HEADER//////////////////////////////////////////////////
#ifndef INMEMORY
#include <malloc.h>
#else
#include <alloc.h>
#include <mram.h>
#endif // INMEMORY



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
#define MARAM_MALLOC(size) meta_space_pt; meta_space_pt += size;

void print_class(struct j_class __mram_ptr* jc){
    uint8_t __mram_ptr* loc = (uint8_t __mram_ptr*) jc;
    int i = 0;

    printf("-------------------------------------------------------------------\n");
    printf("-- JClass Addr = %p\n", jc);
    printf("-- (%p) total_size = %d\n", loc,*(u4 __mram_ptr*)loc);
    loc += 4;
    printf("-- (%p) this_class_index = %d\n", loc, *(u2 __mram_ptr*)loc);
    loc += 2;
    printf("-- (%p) super_class_index = %d\n", loc, *(u2 __mram_ptr*)loc);
    loc += 2;
    printf("-- (%p) super_class_ref = %p\n", loc, *(uint8_t __mram_ptr**)loc);
    loc += sizeof(uint8_t __mram_ptr*);
    printf("-- (%p) access_flags = 0x%04x\n", loc, *(u2 __mram_ptr*)loc);
    loc += 2;
    printf("-- (%p) cp_2b_offset = %d\n", loc, *(u2 __mram_ptr*)loc);
    loc += 2;
    printf("-- (%p) constant_table_items_count = %d\n", loc, *(u4 __mram_ptr*)loc);
    loc += 4;
    printf("-- (%p) constant_table_ref = %p\n", loc, (struct constant_table_item __mram_ptr*)*(u4 __mram_ptr*)loc);
    loc += sizeof(struct constant_table_item __mram_ptr*);
    for(i = 0; i < jc->cp_item_count; i++){
        printf("---- (%p) CP item #%d: 0x%x | 0x%x\n",  &jc->items[i],i + 1, jc->items[i].info, jc->items[i].direct_value);
    }
    printf("-- (%p) field_count = %d\n", loc, *(u4 __mram_ptr*)loc);
    loc += 4;
    printf("-- (%p) fields_pt = %p\n", loc, *(u4 __mram_ptr*)loc);
    loc += 4;
    printf("-- (%p) jmethod_count = %d\n", loc, *(u4 __mram_ptr*)loc);
    loc += 4;
    printf("-- (%p) jmethod_pt = %p\n", loc, *(u4 __mram_ptr*)loc);
    loc += 4;
    printf("-- (%p) string_constant_area_length = %p\n", loc, *(u4 __mram_ptr*)loc);
    loc += 4;
    printf("-- (%p) constant_area_pt = %p\n", loc, *(u4 __mram_ptr*)loc);
    loc += 4;

    printf("-------------------------------------------------------------------\n");
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
    printf("-- (%p) return_type_ref = %p\n", loc, *(uint32_t __mram_ptr*)loc);
    loc += 4;
    printf("-- (%p) params_type_list_ref = %p %p\n", loc, *(uint32_t __mram_ptr*)loc, jm->params);
    loc += 4;
    printf("-- (%p) bytecodes_list_ref = %p %p\n", loc, *(uint32_t __mram_ptr*)loc, jm->bytecodes);
    loc += 4;
    
    // params
    for(i = 0; i < jm->params_count; i++){
        printf("---- (%p) params_type[%d] = %p\n", loc, i, 
                *(uint8_t __mram_ptr**)loc);
        loc += sizeof(uint8_t*);
    }

    // bytecodes
    for(i = 0; i < jm->code_length; i++){
        printf("---- (%p) bytecode[%d] = 0x%02x\n", loc, i,
                *(uint8_t __mram_ptr*)loc);
        loc++;
    }

    printf("-------------------------------------------------------------------\n");
}

void test_case1(){

    struct j_class __mram_ptr* jc = (struct j_class __mram_ptr*)MARAM_MALLOC(sizeof(struct j_class));
    struct j_method __mram_ptr* jm;
    int size = sizeof(struct j_class);
    struct function_thunk fc;

    printf("test case 1\n");
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
    jc->string_constant_pool_length = 1024; // temporary
    
    printf("%p,  %p\n", &jc->items, jc->items);

    // items part
    jc->items = (struct constant_table_item __mram_ptr*)MARAM_MALLOC(sizeof(struct constant_table_item*) * jc->cp_item_count);
    printf("%p,  %p\n", &jc->items, jc->items);
    


    // fields part
    jc->fields = (struct j_field __mram_ptr*)MARAM_MALLOC(sizeof(struct j_field*) * jc->fields_count);
    
    // methods part
    jc->methods = (struct j_method __mram_ptr*)MARAM_MALLOC(sizeof(struct j_method*) * jc->jmethod_count)
    jc->string_constant_pool = (struct uint8_t __mram_ptr*)MARAM_MALLOC(jc->string_constant_pool_length);

    size += sizeof(struct constant_table_item __mram_ptr*) * jc->cp_item_count
          + sizeof(struct j_field __mram_ptr*) * jc->fields_count
          + sizeof(struct j_method __mram_ptr*) * jc->jmethod_count;(uint8_t*)jm + 3;
    jc->total_size = size;


    jm = (struct j_method __mram_ptr*)MARAM_MALLOC(sizeof(struct j_method));
    printf("alloc java method in %p\n", jm);
    size = sizeof(struct j_method);
    jm->access_flags = 0x0001;
    jm->params_count = 2;
    jm->name_index = 26;
    jm->max_stack = 4;
    jm->max_locals = 2;
    jm->code_length = 23;
    jm->return_type = NULL;
    jm->params = NULL;
    jm->bytecodes = NULL;
    jm->params = (struct j_class __mram_ptr*) MARAM_MALLOC(sizeof(struct j_class __mram_ptr*) * jm->params_count);
    jm->bytecodes = (uint8_t __mram_ptr*) MARAM_MALLOC(jm->code_length);


    jm->bytecodes[0] = ILOAD_1;
    jm->bytecodes[1] = IFGE;
    jm->bytecodes[2] = 0x00;
    jm->bytecodes[3] = 0x06;
    jm->bytecodes[4] = ICONST_0;
    jm->bytecodes[5] = IRETURN;
    jm->bytecodes[6] = ILOAD_1;
    jm->bytecodes[7] = ICONST_1;
    jm->bytecodes[8] = IF_ICMPNE;
    jm->bytecodes[9] = 0x00;
    jm->bytecodes[10] = 0x0D;
    jm->bytecodes[11] = ICONST_1;
    jm->bytecodes[12] = IRETURN;
    jm->bytecodes[13] = ILOAD_1;
    jm->bytecodes[14] = ALOAD_0;
    jm->bytecodes[15] = ILOAD_1;
    jm->bytecodes[16] = ICONST_1;
    jm->bytecodes[17] = ISUB;
    jm->bytecodes[18] = INVOKEVIRTUAL;
    jm->bytecodes[19] = 0x00;
    jm->bytecodes[20] = 0x07;
    jm->bytecodes[21] = IMUL;
    jm->bytecodes[22] = IRETURN;
    jm->return_type = 1;
    size += sizeof(uint8_t __mram_ptr*) * jm->params_count + jm->code_length;
    printf("size = %d\n", size);
    jm->total_size = size;
    

    // set direct reference
    jc->items[3].info = 30;
    jc->items[3].direct_value = jc;
    jc->items[7].info = (3 << 16) | 33;
    jc->items[7].direct_value = (uint32_t)jm;

    // object in heap
    uint8_t __mram_ptr* obj = mram_heap_pt;
    *(uint8_t __mram_ptr**) obj = jc;
    mram_heap_pt += 8 + jc->fields_count * 4;
    printf("alloc instance, new mram_heap_pt = %p \n", mram_heap_pt);

    *(uint32_t*)params_buffer_pt = obj;
    params_buffer_pt += 4;
    *(uint32_t*)params_buffer_pt = 2;
    params_buffer_pt += 4;
    fc.func = jm;
    fc.jc = jc;
    fc.params = params_buffer_pt;
    
    print_method(jm);
    print_class(jc);
    interp(fc);
  

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
    


}



void test_dpu_side() {
    struct function_thunk fc;
    int i;
    uint8_t __mram_ptr* pt = mram_heap_space;
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

struct function_thunk read_function(uint8_t __mram_ptr* metadata, uint8_t* params_top_ptr) {
    struct function __mram_ptr* func_pt;
    struct function_thunk fc;

    func_pt = (struct function __mram_ptr*)metadata;
    func_pt->bytecodes = metadata + sizeof(struct function);

    printf("\t\t\t| func size = %d, max_stack = %d, local_var_count = %d, params_count = %d, bytecodes_addr: %p\n",
        func_pt->size, func_pt->max_stack, func_pt->local_slots_count, func_pt->params_count, func_pt->bytecodes);

    fc.func = func_pt;
    fc.params = params_top_ptr;
    return fc;
}

int main() {
    test_dpu_side();
    return 0;
}
