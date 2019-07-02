#!/usr/bin/env bash

maven_version=$(grep -o "<version>.*</version>" pom.xml | grep -o "[0-9\.]*")
git_hash=$(git rev-parse --short HEAD)
full_version="${maven_version}.${git_hash}"
echo "${full_version}"