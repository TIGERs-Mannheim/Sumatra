#!/bin/bash

EXEC_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

if ! type mvn &> /dev/null; then
	echo "Please add the maven executable to your \$PATH"
	exit 1
fi

cd $EXEC_DIR

mvn -pl modules/sumatra-main exec:java

if [ "$?" != "0" ]; then
	echo
	echo
	echo "Launching the program failed. Did you build the application using 'mvn install'???'"
fi
