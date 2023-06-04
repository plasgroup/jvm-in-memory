#ifndef JCLASS_H
#define JCLASS_H
struct j_field{

}

struct j_class{
    uint16_t class_name_index;
    uint8_t* this_class;
    uint8_t* super_class;
    uint16_t access_flag;
    int cp_item_count;
    constant_table_item* items;
    int fields_count;
    j_field* fields;
    int jmethod_count;
    j_methods* methods;
    int string_constant_pool_length;
    uint8_t* string_constant_pool;
}

#endif
