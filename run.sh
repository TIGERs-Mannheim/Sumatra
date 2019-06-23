#!/bin/bash

EXEC_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

if ! type mvn &> /dev/null; then
	echo "Please add the maven executable to your \$PATH"
	exit 1
fi

if [ ! -d "repository" ]; then
    ./build.sh
fi

cd $EXEC_DIR

mvn -pl modules/sumatra-main exec:exec \
    -Dmaven.repo.local=repository \
    --no-snapshot-updates \
    -Dexec.args=" \
    -Xms128m -Xmx4G -server -Xnoclassgc -Xverify:none -Dsun.java2d.d3d=false -XX:+UseConcMarkSweepGC -Djava.net.preferIPv4Stack=true -XX:-OmitStackTraceInFastThrow -XX:+AggressiveOpts \
    -classpath %classpath \
    edu.tigers.sumatra.Sumatra"

if [ "$?" != "0" ]; then
	echo
	echo
	echo "Launching the program failed. Did you build the application using 'mvn install'???'"
fi
