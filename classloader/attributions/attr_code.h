#ifndef ATTR_CODE
#define ATTR_CODE
typedef struct {
	u2 start_pc;
	u2 end_pc;
	u2 handler_pc;
	u2 catch_type;
} ExceptionTable;

typedef struct {
	u2 max_stack;
	u2 max_locals;
	u4 code_length;
	unsigned char* code;
	u2 exception_table_length;
	ExceptionTable* exception_tables;
	u2 attribute_count;
	Attribute* attributes;
} ATTR_INFO_CODE;

#endif // !ATTR_CODE
