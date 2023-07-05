#include "constant_pool.h"
#include <stdio.h>
CP_Item* get_item(const Class* class, const uint16_t cp_idx) {
	if (cp_idx < class->const_pool_count) return &class->items[cp_idx - 1];
	else return NULL;
}

CP_Item* get_class_string(const Class* class, const uint16_t index) {
	CP_Item* i1 = get_item(class, index);
	return get_item(class, i1->value.ref.class_idx);
}
