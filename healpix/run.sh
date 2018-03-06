#!/bin/bash
#export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/Users/julien/anaconda3/lib/python3.6/site-packages/jep/
# export DYLD_FALLBACK_LIBRARY_PATH=$DYLD_FALLBACK_LIBRARY_PATH:/Users/julien/anaconda3/lib/python3.6/site-packages/jep/
#export LD_PRELOAD=~/anaconda3/lib/libpython3.6m.dylib
# Package it
sbt package

HEALPIX=lib/jhealpix.jar
JEP=lib/jep-3.7.1.jar

# Run it!
spark-submit \
  --master local[*] \
  --jars ${HEALPIX},${JEP} \
  target/scala-2.11/healpix_2.11-0.1.0.jar
