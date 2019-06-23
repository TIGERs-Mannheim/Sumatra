#!/bin/bash

for dir in `ls`; do
	if [ -d "$dir/.settings" ]; then
		echo "processing $dir"
		cp module-template/.settings/* $dir/.settings/
	fi
done
