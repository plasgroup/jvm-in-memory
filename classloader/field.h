#ifndef H_FIELD
#define H_FIELD
typedef struct {
	uint16_t flags;
	uint16_t name_idx;
	uint16_t desc_idx;
	uint16_t attrs_count;
	Attribute* attrs;
} Field;
#endif
