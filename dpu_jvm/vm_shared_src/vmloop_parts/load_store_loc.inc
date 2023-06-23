        case STLOC_0: //pop -> slot 0 
            DEBUG_OUT_INSN_PARSED("STLOC_0");
            DEBUG_PRINT(" - write %p to slot 0\n", *SLOTPT (evaluation_stack_pt - SLOTSIZE));
            POP_EVAL_STACK(*SLOTPT FRAME_GET_LOCAL_VARS(frame));

            break;
        case STLOC_1:
            DEBUG_OUT_INSN_PARSED("STLOC_1");
            DEBUG_PRINT(" - write %p to slot 1\n", *SLOTPT (evaluation_stack_pt - SLOTSIZE));
            POP_EVAL_STACK(*SLOTPT (FRAME_GET_LOCAL_VARS(frame) + SLOTSIZE));

            break;
        case STLOC_2:
            DEBUG_OUT_INSN_PARSED("STLOC_2");
            DEBUG_PRINT(" - write %p to slot 2\n", *SLOTPT (evaluation_stack_pt - SLOTSIZE));
            POP_EVAL_STACK(*SLOTPT (FRAME_GET_LOCAL_VARS(frame) + SLOTSIZE * 2));

            break;
        case STLOC_3:
            DEBUG_OUT_INSN_PARSED("STLOC_3");
            DEBUG_PRINT(" - write %p to slot 3\n", *SLOTPT (evaluation_stack_pt - SLOTSIZE));
            POP_EVAL_STACK(*SLOTPT (FRAME_GET_LOCAL_VARS(frame) + SLOTSIZE * 3));
            break;
        case STLOC_S:
            DEBUG_OUT_INSN_PARSED("STLOC_S")
            DEBUG_PRINT(" - write %p to slot %d\n", *SLOTPT (evaluation_stack_pt - SLOTSIZE), code_buffer[pc]);
            DEBUG_PRINT(" - load short int %d\n", code_buffer[pc]);

            POP_EVAL_STACK(*SLOTPT (FRAME_GET_LOCAL_VARS(frame) + SLOTSIZE * code_buffer[pc]));
            INC_PC;
            break;
        case LDLOC_0:
            DEBUG_OUT_INSN_PARSED("LDLOC_0");
            DEBUG_PRINT(" - load from slot %d val = %p\n", 0, *SLOTPT FRAME_GET_LOCAL_VARS(frame));
            PUSH_EVAL_STACK(*SLOTPT FRAME_GET_LOCAL_VARS(frame));

            break;
        case LDLOC_1:
            DEBUG_OUT_INSN_PARSED("LDLOC_1");
            DEBUG_PRINT(" - load from slot %d val = %p\n", 1, *SLOTPT (FRAME_GET_LOCAL_VARS(frame) + SLOTSIZE));
            PUSH_EVAL_STACK(*SLOTPT (FRAME_GET_LOCAL_VARS(frame) + SLOTSIZE));

            break;
        case LDLOC_2:
            DEBUG_OUT_INSN_PARSED("LDLOC_2");
            DEBUG_PRINT(" - load from slot %d val = %p\n", 2, *SLOTPT (FRAME_GET_LOCAL_VARS(frame) + SLOTSIZE * 2));
            PUSH_EVAL_STACK(*SLOTPT (FRAME_GET_LOCAL_VARS(frame) + SLOTSIZE * 2));
            break;
        case LDLOC_3:
            DEBUG_OUT_INSN_PARSED("LDLOC_3");
            DEBUG_PRINT(" - load from slot %d val = %p\n", 3, *SLOTPT (FRAME_GET_LOCAL_VARS(frame) + SLOTSIZE * 3));
            PUSH_EVAL_STACK(*SLOTPT (FRAME_GET_LOCAL_VARS(frame) + SLOTSIZE * 3));
            break;
        case LDLOC_S:
            DEBUG_OUT_INSN_PARSED("LDLOC_S");
            DEBUG_PRINT(" - load from slot %d val = %p\n", code_buffer[pc], *SLOTPT (FRAME_GET_LOCAL_VARS(frame) + SLOTSIZE * code_buffer[pc]));
            DEBUG_PRINT(" - load short int %d\n", code_buffer[pc]);
            PUSH_EVAL_STACK(*SLOTPT (FRAME_GET_LOCAL_VARS(frame) + SLOTSIZE * code_buffer[pc]));
            INC_PC;
            break;