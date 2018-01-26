#!/bin/bash

# Package it
sbt package

# Jars
HEALPIX=lib/jhealpix.jar

# Parameters
catalog=""#"data/catalog.txt"
resolution=512
npoints=10000


# Run it!
spark-submit \
  --master local[*] \
  --jars ${HEALPIX} \
  target/scala-2.11/gridtest_2.11-0.1.0.jar \
  $catalog $resolution $npoints
