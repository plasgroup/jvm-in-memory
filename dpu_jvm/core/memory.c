#include "memory.h"
struct memory mem;

uint8_t __mram_ptr* current_sp[TASKLET_CNT];
uint8_t __mram_ptr* current_fp[TASKLET_CNT] = {0};
uint8_t __mram_ptr* stack_top;
__host uint8_t __mram_ptr* exec_method_pt[TASKLET_CNT];
__host uint8_t __mram_ptr* exec_class_pt[TASKLET_CNT];

__host uint8_t __mram_ptr *mram_heap_pt = 0;
__host uint8_t __mram_ptr* func_pt;

__host uint8_t __mram_ptr* meta_space_pt;

__host uint8_t __mram_ptr* params_buffer_pt[TASKLET_CNT];

uint8_t __mram_ptr* evaluation_stack_pt;

#ifdef DEBUG_STACK
int debug_eval;
#endif



__dma_aligned __mram_noinit uint8_t wram_data_space[WRAM_DATA_SPACE_SIZE];
__dma_aligned __mram_noinit uint8_t m_metaspace[META_SPACE_SIZE];
__dma_aligned __mram_noinit uint8_t m_heapspace[MRAM_HEAP_SIZE];
__dma_aligned __mram_noinit uint8_t params_buffer[PARAMS_BUFFER_SIZE];

uint8_t __mram_ptr* wram_data_space_pt = wram_data_space;


struct static_fields_table __mram_ptr* sfields_table;
struct static_field_line __mram_ptr* static_var_m;

__host uint8_t* return_val;
__host int return_values[1024];


void init_memory() {
    int i;
    int this_tasklet_params_buffer_len = (PARAMS_BUFFER_SIZE / TASKLET_CNT);
    
    mem.mram_heap = (uint8_t __mram_ptr*)(m_heapspace);
    mem.wram = wram_data_space;
    mem.meta_space = m_metaspace;

    DEBUG_PRINT("param_buffer(wram)=%p, sim_wram(wram)=%p, mram = %p\n", params_buffer, mem.wram, 
        (uint8_t __mram_ptr*)((uint8_t __mram_ptr*)mem.mram_heap + (SLOTVAL) mram_heap_pt));
        
    for(i = 0; i < TASKLET_CNT; i++){
        current_sp[me()] = wram_data_space + (WRAM_DATA_SPACE_SIZE / TASKLET_CNT) * me() - 4;
    }
    stack_top = (uint8_t __mram_ptr*)mem.wram;
}




void release_global_memory() {

}
