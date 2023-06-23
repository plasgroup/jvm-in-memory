        case RET:
            DEBUG_OUT_INSN_PARSED("RET");
            DEBUG_PRINT(" - try ret to %p\n", FRAME_GET_RET_ADDR(frame));

            pc = FRAME_GET_RET_ADDR(frame);
            DEBUG_PRINT(" - eval_stack_begin: %p, end: %p\n", FRAME_GET_EVAL_STACK(frame), evaluation_stack_pt);

            //recover
            tmp_frame_begin = FRAME_GET_LAST_FRAME_PT(frame);

            if (FRAME_GET_LAST_FRAME_PT(frame) == 0) {
                DEBUG_PRINT(" - end of program....\n");
                if (FRAME_GET_EVAL_STACK(frame) < evaluation_stack_pt) {
                    POP_EVAL_STACK(ret_val);
                    DEBUG_PRINT(" - get ret_val: %p\n", ret_val);
                }
                DEBUG_PRINT("print local vars: \n");
                for (i = 0; i < func->local_slots_count; i++) {
                    DEBUG_PRINT("slot %d: - %p\n", i, *SLOTPT (FRAME_GET_LOCAL_VARS(frame) + SLOTSIZE * i));
                }
                printf("final frame\n");
                current_frame_top = 0;
            
                return;
            }

            if (FRAME_GET_EVAL_STACK(frame) < evaluation_stack_pt) {
                POP_EVAL_STACK(ret_val);
                DEBUG_PRINT(" - get ret_val: %p\n", ret_val);
                //write to last frame stack

                DEBUG_PRINT("%p\n", FRAME_GET_LAST_FRAME_EVAL_STACK_TOP_PT(frame));
                *SLOTPT FRAME_GET_LAST_FRAME_EVAL_STACK_TOP_PT(frame) = ret_val;
                DEBUG_PRINT(" - write ret value \n");
                evaluation_stack_pt = FRAME_GET_LAST_FRAME_EVAL_STACK_TOP_PT(frame) + sizeof(uint8_t*);
            }
            else {
                evaluation_stack_pt = FRAME_GET_LAST_FRAME_EVAL_STACK_TOP_PT(frame);
            }


            current_frame_end = frame + FRAME_GET_OFFSET_EXT_FIELD2_FRAME_SIZE(frame);

            frame = FRAME_GET_LAST_FRAME_PT(frame);
            printf("return to frame addr: %p\n", frame);
            current_frame_top = frame;

            //frame = tmp_frame_begin;
            DEBUG_PRINT("jump to last func codes's %x\n", pc);
            code_buffer = (FRAME_GET_FUNC_PT(frame))->bytecodes;
            break;
        
        case CALL:
           { 
            
           
            DEBUG_OUT_INSN_PARSED("CALL");
            //PUSH_EVAL_STACK(*SLOTPT (code_buffer + pc)); //TODO, get the real addr

            
            READ_INT32_BIT_BY_BIT(code_buffer + pc, op3);
            printf(" - get call addr - : %p\n", op3);

            #ifdef INMEMORY
                
                //currently DPU side not support multi-function. 
                //Any method call would become recursive call of current function
                
                callee.func = func;
                callee.params = evaluation_stack_pt;
            
            #else
                
                printf("new func size = %p\n", (module1->funcs[op3]).ft->func->size);
                callee.func = (module1->funcs[op3]).ft->func;
                callee.params = evaluation_stack_pt;
                for(int token_index = 0; token_index < callee.func->params_count; token_index ++){
                    DEBUG_PRINT(" - param token %d = %p\n",token_index, *((uint32_t*)(callee.func->types_tokens) + token_index));
                }
            
            #endif
            
            //create_new_vmframe from function thunk

            tmp_frame_begin = create_new_vmframe(callee, &stack_top,  // (uint8_t*)func->bytecodes + 
                pc + 4, evaluation_stack_pt - SLOTSIZE * callee.func->params_count, pc, frame);
            print_frame(tmp_frame_begin);
            evaluation_stack_pt = FRAME_GET_EVAL_STACK(tmp_frame_begin);
            
            //reset some register
            pc = 0;
            #ifdef HOST
                code_buffer = (module1->funcs[op3]).ft->func->bytecodes;
            #endif
            frame = tmp_frame_begin;
            
            DEBUG_PRINT(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n\n >>>>>>new frame>>>>>>>\n");


           } 
            break;
#ifdef HOST
         case DCALL:
         {
            DEBUG_OUT_INSN_PARSED("DCALL");
            
            READ_INT32_BIT_BY_BIT(code_buffer + pc, op3);
            printf(" - get call addr - : %p\n", op3);
            printf("new func size = %p\n", (module1->funcs[op3]).ft->func->size);

            callee.func = (module1->funcs[op3]).ft->func;
            callee.params = evaluation_stack_pt;

            // write function data to dpu
            std::vector<std::vector<uint32_t>> dpu_mram_pt(1);
            dpu_mram_pt.front().resize(1);
            dpu->copy(dpu_mram_pt, "mram_heap_pt");
            printf("dpu_mram_pt = %p\n", dpu_mram_pt[0][0]);


            
                        
            //Parameter Buffer
            std::vector<uint8_t> params = std::vector<uint8_t>();
            

            //Copy Function Data
            
            printf("current cache pt %p, dispatch addr(DPU mram): %p\n",
                 (module1->funcs[op3]).dpu_dispatch_addr, 
                dpu_mram_pt[0][0]);


            if((module1->funcs[op3]).dpu_dispatch_addr == NULL){
                
                // function metadata (size, max_stack, slot count, params_count) 
                std::vector<uint8_t> buffer((uint8_t*)(callee.func), (uint8_t*)(callee.func) + 16);
            
                if(sizeof(uint8_t*) == 8){
                    for(i = 0; i < 8; i++){
                        buffer.push_back(0);
                    }
                }

                std::vector<uint8_t> bcodes = 
                    std::vector<uint8_t>((uint8_t*)(callee.func) + sizeof(struct function),
                                         (uint8_t*)(callee.func) + sizeof(struct function) + callee.func->size);


                //Insert Bytecode
                buffer.insert(buffer.end(), bcodes.begin(), bcodes.end());
                
                (module1->funcs[op3]).dpu_dispatch_addr = dpu_mram_pt[0][0];
                dpu->copy("mram_heap_space", dpu_mram_pt[0][0], buffer, 24 + callee.func->size);
                dpu->copy( "func_pt", dpu_mram_pt);
                dpu_mram_pt[0][0] += 24 + callee.func->size;
                printf("copy %p bytes to dpu mram\n", 24 +  callee.func->size);
            }else{
                dpu_mram_pt[0][0] = (module1->funcs[op3]).dpu_dispatch_addr;
                
                dpu->copy( "func_pt", dpu_mram_pt);
                printf(" - use function cache, func_thunk in %p\n", dpu_mram_pt[0][0]);
            };
            
#define INT_TO_4BYTES_REP(V) (V >> 24) & 0b1111111, (V >> 16) & 0b1111111, (V >> 8) & 0b1111111, V & 0b1111111
            

            for(int token_index = 0; token_index < callee.func->params_count; token_index ++){
                //uint32_t tid = *((uint32_t*)(callee.func->types_tokens) + token_index);
                //uint8_t* slot_val =  *(uint8_t**)(evaluation_stack_pt + SLOTSIZE * ( token_index - callee.func->params_count));
                uint32_t tid = *((uint32_t*)(callee.func->types_tokens) + callee.func->params_count - 1 - token_index);   
                uint8_t* slot_val =  *(uint8_t**)(evaluation_stack_pt - SLOTSIZE - SLOTSIZE * token_index);
                printf(" - param token %d = %p\n",token_index, tid);
                if(tid == 0x1){ //0x1 - int array
                    printf(" -- %p is a reference, should be copied to dpu\n", slot_val );
                    //deep copy to dpu
                    printf(" --- type handler = %p, metadata id = %d\n", slot_val,
                         ((struct MethodTable*)*(uint8_t**)(evaluation_stack_pt + SLOTSIZE * ( token_index - callee.func->params_count)))->metadata_token);
                    if( ((struct MethodTable*)*SLOTPT (evaluation_stack_pt + SLOTSIZE * ( token_index - callee.func->params_count)))->metadata_token == 0x1){
                        printf("is an array, copy\n");
                    }

                    //read array len
                    int arrlen = (int)*(uint8_t**)(slot_val + sizeof(uint8_t*));
                    
                    std::vector<uint8_t> array_data = {INT_TO_4BYTES_REP(arrlen)};
                    std::vector<uint8_t> fields_data = std::vector<uint8_t>(slot_val + sizeof(uint8_t*) + sizeof(uint8_t*)
                        , slot_val + sizeof(uint8_t*) +sizeof(uint8_t*) + (int)arrlen * 4);

                    array_data.insert(array_data.end(), fields_data.begin(), fields_data.end());

                    //padding for dma copy 
                    while(array_data.size() & 0b1111111) array_data.push_back(0);
                    
#define ALIGN_SIZE8(exp) (exp) % 8 == 0 ? (exp) : ((exp) / 8 + 1) * 8

                    printf(" arr len = %p, copy to dpu addr: %p\n", arrlen, dpu_mram_pt[0][0]);
                    dpu->copy("mram_heap_space", dpu_mram_pt[0][0], array_data, ALIGN_SIZE8(4 +  (int)arrlen * 4));
                    
                    //printf("write addr: %p\n", (int)dpu_mram_pt[0][0]);
                    params.push_back((uint8_t)((int)dpu_mram_pt[0][0]) & 0b11111111);
                    params.push_back((uint8_t)((int)dpu_mram_pt[0][0]>>8) & 0b11111111);
                    params.push_back((uint8_t)((int)dpu_mram_pt[0][0]>>16) & 0b11111111);
                    params.push_back((uint8_t)((int)dpu_mram_pt[0][0]>>24) & 0b11111111);

                    dpu_mram_pt[0][0] +=  ALIGN_SIZE8(4 +  (int)arrlen * 4);
                    printf("copy %p bytes to dpu mram\n", ALIGN_SIZE8(4 +  (int)arrlen * 4));

                }else if(tid == 0x0){
                    params.push_back(((uint8_t)slot_val) & 0b11111111);
                    params.push_back(((uint8_t)slot_val >> 8) & 0b11111111);
                    params.push_back(((uint8_t)slot_val >> 16) & 0b11111111);
                    params.push_back(((uint8_t)slot_val >> 24) & 0b11111111);
                }
            }
            for(i = 0; i < callee.func->params_count * 4; i++){
                printf(" - p %p\n", params[i]);
            }
            dpu->copy("params_buffer", params,  ALIGN_SIZE8(callee.func->params_count * 4));
            
            dpu->copy("mram_heap_pt", dpu_mram_pt);
            



            std::chrono::steady_clock::time_point begin = std::chrono::steady_clock::now();
   
            //dpu execution
            dpu->exec();

            
            std::chrono::steady_clock::time_point end = std::chrono::steady_clock::now();
            dpu->log(std::cout);
            
            std::vector<std::vector<uint32_t>> nbCycles(1);
            nbCycles.front().resize(1);
            dpu->copy(nbCycles, "nb_cycles");

            std::vector<std::vector<uint32_t>> clocksPerSec(1);
            clocksPerSec.front().resize(1);
            dpu->copy(clocksPerSec, "CLOCKS_PER_SEC");
            printf(ORANGE "================ DPU execution end, cycles = %d, DPU time = %lf sec, host elapse time = %lf sec =================\n" RESET,
             nbCycles.front().front(), (double)nbCycles.front().front() / clocksPerSec.front().front(), std::chrono::duration_cast<std::chrono::nanoseconds>(end -
                                                                      begin)
                         .count() /
                     1.0e9);


            //get result
            std::vector<std::vector<uint32_t>> ret_val_data(1);
            ret_val_data.front().resize(1);
            dpu->copy(ret_val_data, "ret_val"); //read dpu return value register
            printf("result from dpu: %p\n", ret_val_data[0][0]);
            //return val and nullability
            
#ifdef HOST_DEBUG_DCALL
            //create_new_vmframe from function thunk
            tmp_frame_begin = create_new_vmframe(callee, &stack_top,  // (uint8_t*)func->bytecodes + 
                pc + 4, evaluation_stack_pt - SLOTSIZE * callee.func->params_count, pc, frame);
            print_frame(tmp_frame_begin);
            evaluation_stack_pt = FRAME_GET_EVAL_STACK(tmp_frame_begin);
            
            //reset some register
            pc = 0;
            code_buffer = (module1->funcs[op3]).ft->func->bytecodes;
            
            frame = tmp_frame_begin;
            
            DEBUG_PRINT(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n\n >>>>>>new frame>>>>>>>\n");
#endif

         }
            break;
#endif