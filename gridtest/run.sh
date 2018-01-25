#!/bin/bash

# Package it
sbt package

# Jars
HEALPIX=lib/jhealpix.jar

# Parameters
gridtype="healpix"
npoints=10000000
resolution=512

# Run it!
spark-submit \
  --master local[*] \
  --jars ${HEALPIX} \
  target/scala-2.11/gridtest_2.11-0.1.0.jar \
  $gridtype $npoints $resolution
