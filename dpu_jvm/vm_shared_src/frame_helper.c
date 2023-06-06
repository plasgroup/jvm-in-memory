#include "frame_helper.h"
#include "memory.h"
#include <stdio.h>






void print_frame(uint8_t* fp, uint8_t* sp) {


#define PRINT_FIELD_ADDR(field_name) printf("\t| [addr:" RED "%p" CYAN "] (" RED field_name CYAN ") => \n", fp);


    uint8_t* loc = fp;
    int i;
    struct j_method __mram_ptr *func_pt;
    int operand_stack_len = (sp - fp - SLOTSIZE * 6) / SLOTSIZE;
    printf(CYAN "\n-------------------------------------------------------------------------------------------------------\n");

    
    PRINT_FIELD_ADDR("operand-stack-field");
    for(i = operand_stack_len - 1; i >= 0; i --){
        loc = (uint8_t*)FRAME_LOC(fp, FRAME_OFFSET_OPERAND_STACK_ELEM_PT(i));
        printf("\t\t| operand_stack element %d addr = %p, %lx\n", i, 
           loc,  *SLOTPT loc);
    }


    PRINT_FIELD_ADDR("bytecode-pointer-field");
    loc = (uint8_t*)FRAME_LOC(fp, FRAME_OFFSET_BYTECODE_PT);
    printf("\t\t| bytecode-pointer field addr = %p, %lx\n", i, 
           loc,  *SLOTPT loc);

    PRINT_FIELD_ADDR("constantpool-pointer-field");
    loc = (uint8_t*)FRAME_LOC(fp, FRAME_OFFSET_CONSTANTPOOL_PT);
    printf("\t\t| constantpool-pointer field addr = %p, %lx\n", i, 
           loc,  *SLOTPT loc);

    PRINT_FIELD_ADDR("class-pointer-field");
    loc = (uint8_t*)FRAME_LOC(fp, FRAME_OFFSET_CLASS_PT);
    printf("\t\t| class-pointer field addr = %p, %lx\n", i, 
           loc,  *SLOTPT loc);


    PRINT_FIELD_ADDR("method-pointer-field");
    loc = (uint8_t*)FRAME_LOC(fp, FRAME_OFFSET_METHOD_PT);
    printf("\t\t| method-pointer field addr = %p, %lx\n", i, 
           loc,  *SLOTPT loc);

    PRINT_FIELD_ADDR("ret-pc-field");
    loc = (uint8_t*)FRAME_LOC(fp, FRAME_OFFSET_RETPC_PT);
    printf("\t\t| ret-pc field addr = %p, %lx\n", i, 
           loc,  *SLOTPT loc);

    PRINT_FIELD_ADDR("old-sp-field");
    loc = (uint8_t*)FRAME_LOC(fp, FRAME_OFFSET_OLDSP_PT);
    printf("\t\t| old-sp field addr = %p, %lx\n", i, 
           loc,  *SLOTPT loc);


    PRINT_FIELD_ADDR("old-fp-field");
    loc = (uint8_t*)FRAME_LOC(fp, FRAME_OFFSET_OLDFP_PT);
    printf("\t\t| old-fp field addr = %p, %lx\n", i, 
           loc,  *SLOTPT loc);

    PRINT_FIELD_ADDR("locals-field");

    loc -= 4;
    for(i = 0; i > fp ; loc -= SLOTSIZE, i++){
        printf("\t\t| locals element [%d], addr = %p, %lx\n", i, 
           loc,  *SLOTPT loc);
    }
}


uint8_t* create_new_vmframe(struct function_thunk func_thunk
, uint8_t** sp_pt, uint8_t* fp, uint8_t* return_pc, uint8_t* old_sp)
{
     
 #define INC_STACK(SIZE) *sp_pt = (*sp_pt) + SIZE
     struct function __mram_ptr *func = func_thunk.func;
     uint8_t * new_fp = NULL;
     int i = 0;
     // get param count and locals count
     int locals_count = func_thunk.func->max_locals;
     int params_count = func_thunk.func->params_count;

     

     printf("\n--------------------------------- Create Frame ---------------------------------\n");
     printf("--------- Frame From (%p) ------------\n", *sp_pt);
     


     if(func_thunk.params == current_sp + 4 * params_count){
       printf(" >> create frame from an existed function call\n");
       *(sp_pt) = *(sp_pt) + sizeof(uint8_t*);
       for(i = 0; i < locals_count; i++){
              printf("(%p) ", *sp_pt);
              if(i < params_count){
                  printf("(param) ");
              }else{

              }
              printf("local %d = %d\n", i, *(u4*)*sp_pt);
              //*(uint8_t**)(*(sp_pt)) = *(uint8_t **)func_thunk.params;
              func_thunk.params += sizeof(uint8_t*);
              
              *(sp_pt) = *(sp_pt) + sizeof(uint8_t*);
       }
     }else{
       func_thunk.params -= 4 * params_count;
       for(i = 0; i < locals_count; i++){
              printf("(%p) ", *sp_pt);
              if(i < params_count){
                     printf("(param) ");
              }
              printf("local %d = %d\n", i, *(u4*)func_thunk.params);
              *(uint8_t**)(*(sp_pt)) = *(uint8_t **)func_thunk.params;
              func_thunk.params += sizeof(uint8_t*);
              *(sp_pt) = *(sp_pt) + sizeof(uint8_t*);
       }
     }
     

     //old fp
     current_fp = *sp_pt;
     *(uint32_t*)*sp_pt = fp;
     printf("(%p) --> FP = (%p) -> old-frame-fp = %p \n", *sp_pt, *sp_pt, fp);
     new_fp = *sp_pt;
     *(sp_pt) = *(sp_pt) + 4;
     

     
     //old sp
     *(uint32_t*)*sp_pt = old_sp;
     printf("(%p) old-stack-pointer = %p \n", *sp_pt, *(uint8_t**)*(sp_pt));
     *(sp_pt) = *(sp_pt) + 4;
     

     //return pc
     *(uint32_t*)*sp_pt = (uint32_t)return_pc;
     printf("(%p) return pc = 0x%x \n", *sp_pt, *(uint8_t**)*(sp_pt));
     *(sp_pt) = *(sp_pt) + 4;

     // method

     *(uint8_t**)*sp_pt = (uint8_t*)func_thunk.func;
     printf("(%p) method-ref = %p \n", *sp_pt, *(uint8_t**)*(sp_pt));
     *(sp_pt) = *(sp_pt) + 4;

     // class
     
     *(uint8_t**)*sp_pt = (uint8_t*)func_thunk.jc;
     printf("(%p) class-ref = %p \n", *sp_pt, *(uint8_t**)*(sp_pt));
     *(sp_pt) = *(sp_pt) + 4;

     // cp
     
     *(uint8_t**)*sp_pt = (uint8_t*)func_thunk.jc->items;
     printf("(%p) constant-pool-ref = %p \n", *sp_pt, *(uint8_t**)*(sp_pt)); // TODO: not the right value
     *(sp_pt) = *(sp_pt) + 4;

     // bytecode
     *(uint8_t**)*sp_pt = (uint8_t*)func_thunk.func->bytecodes;
     printf("(%p) bytecodes = %p \n", *sp_pt, *(uint8_t**)*(sp_pt));
     *(sp_pt) = *(sp_pt) + 4;

     // operand stacks;
     
     current_sp -= 4;
     printf("-------> new current_sp = %p\n", current_sp);



     
     printf("------------------------------ End Create Frame ------------------------------\n");


     
     return new_fp;



}