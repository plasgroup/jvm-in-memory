defunct_process=$(ps ef | grep "\-jar\|simulator")
while IFS= read -r line
do
	 pid=$(echo "$line" | cut -d' ' -f2)
	 echo "$pid"
         kill -9 $pid 
done <<< "$defunct_process"
