#ifndef VM_VM_LOOP_H
#define VM_VM_LOOP_H
#include <defs.h>

#if DEBUG_PRINT_ENABLE==1
// Print when flag DEBUG_PRINT_ENABLE is set
#define DEBUG_PRINT(format, ...) printf(format, ##__VA_ARGS__);
#else
// do nothing when flag DEBUG_PRINT_ENABLE is not set
#define DEBUG_PRINT(format, ...) ;
#endif


// output bytecodes information in the interpretation process when DEBUG_OUTPUT_INSN_PARSED is set.
#ifdef DEBUG_OUTPUT_INSN_PARSED
#define DEBUG_OUT_INSN_PARSED(X)  printf("pc:0x%x op = 0x%02x, %s (addr: 0x%08x)\n", pc - 1, code_buffer[pc - 1], X, &code_buffer[pc - 1]);
#else
#define DEBUG_OUT_INSN_PARSED(X) ;
#endif // DEBUG_OUTPUT_




void interp(struct function_thunk func_thunk);
#endif // !VM_VM_LOOP_H
