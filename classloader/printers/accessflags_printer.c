#include "accessflags_printer.h"


#ifndef INMEMORY
void print_class_flags_name(FILE* stream, uint32_t flags) {

	AccessFlags indexes[8] = { ACC_PUBLIC, ACC_FINAL, ACC_SUPER, ACC_INTERFACE, ACC_ABSTRACT, 
								ACC_SYNTHETIC, ACC_ANNOTATION, ACC_ENUM };
	int i;
	char* name;

	for (i = 0; i < 8; i++) {
		if (flags & ( indexes[i])) {
			
			
			switch (indexes[i])
			{
				case ACC_PUBLIC:
					name =  "ACC_PUBLIC";
					break;
				case ACC_FINAL:
					name =  "ACC_FINAL";
					break;
				case ACC_SUPER:
					name = "ACC_SUPER";
					break;
				case ACC_INTERFACE:
					name = "ACC_INTERFACE";
					break;
				case ACC_ABSTRACT:
					name = "ACC_ABSTRACT";
					break;
				case ACC_SYNTHETIC:
					name = "ACC_SYNTHETIC";
					break;
				case ACC_ANNOTATION:
					name = "ACC_ANNOTATION";
					break;
				case ACC_ENUM:
					name = "ACC_ENUM";
					break;
				case ACC_MANDATED:
					name = "ACC_MANDATED";
					break;
				default:
					break;
			}
			
			fprintf(stream, "%s ", name);
			
		}
	}
	fprintf(stream, "\n");
}

void print_method_flags_name(FILE* stream, uint32_t flags) {
	AccessFlags indexes[12] = { ACC_PUBLIC, ACC_PRIVATE, ACC_PROTECTED, ACC_STATIC, ACC_FINAL,
					ACC_SYNCHRONIZED, ACC_BRIDGE, ACC_VARARGS, ACC_NATIVE, ACC_ABSTRACT, 
				ACC_STRICT, ACC_SYNTHETIC
	};
	int i;
	char* name;
	for (i = 0; i < 12; i++) {
		if (flags & (indexes[i])) {
			switch (indexes[i])
			{
			case ACC_PUBLIC:
				name = "ACC_PUBLIC";
				break;
			case ACC_PRIVATE:
				name = "ACC_PRIVATE";
				break;
			case ACC_PROTECTED:
				name = "ACC_PROTECTED";
				break;
			case ACC_STATIC:
				name = "ACC_STATIC";
				break;
			case ACC_FINAL:
				name = "ACC_FINAL";
				break;
			case ACC_SYNCHRONIZED:
				name = "ACC_SYNCHRONIZED";
				break;
			case ACC_BRIDGE:
				name = "ACC_BRIDGE";
				break;
			case ACC_VARARGS:
				name = "ACC_VARARGS";
				break;
			case ACC_NATIVE:
				name = "ACC_NATIVE";
				break;
			case ACC_ABSTRACT:
				name = "ACC_ABSTRACT";
				break;
			case ACC_STRICT:
				name = "ACC_STRICT";
				break;
			case ACC_SYNTHETIC:
				name = "ACC_SYNTHETIC";
				break;
			default:
				break;
			}

			fprintf(stream, "%s ", name);
		}
	}
	fprintf(stream, "\n");
}

void print_classfile_accessflags(FILE* stream, const Class* class) {
	fprintf(stream, "Access flags: %x // ", class->flags);
	print_class_flags_name(stream, be16toh(class->flags));
	fprintf(stream, "\n");
}

void print_method_accessflags(FILE* stream, const Method* method) {	
	fprintf(stream, "\tAccess flags: %x // ", method->flags);
	print_method_flags_name(stream, be16toh(method->flags));
	fprintf(stream, "\n");
}
#endif