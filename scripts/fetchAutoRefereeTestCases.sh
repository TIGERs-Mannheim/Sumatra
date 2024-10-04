#!/usr/bin/env bash

#
# Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
#

set -euo pipefail

if ! git lfs --version; then
  echo "Git LFS must be installed" >&2
  exit 1
fi

TARGET_DIR="modules/moduli-autoreferee/config/autoref-tests"

if [[ ! -d "${TARGET_DIR}" ]]; then
  git clone "https://gitlab.com/robocup-small-size/autoref-tests.git" "${TARGET_DIR}"
else
  git -C "${TARGET_DIR}" pull
fi

git -C "${TARGET_DIR}" lfs pull
