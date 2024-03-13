#include "frame_helper.h"
#include "memory.h"
#include <stdio.h>

// frame printer
void print_frame(uint8_t __mram_ptr* fp, uint8_t __mram_ptr* sp) {
#define PRINT_FIELD_ADDR(field_name) DEBUG_PRINT("\t| [addr:" RED "%p" CYAN "] (" RED field_name CYAN ") => \n", fp);
    uint8_t __mram_ptr* loc = fp;
    int i;
    struct j_method __mram_ptr *func_pt;
    int operand_stack_len = (sp - fp - SLOTSIZE * 6) / SLOTSIZE;
    DEBUG_PRINT(CYAN "\n-------------------------------------------------------------------------------------------------------\n");

    
    PRINT_FIELD_ADDR("operand-stack-field");
    for(i = operand_stack_len - 1; i >= 0; i --){
        loc = (uint8_t __mram_ptr*)FRAME_LOC(fp, FRAME_OFFSET_OPERAND_STACK_ELEM_PT(i));
        DEBUG_PRINT("\t\t| operand_stack element %d addr = %p, %lx\n", i, 
           loc,  *SLOTPT loc);
    }


    PRINT_FIELD_ADDR("bytecode-pointer-field");
    loc = (uint8_t __mram_ptr*)FRAME_LOC(fp, FRAME_OFFSET_BYTECODE_PT);
    DEBUG_PRINT("\t\t| bytecode-pointer field addr = %p, %lx\n", i, 
           loc,  *SLOTPT loc);

    PRINT_FIELD_ADDR("constantpool-pointer-field");
    loc = (uint8_t __mram_ptr*)FRAME_LOC(fp, FRAME_OFFSET_CONSTANTPOOL_PT);
    DEBUG_PRINT("\t\t| constantpool-pointer field addr = %p, %lx\n", i, 
           loc,  *SLOTPT loc);

    PRINT_FIELD_ADDR("class-pointer-field");
    loc = (uint8_t __mram_ptr*)FRAME_LOC(fp, FRAME_OFFSET_CLASS_PT);
    DEBUG_PRINT("\t\t| class-pointer field addr = %p, %lx\n", i, 
           loc,  *SLOTPT loc);


    PRINT_FIELD_ADDR("method-pointer-field");
    loc = (uint8_t __mram_ptr*)FRAME_LOC(fp, FRAME_OFFSET_METHOD_PT);
    DEBUG_PRINT("\t\t| method-pointer field addr = %p, %lx\n", i, 
           loc,  *SLOTPT loc);

    PRINT_FIELD_ADDR("ret-pc-field");
    loc = (uint8_t __mram_ptr*)FRAME_LOC(fp, FRAME_OFFSET_RETPC_PT);
    DEBUG_PRINT("\t\t| ret-pc field addr = %p, %lx\n", i, 
           loc,  *SLOTPT loc);

    PRINT_FIELD_ADDR("old-sp-field");
    loc = (uint8_t __mram_ptr*)FRAME_LOC(fp, FRAME_OFFSET_OLDSP_PT);
    DEBUG_PRINT("\t\t| old-sp field addr = %p, %lx\n", i, 
           loc,  *SLOTPT loc);


    PRINT_FIELD_ADDR("old-fp-field");
    loc = (uint8_t __mram_ptr*)FRAME_LOC(fp, FRAME_OFFSET_OLDFP_PT);
    DEBUG_PRINT("\t\t| old-fp field addr = %p, %lx\n", i, 
           loc,  *SLOTPT loc);

    PRINT_FIELD_ADDR("locals-field");

    loc -= 4;
    for(i = 0; i > fp ; loc -= SLOTSIZE, i++){
        DEBUG_PRINT("\t\t| locals element [%d], addr = %p, %lx\n", i, 
           loc,  *SLOTPT loc);
    }
}




// create new frame
uint8_t __mram_ptr* create_new_vmframe(struct function_thunk func_thunk, uint8_t* return_pc)
{

// Two macros for increase or decrease current SP
#define INC_SP(S) current_sp[me()] += S;
#define DEC_SP(S) current_sp[me()] -= S;


// function structure and FP, SP
     struct function __mram_ptr *func = func_thunk.func;
     uint8_t __mram_ptr* fp = current_fp[me()];
     uint8_t __mram_ptr* sp = current_sp[me()];
     int i = 0;
     int locals_count = func_thunk.func->max_locals;
     int params_count = func_thunk.func->params_count;

     DEBUG_PRINT("\n--------------------------------- Create Frame ---------------------------------\n");
     DEBUG_PRINT("--------- [%d] Frame from (%p) ------------\n", me(), current_sp[me()]);
      
     // if it is not the first frame, we do need push arguments to frame, just increase the SP.
     // presume currently has k frames, and this method want to create the (k+1)-frame.
     // the arguemnts already exists in evaluation stack of k-th frame (in the top of it),
     // aruguments area is in the bottom of a frame, so we can directly increase the SP with size(arguments)
     // On the other hand, if the frame we want to create is the initial frame. We need push arguements to 
     // it from the parameter buffer specified by the func_thunk.params

     if(func_thunk.params == current_sp[me()] + 4 * params_count){
       DEBUG_PRINT(" >> create frame from an existed function call\n");
       INC_SP(sizeof(uint8_t __mram_ptr*))
       for(i = 0; i < locals_count; i++){
              // In the  condition that no the creation of initial frame, just increase the SP. we not need 
              // any MRAM store operation.
              DEBUG_PRINT("(%p) ", current_sp[me()]);
              if(i < params_count){
              // the first m (m < total locals count, m = arguments count) locals are the arguments.
                  DEBUG_PRINT("(param) ");
              }

              DEBUG_PRINT("local %d = %d\n", i, *(u4 __mram_ptr*)current_sp[me()]);
              func_thunk.params += sizeof(uint8_t __mram_ptr*);
              
              INC_SP(sizeof(uint8_t __mram_ptr*))
       }
     }else{
       func_thunk.params -= 4 * params_count;
       DEBUG_PRINT("%p params_count = %d\n", func_thunk.params, params_count);
       for(i = 0; i < locals_count; i++){
              DEBUG_PRINT("(%p) ", current_sp[me()]);
              if(i < params_count){
                     DEBUG_PRINT("(param) ");
              }
              
              DEBUG_PRINT("local %d = %d (addr(p): 0x%08x)\n", i, *(u4 __mram_ptr*)func_thunk.params, func_thunk.params);
             
              // push an argument to the stack
              *(uint8_t __mram_ptr* __mram_ptr*)current_sp[me()] = (uint8_t __mram_ptr*)*(uint32_t __mram_ptr*)func_thunk.params;
              func_thunk.params += sizeof(uint8_t __mram_ptr*);
              INC_SP(sizeof(uint8_t __mram_ptr*))
       }
     }

/*

SP -> 
      _____ operand stack[k] ______
                 ....
      _____ operand stack[0] ______
      _____ pointer to bytecodes ______
      _____ cp         ______
      _____ class      ______
      _____ method     ______
      _____ return pc  ______
      _____ old sp     ______
FP -> _____ old fp     ______
      _____ local m    ______
               ....
      _____ local 1    ______ (params 1)
      _____ local 0    ______ (params 0)
---------------------------------------------
      _____ operand stack[b] ______
                .....
      _____ operand stack[0] ______
      _____ pointer to bytecodes ______
      _____ cp         ______
      _____ class      ______
      _____ method     ______
      _____ return pc  ______
      _____ old sp     ______
      _____ old fp     ______
      _____ local a    ______
               ....
      _____ local 1    ______ (params 1)
      _____ local 0    ______ (params 0)
*/


     DEBUG_PRINT("-------> new current_sp = %p\n", current_sp[me()]);
   
     //old fp
     *(uint32_t __mram_ptr*)current_sp[me()] = current_fp[me()];
     current_fp[me()] = current_sp[me()];
     DEBUG_PRINT("(%p) --> FP = (%p) -> old-frame-fp = %p \n", current_sp[me()], current_sp[me()], fp);
     fp = current_sp[me()];
     INC_SP(sizeof(uint8_t __mram_ptr*))
     
     //old sp
     *(uint32_t __mram_ptr*)current_sp[me()] = sp;
     DEBUG_PRINT("(%p) old-stack-pointer = %p \n", current_sp[me()], *(uint8_t __mram_ptr* __mram_ptr*)current_sp[me()]);
     INC_SP(sizeof(uint8_t __mram_ptr*))

     //return pc
     *(uint32_t __mram_ptr*)current_sp[me()] = (uint32_t)return_pc;
     DEBUG_PRINT("(%p) return pc = 0x%x \n", current_sp[me()], *(uint8_t __mram_ptr* __mram_ptr*)current_sp[me()]);
     INC_SP(sizeof(uint8_t __mram_ptr*))
     
     // method
     *(uint8_t __mram_ptr* __mram_ptr*)current_sp[me()] = (uint8_t __mram_ptr*)func_thunk.func;
     DEBUG_PRINT("(%p) method-ref = %p \n", current_sp[me()], *(uint8_t __mram_ptr* __mram_ptr*)current_sp[me()]);
     INC_SP(sizeof(uint8_t __mram_ptr*))

     // class
     *(uint8_t __mram_ptr* __mram_ptr*)current_sp[me()] = (uint8_t __mram_ptr*)func_thunk.jc;
     DEBUG_PRINT("(%p) class-ref = %p \n", current_sp[me()], *(uint8_t __mram_ptr* __mram_ptr*)current_sp[me()]);
     INC_SP(sizeof(uint8_t __mram_ptr*))

     // cp
     *(uint8_t __mram_ptr* __mram_ptr*)current_sp[me()] = (uint8_t __mram_ptr*)func_thunk.jc->items;
     DEBUG_PRINT("(%p) constant-pool-ref = %p \n", current_sp[me()], *(uint8_t __mram_ptr* __mram_ptr*)current_sp[me()]); // TODO: not the right value
     INC_SP(sizeof(uint8_t __mram_ptr*))

     // bytecode
     *(uint8_t __mram_ptr* __mram_ptr*)current_sp[me()] = (uint8_t __mram_ptr*)func_thunk.func->bytecodes;
     DEBUG_PRINT("(%p) bytecodes = %p \n", current_sp[me()], *(uint8_t __mram_ptr* __mram_ptr*)current_sp[me()]);
     INC_SP(sizeof(uint8_t __mram_ptr*))


     // operand stacks
     current_sp[me()] -= 4;
    
     DEBUG_PRINT("------------------------------ End Create Frame ------------------------------\n");
     
     return fp;
}