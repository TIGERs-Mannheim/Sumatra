#!/usr/bin/env bash

#
# Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
#

set -euo pipefail

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
cd "${SCRIPT_DIR}/.."

if [[ -n "${1:-}" ]]; then
  export COMPOSE_PROJECT_NAME="$1"
elif [[ -z "${COMPOSE_PROJECT_NAME:-}" ]]; then
  export COMPOSE_PROJECT_NAME=sumatra
fi

if [[ -z "${NUM_SIMULATIONS:-}" ]]; then
  NUM_SIMULATIONS=1
fi

for i in $(seq 1 "$NUM_SIMULATIONS"); do
  echo "Starting simulation $i of $NUM_SIMULATIONS"
  # Start containers, rebuilding if necessary and close all when simulation finished
  docker compose up --abort-on-container-exit
done
