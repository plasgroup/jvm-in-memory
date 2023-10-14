compile_output=$(javac -sourcepath ./src -d ./outlib ./src/Main.java -classpath ./dpu.jar)
echo $compile_output

if [ -z "grep $compile_output error" ]
then
	echo "error!!!!!!!!!!"
fi
cd outlib
cp ../src/META-INF/MANIFEST.MF ./
cp ../dpu.jar ./
jar cvfm upmemlib.jar MANIFEST.MF .
