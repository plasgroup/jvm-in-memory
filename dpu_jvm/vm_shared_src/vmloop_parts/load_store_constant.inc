        case BIPUSH: //load byte int to stack
            DEBUG_OUT_INSN_PARSED("BIPUSH");
            DEBUG_PRINT(" - load 1 byte int %d\n", code_buffer[pc]);
            PUSH_EVAL_STACK((int)code_buffer[pc]);
            INC_PC;
            break;
        case SIPUSH:
            DEBUG_OUT_INSN_PARSED("BIPUSH");
            op1 = 0;
            op1 |= code_buffer[pc] << 8;
            INC_PC;
            op1 |= code_buffer[pc];
            DEBUG_PRINT(" - load 2 byte int %d\n", op1);
            PUSH_EVAL_STACK((int)op1);
            break;
        case ICONST_M1:
            DEBUG_OUT_INSN_PARSED("ICONST_M1");
            DEBUG_PRINT(" - load 32bit int = 0x%x\n", -1);
            PUSH_EVAL_STACK(-1);
            break;
        case ICONST_0:
            DEBUG_OUT_INSN_PARSED("ICONST_0");
            DEBUG_PRINT(" - load 32bit int = 0x%x\n", 0);
            PUSH_EVAL_STACK(0);
            break;
        case ICONST_1:
            DEBUG_OUT_INSN_PARSED("ICONST_1");
            DEBUG_PRINT(" - load 32bit int = 0x%x\n", 1);
            PUSH_EVAL_STACK(1);
            break;
        case ICONST_2:
            DEBUG_OUT_INSN_PARSED("ICONST_2");
            DEBUG_PRINT(" - load 32bit int = 0x%x\n", 2);
            PUSH_EVAL_STACK(2);
            break;
        case ICONST_3:
            DEBUG_OUT_INSN_PARSED("ICONST_3");
            DEBUG_PRINT(" - load 32bit int = 0x%x\n", 3);
            PUSH_EVAL_STACK(3);
            break;
        case ICONST_4:
            DEBUG_OUT_INSN_PARSED("ICONST_4");
            DEBUG_PRINT(" - load 32bit int = 0x%x\n", 4);
            PUSH_EVAL_STACK(4);
            break;
        case ICONST_5:
            DEBUG_OUT_INSN_PARSED("ICONST_5");
            DEBUG_PRINT(" - load 32bit int = 0x%x\n", 5);
            PUSH_EVAL_STACK(5);
            break;
        case LCONST_0:
            DEBUG_OUT_INSN_PARSED("LCONST_0");
            DEBUG_PRINT(" - load 64bit int = 0x%x\n", 0);
            PUSH_EVAL_STACK(0);
            PUSH_EVAL_STACK(0);
            break;
        case LCONST_1:
            DEBUG_OUT_INSN_PARSED("LCONST_1");
            DEBUG_PRINT(" - load 64bit int = 0x%x\n", 1);
            PUSH_EVAL_STACK(0);
            PUSH_EVAL_STACK(1);
            break;
      