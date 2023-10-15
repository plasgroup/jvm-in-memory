compile_output=$(javac -sourcepath ./src -d ./out2 ./src/Main.java -classpath ./dpu.jar)
echo $compile_output

if [ -z "grep $compile_output error" ]
then
	echo "error!!!!!!!!!!"
fi
cd out2
cp ../src/META-INF/MANIFEST.MF ./
cp ../dpu.jar ./
jar cvfm bst-latest.jar MANIFEST.MF .
