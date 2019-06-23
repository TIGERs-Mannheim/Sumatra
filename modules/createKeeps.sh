#!/bin/sh

for d in *
do
	if [ -d "$d" ]; then
		touch $d/src/main/java/.keep
		touch $d/src/main/resources/.keep
		touch $d/src/test/java/.keep
		touch $d/src/test/resources/.keep
	fi
done
