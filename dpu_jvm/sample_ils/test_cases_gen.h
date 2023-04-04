#ifndef TEST_IL_TEST_CASES_GEN_H
#define TEST_IL_TEST_CASES_GEN_H

#include "../vm_shared_src/function.h"
#include "../vm_shared_src/frame_helper.h"

#ifdef HOST
#include <cstring>
#else
#endif

//struct function_thunk cons_func();
struct function_thunk cons_func1_variable_def();
struct function_thunk cons_func2_fact_calc_recursive();
struct function_thunk cons_func3_loop();
struct function_thunk cons_func4_arr();
struct function_thunk cons_func_large_arr();
struct function_thunk cons_func_gen_arr();
struct function_thunk cons_func_arr_mul();
struct function_thunk cons_func_dpu_init_array1();
struct function_thunk cons_func_arr_mul2();
struct function_thunk cons_func_arr_mul2_static();
struct function_thunk cons_func_dpu_init_array2();
struct function_thunk cons_func_test_dpu_mul2();
struct function_thunk jvm_cons_func_ret();
#endif // !TEST_IL_TEST_CASES_GEN_H
