#include "memory_read_helper.h"
u1 read_u1(char* addr) {
	return *addr;
}

u2 read_u2(char* addr) {
	u2 v = 0;
	v |= *addr << 8;
	v |= *(addr + 1);
	return v;
}

u4 read_u4(char* addr) {
	u4 v = 0;
	v |= *addr << 24;
	v |= *(addr + 1) << 16;
	v |= *(addr + 2) << 8;
	v |= *(addr + 3);
	return v;
}