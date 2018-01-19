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
# MASTER=spark://192.168.3.213:7077
MEM_EXECUTOR=4G

GEOSPARK=org.datasyslab:geospark:1.0.1
JTS=com.vividsolutions:jts:1.13

# Parameters
tag=test2
ngal=10000000
nshell=2
maxred=5
method=envelope
outdir=out_$tag

# spark -> python
echo \
"""
Launching Spark processing!
"""
for exp in {1..11}
do
  parts=$((2 ** $exp))
  echo "Process $parts parts"

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
done

echo \
"""
Launching Python processing!
"""
# python python/plot_points.py --output $outdir
