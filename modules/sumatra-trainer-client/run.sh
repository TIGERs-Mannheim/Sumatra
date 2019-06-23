#!/bin/bash
# BUILD
mvn clean install -f ../../ -DskipTests
mvn docker:build

# RUN
docker rm sumatra-trainer-client
docker run --name sumatra-trainer-client --network sumatra-trainer-net sumatra-trainer-client
