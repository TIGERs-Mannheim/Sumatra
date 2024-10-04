#!/usr/bin/env bash
#
# Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
#

JDKS="/usr/lib/jvm/java-11-openjdk /usr/lib/jvm/java-16-graalvm /usr/lib/jvm/java-11-graalvm /usr/lib/jvm/zulu-16"

mkdir -p perf

for JAVA_HOME in $JDKS; do
  jdkName=$(basename "$JAVA_HOME")
  echo "Running with $JAVA_HOME"
  export JAVA_HOME
  ./gradlew clean build
  ./gradlew runPathPlanningBenchmark | tee "perf/${jdkName}.log" 
done

for p in perf/*.log; do
  echo "$p"
  tail -n5 "$p" | head -n2
done
