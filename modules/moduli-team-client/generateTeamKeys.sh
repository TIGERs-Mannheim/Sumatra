#!/bin/sh
#
# This script will generate a (new) set of keys for the TIGERs
# team. Those will be used for secure team communication with
# the SSL GameController
#
for team in "TIGERs Mannheim" "YELLOW AI" "BLUE AI"
do
	DIR="src/main/resources/edu/tigers/sumatra"
	PRIVATE="$DIR/$team-team.key.pem"
	PRIVATE_ALT="$PRIVATE.pkcs8"
	PUBLIC="$DIR/$team-team.pub.pem"

	mkdir -p $DIR

	openssl genrsa -out "$PRIVATE" 2048

	openssl rsa -in "$PRIVATE" -outform PEM -pubout -out "$PUBLIC"

	openssl pkcs8 -topk8 -in "$PRIVATE" -out "$PRIVATE_ALT" -nocrypt
done
