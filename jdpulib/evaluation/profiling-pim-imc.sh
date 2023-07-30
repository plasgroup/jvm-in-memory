#!/bin/bash
export SCRIPT_DIR="$(pwd)"
for i in 1000 2000 5000 10000 20000 50000 100000 200000 500000 1000000
do
	perf stat -e uncore_imc_0/cas_count_read/,uncore_imc_1/cas_count_read/,uncore_imc_2/cas_count_read/,uncore_imc_3/cas_count_read/,uncore_imc_4/cas_count_read/,uncore_imc_5/cas_count_read/,uncore_imc_0/cas_count_write/,uncore_imc_1/cas_count_write/,uncore_imc_2/cas_count_write/,uncore_imc_3/cas_count_write/,uncore_imc_4/cas_count_write/,uncore_imc_5/cas_count_write/ -a java -jar jdpulib.jar PIM 2000000 $i  2> "./cpu-only-imc-2000000n-${i}q.txt"
done
