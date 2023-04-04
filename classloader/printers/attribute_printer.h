#ifndef H_ATTRIBUTE_PRINTER
#define H_ATTRIBUTE_PRINTER
#include <stdbool.h>
#include <stdio.h>
#include <stdint.h>
#ifndef INMEMORY
#include <malloc.h>
#endif
#include <stdlib.h>
#include "../classfile_component.h"

#ifndef INMEMORY
void print_attribute(FILE* stream, const Class* class, Attribute* attr);
#endif


#endif // !H_ATTRIBUTE_PRINTER
