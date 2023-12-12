#ifndef JCLASS_H
#define JCLASS_H

#include "memory.h"
struct j_field{
    // currently not implemented
};
struct constant_table_item{
    u4 info;
    u4 direct_value;
};
struct v_table_item{
    u4 classref;
    u4 methodref;
};
struct j_class{
    u4 total_size;  // the class size (bytes)
    
    u2 this_class_name_index; // the index of this class, in the constant pool of this class
    u2 super_class_name_index; // the class of this class, in the constant pool of this class
    
    struct j_class __mram_ptr* super_class; // super class pointer
    u2 access_flag;  // access flag. 
    u2 cp_2b_offset; // constant pool area offset calculate from the beginning of the class.

    u4 cp_item_count;  // the items count in constant pool
    struct constant_table_item __mram_ptr* items;  // items in constant pool
    
    u4 fields_count;  // the count of fields
    struct j_field __mram_ptr* fields; // fields
    
    u4 jmethod_count; // method count
    struct j_methods __mram_ptr* methods; // methods
    
    u4 constant_area_length;   // the size (bytes count) of constant area
    uint8_t __mram_ptr* constant_area; // constant area. contains data like UTF8 string, numerical values, ...

    /* virtual table */
    u4 virtual_table_length;
    struct v_table_item __mram_ptr* virtual_table;
};


#endif
