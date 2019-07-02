#!/usr/bin/env bash

version="$1"
if [[ -z "$version" ]]; then
    echo "Pass the target version, e.g. '2019' to this script"
    exit 1
fi

mvn release:update-versions -DdevelopmentVersion="${version}-SNAPSHOT"

sed -ie "s/VERSION = \"(.*)\"/VERSION = \"${version}\"/" modules/sumatra-model/src/main/java/edu/tigers/sumatra/model/SumatraModel.java