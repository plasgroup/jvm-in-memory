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
	
DPU_DEPS := core/memory.h \
	core/vm_loop.h \
	core/frame_helper.h \
	core/method.h \
	core/jclass.h \

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
	core/frame_helper.c \
	core/vm_loop.c \
	core/memory.c \
	
SHARD_CFILES += core/memory.c \
		  core/frame_helper.c \
          core/vm_loop.c \
		  utils/jstruct_printer.c \
				
SHARD_DEPS += ir/opcode.h \
	   ir/bytecode.h \
	   core/frame_helper.h \
       core/method.h \
       core/memory.h \
       core/vm_loop.h \

DPU_SLAVE_CFILES += dpuslave.c \
    $(SHARD_CFILES)

DPU_CFLAGE += -DINMEMORY -DDEBUG_PRINT_ENABLE=0 -DJAVA_BYTECODE -DNR_TASKLETS=24 -DSLIENT  # -DDEBUG_OUTPUT_INSN_PARSED#  -DDEBUG_OUTPUT_INSN_PARSED
HOST_CFLAGE += -DHOST -DDEBUG_PRINT_ENABLE=1 -DDEBUG_OUTPUT_INSN_PARSED

rundpu:
	$(CXX) --std=c++11 $(HOST_CFLAGE) rundpu.cpp -o rundpu `dpu-pkg-config --cflags --libs dpu` -g

main: $(DEPS) 
	$(CC) $(DPU_CFLAGE) -o main $(CFILES)

dpu_rt: $(DEPS) 
	$(CXX) --std=c++11 $(HOST_CFLAGE) dpu_rt.c -o dpurt `dpu-pkg-config --cflags --libs dpu` -g

dpuslave: $(DPU_DEPS)
	$(CC) $(DPU_CFLAGE) -w -o dpuslave $(DPU_SLAVE_CFILES)

test: core/memory.h
	$(CC) -DINMEMORY test.c core/memory.c -o test


dpuslave_assembly: $(DPU_DEPS)
	$(CC) $(DPU_CFLAGE) -w -S -o dpuslave.S $(DPU_SLAVE_CFILES)