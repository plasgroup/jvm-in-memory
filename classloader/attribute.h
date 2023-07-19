#ifndef H_ATTRIBUTE
#define H_ATTRIBUTE

#include <stdlib.h>
#include "inner_types.h"


typedef struct {
	uint16_t name_idx;
	uint32_t length;
	char* info;
} Attribute;


#include "attributions/attr_code.h"
#include "attributions/attr_stackmappingtable.h"
#include "attributions/attr_linenumbertable.h"
#include "attributions/attr_constantvalue.h"






ATTR_INFO_LineNumberTable* parse_attr_LineNumberTable(char* info);
ATTR_INFO_StackMappingTable* parse_stack_mapping_table_attribute(char* info);
ATTR_INFO_CODE* parse_attr_info_code(char* info);
#endif