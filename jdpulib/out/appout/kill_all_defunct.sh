defunct_process=$(ps ef | grep "\-jar\|simulator")
while IFS= read -r line
do
	 pid=$(echo "$line" | cut -d' ' -f2)
	 echo "$pid"
	 while kill -9 $pid; do
		 sleep 1
	 done
done <<< "$defunct_process"
