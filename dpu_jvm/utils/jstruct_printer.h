#ifndef H_JSTRUCT_PRINTER
#define H_JSTRUCT_PRINTER
#ifdef INMEMORY
#include <mram.h>
#endif
#include "../vm_shared_src/memory.h"
#include "../vm_shared_src/jclass.h"
void print_class(struct j_class __mram_ptr* jc);
void print_method(struct j_method __mram_ptr* jm);
#endif
