
#ifndef CLASS_H
#define CLASS_H
#include <stdio.h>
#include "classfile_component.h"



enum RANGES {
	/* The smallest permitted value for a tag byte */
	MIN_CPOOL_TAG = 1,

	/* The largest permitted value for a tag byte */
	MAX_CPOOL_TAG = 18
};

#ifndef INMEMORY
/* Delegate to read_class(ClassFile) */
Class *read_class_from_file_name(char *f);

/* Parse the given class file into a Class struct. */
Class *read_class(const ClassFile class_file);

/* Parse the attribute properties from file into attr. Assumes class_file.file is at offset relative to reading an attribute struct.
 * See section 4.7 of the JVM spec. */
void parse_attribute(ClassFile class_file, Attribute *attr);

/* Parse the constant pool into class from class_file. ClassFile.file MUST be at the correct seek point i.e. byte offset 11.
 * The number of bytes read is returned. A return value of 0 signifies an invalid constant pool and class may have been changed.
 * See section 4.4 of the JVM spec.
 */
void parse_const_pool(Class *class, const uint16_t const_pool_count, const ClassFile class_file);

/* Parse the initial section of the given class_file up to and including the constant_pool_size section */
void parse_header(ClassFile class_file, Class *class);

/* Return true if class_file's first four bytes match 0xcafebabe. */
bool is_class(FILE *class_file);

#endif

/* Convert the high and low bits of dbl to a double type */
double to_double(const Double dbl);

/* Convert the high and low bits of lng to a long type */
long to_long(const Long lng);

/* Convert the 2-byte field type to a friendly string e.g. "J" to "long" */
char *field2str(const char fld_type);

/* Convert tag byte to its string name/label */
static inline char *tag2str(uint8_t tag) {
	return CPool_strings[tag];
}

#ifndef INMEMORY

/* Write the name and class stats/contents to the given stream. */
void print_class(FILE *stream, const Class *class);
#endif

#endif //CLASS_H__
