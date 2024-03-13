#ifndef H_JSTRUCT_PRINTER
#define H_JSTRUCT_PRINTER
#ifdef INMEMORY
#include <mram.h>
#endif
#include "../core/memory.h"
#include "../core/jclass.h"
#include "../core/method.h"

void print_class(struct j_class __mram_ptr* jc);
void print_virtual_table(struct j_class __mram_ptr* jc);
void print_method(struct j_method __mram_ptr* jm);
#endif
