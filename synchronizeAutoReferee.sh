#!/bin/bash

PROJECTS="autoreferee-gui autoreferee-main common common-bot
          common-clock common-gui common-gui-config common-model
	  common-math
	  moduli-autoreferee moduli-cam moduli-referee
	  moduli-timer moduli-wp sumatra-gui-log
	  sumatra-gui-visualizer moduli-vision moduli-geometry
	  moduli-record sumatra-parent moduli-botmanager moduli-bot-params moduli-cam-logfile moduli-gamelog"

EXEC_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

SUMATRA_DIR="${EXEC_DIR}/"
AUTOREF_DIR="$1"
SUMATRA_MODULE_DIR="${SUMATRA_DIR}/modules"
AUTOREF_MODULE_DIR="${AUTOREF_DIR}/modules"

if [ ! -r "$AUTOREF_DIR/pom.xml" ]; then
	echo "AutoRef dir is invalid"
	exit 1
fi

if ! grep "<name>Autoreferee</name>" $AUTOREF_DIR/pom.xml &> /dev/null; then
	echo "AutoRef dir is invalid"
	exit 1
fi


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

cp $SUMATRA_DIR/config/moduli/moduli_autoreferee.xml $AUTOREF_DIR/config/moduli/moduli.xml
