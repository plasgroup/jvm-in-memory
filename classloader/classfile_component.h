#ifndef H_CLASSFILE_COMPONENT
#define H_CLASSFILE_COMPONENT

//#include <endian.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include "inner_types.h"
#include "attribute.h"
#include "method.h"
#include "field.h"
#include "attribute.h"
#include "access_flags.h"

/* A wrapper for FILE structs that also holds the file name.  */
#ifndef INMEMORY
typedef struct {
	char* file_name;
	FILE* file;
} ClassFile;
#endif

typedef struct {
	uint8_t tag; // the tag byte
	union {
		String string;
		float flt;
		Double dbl;
		Long lng;
		int32_t integer;
		Ref ref; /* A method, field or interface reference */
	} value;
} CP_Item;

/* The .class structure */
typedef struct {
	char* file_name;
	uint16_t minor_version;
	uint16_t major_version;
	uint16_t const_pool_count;
	//cp_info constant_pool
	uint32_t pool_size_bytes;
	CP_Item* items;
	//access flag
	uint16_t flags;
	uint16_t this_class;
	uint16_t super_class;
	uint16_t interfaces_count;
	Ref* interfaces;
	uint16_t fields_count;
	Field* fields;
	uint16_t methods_count;
	Method* methods;
	uint16_t attributes_count;
	Attribute* attributes;
} Class;







#include "constant_pool.h"


#endif