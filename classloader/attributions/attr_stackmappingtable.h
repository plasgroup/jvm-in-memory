#ifndef H_ATTR_STACKMAPFRAME
#define H_ATTR_STACKMAPFRAME

enum verifiy_type {
	ITEM_Top = 0,
	ITEM_Integer = 1,
	ITEM_Float = 2,
	ITEM_Double = 3,
	ITEM_Long = 4,
	ITEM_Null = 5,
	ITEM_UninitializedThis = 6,
	ITEM_Object = 7,
	ITEM_Uninitialized = 8,
};

typedef struct {  } Top_variable_info;
typedef struct { } Integer_variable_info;
typedef struct { } Float_variable_info;
typedef struct {  } Long_variable_info;
typedef struct {  } Double_variable_info;
typedef struct { } Null_variable_info;
typedef struct {  } UninitializedThis_variable_info;
typedef struct { u2 cpool_index; } Object_variable_info;
typedef struct { u2 offset; } Uninitialized_variable_info;

typedef struct {
	u1 tag;
	union {
		Top_variable_info top_variable_info;
		Integer_variable_info integer_variable_info;
		Float_variable_info float_variable_info;
		Long_variable_info long_variable_info;
		Double_variable_info double_variable_info;
		Null_variable_info null_variable_info;
		UninitializedThis_variable_info uninitializedThis_variable_info;
		Object_variable_info object_variable_info;
		Uninitialized_variable_info uninitialized_variable_info;
	} value;
} verification_type_info;




typedef struct {
} same_frame;

typedef struct {
	verification_type_info* verification_types;
} same_locals_1_stack_item_frame;

typedef struct {
	u2 offset_delta;
	verification_type_info* verification_types;
} same_locals_1_stack_item_frame_extended;

typedef struct {
	u2 offset_delta;
} chop_frame;

typedef struct {
	u2 offset_delta;
} same_frame_extended;

typedef struct {
	u2 offset_delta;
	verification_type_info* locals;
} append_frame;


typedef struct {
	u2 offset_delta;
	u2 number_of_locals;
	verification_type_info* locals;
	u2 number_of_stack_items;
	verification_type_info* verification_types;
} full_frame;



typedef struct {
	u1 frame_type;
	union stack_map_frame {
		same_frame t_same_frame;
		same_locals_1_stack_item_frame t_same_locals_1_stack_item_frame;
		same_locals_1_stack_item_frame_extended t_same_locals_1_stack_item_frame_extended;
		chop_frame t_chop_frame;
		same_frame_extended t_same_frame_extended;
		append_frame t_append_frame;
		full_frame t_full_frame;
	} map_frame;
} StackMapFrame;



typedef struct {
	u2 number_of_entries;
	StackMapFrame* entries;
} ATTR_INFO_StackMappingTable;

#endif // !H_ATTR_STACKMAPFRAME
