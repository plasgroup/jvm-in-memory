        case BRFALSE_S:
            DEBUG_OUT_INSN_PARSED("BRFALSE_S")
            DEBUG_PRINT(" - load short int %d\n", code_buffer[pc]);
            POP_EVAL_STACK(op1)
                if (op1 == 0) {
                    DEBUG_PRINT("false, jump to %d\n", code_buffer[pc]);
                    pc = code_buffer[pc];
                }
                else {
                    INC_PC;
                }
            break;
        case BRTRUE_S:
            DEBUG_OUT_INSN_PARSED("BRTRUE_S");
            DEBUG_PRINT(" - load short int %d\n", code_buffer[pc]);
            POP_EVAL_STACK(op1)
                if (op1) 
                    pc = code_buffer[pc];
                else 
                    INC_PC;
            break;
        case BR_S:
            DEBUG_OUT_INSN_PARSED("BR_S");
            DEBUG_PRINT(" - load short int %d\n", code_buffer[pc]);
            pc = code_buffer[pc];
            break;