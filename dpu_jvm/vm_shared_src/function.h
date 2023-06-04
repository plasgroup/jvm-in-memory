#ifndef VM_FUNCTION_H
#define VM_FUNCTION_H

#ifdef INMEMORY
#include <mram.h>
#endif

#include "memory.h"

#define u2 uint16_t;
#define u4 uint32_t;
#define ref_type uint8_t __mram_ptr*

struct j_method{
    u4 total_size;
    u2 access_flags;
    ref_type return_type;
    u2 params_count;
    ref_type params; // type_refs
    u2 name_index;
    u2 max_stack;
    u2 max_locals;
    u4 code_length;
    ref_type bytecodes;

};

struct function {
    int size;
    int max_stack;
    int local_slots_count; 
    int params_count;
    uint8_t __mram_ptr* bytecodes;
    uint8_t __mram_ptr* types_tokens;

#ifdef INMEMORY
   // STRUCT_PADDING(4);
#endif // INMEMORY
};

struct function_thunk
{
    struct j_method __mram_ptr *func;
    uint8_t* params;
};

#endif // !VM_FUNCTION_H
