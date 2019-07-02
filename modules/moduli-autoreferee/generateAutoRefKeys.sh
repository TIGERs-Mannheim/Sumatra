#!/bin/sh
#
# This script will generate a (new) set of keys for the TIGERs
# autoref
#
DIR="src/main/resources/keys"
PRIVATE="$DIR/TIGERs-Mannheim-autoRef.key.pem"
PRIVATE_ALT="$PRIVATE.pkcs8"
PUBLIC="$DIR/TIGERs-Mannheim-autoRef.pub.pem"

mkdir -p $DIR

openssl genrsa -out "$PRIVATE" 2048

openssl rsa -in "$PRIVATE" -outform PEM -pubout -out "$PUBLIC"

openssl pkcs8 -topk8 -in "$PRIVATE" -out "$PRIVATE_ALT" -nocrypt
