#include "jstruct_printer.h"

void print_class(struct j_class __mram_ptr* jc){
    uint8_t __mram_ptr* loc = (uint8_t __mram_ptr*) jc;
    int i = 0;

    DEBUG_PRINT("-------------------------------------------------------------------\n");
    DEBUG_PRINT("-- JClass Addr = %p\n", jc);
    DEBUG_PRINT("-- (%p) total_size = %d\n", loc,*(u4 __mram_ptr*)loc);
    loc += 4;
    DEBUG_PRINT("-- (%p) this_class_index = %d\n", loc, *(u2 __mram_ptr*)loc);
    loc += 2;
    DEBUG_PRINT("-- (%p) super_class_index = %d\n", loc, *(u2 __mram_ptr*)loc);
    loc += 2;
    DEBUG_PRINT("-- (%p) super_class_ref = %p\n", loc, *(u4 __mram_ptr*)loc);
    loc += sizeof(uint8_t __mram_ptr*);
    
    DEBUG_PRINT("-- (%p) access_flags = 0x%04x\n", loc, *(u2 __mram_ptr*)loc);
    loc += 2;
    DEBUG_PRINT("-- (%p) cp_2b_offset = %d\n", loc, *(u2 __mram_ptr*)loc);
    loc += 2;
    DEBUG_PRINT("-- (%p) constant_table_items_count = %d\n", loc, *(u4 __mram_ptr*)loc);
    loc += 4;
    DEBUG_PRINT("-- (%p) constant_table_ref = %p\n", loc, (struct constant_table_item __mram_ptr*)*(u4 __mram_ptr*)loc);
    loc += sizeof(struct constant_table_item __mram_ptr*);
    for(i = 1; i < jc->cp_item_count; i++){
        DEBUG_PRINT("---- (%p) CP item #%d: 0x%08x | 0x%08x\n",  &jc->items[i],i, jc->items[i].info, jc->items[i].direct_value);
    }
    DEBUG_PRINT("-- (%p) field_count = %d\n", loc, *(u4 __mram_ptr*)loc);
    loc += 4;
    DEBUG_PRINT("-- (%p) fields_pt = %p\n", loc, *(u4 __mram_ptr*)loc);
    loc += 4;
    DEBUG_PRINT("-- (%p) jmethod_count = %d\n", loc, *(u4 __mram_ptr*)loc);
    loc += 4;
    DEBUG_PRINT("-- (%p) jmethod_pt = %p\n", loc, *(u4 __mram_ptr*)loc);
    loc += 4;
    DEBUG_PRINT("-- (%p) string_constant_area_length = %d\n", loc, *(u4 __mram_ptr*)loc);
    loc += 4;
    DEBUG_PRINT("-- (%p) constant_area_pt = %p\n", loc, *(u4 __mram_ptr*)loc);
    loc += 4;
    DEBUG_PRINT("-- (%p) virtual_table_length = %d\n", loc, *(u4 __mram_ptr*)loc);
    loc += 4;
    DEBUG_PRINT("-- (%p) virtual_table_pt = %p\n", loc, *(u4 __mram_ptr*)loc);
    loc += 4;

    DEBUG_PRINT("-------------------------------------------------------------------\n");
}



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
    // bytecodes
    for(i = 0; i < jm->code_length; i++){
        DEBUG_PRINT("---- (%p) bytecode[%d] = 0x%02x\n", loc, i,
                *(uint8_t __mram_ptr*)loc);
        loc++;
    }
    DEBUG_PRINT("-------------------------------------------------------------------\n");
}