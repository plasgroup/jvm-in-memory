#include "frame_helper.h"
#include "memory.h"
#include <stdio.h>






void print_frame(uint8_t __mram_ptr* fp, uint8_t __mram_ptr* sp) {


#define PRINT_FIELD_ADDR(field_name) printf("\t| [addr:" RED "%p" CYAN "] (" RED field_name CYAN ") => \n", fp);


    uint8_t __mram_ptr* loc = fp;
    int i;
    struct j_method __mram_ptr *func_pt;
    int operand_stack_len = (sp - fp - SLOTSIZE * 6) / SLOTSIZE;
    printf(CYAN "\n-------------------------------------------------------------------------------------------------------\n");

    
    PRINT_FIELD_ADDR("operand-stack-field");
    for(i = operand_stack_len - 1; i >= 0; i --){
        loc = (uint8_t __mram_ptr*)FRAME_LOC(fp, FRAME_OFFSET_OPERAND_STACK_ELEM_PT(i));
        printf("\t\t| operand_stack element %d addr = %p, %lx\n", i, 
           loc,  *SLOTPT loc);
    }


    PRINT_FIELD_ADDR("bytecode-pointer-field");
    loc = (uint8_t __mram_ptr*)FRAME_LOC(fp, FRAME_OFFSET_BYTECODE_PT);
    printf("\t\t| bytecode-pointer field addr = %p, %lx\n", i, 
           loc,  *SLOTPT loc);

    PRINT_FIELD_ADDR("constantpool-pointer-field");
    loc = (uint8_t __mram_ptr*)FRAME_LOC(fp, FRAME_OFFSET_CONSTANTPOOL_PT);
    printf("\t\t| constantpool-pointer field addr = %p, %lx\n", i, 
           loc,  *SLOTPT loc);

    PRINT_FIELD_ADDR("class-pointer-field");
    loc = (uint8_t __mram_ptr*)FRAME_LOC(fp, FRAME_OFFSET_CLASS_PT);
    printf("\t\t| class-pointer field addr = %p, %lx\n", i, 
           loc,  *SLOTPT loc);


    PRINT_FIELD_ADDR("method-pointer-field");
    loc = (uint8_t __mram_ptr*)FRAME_LOC(fp, FRAME_OFFSET_METHOD_PT);
    printf("\t\t| method-pointer field addr = %p, %lx\n", i, 
           loc,  *SLOTPT loc);

    PRINT_FIELD_ADDR("ret-pc-field");
    loc = (uint8_t __mram_ptr*)FRAME_LOC(fp, FRAME_OFFSET_RETPC_PT);
    printf("\t\t| ret-pc field addr = %p, %lx\n", i, 
           loc,  *SLOTPT loc);

    PRINT_FIELD_ADDR("old-sp-field");
    loc = (uint8_t __mram_ptr*)FRAME_LOC(fp, FRAME_OFFSET_OLDSP_PT);
    printf("\t\t| old-sp field addr = %p, %lx\n", i, 
           loc,  *SLOTPT loc);


    PRINT_FIELD_ADDR("old-fp-field");
    loc = (uint8_t __mram_ptr*)FRAME_LOC(fp, FRAME_OFFSET_OLDFP_PT);
    printf("\t\t| old-fp field addr = %p, %lx\n", i, 
           loc,  *SLOTPT loc);

    PRINT_FIELD_ADDR("locals-field");

    loc -= 4;
    for(i = 0; i > fp ; loc -= SLOTSIZE, i++){
        printf("\t\t| locals element [%d], addr = %p, %lx\n", i, 
           loc,  *SLOTPT loc);
    }
    // printf("\n-------------------------------------------------------------------------------------------------------\n");
    // printf(RESET);

    // func_pt = (struct function __mram_ptr *)FRAME_GET_FUNC_PT(frame);
    // printf("\t\t| strut function addr = %p, %lx\n", FRAME_GET_FUNC_PT(frame), *SLOTPT fp);
    // printf("\t\t\t| func size = %d, max_stack = %d, local_var_count = %d, params_count = %d, bytecodes_addr: %p\n",
    //     func_pt->size, func_pt->max_stack, func_pt->local_slots_count, func_pt->params_count, func_pt->bytecodes);


    // fp = FRAME_LOC(frame, FRAME_OFFSET_LAST_FRAME_PT);
    // PRINT_FIELD_ADDR("last-frame-begin-field");
    // printf("\t\t| last-frame-begin = %p\n", *(uint8_t**)(fp));


    // fp = FRAME_LOC(frame, FRAME_OFFSET_LAST_FRAME_EVAL_STACK_TOP_PT);
    // PRINT_FIELD_ADDR("last-frame-eval-stack-top-pt-field");

    // printf("\t\t| last-frame-eval-stack-top-pt = %p\n", *(uint8_t**)(fp));


    // fp = FRAME_LOC(frame, FRAME_OFFSET_EXT_FIELD1_LOCAL_VARS_OFFSET);
    // PRINT_FIELD_ADDR("offsets-field");
    // printf("\t\t| local_var_loc_offset = %d, should from %p\n", *(short*)(fp), frame + *(short*)(fp));
    // fp = FRAME_LOC(frame, FRAME_OFFSET_EXT_FIELD1_SAVED_REG_OFFSET);
    // printf("\t\t| save_reg_offset = %d, should from %p\n", *(short*)(fp), frame + *(short*)(fp));
    // fp = FRAME_LOC(frame, FRAME_OFFSET_EXT_FIELD2_FRAME_SIZE);
    // printf("\t\t| frame_size = %d, should end at %p\n", *(short*)(fp), frame + *(short*)(fp));
    // fp = FRAME_LOC(frame, FRAME_OFFSET_EXT_FIELD2_RETAIN1);
    // printf("\t\t| retain_1 = %d\n", *(short*)(fp));

    // fp = FRAME_LOC(frame, FRAME_OFFSET_PARAMS);
    // PRINT_FIELD_ADDR("params-field");
    // for (i = 0; i < func_pt->params_count; i++) {
    //     printf("\t\t| Param %d: %p\n", i, *SLOTPT fp);
    //     fp += SLOTSIZE;
    // }

    // fp = FRAME_LOC(frame, FRAME_OFFSET_LOCAL_VARS(frame));
    // PRINT_FIELD_ADDR("local-vars-field");
    // for (i = 0; i < func_pt->local_slots_count; i++) {
    //     printf("\t\t| loc var %d: %d\n", i, *SLOTPT fp);
    //     fp += SLOTSIZE;
    // }


    // fp = FRAME_LOC(frame, FRAME_OFFSET_EVAL_STACK(frame));
    // PRINT_FIELD_ADDR("eval-stack-field");
    // for (i = 0; i < func_pt->max_stack; i++) {
    //     printf("\t\t| index %d: %d\n", i, *SLOTPT fp);
    //     fp += SLOTSIZE;
    // }
    // printf("\t\ttop\n");
    // fp = FRAME_LOC(frame, FRAME_OFFSET_RETURN_ADDR(frame));
    // PRINT_FIELD_ADDR("return-addr-field");
    // printf("\t\t| return addr:  %d\n", *SLOTPT fp);
    // printf("\n-------------------------------------------------------------------------------------------------------\n");
    // printf(RESET);

}


uint8_t* create_new_vmframe(struct function_thunk func_thunk
, uint8_t** sp_pt, uint8_t* fp, uint8_t* return_addr, uint8_t* last_frame_eval_stack_top, int pc, uint8_t* current_vm_frame,
      uint8_t* method_pt, uint8_t* class_pt, uint8_t* constant_pool_pt,  uint8_t* bytecode_pointer_pt) {
 #define INC_STACK(SIZE) *sp_pt = (*sp_pt) + SIZE

     struct function __mram_ptr *func = func_thunk.func;
     uint8_t* new_fp = NULL:
     uint8_t* old_sp = *sp_pt;
     // get param count and locals count
     int locals_count = 0;


     *(sp_pt) = *(sp_pt) + SLOTSIZE * locals_count;

     //old fp
     *(sp_pt) = *(sp_pt) + 4;
     *(uint8_t __mram_ptr**)sp_pt = fp;
     new_fp = *sp_pt;

     //old sp
     *(sp_pt) = *(sp_pt) + 4;
     *(uint8_t __mram_ptr**)sp_pt = old_sp;

     // return pc
     *(sp_pt) = *(sp_pt) + 4;
     *(uint8_t __mram_ptr**)sp_pt = return_addr;

     //method
     *(sp_pt) = *(sp_pt) + 4;
     *(uint8_t __mram_ptr**)sp_pt = method_pt;

     // class
     *(sp_pt) = *(sp_pt) + 4;
     *(uint8_t __mram_ptr**)sp_pt = class_pt;

       // cp
     *(sp_pt) = *(sp_pt) + 4;
     *(uint8_t __mram_ptr**)sp_pt = bytecode_pointer_pt;

     return fp;




//     uint8_t* frame = current_frame_end;
//     uint8_t* params_pt = func_thunk.params;
//     int i;
//     short offset = 0;
//     printf("create frame from addr 0x%p, func bytecode size = %p\n", *stack_top_pt, func->size);


//     (*(struct function __mram_ptr **)(*stack_top_pt)) = func_thunk.func;
//     INC_STACK(sizeof(struct function __mram_ptr *));
//     offset += sizeof(struct function __mram_ptr *);

//     printf(" -- wrote function metadata\n");

    
//     //last frame begin
//     (*((uint8_t**)(*stack_top_pt))) = current_frame_top;
//     FRAME_GET_LAST_FRAME_PT(frame) = current_frame_top;

//     printf(" -- write last frame begin\n");
//     INC_STACK(sizeof(uint8_t*));
//     offset += sizeof(uint8_t*);

    

//     //last frame eval stack top
//     (*((uint8_t**)(*stack_top_pt))) = last_frame_eval_stack_top;
//     FRAME_GET_LAST_FRAME_EVAL_STACK_TOP_PT(frame) = last_frame_eval_stack_top;
//     INC_STACK(sizeof(uint8_t*));
//     offset += sizeof(uint8_t*);
//     printf(" -- write last frame eval stack top\n");
    

//     //two extend field
//     printf("write from %p\n", *stack_top_pt);
// #define EXT_FIELD_SIZE 8
//     FRAME_GET_OFFSET_EXT_FIELD1_LOCAL_VARS_OFFSET(frame) = (short)(offset + EXT_FIELD_SIZE + SLOTSIZE * func->params_count);
//     FRAME_GET_OFFSET_EXT_FIELD1_SAVED_REG_OFFSET(frame) = (short)(offset + EXT_FIELD_SIZE + SLOTSIZE * func->params_count + SLOTSIZE * func->local_slots_count);
//     INC_STACK(sizeof(short) * 4);
//     offset += sizeof(short) * 4;

//     printf(" -- write offset\n");

    
//     //Params space
//     for (i = 0; i < func->params_count; i++) {

//         params_pt -= SLOTSIZE;

//         *(SLOTPT (*stack_top_pt)) = *SLOTPT params_pt;
//         printf(" > put param: %p to stack. offset %d/%p\n", *SLOTPT params_pt, offset, (uint8_t*)(FRAME_OFFSET_PARAMS));
//         INC_STACK(SLOTSIZE);
//         offset += SLOTSIZE;

//     }
//     printf(" -- wrote params\n");
    

//     //Local variable space
//     //frame->local_slots_begin = *stack_top_pt;
//     INC_STACK(SLOTSIZE * func->local_slots_count);
//     offset += SLOTSIZE * func->local_slots_count;
//     printf(" -- wrote local slots\n");

    

//     INC_STACK(SLOTSIZE * func->max_stack);
//     offset += SLOTSIZE * func->max_stack;

//     printf(" -- wrote stack\n");

    
//     *((void**)*stack_top_pt) = return_addr;
//     printf(" -- wrote ret\n");
    
//     INC_STACK(sizeof(void*));
//     offset += sizeof(void*);
//     *(short*)FRAME_LOC(frame, FRAME_OFFSET_EXT_FIELD2_FRAME_SIZE) = (short)(offset);

//     printf(" -- wrote offsets\n");
    
//     current_frame_top = current_frame_end;
//     current_frame_end = *stack_top_pt;
//     printf("%p\n", current_frame_top);
    //return current_frame_top;
}