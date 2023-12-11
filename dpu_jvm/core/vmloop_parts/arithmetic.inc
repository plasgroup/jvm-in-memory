        case ADD:
            DEBUG_OUT_INSN_PARSED("ADD")
                POP_EVAL_STACK(op2)
                POP_EVAL_STACK(op1)
                DEBUG_PRINT(" - add %d + %d = %d\n", op1, op2, op1 + op2);
            PUSH_EVAL_STACK(op1 + op2)
                break;
        case SUB:
            DEBUG_OUT_INSN_PARSED("SUB")
                POP_EVAL_STACK(op2)
                POP_EVAL_STACK(op1)
                DEBUG_PRINT(" - sub %d - %d = %d\n", op1, op2, op1 - op2);
            PUSH_EVAL_STACK(op1 - op2)
                break;
        case MUL:
            DEBUG_OUT_INSN_PARSED("MUL")
                POP_EVAL_STACK(op2)
                POP_EVAL_STACK(op1)
                PUSH_EVAL_STACK(op1 * op2)
                DEBUG_PRINT(" - mul %d and %d = %d\n", op1, op2, op1 * op2);
            break;
        case DIV:
            DEBUG_OUT_INSN_PARSED("DIV")
                POP_EVAL_STACK(op2)
                POP_EVAL_STACK(op1)
                PUSH_EVAL_STACK(op1 / op2)
                break;