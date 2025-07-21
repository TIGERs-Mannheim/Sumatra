#!/usr/bin/env bash

#
# Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
#

set -euo pipefail

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
cd "${SCRIPT_DIR}/.."

if [[ -n "${1:-}" ]]; then
  export COMPOSE_PROJECT_NAME="$1"
fi

docker compose --profile watch up
