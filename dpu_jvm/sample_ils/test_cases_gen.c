// #include <stdint.h>
// #include "test_cases_gen.h"
// #include "../ir/opcode.h"
// #include "../ir/bytecode.h"
// #include "../vm_shared_src/memory.h"

// /*
// ����1��ʹ��DMA�Ļ���Ҫע��alignment
// wram��mram�ĵ�ַת���ǳ� ��䤳����

// */

// #define PUSH_PARAM(X) *SLOTPT params_buffer_pt = X; \
// 					params_buffer_pt += SLOTSIZE; \

// struct function_thunk cons_func(int max_stack, int func_size, int local_slots_count, uint8_t* bytecodes, int params_count, int* params,\
// 	uint32_t* param_type_tokens) {
// 	struct function __mram_ptr *foo;
// 	struct function_thunk fc;
// 	uint8_t __mram_ptr* bytecodes_begin;
// 	uint8_t __mram_ptr* param_tokens_begin;
// 	int i;

// 	// | func | bytecodes | params tokens
// 	foo = MRAM_METASPACE_ALLOC_STRUT(struct function);
// 	bytecodes_begin = meta_space_pt;
// 	param_tokens_begin = bytecodes_begin + func_size;
// 	foo->size = func_size;
// 	foo->bytecodes = bytecodes_begin;
// 	foo->max_stack = max_stack;
// 	foo->local_slots_count = local_slots_count;
// 	foo->params_count = params_count;
// 	foo->types_tokens = param_tokens_begin;


// 	MRAM_METASPACE_ALLOC_S(func_size + sizeof(uint32_t) * foo->params_count);

// #ifdef INMEMORY
// 	mram_write(bytecodes, bytecodes_begin, func_size);
// #else
// 	memcpy(bytecodes_begin, bytecodes, func_size);
// #endif // !INMEMORY


	

// 	//write param_type_tokens if the ptr is not NULL.
// 	if (params_count != 0 && param_type_tokens != NULL) {
// 		printf(" ------------------------ write tokens\n");
// 		for (int tid = 0; tid < params_count; tid++) {
// 			printf(" - %p\n", param_type_tokens[tid]);
// 		}

// #ifdef INMEMORY
// 		mram_write(param_type_tokens, param_tokens_begin, sizeof(uint32_t) * params_count);
// #else
// 		memcpy(param_tokens_begin, param_type_tokens, sizeof(uint32_t) * params_count);
// #endif // !INMEMORY

// 	}


// 	//put params
// 	if (params != NULL) {
// 		for (i = 0; i < params_count; i++) {
// 			PUSH_PARAM(params[i]);
// 		}
// 	}
	

// 	fc.func = foo;
// 	fc.params = params_buffer_pt;
// 	return fc;
// }

// struct function_thunk cons_func1_variable_def() {
// 	struct function_thunk fc;
	
//     uint8_t bytecodes[0x38] = {
// 		#include "test.il"
// 	};


// 	fc = cons_func(3, 0x38, 6, bytecodes, 0, 0, 0);
// 	return fc;
// }
// struct function_thunk cons_func2_fact_calc_recursive() {
// 	struct function_thunk fc;
// 	int params[1] = { 10 };
// 	uint32_t tokens[1] = {0x0};

// 	uint8_t bytecodes[0x20] = {
// 		#include "fact.il"
// 	};

// 	fc = cons_func(3, 0x20, 2, bytecodes, 1, params, tokens);
// 	return fc;
// }
// struct function_thunk cons_func3_loop() {
// 	struct function_thunk fc;

// 	int params[1] = { 10 };

// 	uint32_t tokens[1] = {0x0};
// 	uint8_t bytecodes[0x40] = {
// 		#include "loop.il"
// 	};

// 	fc = cons_func(3, 0x40, 6, bytecodes, 1, params, tokens);
// 	return fc;


// }


// struct function_thunk cons_func4_arr() {
// 	struct function_thunk fc;

// 	uint8_t bytecodes[0x98] = {
// 		#include "array.il"
// 	};

// 	fc = cons_func(5, 0x98, 12, bytecodes, 0, 0, 0);
// 	return fc;
// }


// struct function_thunk cons_func_large_arr() {

// 	struct function_thunk fc;

// 	//int params[..] = {...}

// 	uint8_t bytecodes[160] = {

// NOP,  //IL_0000
// LDC_I4, 0x14,0x00, 0x0, 0,  //IL_0001
// NEWARR, 0, 0, 0, 0,  //IL_0006
// STLOC_0,  //IL_000b
// LDC_I4, 0x14,0x00, 0x0,0x0,  //IL_000c
// NEWARR, 0, 0, 0, 0,  //IL_0011
// STLOC_1,  //IL_0016
// LDC_I4_0,  //IL_0017
// STLOC_2,  //IL_0018
// BR_S, 0x25,  //IL_0019
// NOP,  //IL_001b
// LDLOC_0,  //IL_001c
// LDLOC_2,  //IL_001d
// LDLOC_2,  //IL_001e
// STELEM_I4,  //IL_001f
// NOP,  //IL_0020
// LDLOC_2,  //IL_0021
// LDC_I4_1,  //IL_0022
// ADD,  //IL_0023
// STLOC_2,  //IL_0024
// LDLOC_2,  //IL_0025
// LDLOC_0,  //IL_0026
// LDLEN,  //IL_0027
// CONV_I4,  //IL_0028
// 0xfe, CLT,  //IL_0029
// STLOC_3,  //IL_002b
// LDLOC_3,  //IL_002c
// BRTRUE_S, 0x1b,  //IL_002d
// LDC_I4_0,  //IL_002f
// STLOC_S, 4,  //IL_0030
// BR_S, 0x44,  //IL_0032
// NOP,  //IL_0034
// LDLOC_1,  //IL_0035
// LDLOC_S, 4,  //IL_0036
// LDC_I4_5,  //IL_0038
// LDLOC_S, 4,  //IL_0039
// ADD,  //IL_003b
// STELEM_I4,  //IL_003c
// NOP,  //IL_003d
// LDLOC_S, 4,  //IL_003e
// LDC_I4_1,  //IL_0040
// ADD,  //IL_0041
// STLOC_S, 4,  //IL_0042
// LDLOC_S, 4,  //IL_0044
// LDLOC_1,  //IL_0046
// LDLEN,  //IL_0047
// CONV_I4,  //IL_0048
// 0xfe, CLT,  //IL_0049
// STLOC_S, 5,  //IL_004b
// LDLOC_S, 5,  //IL_004d
// BRTRUE_S, 0x34,  //IL_004f

// LDC_I4_0,  //IL_0051
// STLOC_S, 6,  //IL_0052
// BR_S, 0x6b,  //IL_0054
// NOP,  //IL_0056
// LDLOC_0,  //IL_0057
// LDLOC_S, 6,  //IL_0058
// LDLOC_0,  //IL_005a
// LDLOC_S, 6,  //IL_005b
// LDELEM_I4,  //IL_005d
// LDLOC_1,  //IL_005e
// LDLOC_S, 6,  //IL_005f
// LDELEM_I4,  //IL_0061
// MUL,  //IL_0062
// STELEM_I4,  //IL_0063
// NOP,  //IL_0064
// LDLOC_S, 6,  //IL_0065
// LDC_I4_1,  //IL_0067
// ADD,  //IL_0068
// STLOC_S, 6,  //IL_0069
// LDLOC_S, 6,  //IL_006b
// LDLOC_1,  //IL_006d
// LDLEN,  //IL_006e
// CONV_I4,  //IL_006f
// 0xfe, CLT,  //IL_0070
// STLOC_S, 7,  //IL_0072
// LDLOC_S, 7,  //IL_0074
// BRTRUE_S, 0x56,  //IL_0076
// LDLOC_0,  //IL_0078
// STLOC_S, 8,  //IL_0079
// BR_S, 0x7d,  //IL_007b
// LDLOC_S, 8,  //IL_007d
// RET  //IL_007f


// 	};

// 	fc = cons_func(5, 128, 9, bytecodes, 0, 0, 0);

// 	//params 0

// 	return fc;

// }


// struct function_thunk cons_func_gen_arr() {

// 	struct function_thunk fc;

// 	//int params[..] = {...}

// 	uint8_t bytecodes[128] = {

// NOP,  //IL_0000
// LDC_I4, 0x14,0,0x0,0x0,  //IL_0001
// NEWARR, 0, 0, 0x0, 0,  //IL_0006
// STLOC_0,  //IL_000b
// LDC_I4, 0x14,0,0x0,0x0,  //IL_000c
// NEWARR, 0, 0, 0, 0,  //IL_0011

// STLOC_1,  //IL_0016
// LDC_I4_0,  //IL_0017
// STLOC_2,  //IL_0018
// BR_S, 0x25,  //IL_0019
// NOP,  //IL_001b
// LDLOC_0,  //IL_001c
// LDLOC_2,  //IL_001d
// LDLOC_2,  //IL_001e
// STELEM_I4,  //IL_001f
// NOP,  //IL_0020
// LDLOC_2,  //IL_0021
// LDC_I4_1,  //IL_0022
// ADD,  //IL_0023
// STLOC_2,  //IL_0024
// LDLOC_2,  //IL_0025

// LDLOC_0,  //IL_0026
// LDLEN,  //IL_0027
// CONV_I4,  //IL_0028
// 0xFE, CLT,  //IL_0029
// STLOC_3,  //IL_002b
// LDLOC_3,  //IL_002c
// BRTRUE_S, 0x1b,  //IL_002d
// LDC_I4_0,  //IL_002f
// STLOC_S, 4,  //IL_0030
// BR_S, 0x44,  //IL_0032
// NOP,  //IL_0034
// LDLOC_1,  //IL_0035
// LDLOC_S, 4,  //IL_0036
// LDC_I4_5,  //IL_0038
// LDLOC_S, 4,  //IL_0039
// ADD,  //IL_003b
// STELEM_I4,  //IL_003c
// NOP,  //IL_003d
// LDLOC_S, 4,  //IL_003e
// LDC_I4_1,  //IL_0040
// ADD,  //IL_0041
// STLOC_S, 4,  //IL_0042
// LDLOC_S, 4,  //IL_0044
// LDLOC_1,  //IL_0046
// LDLEN,  //IL_0047
// CONV_I4,  //IL_0048
// 0xFE, CLT,  //IL_0049
// STLOC_S, 5,  //IL_004b
// LDLOC_S, 5,  //IL_004d
// BRTRUE_S, 0x34,  //IL_004f
// LDLOC_0,  //IL_0051
// //LDLOC_1,  //IL_0052
// DCALL, 2, 0, 0, 0,  //IL_0053 call
// STLOC_S, 6,  //IL_0058
// LDLOC_1,  //IL_005a
// DCALL, 3, 0, 0, 0, //5b
// LDLOC_1,  //IL_005a
// DCALL, 3, 0, 0, 0, //5b
// BR_S, 0x67,  //IL_0060
// LDLOC_S, 6,  //IL_0062
// RET  //IL_005e

// 	};

// 	fc = cons_func(4, 128, 7, bytecodes, 0, 0, 0);

// 	//params 0

// 	return fc;

// }


// struct function_thunk cons_func_dpu_init_array1() {
// 	struct function_thunk fc;
// 	uint32_t tokens[1] = { 0x1 };
// 	uint8_t bytecodes[8] = {
// 		NOP,
// 		LDARG_0,
// 		STSFLD, 0x0, 0, 0, 0x4, //sfield 1
// 		RET
// 	};
// 	fc = cons_func(8, 8, 0, bytecodes, 1, 0, tokens);

// 	//params 1

// 	return fc;
// }


// //init two array b and c to DPU
// struct function_thunk cons_func_dpu_init_array2() {
// 	struct function_thunk fc;
// 	uint32_t tokens[2] = { 0x1, 0x1 };
// 	uint8_t bytecodes[24] = {
// 		NOP,
// 		LDARG_0,
// 		STSFLD, 0x1, 0, 0, 0x4, //sfield 1 dpu::b
// 		LDARG_1,
// 		STSFLD, 0x2, 0, 0, 0x4, //sfield 2 dpu::c
// 		RET
// 	};

// 	fc = cons_func(8, 24, 0, bytecodes, 2, 0, tokens);
	
// 	// params 1
// 	return fc;
// }

// struct function_thunk cons_func_test_dpu_mul2() {

// 	struct function_thunk fc;

// 	//int params[..] = {...}

// 	uint8_t bytecodes[128] = {

// NOP,  //IL_0000
// LDC_I4, 0x14,0x0,0x0,0x0,  //IL_0001
// NEWARR, 0, 0, 0, 0,  //IL_0006
// STLOC_0,  //IL_000b
// LDC_I4, 0x14,0x0,0x0,0x0,  //IL_000c
// NEWARR, 0, 0, 0, 0,  //IL_0011
// STLOC_1,  //IL_0016
// LDC_I4_0,  //IL_0017
// STLOC_2,  //IL_0018
// BR_S, 0x25,  //IL_0019
// NOP,  //IL_001b
// LDLOC_0,  //IL_001c
// LDLOC_2,  //IL_001d
// LDLOC_2,  //IL_001e
// STELEM_I4,  //IL_001f
// NOP,  //IL_0020
// LDLOC_2,  //IL_0021
// LDC_I4_1,  //IL_0022
// ADD,  //IL_0023
// STLOC_2,  //IL_0024
// LDLOC_2,  //IL_0025
// LDLOC_0,  //IL_0026
// LDLEN,  //IL_0027
// CONV_I4,  //IL_0028
// 0xFE, CLT,  //IL_0029
// STLOC_3,  //IL_002b
// LDLOC_3,  //IL_002c
// BRTRUE_S, 0x1b,  //IL_002d
// LDC_I4_0,  //IL_002f
// STLOC_S, 4,  //IL_0030
// BR_S, 0x44,  //IL_0032
// NOP,  //IL_0034
// LDLOC_1,  //IL_0035
// LDLOC_S, 4,  //IL_0036
// LDC_I4_5,  //IL_0038
// LDLOC_S, 4,  //IL_0039
// ADD,  //IL_003b
// STELEM_I4,  //IL_003c
// NOP,  //IL_003d
// LDLOC_S, 4,  //IL_003e
// LDC_I4_1,  //IL_0040
// ADD,  //IL_0041
// STLOC_S, 4,  //IL_0042
// LDLOC_S, 4,  //IL_0044
// LDLOC_1,  //IL_0046
// LDLEN,  //IL_0047
// CONV_I4,  //IL_0048
// 0xFE, CLT,  //IL_0049
// STLOC_S, 5,  //IL_004b
// LDLOC_S, 5,  //IL_004d
// BRTRUE_S, 0x34,  //IL_004f
// LDLOC_0,  //IL_0051
// LDLOC_1,  //IL_0052
// DCALL, 5, 0, 0, 0,  //IL_0053 //init
// NOP,  //IL_0058
// DCALL, 6, 0, 0, 0,  //IL_0059
// NOP,  //IL_005e
// DCALL, 6, 0, 0, 0,  //IL_005f
// NOP,  //IL_0064
// DCALL, 6, 0, 0, 0,  //IL_0065
// NOP,  //IL_006a
// DCALL, 6, 0, 0, 0,  //IL_006b
// NOP,  //IL_0070
// DCALL, 6, 0, 0, 0,  //IL_0071
// NOP,  //IL_0076
// RET  //IL_0077


// 	};

// 	fc = cons_func(4, 120, 6, bytecodes, 0, 0, 0);

// 	//params 0

// 	return fc;

// }

// // multiply two arrays provided by parameters.
// struct function_thunk cons_func_arr_mul() {

// 	struct function_thunk fc;


// 	//int params[..] = {...}

// 	uint32_t tokens[2] = {0x1, 0x1};
// 	uint8_t bytecodes[40] = {

// NOP,  //IL_0000
// LDC_I4_0,  //IL_0001
// STLOC_0,  //IL_0002
// BR_S, 0x15,  //IL_0003
// NOP,  //IL_0005
// LDARG_0,  //IL_0006
// LDLOC_0,  //IL_0007
// LDARG_0,  //IL_0008
// LDLOC_0,  //IL_0009
// LDELEM_I4,  //IL_000a
// LDARG_1,  //IL_000b
// LDLOC_0,  //IL_000c
// LDELEM_I4,  //IL_000d
// MUL,  //IL_000e
// STELEM_I4,  //IL_000f
// NOP,  //IL_0010
// LDLOC_0,  //IL_0011
// LDC_I4_1,  //IL_0012
// ADD,  //IL_0013
// STLOC_0,  //IL_0014
// LDLOC_0,  //IL_0015
// LDARG_1,  //IL_0016
// LDLEN,  //IL_0017
// CONV_I4,  //IL_0018
// 0xFE, CLT,  //IL_0019
// STLOC_1,  //IL_001b
// LDLOC_1,  //IL_001c
// BRTRUE_S, 0x5,  //IL_001d
// LDARG_0,  //IL_001f
// STLOC_2,  //IL_0020
// BR_S, 0x23,  //IL_0021
// LDLOC_2,  //IL_0023
// RET  //IL_0024


// 	};

// 	fc = cons_func(5, 40, 3, bytecodes, 2, 0, tokens);

// 	//params 2

// 	return fc;

// }

// // multiply a static array a and an array provide by the parameter.
// struct function_thunk cons_func_arr_mul2() {

// 	struct function_thunk fc;


// 	//int params[..] = {...}

// 	uint32_t tokens[1] = { 0x1 };
// 	uint8_t bytecodes[56] = {
// 		NOP, //0
// 		LDC_I4_0, //1
// 		STLOC_0, //2
// 		BR_S, 0x1d, //3
// 		NOP, //5
// 		LDSFLD, 0,0,0,4, //6
// 		LDLOC_0,  //b
// 		LDSFLD, 0,0,0,4, //c
// 		LDLOC_0, //11
// 		LDELEM_I4, //12
// 		LDARG_0, //13
// 		LDLOC_0, //14
// 		LDELEM_I4, //15
// 		MUL, //16
// 		STELEM_I4, //17
// 		NOP, //18
// 		LDLOC_0, //19
// 		LDC_I4_1, //1a
// 		ADD, //1b
// 		STLOC_0, //1c
// 		LDLOC_0, //1d
// 		LDARG_0, //1e
// 		LDLEN,   //1f
// 		CONV_I4, //20
// 		0xFE, CLT,     //21
// 		STLOC_1,  //23
// 		LDLOC_1, //24
// 		BRTRUE_S, 0x5, //25
// 		LDSFLD, 0,0,0,4, //27
// 		STLOC_2, //2c
// 		BR_S, 0x2F, //2d
// 		LDLOC_2,  //2f
// 		RET  //30
// 	};

// 	fc = cons_func(5, 56, 3, bytecodes, 1, 0, tokens);

// 	//params 2

// 	return fc;
// }



// // multiply two static array b and c.
// struct function_thunk cons_func_arr_mul2_static() {

// 	struct function_thunk fc;


// 	//int params[..] = {...}

// 	//uint32_t tokens[1] = { 0x1 };
// 	uint8_t bytecodes[56] = {
// 		NOP, //0
// 		LDC_I4_0, //1
// 		STLOC_0, //2
// 		BR_S, 0x21, //3
// 		NOP, //5
// 		LDSFLD, 1,0,0,4, //6  dpu::b
// 		LDLOC_0,  //b
// 		LDSFLD, 1,0,0,4, //c  dpu::b
// 		LDLOC_0, //11
// 		LDELEM_I4, //12
// 		LDSFLD, 2,0,0,4, //13 dpu::c
// 		LDLOC_0, //18
// 		LDELEM_I4, //19
// 		MUL, //1a
// 		STELEM_I4, //1b
// 		NOP, //1c
// 		LDLOC_0, //1d
// 		LDC_I4_1, //1e
// 		ADD, //1f
// 		STLOC_0, //20
// 		LDLOC_0, //21
// 		LDSFLD, 1,0,0,4, //22
// 		LDLEN,   //27
// 		CONV_I4, //28
// 		0xFE, CLT,     //29
// 		STLOC_1,  //2b
// 		LDLOC_1,  //2c
// 		BRTRUE_S, 0x5, //2d
// 		RET  //2f
// 	};

// 	fc = cons_func(5, 56, 2, bytecodes, 0, 0, 0);

// 	//params 2

// 	return fc;
// }



// // multiply two static array b and c.
// struct function_thunk jvm_cons_func_ret() {

// 	struct function_thunk fc;


// 	//int params[..] = {...}

// 	//{return; }
	
// 	//uint32_t tokens[1] = { 0x1 };
// 	uint8_t bytecodes[4] = {
// 		IRETURN
// 	};

// 	fc = cons_func(0, 4, 0, bytecodes, 0, 0, 0);

// 	//params 0

// 	return fc;
// }
