#!/usr/bin/env bash

#
# Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
#

set -e

if [[ ! -f gradlew ]]; then
  echo "Error: Must be executed from root dir"
  exit 1
fi

if [[ -n "$1" ]]; then
  export PROJECT_NAME="$1"
fi

docker compose -f ./docker-compose.simulation.watch.yml up
