#include "jstruct_printer.h"
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
    printf("-- (%p) super_class_ref = %p\n", loc, *(u4 __mram_ptr*)loc);
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
        printf("---- (%p) CP item #%d: 0x%08x | 0x%08x\n",  &jc->items[i],i + 1, jc->items[i].info, jc->items[i].direct_value);
    }
    printf("-- (%p) field_count = %d\n", loc, *(u4 __mram_ptr*)loc);
    loc += 4;
    printf("-- (%p) fields_pt = %p\n", loc, *(u4 __mram_ptr*)loc);
    loc += 4;
    printf("-- (%p) jmethod_count = %d\n", loc, *(u4 __mram_ptr*)loc);
    loc += 4;
    printf("-- (%p) jmethod_pt = %p\n", loc, *(u4 __mram_ptr*)loc);
    loc += 4;
    printf("-- (%p) string_constant_area_length = %d\n", loc, *(u4 __mram_ptr*)loc);
    loc += 4;
    printf("-- (%p) constant_area_pt = %p\n", loc, *(u4 __mram_ptr*)loc);
    loc += 4;

    printf("-------------------------------------------------------------------\n");
}
