            case CEQ:
                DEBUG_OUT_INSN_PARSED("[ext] CEQ")
                    POP_EVAL_STACK(op2)
                    POP_EVAL_STACK(op1)
                    PUSH_EVAL_STACK(op1 == op2 ? 1 : 0)
                    DEBUG_PRINT(" - cmp %d, %d, put %d\n", op2, op1, op1 == op2 ? 1 : 0);
                break;
            case CGT:
                DEBUG_OUT_INSN_PARSED("[ext] CGT")
                    POP_EVAL_STACK(op2)
                    POP_EVAL_STACK(op1)
                    PUSH_EVAL_STACK(op1 > op2 ? 1 : 0)
                    DEBUG_PRINT(" - cmp %d, %d, put %d\n", op2, op1, op1 > op2 ? 1 : 0);
                break;
            case CLT:
                DEBUG_OUT_INSN_PARSED("[ext] CLT")
                    POP_EVAL_STACK(op2)
                    POP_EVAL_STACK(op1)
                    PUSH_EVAL_STACK(op1 < op2 ? 1 : 0)
                    DEBUG_PRINT(" - cmp %d, %d, put %d\n", op2, op1, op1 < op2 ? 1 : 0);
                break;