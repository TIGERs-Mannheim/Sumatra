#!/usr/bin/env bash

#
# Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
#

set -e

if [[ -z "${SUMATRA_IMAGE_SIMULATOR}" ]]; then
  export SUMATRA_IMAGE_SIMULATOR="registry.gitlab.tigers-mannheim.de/main/sumatra:latest"
fi
if [[ -z "${SUMATRA_IMAGE_BLUE}" ]]; then
  export SUMATRA_IMAGE_BLUE="registry.gitlab.tigers-mannheim.de/main/sumatra:latest"
fi
if [[ -z "${SUMATRA_IMAGE_YELLOW}" ]]; then
  export SUMATRA_IMAGE_YELLOW="registry.gitlab.tigers-mannheim.de/main/sumatra:latest"
fi
if [[ -z "${SUMATRA_TARGET_STAGE_TIME}" ]]; then
  export SUMATRA_TARGET_STAGE_TIME="0"
fi
if [[ -z "${SUMATRA_TIMEOUT}" ]]; then
  export SUMATRA_TIMEOUT="900"
fi
if [[ -z "${NUM_SIMULATIONS}" ]]; then
  NUM_SIMULATIONS=1
fi
if [[ -z "${PRODUCTIVE}" ]]; then
  PRODUCTIVE=false
fi
if [[ -z "${PROJECT_NAME}" ]]; then
  PROJECT_NAME=sumatra
fi

if [ "${PRODUCTIVE}" = true ]; then
  export SUMATRA_AI_ADDITIONAL_ARGS="--productive"
  export SUMATRA_SIMULATOR_ADDITIONAL_ARGS="--productive"
else
  export SUMATRA_AI_ADDITIONAL_ARGS=""
  export SUMATRA_SIMULATOR_ADDITIONAL_ARGS=""
fi

echo "Running ${PROJECT_NAME} with:"
echo "SUMATRA_IMAGE_SIMULATOR: ${SUMATRA_IMAGE_SIMULATOR}"
echo "SUMATRA_IMAGE_BLUE: ${SUMATRA_IMAGE_BLUE}"
echo "SUMATRA_IMAGE_YELLOW: ${SUMATRA_IMAGE_YELLOW}"
echo "SUMATRA_TARGET_STAGE_TIME: ${SUMATRA_TARGET_STAGE_TIME}"
echo "SUMATRA_TIMEOUT: ${SUMATRA_TIMEOUT}"
echo "SUMATRA_AI_ADDITIONAL_ARGS: ${SUMATRA_AI_ADDITIONAL_ARGS}"
echo "SUMATRA_SIMULATOR_ADDITIONAL_ARGS: ${SUMATRA_SIMULATOR_ADDITIONAL_ARGS}"
echo "NUM_SIMULATIONS: ${NUM_SIMULATIONS}"

export USER_ID="${UID}"
export GROUP_ID="${GID}"
export PROJECT_NAME

# Update images
docker compose -p "${PROJECT_NAME}_simulation" -f docker-compose.simulation.yml pull || echo "Could not pull all images. That's fine for local images"

if [[ "${USE_LOCAL_STATISTICS}" == true ]]; then
  docker compose -p "sumatra_stats" -f docker-compose.statistics.yml up -d
fi

# Create data folders
mkdir -p "temp/${PROJECT_NAME}/yellow/data"
mkdir -p "temp/${PROJECT_NAME}/blue/data"
mkdir -p "temp/${PROJECT_NAME}/simulator/data"
mkdir -p "temp/${PROJECT_NAME}/simulator/build"

for i in $(seq 1 $NUM_SIMULATIONS); do
  echo "Starting simulation $i of $NUM_SIMULATIONS"
  # Start containers, rebuilding if necessary and close all when simulation finished
  docker compose -p "${PROJECT_NAME}_simulation" -f docker-compose.simulation.yml up --abort-on-container-exit
done

# Stop everything
docker compose -p "${PROJECT_NAME}_simulation" -f docker-compose.simulation.yml down
