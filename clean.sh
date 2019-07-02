#!/usr/bin/env bash

# remove Maven build directories
mvn clean

# remove maven local repository (all dependencies must be downloaded again!)
rm -rf repository