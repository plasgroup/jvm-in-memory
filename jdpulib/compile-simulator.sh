compile_output=$(javac -sourcepath ./src -d ./out2 ./src/Main.java -classpath)
echo $compile_output

if [ -z "grep $compile_output error" ]
then
	echo "error!!!!!!!!!!"
fi
cd out2
cp ../src/META-INF/MANIFEST.MF ./
cp ../dpu.jar ./
jar cvfm simulator-server.jar MANIFEST.MF .

