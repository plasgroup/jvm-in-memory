
#include <dpu>
#include <assert.h>
#include <stdint.h>
#include <stdio.h>

#ifndef DPU_BINARY
#define DPU_BINARY "dpuslave"
#endif

/* Size of the buffer for which we compute the checksum: 64KBytes. */
#define BUFFER_SIZE (1 << 16)

void populate_mram(struct dpu_set_t set) {
  uint8_t buffer[BUFFER_SIZE];

  for (int byte_index = 0; byte_index < BUFFER_SIZE; byte_index++) {
    buffer[byte_index] = (uint8_t)byte_index;
  }
  char* symbol = "m_metaspace";
  DEBUG_PRINT("symbol = %s\n", symbol);
  DPU_ASSERT(dpu_broadcast_to(set, symbol, 0, buffer, BUFFER_SIZE, DPU_XFER_DEFAULT));
}

int main() {
  struct dpu_set_t set, dpu;
  uint32_t checksum;

  DPU_ASSERT(dpu_alloc(1, NULL, &set));
  DPU_ASSERT(dpu_load(set, DPU_BINARY, NULL));
  populate_mram(set);

  DPU_ASSERT(dpu_launch(set, DPU_SYNCHRONOUS));
  DPU_FOREACH(set, dpu) {
    DPU_ASSERT(dpu_copy_from(dpu, "checksum", 0, (uint8_t *)&checksum, sizeof(checksum)));
    DEBUG_PRINT("Computed checksum = 0x%08x\n", checksum);
  }
  DPU_ASSERT(dpu_free(set));
  return 0;
}

// int main(int argc, char *args[]) {
// 	if (argc == 1) {
// 		DEBUG_PRINT("Please pass at least 1 .class file to open");
// 		exit(EXIT_FAILURE);
// 	}

// 	int i;
// 	for (i = 1; i < argc; i++) {
// 		char *file_name = args[i];
// 		FILE *file = fopen(file_name, "r");

// 		if (!file) {
// 			DEBUG_PRINT("Could not open '%s': %s\n", file_name, strerror(errno));
// 			continue;
// 		}

// 		// Check the file header for .class nature
// 		if (!is_class(file)) {
// 			DEBUG_PRINT("Skipping '%s': not a valid class file\n", file_name);
// 			continue;
// 		}

// 		const ClassFile class_file = {
// 			file_name,
// 			file
// 		};

// 		Class *class = read_class(class_file);
// 		if (class == NULL) {
// 			fDEBUG_PRINT(stderr, "Parsing aborted; invalid class file contents: %s\n", class_file.file_name);
// 		} else {
// 			// yay, valid!
// 			print_class(stdout, class);
// 		}

// 		free(class);
// 		fclose(file);
// 	}

// 	exit(EXIT_SUCCESS);
// }

