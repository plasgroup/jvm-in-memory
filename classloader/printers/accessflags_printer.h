#ifndef H_ACCESSFLAGS_PRINTS
#define H_ACCESSFLAGS_PRINTS
#include <stdbool.h>
#include <stdio.h>
#include <stdint.h>
#ifndef INMEMORY
#include <malloc.h>
#endif

#ifdef INMEMORY

#endif
#include <stdlib.h>
#include "../classfile_component.h"
#ifndef INMEMORY
void print_class_flags_name(FILE* stream, uint32_t flags);
void print_classfile_accessflags(FILE* stream, const Class* class);
void print_method_accessflags(FILE* stream, const Method* method);
#endif
#endif // ! H_ACCESSFLAGS_PRINTS
