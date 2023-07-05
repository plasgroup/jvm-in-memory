#ifndef PRINT_H
#define PRINT_H

#include <stdbool.h>
#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>

#include "../class.h"
#include "../constant_pool.h"
#include "../../utils/printer_color.h"

#include "constant_table_printer.h"
#include "accessflags_printer.h"
#include "attribute_printer.h"

#ifndef INMEMORY
void print_class(FILE *stream, const Class *class);
#endif

#endif //PRINT_H
