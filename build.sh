#!/usr/bin/env bash

mvn clean install -DskipTests -Dmaven.javadoc.skip=true -Dmaven.repo.local=repository
