#ifndef IR_OPCODE
#define IR_OPCODE

#define NOP 0x00

//LDARG: load argument
#define LDARG_0 0x02
#define LDARG_1 0x03
#define LDARG_2 0x04
#define LDARG_3 0x05
//LDLOC: load local varible i to the evaluation stack
#define LDLOC_0 0x06
#define LDLOC_1 0x07
#define LDLOC_2 0x08
#define LDLOC_3 0x09
//STLOC: pop the top element in evaluation stack to the local slot i
#define STLOC_0 0x0A
#define STLOC_1 0x0B
#define STLOC_2 0x0C
#define STLOC_3 0x0D
#define LDARG_S 0x0E
#define LDARGA_S 0x0F
#define STARG_S 0x10
#define LDLOC_S 0x11
#define LDLOCA_S 0x12
#define STLOC_S 0x13
#define LDNULL 0x14

// load int32bit to evaluation stack
#define LDC_I4_M1 0x15
#define LDC_I4_0 0x16
#define LDC_I4_1 0x17
#define LDC_I4_2 0x18
#define LDC_I4_3 0x19
#define LDC_I4_4 0x1A
#define LDC_I4_5 0x1B
#define LDC_I4_6 0x1C
#define LDC_I4_7 0x1D
#define LDC_I4_8 0x1E
#define LDC_I4_S 0x1F
//Pushes a supplied value of type int32 onto the evaluation stack as an int32.
#define LDC_I4 0x20
#define LDC_I8 0x21
#define LDC_R4 0x22
#define LDC_R8 0x23
#define POP 0x26
#define JMP 0x27
#define CALL 0x28
#define DCALL 0x29
#define BR_S 0x2B
#define BRFALSE_S 0x2C
#define BRTRUE_S 0x2D
#define BEQ_S 0x2E
#define BGE_S 0x2F
#define BGT_S 0x30
#define BLE_S 0x31
#define BLT_S 0x32
#define BNE_UN_S 0x33
#define BGE_UN_S 0x34
#define BGT_UN_S 0x35
#define BLE_UN_S 0x36
#define BLT_UN_S 0x37
#define BR 0x38
#define BRFALSE 0x39
#define BRTRUE 0x3A
#define BEQ 0x3B
#define BGE 0x3C
#define BGT 0x3D
#define BLE 0x3E
#define BLT 0x3F
#define BNE_UN 0x40
#define BGE_UN 0x41
#define BGT_UN 0x42
#define BLE_UN 0x43
#define BLT_UN 0x44
#define ADD 0x58
#define SUB 0x59
#define MUL 0x5A
#define DIV 0X5B
#define DIV_UN 0X5C
#define CONV_I1 0x67
#define CONV_I2 0x68
#define CONV_I4 0x69
#define CONV_I8 0x6A
#define CONV_R4 0x6B
#define CONV_R8 0x6C
#define CONV_U4 0x6D
#define CONV_U8 0x6E

#define NEWOBJ 0x73
#define LDFLD 0x7B
#define STFLD 0x7D

#define LDSFLD 0x7E
#define STSFLD 0x80
#define NEWARR 0x8D
#define LDLEN 0x8E
#define STELEM_I4 0x9E
#define STELEM_I8 0x9F

#define LDELEM_I4 0x94
#define LDELEM_I8 0x96
//
#define EXT_PREFIX0 (uint8_t)0xFE 

#define CEQ 0x01
#define CLT 0x04
#define CGT 0x02


#define RET 0x2A
#endif