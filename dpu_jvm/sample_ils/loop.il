    NOP,
        LDC_I4_0,
        STLOC_0,
        LDC_I4_0,
        STLOC_2,
        BR_S, 0x11,
        NOP,
        LDLOC_0,
        LDLOC_2,
        ADD,
        STLOC_0,
        NOP,
        LDLOC_2,
        LDC_I4_1,
        ADD,
        STLOC_2,
        LDLOC_2,
        LDARG_0,
        0xFE, CLT,
        STLOC_3,
        LDLOC_3,
        BRTRUE_S, 0x7,
        LDC_I4_S, 4,
        STLOC_1,
        BR_S, 0x2a,
        NOP,
        LDLOC_0,
        LDLOC_0,
        LDLOC_1,
        MUL,
        ADD,
        STLOC_0,
        LDLOC_1,
        LDC_I4_1,
        SUB,
        STLOC_1,
        NOP,
        LDLOC_1,
        LDC_I4_0,
        0xFE, CGT,
        STLOC_S, 4,
        LDLOC_S, 4,
        BRTRUE_S, 0x1e,
        LDLOC_0,
        STLOC_S, 5,
        BR_S, (uint8_t)0x39,
        LDLOC_S, 5,
        RET

        /*
        	int sum = 0;
		for(int i = 0; i < x; i++)
        {
			sum += i;
        }
		int cond1 = 10;
		while(cond1 > 0)
        {
			sum += (sum * cond1);
			cond1 -= 1;
        }
		return sum;
        */