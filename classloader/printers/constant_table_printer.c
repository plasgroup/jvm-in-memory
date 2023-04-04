#include "constant_table_printer.h"

#ifndef INMEMORY
void print_item_real_represent(FILE* stream, const Class* class, uint16_t item_id) {
	CP_Item* s = get_item(class, item_id);
	if (s->tag == STRING_UTF8) {
		fprintf(stream, "%s", s->value.string.value);
	}
	else if (s->tag == CLASS || s->tag == STRING) {
		fprintf(stream, "%s", get_item(class, s->value.ref.class_idx)->value.string.value);
	}
	else if (s->tag == FIELD || s->tag == METHOD || s->tag == INTERFACE_METHOD || s->tag == NAME) {
		char* left = get_item(class, s->value.ref.class_idx)->value.string.value;
		char* right = get_item(class, s->value.ref.name_idx)->value.string.value;
		print_item_real_represent(stream, class, s->value.ref.class_idx);
		fprintf(stream, ".");
		print_item_real_represent(stream, class, s->value.ref.name_idx);

	}
}

void print_CP_pool(FILE* stream, const Class* class) {
	CP_Item* s;

	uint16_t i = 1; // constant pool indexes start at 1, get_item converts to pointer index
	while (i < class->const_pool_count) {
		s = get_item(class, i);
		fprintf(stream, "Item #%u %s: ", i, tag2str(s->tag));
		if (s->tag == STRING_UTF8) {
			fprintf(stream, "%s\n", s->value.string.value);
		}
		else if (s->tag == INTEGER) {
			fprintf(stream, "%d\n", s->value.integer);
		}
		else if (s->tag == FLOAT) {
			fprintf(stream, "%f\n", s->value.flt);
		}
		else if (s->tag == LONG) {
			fprintf(stream, "%ld\n", to_long(s->value.lng));
		}
		else if (s->tag == DOUBLE) {
			fprintf(stream, "%lf\n", to_double(s->value.dbl));
		}
		else if (s->tag == CLASS || s->tag == STRING) {
			fprintf(stream, "#%u \t\t// %s\n", s->value.ref.class_idx, get_item(class, s->value.ref.class_idx)->value.string.value);
		}
		else if (s->tag == FIELD || s->tag == METHOD || s->tag == INTERFACE_METHOD || s->tag == NAME) {

			fprintf(stream, "#%u.#%u \t\t// ", s->value.ref.class_idx, s->value.ref.name_idx);
			print_item_real_represent(stream, class, i);
			fprintf(stream, "\n");
		}
		i++;
	}
	fprintf(stream, "*********************************************\n");
	fprintf(stream, "\n" PCOLOR_RESET);
}
#endif