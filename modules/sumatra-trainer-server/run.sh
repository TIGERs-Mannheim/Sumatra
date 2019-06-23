#!/bin/bash
# BUILD
mvn install -DskipTests -f ../../
mvn docker:build

# RUN
docker rm sumatra-trainer-server
docker run --name sumatra-trainer-server --network sumatra-trainer-net -p 8080:8080 sumatra-trainer-server
