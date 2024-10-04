#!/usr/bin/env bash

# Create an incremented merge version tag and push it

currentMergeNumber=$(git tag | grep merge/ | sed -e 's/merge\///' | sort -n | tail -n1)
nextMergeNumber=$((currentMergeNumber + 1))

tagName="merge/${nextMergeNumber}"
git tag "${tagName}"
git push origin "${tagName}"
