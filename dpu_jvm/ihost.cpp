
#include <iostream>
#include <iomanip>

//#include "ir/opcode.h"
//#include "vm_shared_src/frame_helper.h"
//#include "vm_shared_src/function.h"
//#include "vm_shared_src/memory.h"
//#include "vm_shared_src/vm_loop.h"
//#include "sample_ils/test_cases_gen.h"
//#include "host_vm/object.h"

struct Module* module1;

void test_dpu_side() {
   // struct function_thunk fc;
    int i;
    int num = 0;
    //init_memory();

    //fc = cons_func_large_arr();

    //uint8_t* pt = mram_heap_pt;

    //interp(fc);

    //// Print the result array
    //for (i = 0; i < 40; i++) {
    //    num = 0;

    //    READ_INT32_BIT_BY_BIT(pt, num);
    //    printf("%d\n", num);
    //    pt += 4;
    //}

    //release_global_memory();

}

//void create_module(function_thunk* funcs, int size) {
//    module1 = MRAM_METASPACE_ALLOC_STRUT(struct Module);
//    module1->func_count = size;
//    module1->funcs = (struct FunctionADDRMapTableItem*)((uint8_t*)module1 + sizeof(struct Module));
//    MRAM_METASPACE_ALLOC_S(sizeof(struct FunctionADDRMapTableItem) * (size));
//    for (int i = 0; i < size; i++) {
//        *(struct function_thunk**)((uint8_t*)module1->funcs + i * sizeof(struct FunctionADDRMapTableItem)) = &funcs[i];
//    }
//}

int main(void) {
//    struct function_thunk fc;
//    int i;
//    int num = 0;
//
//    init_memory();
//
//    function_thunk funcs[7] = { cons_func_gen_arr(), cons_func_arr_mul(),
//        cons_func_dpu_init_array1(), cons_func_arr_mul2(),
//        cons_func_test_dpu_mul2(), cons_func_dpu_init_array2(), cons_func_arr_mul2_static()
//    };
//
//    create_module(funcs, 7);
//
//    struct function_thunk f1 = *(module1->funcs)[4].ft;
//    printf("size = %p\n", f1.func->size);
//
//
//
//    //try {
//
//    // interpretation
//    interp(f1);
//
//
//
//    printf("ret val = %p\n", ret_val);
//
//
//    if (ret_val == 0) {
//        return;
//    }
//#pragma region TEMP_TEST_AREA
//    // print the array after finishing interpretation.
//#define READ_ELEM_I4(REF, I) \
//        printf(" - %d\n", *(int*)((uint8_t*)REF + sizeof(uint8_t*) * 2 + 4 * I));
//    for (int k = 0; k < 20; k++) {
//        READ_ELEM_I4(ret_val, k);
//    }
//#pragma endregion
//
//
//
//    //}
//    /*catch (const DpuError& e) {
//        std::cerr << e.what() << std::endl;
//    }*/
//
//
//    return 0;
}


