for i in 2000000 10000000 100000000 200000000
do
	echo "generate $i key-value pairs"
	java -jar generate-key-values.jar $i
done
