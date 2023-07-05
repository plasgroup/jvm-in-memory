#ifndef H_CONSTANT_POOL
#define H_CONSTANT_POOL
#include "classfile_component.h"

typedef enum {
	STRING_UTF8 = 1, /* occupies 2+x bytes */
	INTEGER = 3, /* 32bit two's-compliment big endian int */
	FLOAT = 4, /* 32-bit single precision */
	LONG = 5, /* Long: a signed 64-bit two's complement number in big-endian format (takes two slots in the constant pool table) */
	DOUBLE = 6, /* Double: a 64-bit double-precision IEEE 754 floating-point number (takes two slots in the constant pool table) */
	CLASS = 7, /* Class reference: an index within the constant pool to a UTF-8 string containing the fully qualified class name (in internal format) */
	STRING = 8, /* String reference: an index within the constant pool to a UTF-8 string */
	FIELD = 9, /* Field reference: two indexes within the constant pool, the first pointing to a Class reference, the second to a Name and Type descriptor. */
	METHOD = 10, /* Method reference: two indexes within the constant pool, the first pointing to a Class reference, the second to a Name and Type descriptor. */
	INTERFACE_METHOD = 11, /* Interface method reference: two indexes within the constant pool, the first pointing to a Class reference, the second to a Name and Type descriptor. */
	NAME = 12, /* Name and type descriptor: 2 indexes to UTF-8 strings, the first representing a name and the second a specially encoded type descriptor. */
	METHOD_HANDLE = 15,
	METHOD_TYPE = 16,
	INVOKE_DYNAMIC = 18
} CPool_t;


static char* CPool_strings[] = {
	"Undefined", // 0
	"String_UTF8",
	"Undefined", // 2
	"Integer",
	"Float",
	"Long",
	"Double",
	"Class",
	"String",
	"Field",
	"Method",
	"InterfaceMethod",
	"Name",
	"Undefined", // 13
	"Undefined", // 14
	"MethodHandle",
	"MethodType",
	"InvokeDynamic"
};




CP_Item* get_item(const Class* class, const uint16_t cp_idx);

CP_Item* get_class_string(const Class* class, const uint16_t index);
#endif