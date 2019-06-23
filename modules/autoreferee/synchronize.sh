#!/bin/bash

PROJECTS="autoreferee-gui autoreferee-main common common-bot
          common-clock common-gui common-gui-config common-model
	  moduli-autoreferee moduli-cam moduli-referee
	  moduli-timer moduli-wp moduli-wp-kalman sumatra-gui-log
	  sumatra-gui-visualizer"

if [ ! -r "pom.xml" ]; then
	echo "Please cd into the AutoRef repository root"
	exit 1
fi

if ! grep "<name>Autoreferee</name>" pom.xml &> /dev/null; then
	echo "Please cd into the AutoRef repository root"
	exit 1
fi

EXEC_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

SUMATRA_DIR="${EXEC_DIR}/../../"
AUTOREF_DIR="$(pwd)"
SUMATRA_MODULE_DIR="${SUMATRA_DIR}/modules"
AUTOREF_MODULE_DIR="${AUTOREF_DIR}/modules"

if [ ! -d ${SUMATRA_MODULE_DIR} ] || [ ! -d ${AUTOREF_MODULE_DIR} ]; then
	echo "Module folders do not exist!"
	exit 1
fi

for proj in $PROJECTS; do
	source="${SUMATRA_MODULE_DIR}/${proj}"
	target="${AUTOREF_MODULE_DIR}/${proj}"
	if [ -d "${target}" ]; then
		rm -r "${target}"
	fi
	cp -r "${source}" "${target}"
done

cp "${EXEC_DIR}/README.md" "${AUTOREF_DIR}"
cp "${EXEC_DIR}/license.txt" "${AUTOREF_DIR}"

