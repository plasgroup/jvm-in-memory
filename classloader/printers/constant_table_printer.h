#ifndef H_CP_PRINTER
#define H_CP_PRINTER
#include "../class.h"
#include "../../utils/printer_color.h"

#ifndef INMEMORY
void print_item_real_represent(FILE* stream, const Class* class, uint16_t item_id);
void print_CP_pool(FILE* stream, const Class* class);
#endif

#endif