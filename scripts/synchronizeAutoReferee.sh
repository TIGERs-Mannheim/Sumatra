#!/usr/bin/env bash

#
# Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
#

set -euo pipefail

PROJECTS="
autoreferee-gui
moduli-autoreferee
moduli-autoreferee-ci
common
common-bot
common-gui
common-gui-config
common-math
sumatra-model
sumatra-snapshot
moduli-cam
moduli-referee
moduli-wp
moduli-vision
moduli-gamelog
moduli-geometry
moduli-record
sumatra-gui-log
sumatra-gui-visualizer
sumatra-gui-replay
sumatra-gui-referee
"

SUMATRA_DIR="$(pwd)"
AUTOREF_DIR="$1"
SUMATRA_MODULE_DIR="${SUMATRA_DIR}/modules"
AUTOREF_MODULE_DIR="${AUTOREF_DIR}/modules"

if [[ ! -r "$AUTOREF_DIR/build.gradle" ]]; then
  echo "AutoRef dir is invalid"
  exit 1
fi

if [[ ! -d "${SUMATRA_MODULE_DIR}" ]]; then
  echo "Sumatra module folders do not exist: ${SUMATRA_MODULE_DIR}"
  exit 1
fi

if [[ ! -d "${AUTOREF_MODULE_DIR}" ]]; then
  echo "AutoReferee module folders do not exist: ${AUTOREF_MODULE_DIR}"
  exit 1
fi

# Delete all modules first
rm -r "${AUTOREF_MODULE_DIR:?}"
mkdir "${AUTOREF_MODULE_DIR:?}"

# Then copy each listed module over
for proj in ${PROJECTS}; do
  source="${SUMATRA_MODULE_DIR}/${proj}"
  target="${AUTOREF_MODULE_DIR}/${proj}"
  cp -r "${source}" "${target}"
done

rm -r "${AUTOREF_DIR}/buildSrc"
cp -r "${SUMATRA_DIR}/buildSrc" "${AUTOREF_DIR}"
cp -r "${SUMATRA_DIR}/gradle" "${AUTOREF_DIR}"
cp "${SUMATRA_DIR}/config/moduli/autoreferee.xml" "${AUTOREF_DIR}/config/moduli/moduli.xml"
cp "${SUMATRA_DIR}/config/moduli/autoreferee-ci.xml" "${AUTOREF_DIR}/config/moduli/moduli-ci.xml"
cp "${SUMATRA_DIR}/config/ssl-game-controller.yaml" "${AUTOREF_DIR}/config/ssl-game-controller.yaml"
cp "${SUMATRA_DIR}/src/main/jib/config/engine-default.yaml" "${AUTOREF_DIR}/src/main/jib/config/engine-default.yaml"
