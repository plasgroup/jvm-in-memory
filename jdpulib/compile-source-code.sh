javac -sourcepath ./src -d ./out2 ./src/Main.java -classpath ./dpu.jar
cd out2
cp ../src/META-INF/MANIFEST.MF ./
cp ../dpu.jar ./
jar cvfm bst-latest.jar MANIFEST.MF .
