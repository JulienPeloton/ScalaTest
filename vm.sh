#!/bin/bash

# HOSTNAME
sudo echo "134.158.75.49 $HOSTNAME" | sudo tee -a /etc/hosts

# JAVA
sudo apt-get -y update
sudo apt-get install -y openjdk-8-jdk

# PYTHON (ANACONDA)
CONTREPO=https://repo.continuum.io/archive/
# Stepwise filtering of the html at $CONTREPO
# Get the topmost line that matches our requirements, extract the file name.
ANACONDAURL=$(wget -q -O - $CONTREPO index.html | grep "Anaconda3-" | grep "Linux" | grep "86_64" | head -n 1 | cut -d \" -f 2)
wget -O ~/anaconda.sh $CONTREPO$ANACONDAURL
bash ~/anaconda.sh

# SCALA
sudo apt install scala

# sbt
wget https://dl.bintray.com/sbt/native-packages/sbt/0.13.9/sbt-0.13.9.tgz
tar -xvf sbt-0.13.9.tgz
sudo mkdir /usr/lib/sbt
sudo mv sbt /usr/lib/sbt/sbt-0.13.9
sudo touch /usr/bin/sbt
sudo ln -fs /usr/lib/sbt/sbt-0.13.9/bin/sbt /usr/bin/sbt
#Need to run this to download needed jar files
sbt -version
# Clean up
rm sbt-0.13.9.tgz

# Spark
# sudo apt-get install spark
mkdir spark
curl http://d3kbcqa49mib13.cloudfront.net/spark-2.0.2.tgz | tar xvz -C spark
cd spark/spark-2.0.2
./build/sbt -Pyarn -Phadoop-2.7 package
./build/mvn -DskipTests clean package
