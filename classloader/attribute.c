#include "attribute.h"
#include "../utils/memory_read_helper.h"


ATTR_INFO_LineNumberTable* parse_attr_LineNumberTable(char* info) {
	ATTR_INFO_LineNumberTable* info_lt = (ATTR_INFO_LineNumberTable*)malloc(sizeof(ATTR_INFO_LineNumberTable));
	u2 length;
	int i;
	info_lt->linenumber_table_length = read_u2(info);
	length = info_lt->linenumber_table_length;
	info += 2;

	info_lt->line_number_table = (LineNumberTableItem*)malloc(sizeof(LineNumberTableItem) * length);
	for (i = 0; i < length; i++) {
		info_lt->line_number_table[i].start_pc = read_u2(info);
		info += 2;
		info_lt->line_number_table[i].line_number = read_u2(info);
		info += 2;
	}
	return info_lt;
}

ATTR_INFO_CODE* parse_attr_info_code(char* info) {
	ATTR_INFO_CODE* info_code = (ATTR_INFO_CODE*)malloc(sizeof(ATTR_INFO_CODE));
	int i = 0;
	char* pt;
	u4 length;
	info_code->max_stack = read_u2(info);
	info += 2;
	info_code->max_locals = read_u2(info);
	info += 2;
	info_code->code_length = read_u4(info);
	info += 4;
	length = info_code->code_length;
	
	info_code->code = (char*)malloc(length);
	pt = info_code->code;
	for (i = 0; i < length; i++, pt++, info++) {
		*pt = *info;	
	}

	

	info_code->exception_table_length = read_u2(info);
	info += 2;

	length = info_code->exception_table_length;
	info_code->exception_tables = (ExceptionTable*) malloc(sizeof(ExceptionTable) * length);

	for (i = 0; i < length; i++) {
		info_code->exception_tables[i].start_pc = read_u2(info);
		info += 2;
		info_code->exception_tables[i].end_pc = read_u2(info);
		info += 2;
		info_code->exception_tables[i].handler_pc = read_u2(info);
		info += 2;
		info_code->exception_tables[i].catch_type = read_u2(info);
		info += 2;
	}
	info_code->attribute_count = read_u2(info);
	info += 2;
	length = info_code->attribute_count;
	info_code->attributes = (Attribute*)malloc(sizeof(Attribute) * length);
	for (i = 0; i < length; i++) {
		info_code->attributes[i].name_idx = read_u2(info);
		info += 2;
		info_code->attributes[i].length = read_u4(info);
		info += 4;
		info_code->attributes[i].info = info;
		info += info_code->attributes[i].length;
	}

	return info_code;
}


ATTR_INFO_StackMappingTable* parse_stack_mapping_table_attribute(char* info) {
	ATTR_INFO_StackMappingTable* info_smt = (ATTR_INFO_StackMappingTable*)malloc(sizeof(ATTR_INFO_StackMappingTable));
	int i, j;
	int frame_type;
	u1 v_tag;
	info_smt->number_of_entries = read_u2(info);
	info += 2;
	info_smt->entries = 
		(StackMapFrame*)malloc(sizeof(StackMapFrame) * info_smt->number_of_entries);
	for (i = 0; i < info_smt->number_of_entries; i++) {
		info_smt->entries[i].frame_type = read_u1(info);
		frame_type = info_smt->entries[i].frame_type;
		info += 1;

		if (frame_type < 64) {

		}else if (frame_type >= 64 && frame_type <= 127) {
			info_smt->entries[i].map_frame.t_same_locals_1_stack_item_frame.verification_types
				= (verification_type_info*)malloc(sizeof(verification_type_info));
			info += sizeof(verification_type_info);
		}
		else if (frame_type == 247) {
			info_smt->entries[i].map_frame.t_same_locals_1_stack_item_frame_extended.offset_delta
				= read_u2(info);
			info += 2;
			info_smt->entries[i].map_frame.t_same_locals_1_stack_item_frame_extended.verification_types
				= (verification_type_info*)malloc(sizeof(verification_type_info));

			v_tag = read_u1(info);

			info_smt->entries[i].map_frame.t_same_locals_1_stack_item_frame_extended
				.verification_types[0].tag = v_tag;

			info += 1;
			if (v_tag == ITEM_Object || v_tag == ITEM_Uninitialized) {
				info_smt->entries[i].map_frame.t_same_locals_1_stack_item_frame_extended
					.verification_types[0].value.object_variable_info.cpool_index = read_u2(info);
				info += 2;
			}

		}
		else if (frame_type >= 248 && frame_type <= 250) {
			info_smt->entries[i].map_frame.t_chop_frame.offset_delta = read_u2(info);
			info += 2;
		}
		else if (frame_type == 251) {
			info_smt->entries[i].map_frame.t_same_frame_extended.offset_delta = read_u2(info);
			info += 2;
		}
		else  if (frame_type >= 252 && frame_type <= 254) {
			info_smt->entries[i].map_frame.t_append_frame.offset_delta = read_u2(info);
			info += 2;
			info_smt->entries[i].map_frame.t_append_frame.locals
				= (verification_type_info*)malloc(sizeof(verification_type_info) * (frame_type - 251));
			
			for (j = 0; j < frame_type - 251; j++) {
				v_tag = read_u1(info);

				info_smt->entries[i].map_frame.t_append_frame.locals[j].tag = v_tag;

				info += 1;
				if (v_tag == ITEM_Object || v_tag == ITEM_Uninitialized) {
					info_smt->entries[i].map_frame.t_append_frame.locals[j]
						.value.object_variable_info.cpool_index = read_u2(info);
					info += 2;
				}
			}
		}
		else {
			info_smt->entries[i].map_frame.t_full_frame.offset_delta = read_u2(info);
			info += 2;
			info_smt->entries[i].map_frame.t_full_frame.number_of_locals = read_u2(info);
			info += 2;

			info_smt->entries[i].map_frame.t_full_frame.locals
				= (verification_type_info*)malloc(sizeof(verification_type_info) * info_smt->entries[i].map_frame.t_full_frame.number_of_locals);
			for (j = 0; j < info_smt->entries[i].map_frame.t_full_frame.number_of_locals; j++) {
				v_tag = read_u1(info);

				info_smt->entries[i].map_frame.t_full_frame.locals[j]
					.tag = v_tag;

				info += 1;
				if (v_tag == ITEM_Object || v_tag == ITEM_Uninitialized) {
					info_smt->entries[i].map_frame.t_full_frame
						.locals[j].value.object_variable_info.cpool_index = read_u2(info);
					info += 2;
				}
			}

			info_smt->entries[i].map_frame.t_full_frame.number_of_stack_items = read_u2(info);
			info += 2;

			info_smt->entries[i].map_frame.t_full_frame.verification_types
				= (verification_type_info*)malloc(sizeof(verification_type_info) * info_smt->entries[i].map_frame.t_full_frame.number_of_stack_items);
			
			for (j = 0; j < info_smt->entries[i].map_frame.t_full_frame.number_of_stack_items; j++) {
				v_tag = read_u1(info);

				info_smt->entries[i].map_frame.t_full_frame.verification_types[j]
					.tag = v_tag;
				info += 1;
				if (v_tag == ITEM_Object || v_tag == ITEM_Uninitialized) {
					info_smt->entries[i].map_frame.t_full_frame
						.verification_types[j].value.object_variable_info.cpool_index = read_u2(info);
					info += 2;
				}
			}


		}
	}
	return info_smt;
}