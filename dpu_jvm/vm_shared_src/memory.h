#ifndef VM_MEMORY_H
#define VM_MEMORY_H
#include <stdint.h>

#ifdef INMEMORY
#include <alloc.h>
#include <mram.h>
#else
#include <malloc.h>
#endif // INMEMORY

#define RESET   "\033[0m"
#define RED     "\033[31m"      /* Red */
#define DEBUG_STACK
#define STRUCT_PADDING(X) uint8_t padding[X]


#ifdef HOST
#define __mram_ptr
#define __host
#define __mram_noinit
#define __mram__
#define SLOTVAL uint64_t
#define REGVAL SLOTVAL



#else
#define SLOTVAL uint32_t
#define REGVAL SLOTVAL
#endif

#pragma region Eval_Stack

#ifdef DEBUG_STACK
extern int debug_eval;
#define LOG_STACK_PUSH(X) printf(RED "\t[Push %d]\n" RESET, X);
#define LOG_STACK_POP(X) printf(RED "\t[Pop %d]\n" RESET, X);
#endif

#define INC_EVAL_STACK evaluation_stack_pt += SLOTSIZE;
#define DESC_EVAL_STACK evaluation_stack_pt -= SLOTSIZE;
#define REF_EVAL_STACK_CURRENT_SLOT *SLOTPT (evaluation_stack_pt)


#define READ_INT32_BIT_BY_BIT(ADDR, REG) \
	REG = 0; \
	REG |= *(uint8_t __mram_ptr *)(ADDR); \
    REG |= (*(uint8_t __mram_ptr *)((ADDR) + 1) << 8); \
    REG |= ( *(uint8_t __mram_ptr *)((ADDR) + 2) << 16); \
    REG |= ( *(uint8_t __mram_ptr *)((ADDR) + 3) << 24);

#define GET_CLASSSTRUT(OBJREF, OUTVAR) \
    OUTVAR = *(uint32_t __mram_ptr) OBJREF

#ifdef LOG_STACK_POP_EVENT
#define PUSH_EVAL_STACK(X) \
      debug_eval = X; \
      LOG_STACK_PUSH(debug_eval); \
      REF_EVAL_STACK_CURRENT_SLOT = debug_eval; \
      INC_EVAL_STACK
#else
#define PUSH_EVAL_STACK(X) \
      REF_EVAL_STACK_CURRENT_SLOT = X; \
      INC_EVAL_STACK 
#endif





#define EVAL_STACK_TOPSLOT_VALUE *SLOTPT (evaluation_stack_pt - SLOTSIZE)


#ifdef LOG_STACK_POP_EVENT
#define POP_EVAL_STACK(X) \
      debug_eval = EVAL_STACK_TOPSLOT_VALUE; \
      LOG_STACK_POP(debug_eval); \
      X = debug_eval; \
      DESC_EVAL_STACK
#else
#define POP_EVAL_STACK(X) \
    X = EVAL_STACK_TOPSLOT_VALUE; \
    DESC_EVAL_STACK
#endif

#pragma endregion





struct memory {
    uint8_t __mram_ptr *mram_heap;
    uint8_t __mram_ptr* meta_space;
    uint8_t __mram_ptr* method_space;
    uint8_t* wram;

};

#define WRAM_SIZE (8 * 1024)
#define MRAM_HEAP_SIZE (16 * 1024 * 1024)
#define PARAMS_BUFFER_SIZE (4 * 1024)
#define WRAM_DATA_SPACE_SIZE (4 * 1024)
#define META_SPACE_SIZE (4 * 1024)
#define METHOD_SPACE_SIZE (4 * 1024)


extern struct memory mem;


extern uint8_t* params_buffer_pt;

extern uint8_t* stack_top;

extern __host uint8_t __mram_ptr *mram_heap_pt;
extern __host uint8_t __mram_ptr *func_pt;
extern __host uint8_t __mram_ptr* meta_space_pt;
extern __host uint8_t __mram_ptr* method_space_pt;


extern uint8_t* evaluation_stack_pt;

extern uint8_t* current_frame_end;
extern uint8_t* current_frame_top;


extern uint8_t __mram_noinit mram_heap_space[MRAM_HEAP_SIZE];
extern uint8_t __mram_noinit mram_meta_space[META_SPACE_SIZE];
extern uint8_t __mram_noinit mram_method_space[METHOD_SPACE_SIZE];


extern __host uint8_t wram_data_space[WRAM_DATA_SPACE_SIZE];
extern __host uint8_t wram_frames_space[WRAM_SIZE];
extern __host uint8_t params_buffer[PARAMS_BUFFER_SIZE];

extern struct static_fields_table __mram_ptr* sfields_table;
extern struct static_field_line __mram_ptr* static_var_m;


#define ALIGN_8BYTE(ADDR) (ADDR + 7) & -8
#define MRAM_HEAP_ALLOC_STRUT(TYPE) \
                                    (TYPE __mram_ptr *)mram_heap_pt; \
                                    mram_heap_pt += sizeof(TYPE); \

#define MRAM_HEAP_ALLOC_S(S) \       
                                    mram_heap_pt; \
                                    mram_heap_pt += S; \

#define MRAM_METHODSPACE_ALLOC_STRUT(TYPE) \ 
                                    (TYPE __mram_ptr *)method_space_pt; \
                                    method_space_pt += sizeof(TYPE); \

#define MRAM_METHODSPACE_ALLOC_S(S) \ 
                                    method_space_pt; \
                                    method_space_pt += S; \

#define MRAM_METASPACE_ALLOC_STRUT(TYPE) \ 
                                    (TYPE __mram_ptr *)meta_space_pt; \
                                    meta_space_pt += sizeof(TYPE); \

#define MRAM_METASPACE_ALLOC_S(S) \ 
                                    meta_space_pt; \
                                    meta_space_pt += S; \


#pragma region array_buffer_for_scrtachpad(unuse)
#define SET_DIRTY(CLine, bit2) *(uint8_t*)(Cline + 32 + 16) = bit2
#define ARRAY_CACHE_SIZE (1024)
#define ARRAY_CACHE_SEGMENT_BITS (32 - 10)
#define ARRAY_CACHE_ITEM_COUNT 3
#define ARRAY_CACHE_DIRTY_BITS 2
#define ARRAY_CACHE_LINE_SIZE (32 + ARRAY_CACHE_SEGMENT_BITS + ARRAY_CACHE_DIRTY_BITS + ARRAY_CACHE_SIZE)


struct array_buffer_cache_item {
    uint8_t* array;
    int state;
    uint8_t* page_num;
#ifdef INMEMORY
    __dma_aligned
#endif
    uint8_t* buffer;
};

struct function_inline_array_buffer_cache {
    struct array_buffer_cache_item cache_lines[3];
};


extern struct function_inline_array_buffer_cache inline_array_buffer_cache;

#pragma endregion


extern uint8_t* wram_data_space_pt;

void init_memory();
void release_global_memory();



struct static_field_line {
    uint32_t type_token;
    SLOTVAL value;
};

struct static_fields_table {
    uint32_t length;
    struct static_field_line __mram_ptr* lines;
};


#endif // !VM_MEMORY_H
