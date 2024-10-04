#!/usr/bin/env bash

i=0
while true; do
    i=$((i+1))

    echo "Starting run $i"
    if ./gradlew integrationTest --rerun; then
        echo "Run $i succeeded"
    else
        echo "Run $i failed"
        exit 1
    fi
done