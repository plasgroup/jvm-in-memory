#ifndef VM_FUNCTION_H
#define VM_FUNCTION_H

#ifdef INMEMORY
#include <mram.h>
#endif

#include "memory.h"

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
    struct function __mram_ptr *func;
    uint8_t* params;
};

#endif // !VM_FUNCTION_H
