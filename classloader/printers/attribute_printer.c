#include "attribute_printer.h"
#include "../../utils/printer_color.h"
char* bytecodes_string[] = {
"NOP",
"ACONST_NULL",
"ICONST_M1",
"ICONST_0",
"ICONST_1",
"ICONST_2",
"ICONST_3",
"ICONST_4",
"ICONST_5",
"LCONST_0",
"LCONST_1",
"FCONST_0",
"FCONST_1",
"FCONST_2",
"DCONST_0",
"DCONST_1",
"BIPUSH",
"SIPUSH",
"LDC",
"LDC_W",
"LDC2_W",
"ILOAD",
"LLOAD",
"FLOAD",
"DLOAD",
"ALOAD",
"ILOAD_0",
"ILOAD_1",
"ILOAD_2",
"ILOAD_3",
"LLOAD_0",
"LLOAD_1",
"LLOAD_2",
"LLOAD_3",
"FLOAD_0",
"FLOAD_1",
"FLOAD_2",
"FLOAD_3",
"DLOAD_0",
"DLOAD_1",
"DLOAD_2",
"DLOAD_3",
"ALOAD_0",
"ALOAD_1",
"ALOAD_2",
"ALOAD_3",
"IALOAD",
"LALOAD",
"FALOAD",
"DALOAD",
"AALOAD",
"BALOAD",
"CALOAD",
"SALOAD",
"ISTORE",
"LSTORE",
"FSTORE",
"DSTORE",
"ASTORE",
"ISTORE_0",
"ISTORE_1",
"ISTORE_2",
"ISTORE_3",
"LSTORE_0",
"LSTORE_1",
"LSTORE_2",
"LSTORE_3",
"FSTORE_0",
"FSTORE_1",
"FSTORE_2",
"FSTORE_3",
"DSTORE_0",
"DSTORE_1",
"DSTORE_2",
"DSTORE_3",
"ASTORE_0",
"ASTORE_1",
"ASTORE_2",
"ASTORE_3",
"IASTORE",
"LASTORE",
"FASTORE",
"DASTORE",
"AASTORE",
"BASTORE",
"CASTORE",
"SASTORE",
"POP",
"POP2",
"DUP",
"DUP_X1",
"DUP_X2",
"DUP2",
"DUP2_X1",
"DUP2_X2",
"SWAP",
"IADD",
"LADD",
"FADD",
"DADD",
"ISUB",
"LSUB",
"FSUB",
"DSUB",
"IMUL",
"LMUL",
"FMUL",
"DMUL",
"IDIV",
"LDIV",
"FDIV",
"DDIV",
"IREM",
"LREM",
"FREM",
"DREM",
"INEG",
"LNEG",
"FNEG",
"DNEG",
"ISHL",
"LSHL",
"ISHR",
"LSHR",
"IUSHR",
"LUSHR",
"IAND",
"LAND",
"IOR",
"LOR",
"IXOR",
"LXOR",
"IINC",
"I2L",
"I2F",
"I2D",
"L2I",
"L2F",
"L2D",
"F2I",
"F2L",
"F2D",
"D2I",
"D2L",
"D2F",
"I2B",
"I2C",
"I2S",
"LCMP",
"FCMPL",
"FCMPG",
"DCMPL",
"DCMPG",
"IFEQ",
"IFNE",
"IFLT",
"IFGE",
"IFGT",
"IFLE",
"IF_ICMPEQ",
"IF_ICMPNE",
"IF_ICMPLT",
"IF_ICMPGE",
"IF_ICMPGT",
"IF_ICMPLE",
"IF_ACMPEQ",
"IF_ACMPNE",
"GOTO",
"JSR",
"RET",
"TABLESWITCH",
"LOOKUPSWITCH",
"IRETURN",
"LRETURN",
"FRETURN",
"DRETURN",
"ARETURN",
"RETURN",
"GETSTATIC",
"PUTSTATIC",
"GETFIELD",
"PUTFIELD",
"INVOKEVIRTUAL",
"INVOKESPECIAL",
"INVOKESTATIC",
"INVOKEINTERFACE",
"INVOKEDYNAMIC",
"NEW",
"NEWARRAY",
"ANEWARRAY",
"ARRAYLENGTH",
"ATHROW",
"CHECKCAST",
"INSTANCEOF",
"MONITORENTER",
"MONITOREXIT",
"WIDE",
"MULTIANEWARRAY",
"IFNULL",
"IFNONNULL",
"GOTO_W",
"JSR_W",
"BREAKPOINT",
"IMPDEP1",
"IMPDEP2",
};

u2 operand_count[] = {
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
1,
2,
1,
2,
2,
1,
1,
1,
1,
1,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
1,
1,
1,
1,
1,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
2,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
0,
2,
2,
2,
2,
2,
2,
2,
2,
2,
2,
2,
2,
2,
2,
2,
2,
1,
0xff,
0xff,
0,
0,
0,
0,
0,
0,
2,
2,
2,
2,
2,
2,
2,
4,
4,
2,
1,
2,
0,
0,
2,
2,
0,
0,
3,
3,
2,
2,
4,
4,
0xff,
0xff,
0xff,
};


int is_str_eq(char* s1, char* s2) {
	while (*s1 != 0 && *s2 != 0) {
		if (*s1 != *s2) return 0;
		s1 += 1;
		s2 += 1;
	}
	return (*s1 == 0 && *s2 == 0) ? 1 : 0;
}


#ifndef INMEMORY
void print_stack_mapping_table_attribute(FILE* stream, const Class* class, Attribute* attr) {
	int data_pos;
	int i;
	fprintf(stream, "\tAttribute: %s\n", attr->info);

	ATTR_INFO_StackMappingTable* attr_info_smt = parse_stack_mapping_table_attribute(attr->info);

	fprintf(stream, PCOLOR_CYAN "\t\tEntries Count = %u\n", attr_info_smt->number_of_entries);
	for (i = 0; i < attr_info_smt->number_of_entries; i++) {
		fprintf(stream, PCOLOR_RED "\t\t\tItem %u: Tag = %u ", i, attr_info_smt->entries[i].frame_type);
		u1 frame_type = attr_info_smt->entries[i].frame_type;
		if (frame_type < 64) {
			fprintf(stream, " /* SAME */ \n");
		}
		else if (frame_type >= 64 && frame_type <= 127) {
			fprintf(stream, " /* SAME_LOCALS_1_STACK */ \n");
			fprintf(stream, "\t\t\t\tverification_type 1: Tag = %u\n", attr_info_smt->entries[i].map_frame.t_same_locals_1_stack_item_frame
				.verification_types->tag);
			
		}
		else if (frame_type == 247) {
			fprintf(stream, " /* SAME_LOCALS_1_STACK_ITEM_EXTENDED */ \n");
		}
		else if (frame_type >= 248 && frame_type <= 250) {
			fprintf(stream, " /* CHOP */ \n");
		}
		else if (frame_type == 251) {
			fprintf(stream, " /* SAME_FRAME_EXTENDED */ \n");
		}
		else  if (frame_type >= 252 && frame_type <= 254) {
			fprintf(stream, " /* APPEND */ \n");
		}
		else {
			fprintf(stream, " /* FULL_FRAME */ \n");

		}
	}

	fprintf(stream, "\n\n");
}


void print_line_number_table_attribute(FILE* stream, const Class* class, Attribute* attr) {
	int data_pos;
	int i;
	fprintf(stream, "\tAttribute: %s\n", attr->info);


	ATTR_INFO_LineNumberTable* attr_info_lt = parse_attr_LineNumberTable(attr->info);

	fprintf(stream, PCOLOR_CYAN "\t\tLineNumberTable Length = %u\n", attr_info_lt->linenumber_table_length);
	for (i = 0; i < attr_info_lt->linenumber_table_length; i++) {
		fprintf(stream, PCOLOR_RED "\t\t\tItem %u: line %u, pc = %u\n", i
							, attr_info_lt->line_number_table[i].line_number
							, attr_info_lt->line_number_table[i].start_pc);
	}

	fprintf(stream, "\n\n");
}

void print_code_attribute(FILE* stream, const Class* class, Attribute* attr) {
	int data_pos;
	int i;
	fprintf(stream, "\tAttribute: %s\n", attr->info);


	ATTR_INFO_CODE* attr_info_code = parse_attr_info_code(attr->info);
	fprintf(stream, PCOLOR_CYAN "\t\tMaxStack = %u\n", attr_info_code->max_stack);
	fprintf(stream, PCOLOR_CYAN "\t\tMaxLocals = %u\n", attr_info_code->max_locals);
	fprintf(stream, PCOLOR_CYAN "\t\tCode Length = %u\n", attr_info_code->code_length);
	fprintf(stream, PCOLOR_CYAN "\t\t\tCode:\n" PCOLOR_RED);

	
	for (data_pos = 0; data_pos < attr_info_code->code_length; data_pos++) {
		fprintf(stream, PCOLOR_CYAN "\t\t\t%08u:" PCOLOR_RED " 0x%02x\t " PCOLOR_GREEN "%s" PCOLOR_RESET, data_pos, *(attr_info_code->code + data_pos),
			//\n
			bytecodes_string[*(attr_info_code->code + data_pos)]);
		int oprands_length = operand_count[*(attr_info_code->code + data_pos)];
		if (oprands_length != 0xff) {
			for (i = 0; i < oprands_length; i++) {
				data_pos++;
				fprintf(stream, " 0x%02x ", *(attr_info_code->code + data_pos));
			}
		}
		
		fprintf(stream, "\n");
	}

	fprintf(stream, PCOLOR_CYAN "\t\tException Items Count = %u\n", attr_info_code->exception_table_length);


	fprintf(stream, PCOLOR_CYAN "\t\t\tExceptions:\n" PCOLOR_RED);
	for (i = 0; i < attr_info_code->exception_table_length; i++) {
		fprintf(stream, PCOLOR_YELLOW "\t\t\tException Item %u:\n", i);
		fprintf(stream, PCOLOR_YELLOW "\t\t\t\tstart_pc = %u:\n", 
			attr_info_code->exception_tables[i].start_pc);
		fprintf(stream, PCOLOR_YELLOW "\t\t\t\tend_pc = %u:\n",
			attr_info_code->exception_tables[i].end_pc);
	}

	fprintf(stream, PCOLOR_CYAN "\t\tAttribute Items Count = %u\n", attr_info_code->attribute_count);
	for (i = 0; i < attr_info_code->attribute_count; i++) {
		fprintf(stream, PCOLOR_YELLOW "\t\t\tAttribute Item %u:\n" PCOLOR_PURPLE, i);
		print_attribute(stream, class, (attr_info_code->attributes + i));
		fprintf(stream, "\n");
	}


	fprintf(stream, "\n\n");
}


void print_attribute(FILE* stream, const Class* class, Attribute* attr) {
	CP_Item* name = get_item(class, attr->name_idx);
	fprintf(stream, "\tAttribute name: %s\n", name->value.string.value);
	fprintf(stream, "\tAttribute length: %d\n", attr->length);

	if (is_str_eq(name->value.string.value, "Code")) {
		print_code_attribute(stream, class, attr);
	}
	else if (is_str_eq(name->value.string.value, "LineNumberTable")){
		print_line_number_table_attribute(stream, class, attr);
	}
	else if (is_str_eq(name->value.string.value, "StackMapTable")) {
		print_stack_mapping_table_attribute(stream, class, attr);
	}
}


#endif