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

GEOSPARK=org.datasyslab:geospark:1.0.1
JTS=com.vividsolutions:jts:1.13

# Parameters
tag=test2
ngal=100000000
nshell=10
maxred=5
method=envelope
outdir=out_$tag
parts=1000

# spark -> python
echo \
"""
Launching Spark processing!
"""
spark-submit \
  --master ${MASTER} \
  --executor-memory ${MEM_EXECUTOR} \
  --packages ${GEOSPARK},${JTS} \
  target/scala-2.11/ProcessSims_2.11-0.1.0.jar \
  $ngal \
  $nshell \
  $maxred \
  $method \
  $outdir \
  $parts

echo \
"""
Launching Python processing!
"""
# python python/plot_points.py --output $outdir
