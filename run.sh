#!/usr/bin/env bash

if [[ ! -d "modules/sumatra-main/target" ]]; then
    echo "Make sure to run ./build.sh first! It must also be executed after a code update or you will run with the old version."
    exit 1
fi

args="$@"

export CLASSPATH="modules/sumatra-main/target/lib/*"
JAVA_OPTS="-Xms64m -Xmx4G -server -Xnoclassgc -Xverify:none -Dsun.java2d.d3d=false -XX:+UseConcMarkSweepGC -Djava.net.preferIPv4Stack=true -XX:-OmitStackTraceInFastThrow -XX:+AggressiveOpts"

java ${JAVA_OPTS} edu.tigers.sumatra.Sumatra $args
