#ifndef H_ATTR_LINENUMBERTABLE

typedef struct {
	u2 start_pc;
	u2 line_number;
} LineNumberTableItem;

typedef struct {
	u2 linenumber_table_length;
	LineNumberTableItem* line_number_table;
} ATTR_INFO_LineNumberTable;
#endif // !H_ATTR_LINENUMBERTABLE
