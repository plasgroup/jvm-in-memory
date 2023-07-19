import numpy as np
import sys

def read_file(fpath):
    c = ""
    with open(fpath, 'r') as f:
        c = f.read()
    return c
    
c = read_file("./tmp.txt")
cache_miss_part_from = (c.find("cache-miss"))
c = c[cache_miss_part_from:].split('\n')

cache_miss_parts = []
for i in range(0, len(c)):
	if(c[i].strip() != ""):
		cache_miss_parts.append(c[i])
	else:
		break
miss_count = float(cache_miss_parts[1].split(' ')[-1])
for line in cache_miss_parts:
	pos = (line.find('search'))
	if(pos != -1):
		precent = float(line.strip().split(' ')[0].replace('%',''))
		print(str(precent / 100.0) + ' ' + str(miss_count))
		break
        
		
