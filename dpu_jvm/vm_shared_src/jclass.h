#ifndef JCLASS_H
#define JCLASS_H

#include "memory.h"
struct j_field{

};
struct constant_table_item{
    u4 info;
    u4 direct_value;
};
struct j_class{
    u4 total_size;
    u2 this_class_name_index;
    u2 super_class_name_index;
    struct j_class __mram_ptr* super_class;
    u2 access_flag;
    u2 cp_2b_offset;
    u4 cp_item_count;
    struct constant_table_item __mram_ptr* items;
    u4 fields_count;
    struct j_field __mram_ptr* fields;
    u4 jmethod_count;
    struct j_methods __mram_ptr* methods;
    u4 string_constant_pool_length;
    uint8_t __mram_ptr* string_constant_pool;
};

#endif
