#ifndef VM_FUNCTION_H
#define VM_FUNCTION_H

#ifdef INMEMORY
#include <mram.h>
#endif

#include "memory.h"


struct j_method{
    u4 total_size;
    u2 access_flags;
    u2 params_count;
    u2 name_index;
    u2 max_stack;
    u2 max_locals;
    u2 retained;
    u4 code_length;
    struct j_class __mram_ptr* return_type;
    struct j_method __mram_ptr* params;
    uint8_t __mram_ptr* bytecodes;
    
};



struct function_thunk
{
    struct j_method __mram_ptr *func;
    struct j_class __mram_ptr *jc;
    uint8_t* params;
};

#endif // !VM_FUNCTION_H

