#ifndef H_ATTR_EXCEPTIONS
#define H_ATTR_EXCEPTIONS
typedef struct {
	u2 number_of_exceptions;
	u2 *e
} ATTR_INFO_Exceptions;
#endif // !H_ATTR_EXCEPTIONS

//object method, parameters -> DPU



x.m() {
	D obj_y;
	obj_y.xxx(); //x 2nd: cached
	obj_y.member = ...;
}

// x' -> DPU data
// DPU.getInstance(115).createObject(C.class)
// java 