CC := dpu-upmem-dpurte-clang

CXX := g++

DPU_CFLAGE += -DINMEMORY -DDEBUG_PRINT_ENABLE=0 


DEPS := \
	classloader/class.h \
	classloader/printers/print.h \
	classloader/access_flags.h \
	classloader/inner_types.h \
	classloader/constant_pool.h \
	classloader/attribute.h \
	classloader/method.h \
	classloader/field.h \
	classloader/printers/constant_table_printer.h \
	classloader/printers/accessflags_printer.h \
	classloader/printers/attribute_printer.h \
	utils/memory_read_helper.h \
	utils/jstruct_printer.h
	

DPU_DEPS := vm_shared_src/memory.h \
	vm_shared_src/vm_loop.h \
	vm_shared_src/frame_helper.h \
	vm_shared_src/method.h \
	vm_shared_src/jclass.h \

CFILES := \
	dpu_rt.c \
	classloader/class.c \
	classloader/constant_pool.c \
	classloader/printers/print.c \
	classloader/printers/constant_table_printer.c \
	classloader/printers/accessflags_printer.c \
	classloader/printers/attribute_printer.c \
	classloader/access_flags.c \
	classloader/attribute.c \
	utils/memory_read_helper.c \

DPU_CILES := \
	vm_shared_src/frame_helper.c \
	vm_shared_src/vm_loop.c \
	vm_shared_src/memory.c \
	
SHARD_CFILES += vm_shared_src/memory.c \
		  vm_shared_src/frame_helper.c \
          vm_shared_src/vm_loop.c \
		  utils/jstruct_printer.c \
          sample_ils/test_cases_gen.c \
				
SHARD_DEPS += ir/opcode.h \
	   ir/bytecode.h \
	   vm_shared_src/frame_helper.h \
       vm_shared_src/method.h \
       vm_shared_src/memory.h \
       vm_shared_src/vm_loop.h \
       sample_ils/test_cases_gen.h \

DPU_SLAVE_CFILES += dpuslave.c \
    $(SHARD_CFILES)

DPU_CFLAGE += -DINMEMORY -DDEBUG_PRINT_ENABLE=0 -DJAVA_BYTECODE -DNR_TASKLETS=24 -DDEBUG_OUTPUT_INSN_PARSED# -DSLIENT  # -DDEBUG_OUTPUT_INSN_PARSED  -DDEBUG_OUTPUT_INSN_PARSED
HOST_CFLAGE += -DHOST -DDEBUG_PRINT_ENABLE=1 -DDEBUG_OUTPUT_INSN_PARSED

rundpu:
	$(CXX) --std=c++11 $(HOST_CFLAGE) rundpu.cpp -o rundpu `dpu-pkg-config --cflags --libs dpu` -g

main: $(DEPS) 
	$(CC) $(DPU_CFLAGE) -o main $(CFILES)

dpu_rt: $(DEPS) 
	$(CXX) --std=c++11 $(HOST_CFLAGE) dpu_rt.c -o dpurt `dpu-pkg-config --cflags --libs dpu` -g

dpuslave: $(DPU_DEPS)
	$(CC) $(DPU_CFLAGE) -w -o dpuslave $(DPU_SLAVE_CFILES)

test: vm_shared_src/memory.h
	$(CC) -DINMEMORY test.c vm_shared_src/memory.c -o test


dpuslave_assembly: $(DPU_DEPS)
	$(CC) $(DPU_CFLAGE) -w -S -o dpuslave.S $(DPU_SLAVE_CFILES)