#include "print.h"




#ifndef INMEMORY
void print_class(FILE *stream, const Class *class) {
	fprintf(stream, "File: %s\n", class->file_name);
	fprintf(stream, "Minor number: %u \n", class->minor_version);
	fprintf(stream, "Major number: %u \n", class->major_version);
	fprintf(stream, "Constant pool items count: %u \n", class->const_pool_count);
	fprintf(stream, "Constant table size: %ub \n", class->pool_size_bytes);
	fprintf(stream, "\n" PCOLOR_GREEN "Printing constant pool of %d items...\n", class->const_pool_count-1);
	fprintf(stream, "*********************************************\n");

	uint16_t i = 1;

	print_CP_pool(stream, class);
	print_classfile_accessflags(stream, class);
	
	
	CP_Item *cl_str = get_class_string(class, class->this_class);
	fprintf(stream, "This class: #%u\t\t// %s\n", class->this_class, cl_str->value.string.value);

	cl_str = get_class_string(class, class->super_class);
	fprintf(stream, "Super class: #%u\t\t// %s\n", class->super_class, cl_str->value.string.value);

	
	fprintf(stream, "Interfaces count: %u\n", class->interfaces_count);

	fprintf(stream, "Printing %u interfaces...\n", class->interfaces_count);
	if (class->interfaces_count > 0) {
		Ref *iface = class->interfaces;
		CP_Item *the_class;
		uint16_t idx = 0;
		while (idx < class->interfaces_count) {
			the_class = get_item(class, iface->class_idx); // the interface class reference
			CP_Item *item = get_item(class, the_class->value.ref.class_idx);
			String string = item->value.string;
			fprintf(stream, "Interface: %s\n", string.value);
			idx++;
			iface = class->interfaces + idx; // next Ref
		}
	}

	fprintf(stream, "Printing %d fields...\n", class->fields_count);

	if (class->fields_count > 0) {
		Field *field = class->fields;
		uint16_t idx = 0;
		while (idx < class->fields_count) {
			CP_Item *name = get_item(class, field->name_idx);
			CP_Item *desc = get_item(class, field->desc_idx);
			printf("%s %s\n", field2str(desc->value.string.value[0]), name->value.string.value);
			Attribute at;
			if (field->attrs_count > 0) {
				int aidx = 0;
				while (aidx < field->attrs_count) {
					at = field->attrs[aidx];
					CP_Item *name = get_item(class, at.name_idx);
					fprintf(stream, "\tAttribute name: %s\n", name->value.string.value);
					fprintf(stream, "\tAttribute length %d\n", at.length);
					fprintf(stream, "\tAttribute: %s\n", at.info);
					aidx++;
				}
			}
			idx++;
			field = class->fields + idx;
		}
	}

	fprintf(stream, "Printing %u methods...\n", class->methods_count);
	i = 0;
	if (class->methods_count > 0) {
		Method *method = class->methods;
		uint16_t idx = 0;
		while (idx < class->methods_count) {
			CP_Item *name = get_item(class, method->name_idx);
			CP_Item *desc = get_item(class, method->desc_idx);
			printf(PCOLOR_PURPLE "Method Name = #%u\t// %s \n Descriptor = #%u\t// %s \n" PCOLOR_GREEN, 
				method->name_idx, name->value.string.value, method->desc_idx, desc->value.string.value);
			Attribute* at;
			if (method->attrs_count > 0) {
				int aidx = 0;
				int data_pos;
				
				while (aidx < method->attrs_count) {
					at = method->attrs + aidx;
					CP_Item *name = get_item(class, at->name_idx);
					fprintf(stream, "\t");
					print_method_accessflags(stream,  method);

					fprintf(stream, PCOLOR_RED "\tAttribute Item :%d\n" PCOLOR_GREEN, aidx);
					print_attribute(stream, class, at);
					
					
					
					aidx++;
				}
			}
			idx++;
			method = class->methods + idx;
		}
		
	}
	fprintf(stream, PCOLOR_RESET);
	fprintf(stream, "Printing %u attributes...\n", class->attributes_count);
	if (class->attributes_count > 0) {
		Attribute at;
		int aidx = 0;
		while (aidx < class->attributes_count) {
			at = class->attributes[aidx];
			CP_Item *name = get_item(class, at.name_idx);
			fprintf(stream, "\tAttribute name: %s", name->value.string.value);
			fprintf(stream, "\tAttribute length %d\n", at.length);
			fprintf(stream, "\tAttribute: %s\n", at.info);
			aidx++;
		}
	}

}
#endif
