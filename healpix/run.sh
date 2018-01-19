#!/bin/bash

# Package it
sbt package

# Run it!
spark-submit --master local[*] --jars lib/jhealpix.jar target/scala-2.11/healpix_2.11-0.1.0.jar
