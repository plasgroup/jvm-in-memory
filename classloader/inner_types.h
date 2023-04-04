#ifndef H_INNER_TYPES
#define H_INNER_TYPES
#include <stdint.h>

#define u1 uint8_t
#define u2 uint16_t
#define u4 uint32_t


typedef struct {
	uint32_t high;
	uint32_t low;
} Double;

typedef struct {
	uint32_t high;
	uint32_t low;
} Long;

/* Wraps references to an item in the constant pool */
typedef struct {
	uint16_t class_idx;
	uint16_t name_idx;
} Ref;

typedef struct {
	uint16_t length;
	char* value;
} String;

#endif

