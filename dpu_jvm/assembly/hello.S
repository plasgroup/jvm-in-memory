// Hello world, written in assembly: computes the checksum of "hello world"

.text
.globl __bootstrap
__bootstrap:
#define stringPointer r1
#define checksum r0
	move checksum, 0
	move stringPointer, hello

#define currentCharacter r2
	checksum_loop:
		// Load the current character
		lbu  currentCharacter , stringPointer , 0
		// And exit if this character is 0
		jz   currentCharacter, end_of_loop
		add checksum, checksum, currentCharacter
		// Move to next character
		add stringPointer, stringPointer, 1, true,	checksum_loop

	end_of_loop:
		stop

.data
.globl hello
hello:
    .string "hello world"