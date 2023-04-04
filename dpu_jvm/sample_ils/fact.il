NOP,
        LDARG_0,
        LDC_I4_1,
        0xFE, CEQ,
        STLOC_0,
        LDLOC_0,
        BRFALSE_S, 0xd,
        LDC_I4_1,
        STLOC_1,
        BR_S, 0x1a,
        LDARG_0,
        LDARG_0,
        LDC_I4_1,
        SUB,
        CALL, 0, 0, 0, 0, //call
        MUL,
        STLOC_1,
        BR_S, 0x1a,
        LDLOC_1,
        RET

/*
	    if(x == 1) return 1;
	    return x * fact(x - 1);
*/