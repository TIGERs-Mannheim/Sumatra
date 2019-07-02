#!/usr/bin/env bash

args="${@}"
mvn install -Pfast -Dmaven.repo.local=repository ${args}
