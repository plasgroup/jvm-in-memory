echo "preparing JDK"
curl https://download.java.net/java/GA/jdk17/0d483333a00540d886896bac774ff48b/35/GPL/openjdk-17_linux-x64_bin.tar.gz -o ~/jdk-17.0.1.tar.gz
cd ~
echo "unpacking.."
tar -xvf jdk-17.0.1.tar.gz
