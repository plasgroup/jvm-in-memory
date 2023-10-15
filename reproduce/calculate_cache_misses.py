import numpy as np
import re

cpu_record_path = './record_files/[q]cpu-profile-100000000n-500000q.txt'
pim_record_path = './record_files/[q]pim-profile-100000000n-500000q.txt'

def read_file(fpath):
    c = ""
    with open(fpath) as f:
        c = f.read()
        f.close()
    return c

def analyze(c):
    parsed_map = {}
    lines = c.split('\n')
    current_section = ''
    section_from = -1
    for i in range(0, len(lines)):
        line = lines[i]
        m = re.match(r'# Samples:.*event.*\'(.*)\'', line)
        if(m != None):
            section_name = m.group(1)
            section_from = i
            current_section = section_name
            parsed_map[section_name] = {'items':[]}

        m = re.match(r'# Event count \(approx.\): ([0-9]+)', line)
        if(m != None):
            parsed_map[section_name]['event_count'] = (int)(m.group(1))

        if(len(line) > 0 and line[0] != '#'):
            parsed_map[current_section]['items'].append(line)
    return parsed_map



filepath_map = {'cpu': cpu_record_path, 'pim': pim_record_path}
def calc_cache_misses(exp_type = 'cpu'):
    c = read_file(filepath_map[exp_type])
    rec = analyze(c)
    llc_load_misses_event_count = -1
    search_llc_load_misses = -1
    for k in rec.keys():
        if(k.find('LLC-load-misses') > 0):
            if(llc_load_misses_event_count != -1):
                continue
            llc_load_misses_event_count = rec[k]['event_count']
            print("%s LLC misses event count  = %d" % ((exp_type).upper(), llc_load_misses_event_count))
            for item in rec[k]['items']:
                if(item.find('pim.algorithm.TreeNode.search') >= 0):
                    percentage = item.strip().split(' ')[0]
                    print("%s LLC misses percentage  = %s" % ((exp_type).upper(), percentage))
                    v = ((float)(percentage.replace('%',''))/100.0)
                    search_llc_load_misses = v * llc_load_misses_event_count
                    print("%s search LLC misses count approx. = %d" % ((exp_type).upper(), search_llc_load_misses))
    return search_llc_load_misses


cpu_search_misses = calc_cache_misses('cpu')
pim_search_misses = calc_cache_misses('pim')
print("factors = %lf", pim_search_misses / cpu_search_misses)
