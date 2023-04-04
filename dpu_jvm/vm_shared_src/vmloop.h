#ifndef VM_VM_LOOP_H
#define VM_VM_LOOP_H

#ifdef HOST
#include "../host_vm/object.h"
#endif

//return value register
extern __host SLOTVAL ret_val;

#if DEBUG_PRINT_ENABLE==1
#ifdef HOST
#define DEBUG_PRINT(format, ...) printf(format, ##__VA_ARGS__);
#else
#define DEBUG_PRINT(format, ...) printf(format, ##__VA_ARGS__);
#endif
#else
#define DEBUG_PRINT(format, ...) ;
#endif



#ifdef DEBUG_OUTPUT_INSN_PARSED
#define DEBUG_OUT_INSN_PARSED(X) printf("addr:0x%x op = %d, %s\n", pc - 1, code_buffer[pc - 1], X);
#else
#define DEBUG_OUT_INSN_PARSED(X) ;
#endif // DEBUG_OUTPUT_


void interp(struct function_thunk func_thunk);
#endif // !VM_VM_LOOP_H
