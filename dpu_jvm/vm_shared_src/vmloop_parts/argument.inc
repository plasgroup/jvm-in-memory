case LDARG_0:
            DEBUG_OUT_INSN_PARSED("LDARG_0");
            DEBUG_PRINT("load arg 0 = %p\n", *SLOTPT (FRAME_GET_PARAMS(frame)));
            PUSH_EVAL_STACK(*SLOTPT (FRAME_GET_PARAMS(frame)))

            break;
case LDARG_1:
            DEBUG_OUT_INSN_PARSED("LDARG_1");
            DEBUG_PRINT("load arg 1 = %p\n", *SLOTPT (SLOTPT (FRAME_GET_PARAMS(frame)) + 1));
            PUSH_EVAL_STACK(*SLOTPT (SLOTPT (FRAME_GET_PARAMS(frame)) + 1))
            break;
case LDARG_2:
            DEBUG_OUT_INSN_PARSED("LDARG_2");
            DEBUG_PRINT("load arg 2 = %p\n", *SLOTPT (SLOTPT (FRAME_GET_PARAMS(frame)) + 2));
            PUSH_EVAL_STACK(*SLOTPT (SLOTPT (FRAME_GET_PARAMS(frame)) + 2))
            break;
case LDARG_3:
            DEBUG_OUT_INSN_PARSED("LDARG_3");
            DEBUG_PRINT("load arg 3 = %p\n", *SLOTPT (SLOTPT (FRAME_GET_PARAMS(frame)) + 3));
            PUSH_EVAL_STACK(*SLOTPT (SLOTPT (FRAME_GET_PARAMS(frame)) + 3))
            break;
case LDARG_S:
            DEBUG_OUT_INSN_PARSED("LDARG_S");
            DEBUG_PRINT(" - load short int %p\n", code_buffer[pc]);
            DEBUG_PRINT("load arg loc = %p\n", *SLOTPT (SLOTPT (FRAME_GET_PARAMS(frame)) + 3));
            PUSH_EVAL_STACK(*SLOTPT (SLOTPT (FRAME_GET_PARAMS(frame)) + (int)code_buffer[pc]));
            INC_PC;
            break;


            