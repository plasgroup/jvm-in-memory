#ifndef VM_FRAME_HELPER_H
#define VM_FRAME_HELPER_H

#include <stdint.h>
#include "method.h"
#include "jclass.h"


// For colorful printing
#define CYAN    "\033[36m"      /* Cyan */
#define RESET   "\033[0m"
#define RED     "\033[31m"      /* Red */
#define ORANGE  "\033[33m"   /*orange*/


#define SLOTSIZE sizeof(uint8_t*)
#ifdef INMEMORY
#define SLOTPT (uint32_t*)
#define MSLOTP (uint32_t __mram_ptr*)
#else
#define SLOTPT (uint64_t*)
#define MSLOTP (uint64_t __mram_ptr *)
#endif


#pragma region Frame


// Indexer
#define FRAME_LOC(FP, OFFSET) (FP + OFFSET) 


#pragma region FRAME_OFFSETS
// Offsets
#define FRAME_OFFSET_OLDFP_PT 0
#define FRAME_OFFSET_LOCALS_PT(LOCAL_INDEX) (- LOCAL_INDEX * SLOTSIZE)
#define FRAME_OFFSET_OLDSP_PT SLOTSIZE
#define FRAME_OFFSET_RETPC_PT FRAME_OFFSET_OLDSP_PT + SLOTSIZE
#define FRAME_OFFSET_METHOD_PT FRAME_OFFSET_RETPC_PT + SLOTSIZE
#define FRAME_OFFSET_CLASS_PT FRAME_OFFSET_METHOD_PT + SLOTSIZE
#define FRAME_OFFSET_CONSTANTPOOL_PT FRAME_OFFSET_CLASS_PT + SLOTSIZE
#define FRAME_OFFSET_BYTECODE_PT FRAME_OFFSET_CONSTANTPOOL_PT + SLOTSIZE
#define FRAME_OFFSET_OPERAND_STACK_ELEM_PT(OPRAND_INDEX) FRAME_OFFSET_BYTECODE_PT + OPRAND_INDEX * SLOTSIZE


#pragma endregion



#pragma region FRAME_GETTER
//// Getter

#define FRAME_GET_OLDFP(FP) *(uint8_t**)FRAME_LOC(FP, FRAME_OFFSET_OLDFP_PT)
#define FRAME_GET_LOCALS(FP, LOCAL_INDEX) *(uint8_t**)FRAME_LOC(FP, FRAME_OFFSET_LOCALS_PT(LOCAL_INDEX))
#define FRAME_GET_OLDSP(FP) *(uint8_t**)FRAME_LOC(FP, FRAME_OFFSET_OLDSP_PT)
#define FRAME_GET_RETPC(FP) *(uint8_t**)FRAME_LOC(FP, FRAME_OFFSET_RETPC_PT)
#define FRAME_GET_METHOD(FP) *(struct j_method __mram_ptr**)FRAME_LOC(FP, FRAME_OFFSET_METHOD_PT)
#define FRAME_GET_CLASS(FP) *(struct j_class __mram_ptr**)FRAME_LOC(FP, FRAME_OFFSET_CLASS_PT)
#define FRAME_GET_CONSTANTPOOL(FP) *(uint8_t __mram_ptr**)FRAME_LOC(FP, FRAME_OFFSET_CONSTANTPOOL_PT)
#define FRAME_GET_BYTECODE(FP) *(uint8_t __mram_ptr**)FRAME_LOC(FP, FRAME_OFFSET_BYTECODE_PT)
#define FRAME_GET_OPERAND_STACK_ELEM(FP, OPRAND_INDEX) *(uint8_t**)FRAME_LOC(FP, FRAME_OFFSET_OPERAND_STACK_ELEM_PT(OPRAND_INDEX))


#define FRAME_GET_OPERAND_STACK_SIZE(FP, SP) ((SP - FP - 24) / 4)

#pragma endregion



//helper functions

#pragma endregion
void print_frame(uint8_t* fp, uint8_t* sp);
uint8_t* create_new_vmframe(struct function_thunk func_thunk,
                            uint8_t* return_pc);

#endif