#include "memory.h"
struct memory mem;

uint8_t* current_sp = wram_data_space - 4;
uint8_t* current_fp = 0;
uint8_t* current_fbegin = 0;
uint8_t* stack_top;
__host uint8_t __mram_ptr* exec_method_pt;
__host uint8_t __mram_ptr* exec_class_pt;

__host uint8_t __mram_ptr *mram_heap_pt = 0;
__host uint8_t __mram_ptr* func_pt;

__host uint8_t __mram_ptr* meta_space_pt;

__host uint8_t* params_buffer_pt;

uint8_t* evaluation_stack_pt;

#ifdef DEBUG_STACK
int debug_eval;
#endif


#ifdef INMEMORY && ARRAY_CACHE
struct function_inline_array_buffer_cache inline_array_buffer_cache;
#endif


__dma_aligned __mram_noinit uint8_t m_heapspace[MRAM_HEAP_SIZE];
__dma_aligned __mram_noinit uint8_t m_metaspace[META_SPACE_SIZE];


__host uint8_t params_buffer[PARAMS_BUFFER_SIZE];
uint8_t wram_data_space[WRAM_DATA_SPACE_SIZE];
uint8_t wram_frames_space[WRAM_SIZE];


uint8_t* wram_data_space_pt = wram_data_space;


struct static_fields_table __mram_ptr* sfields_table;
struct static_field_line __mram_ptr* static_var_m;

__host uint8_t* return_val;

void init_memory() {
    int i;

#ifdef INMEMORY
    buddy_init(1024 * 4);
#else
#endif // INMEMORY

    mem.mram_heap = (uint8_t __mram_ptr*)(m_heapspace);
    mem.wram = wram_frames_space;
    mem.meta_space = m_metaspace;

#ifdef INMEMORY && ARRAY_CACHE
    DEBUG_PRINT("%x\n", ARRAY_CACHE_ITEM_COUNT * ARRAY_CACHE_LINE_SIZE);
    for (i = 0; i < ARRAY_CACHE_ITEM_COUNT; i++) {
        inline_array_buffer_cache.cache_lines[i].array = (uint8_t*)0xFFFFFFFF;
    }
#endif
    
    printf("param_buffer(wram)=%p, sim_wram(wram)=%p, mram = %p\n", params_buffer, mem.wram, 
        (uint8_t __mram_ptr*)((uint8_t __mram_ptr*)mem.mram_heap + (SLOTVAL) mram_heap_pt));

    //mram_heap_pt = (uint8_t __mram_ptr *)(mem.mram_heap) + (SLOTVAL) (mram_heap_pt);
    //meta_space_pt = m_metaspace;
    stack_top = (uint8_t*)mem.wram;
    //params_buffer_pt = params_buffer;

}




void release_global_memory() {
#ifdef INMEMORY
    buddy_free(params_buffer);
    buddy_free(mem.wram);
#else
#endif
}
