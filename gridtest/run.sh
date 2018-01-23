#!/bin/bash

# Package it
sbt package

# Jars
HEALPIX=lib/jhealpix.jar

# Run it!
spark-submit \
  --master local[*] \
  --jars ${HEALPIX} \
  target/scala-2.11/gridtest_2.11-0.1.0.jar
