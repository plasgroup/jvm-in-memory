        case LDC_I4_S: //load short int to stack
            DEBUG_OUT_INSN_PARSED("LDC_I4_S");
            DEBUG_PRINT(" - load short int %d\n", code_buffer[pc]);
            PUSH_EVAL_STACK((int)code_buffer[pc]);
            INC_PC;
            break;
        case LDC_I4:
            DEBUG_OUT_INSN_PARSED("LDC_I4");
            READ_INT32_BIT_BY_BIT(code_buffer + pc, op1)
            DEBUG_PRINT(" - load 32bit int = 0x%x\n", op1);
            PUSH_EVAL_STACK(op1)
            pc += 4;
            break;
        case LDC_I4_M1:
            DEBUG_OUT_INSN_PARSED("LDC_I4_0");
            DEBUG_PRINT(" - load 32bit int = 0x%x\n", -1);
            PUSH_EVAL_STACK(-1);
            break;
        case LDC_I4_0:
            DEBUG_OUT_INSN_PARSED("LDC_I4_0");
            DEBUG_PRINT(" - load 32bit int = 0x%x\n", 0);
            PUSH_EVAL_STACK(0);
            break;
        case LDC_I4_1:
            DEBUG_OUT_INSN_PARSED("LDC_I4_1");
            DEBUG_PRINT(" - load 32bit int = 0x%x\n", 1);
            PUSH_EVAL_STACK(1);
            break;
        case LDC_I4_2:
            DEBUG_OUT_INSN_PARSED("LDC_I4_2");
            DEBUG_PRINT(" - load 32bit int = 0x%x\n", 2);
            PUSH_EVAL_STACK(2);
            break;
        case LDC_I4_3:
            DEBUG_OUT_INSN_PARSED("LDC_I4_3");
            DEBUG_PRINT(" - load 32bit int = 0x%x\n", 3);
            PUSH_EVAL_STACK(3);
            break;
        case LDC_I4_4:
            DEBUG_OUT_INSN_PARSED("LDC_I4_4");
            DEBUG_PRINT(" - load 32bit int = 0x%x\n", 4);
            PUSH_EVAL_STACK(4)
            break;
        case LDC_I4_5:
            DEBUG_OUT_INSN_PARSED("LDC_I4_5");
            DEBUG_PRINT(" - load 32bit int = 0x%x\n", 5);
            PUSH_EVAL_STACK(5);
            break;
        case LDC_I4_6:
            DEBUG_OUT_INSN_PARSED("LDC_I4_6");
            DEBUG_PRINT(" - load 32bit int = 0x%x\n", 6);
            PUSH_EVAL_STACK(6)
            break;
        case LDC_I4_7:
            DEBUG_OUT_INSN_PARSED("LDC_I4_7");
            DEBUG_PRINT(" - load 32bit int = 0x%x\n", 7);
            PUSH_EVAL_STACK(7);
            break;
        case LDC_I4_8:
            DEBUG_OUT_INSN_PARSED("LDC_I4_8");
            DEBUG_PRINT(" - load 32bit int = 0x%x\n", 8);
            PUSH_EVAL_STACK(8);
            break;