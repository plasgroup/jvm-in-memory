// Unused

#include "ir/opcode.h"
#include <perfcounter.h>
#include <stdio.h>
#include <string.h>
#include <stdint.h>
#include "core/frame_helper.h"
#include "core/function.h"
#include "core/memory.h"
#include "core/vm_loop.h"
#include "sample_ils/test_cases_gen.h"
#include "host_vm/dpu_com.h"

////////////////////////////////////////////// HEADER//////////////////////////////////////////////////
#ifndef INMEMORY
#include <malloc.h>
#else
#include <alloc.h>
#include <mram.h>
#endif // INMEMORY



char inited = 0;
__host uint32_t nb_cycles = 0;


void test_dpu_side() {
    struct function_thunk fc;
    int i;
    uint8_t __mram_ptr* pt = mram_heap_space;
    int num = 0;
    if (inited == 0) {

        init_memory();
        inited = 1;
    }
    DEBUG_PRINT(RED " --------------------- (IN DPU) -----------------------------\n" RESET);


    fc.func = (struct function __mram_ptr*)func_pt;
    DEBUG_PRINT(RED " func - bytecodes size = 0x%x\n", fc.func->size);
    DEBUG_PRINT(" max stack = %d\n", fc.func->max_stack);
    DEBUG_PRINT(" local_slots_count = %d\n", fc.func->local_slots_count);
    DEBUG_PRINT(" params_count = %d\n", fc.func->params_count);

    //params
    for (i = 0; i < fc.func->params_count * 2; i++) {
        //it seem *(uint64_t*) will cause a problem(align?), which will return 0;
        DEBUG_PRINT(" params %d = %p\n", i, *(uint32_t __mram_ptr*)(params_buffer + i * 4));
    }

    fc.params = params_buffer + fc.func->params_count * SLOTSIZE;
    fc.func->bytecodes = (uint8_t __mram_ptr*)fc.func + sizeof(struct function);


    //DCALL 
    interp(fc);



    release_global_memory();
    DEBUG_PRINT(RED " --------------------- (END DPU) -----------------------------\n" RESET);
}

struct function_thunk read_function(uint8_t __mram_ptr* metadata, uint8_t* params_top_ptr) {
    struct function __mram_ptr* func_pt;
    struct function_thunk fc;

    func_pt = (struct function __mram_ptr*)metadata;
    func_pt->bytecodes = metadata + sizeof(struct function);

    DEBUG_PRINT("\t\t\t| func size = %d, max_stack = %d, local_var_count = %d, params_count = %d, bytecodes_addr: %p\n",
        func_pt->size, func_pt->max_stack, func_pt->local_slots_count, func_pt->params_count, func_pt->bytecodes);

    fc.func = func_pt;
    fc.params = params_top_ptr;
    return fc;

}

int main() {

    perfcounter_config(COUNT_CYCLES, true);
    test_dpu_side();
    nb_cycles = perfcounter_get();
    // DEBUG_PRINT(ORANGE "================ DPU execution end, cycles = %d, DPU time = %lf sec =================\n" RESET);
    return 0;
}
