#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
cd "${SCRIPT_DIR}/.."

if [[ -n "${1:-}" ]]; then
  export COMPOSE_PROJECT_NAME="$1"
fi

docker compose --profile watch up
