#!/usr/bin/env bash

export MVN_OPTIONS='-B -Dmaven.repo.local=repository'

# build once
mvn ${MVN_OPTIONS} clean install -Pfast || exit 1

i=0
while true; do
    i=$((i+1))

    if mvn ${MVN_OPTIONS} -PintegrationTests -pl modules/sumatra-ai-integration-test test; then
        echo "Run $i succeeded"
    else
        echo "Run $i failed"
        exit 1
    fi
done