import numpy as np
import re
import sys


def analyze(filename, profiling_items, method_name):
    def normalize_sample_count(desc):
        return int(desc.replace('K', "000").replace("M", "000000"))
    res_dict = {}
    for item in profiling_items:
        res_dict[item] = {}
    current_body = None
    with open(filename, "r") as f:
        lines = f.read().split("\n")
        for line in lines:
            #print(line)
            m = (re.match(r'.*event \'(.*)\'', line))
            #print(line)
            event_count_match = re.match(r'.*Event count \(approx.\): ([0-9]+).*', line)
            if event_count_match != None:
                event_count = (normalize_sample_count(event_count_match.group(1)))
                res_dict[current_body]['event_count'] = event_count

            if m != None:
                current_body = (m.group(1))
                recorded = False
            else:
                if current_body is None:
                    continue
                if current_body in res_dict.keys():
                    m = re.match(r'.*' + method_name.replace('(', '\(').replace(")", "\)") + ".*", line)
                    if m != None and not recorded:
                        m = re.match(r'.*([0-9]+\.[0-9]+\%).*', line)
                        percent = (m.group(1))
                        res_dict[current_body]['percent'] = percent
                        recorded = True
        f.close()
        for k in res_dict.keys():
            if 'percent' not in res_dict[k] or 'event_count' not in res_dict[k]:
                res_dict.pop(k)
            else:
                res_dict[k]['approx_count'] = res_dict[k]['event_count'] * float(res_dict[k]['percent'].replace('%','')) / 100.0
    return res_dict

def main():
    args = sys.argv
    if(len(args) <= 3):
        print("Usage: python analyze_perf_record.py record_file_path method_matching_name perf_item_1 [perf_item_2] ... [perf_item_n]")
        exit(1)
    record_filename = args[1]
    query_method = args[2]
    print("query method name = %s" % query_method)
    print("query perf events = %s" % str(args[3:]))
    analyze_result = analyze(record_filename, args[3:], query_method)
    print(analyze_result)

if __name__ == '__main__':
    main()

