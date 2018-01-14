#!/bin/bash

############################
# Compile the scala code
############################
sbt package

############################
# Launch the Pipeline
############################
# Resources needed
MASTER=local[*]
MEM_EXECUTOR=4G

# Define the needed jars
JARDIR=/Users/julien/Documents/workspace/jars
# jts=${JARDIR}/jts-1.13.jar
# geospark=${JARDIR}/geospark-1.0.1.jar
plot=${JARDIR}/swiftvis2_2.11-0.1.0-SNAPSHOT.jar

GEOSPARK=org.datasyslab:geospark:1.0.1
JTS=com.vividsolutions:jts:1.13

# Parameters
tag=test1
ngal=1000000
nshell=10
maxred=1
method=envelope
outdir=out_$tag

# spark -> python
echo \
"""
Launching Spark processing!
"""
spark-submit \
  --master ${MASTER}\
  --executor-memory ${MEM_EXECUTOR} \
  --jars $plot \
  --packages ${GEOSPARK},${JTS} \
  target/scala-2.11/ProcessSims_2.11-0.1.0.jar \
  $ngal \
  $nshell \
  $maxred \
  $method \
  $outdir

echo \
"""
Launching Python processing!
"""
# python python/plot_points.py --output $outdir
