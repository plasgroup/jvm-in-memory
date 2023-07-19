        NOP,           //0x00
        LDC_I4_S, 52,  //0x01
        STLOC_0,       //0x03
        LDC_I4,  0b11111011, 0, 0,0, //0x04
        STLOC_1,  //0x09
        LDLOC_0,  //0x0a
        LDLOC_1,  //0x0b
        ADD,      //0x0c
        STLOC_2,  //0x0d
        LDLOC_2,  //0x0e
        LDLOC_1,  //0x0f
        MUL,      //0x10
        STLOC_3,  //0x11
        LDLOC_1,  //0x12
        LDLOC_0,  //0x13
        DIV,      //0x14
        STLOC_S, 4, //0x15
        LDLOC_S, 4, //0x17
        LDLOC_3, //0x19
        0xFE, CEQ, //0x1a
        STLOC_S, 5, //0x1c
        LDLOC_S, 5, //0x1e
        BRFALSE_S, 0x2c, //0x20
        LDLOC_S, 4, //0x22
        NOP,0,0,0,0, //call
        NOP, //0x29
        BR_S, 0x33, //0x2a
        LDLOC_3, //0x2c
        NOP,0,0,0,0, //call
        NOP,
        RET //0x33

        /*
         int a = 52;
	    int b = 251;
	    int c = a + b;
	    int d = c * b;
	    int e = b / d;
	    int f;
	    if(e == d)
	       f = d;
	    else
	    	f = e;
        */