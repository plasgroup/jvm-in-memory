		
		NOP, //0
		LDC_I4_5, //1
		NEWARR,0, 0, 0, 0, //2
		STLOC_0, //7
		LDC_I4_5, //8
		NEWARR,0, 0, 0, 0, //9
		STLOC_1, //e
		LDC_I4_0,
		STLOC_2,
		LDC_I4_0,
		STLOC_3,
		BR_S, 0x001f,
		NOP,
		LDLOC_0,
		LDLOC_3,
		LDLOC_3,
		STELEM_I4,
		NOP,
		LDLOC_3,
		LDC_I4_1,
		ADD,
		STLOC_3,
		LDLOC_3, //0x1f
		LDC_I4_5,
		0xFE, CLT,
		STLOC_S, 4,
		LDLOC_S, 4,
		BRTRUE_S, 0x15,
		LDC_I4_0,
		STLOC_S, 5,
		BR_S, 0x3e,
		NOP,
		LDLOC_1,
		LDLOC_S, 5,
		LDC_I4_5,
		LDLOC_S, 5,
		ADD, 
		STELEM_I4,
		NOP,
		LDLOC_S, 5,
		LDC_I4_1,
		ADD,
		STLOC_S, 5,
		LDLOC_S, 5,
		LDC_I4_5,
		0xfe, CLT,
		STLOC_S, 6,
		LDLOC_S, 6,
		BRTRUE_S, 0x2e,
		LDC_I4_0,
		STLOC_S, 7,
		BR_S, 0x63,
		NOP,
		LDLOC_0,
		LDLOC_S, 7,
		LDLOC_0,
		LDLOC_S, 7,
		LDELEM_I4,
		LDLOC_1,
		LDLOC_S, 7,
		LDELEM_I4,
		ADD,
		STELEM_I4,
		NOP,
		LDLOC_S, 7,
		LDC_I4_1,
		ADD,
		STLOC_S, 7,
		LDLOC_S, 7,
		LDC_I4_5,
		0xfe, CLT,
		STLOC_S, 8,
		LDLOC_S, 8,
		BRTRUE_S, 0x4e,
		LDC_I4_0,
		STLOC_S, 9,
		BR_S, 0x82,
		NOP,
		LDLOC_2,
		LDLOC_0,
		LDLOC_S, 9,
		LDELEM_I4,
		ADD,
		STLOC_2,
		NOP,
		LDLOC_S, 9,
		LDC_I4_1,
		ADD,
		STLOC_S, 9,
		LDLOC_S, 9,
		LDC_I4_5,
		0xfe, CLT,
		STLOC_S, 10,
		LDLOC_S, 10,
		BRTRUE_S, 0x73,
		LDLOC_2,
		STLOC_S, 11,
		BR_S, 0x92,
		LDLOC_S, 11,
		RET

/*
public static int arr(){
		int[] a = new int[5];
		int[] b = new int[5];
		int sum = 0;
		for(int i = 0; i < 5; i++){
		    a[i] = i;
		}
		for(int i = 0; i < 5; i++){
		    b[i] = 5 + i;
		}
		for(int i = 0; i < 5; i++){
		    a[i] = a[i] + b[i];
		}
		for(int i = 0; i < 5; i++){
		    sum += a[i];
		}
		return sum;
	}
*/		

