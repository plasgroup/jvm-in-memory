#ifndef H_METHOD
#define H_METHOD
#include "classfile_component.h"
typedef struct {
	uint16_t flags;
	uint16_t name_idx;
	uint16_t desc_idx;
	uint16_t attrs_count;
	Attribute* attrs;
} Method;
#endif