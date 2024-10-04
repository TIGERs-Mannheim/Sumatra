#!/usr/bin/env bash

#
# Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
#

set -e

if [[ ! -f gradlew ]]; then
  echo "Error: Must be executed from root dir"
  exit 1
fi

# Build the docker image
./gradlew jibDockerBuild

if [[ -z "${SUMATRA_INFLUX_DB_URL}" ]]; then
  # Use local InfluxDB
  export SUMATRA_INFLUX_DB_URL=http://influxdb:8086
  export USE_LOCAL_STATISTICS=true
fi
if [[ -z "${SUMATRA_IMAGE_BLUE}" ]]; then
  # Use latest local image for blue
  export SUMATRA_IMAGE_BLUE=sumatra:latest
fi
if [[ -z "${SUMATRA_IMAGE_YELLOW}" ]]; then
  # Use latest local image for yellow
  export SUMATRA_IMAGE_YELLOW=sumatra:latest
fi
if [[ -z "${SUMATRA_IMAGE_SIMULATOR}" ]]; then
  # Use latest local image for simulation
  export SUMATRA_IMAGE_SIMULATOR=sumatra:latest
fi
if [[ -z "${PRODUCTIVE}" ]]; then
  # Enable recording
  export PRODUCTIVE=true
fi
if [[ -z "${NUM_SIMULATIONS}" ]]; then
  # Set number of simulations
  export NUM_SIMULATIONS=1
fi
if [[ -n "$1" ]]; then
  export PROJECT_NAME="$1"
fi

# Start simulation
./scripts/runDockerSimulation.sh
