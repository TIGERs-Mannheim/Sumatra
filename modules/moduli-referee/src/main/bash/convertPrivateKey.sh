#!/usr/bin/env bash
#
# The official tool from the SSL-Game-Controller Repository
# generates a private key that cannot be read with Java by default
# The solution is to convert it to the PKCS#8 format.
#
# To use this skript you need the original key and the openssl
# tool installed
PRIVATE_KEY=$1

openssl pkcs8 -topk8 -in "$PRIVATE_KEY" -out "src/main/resources/keys/TIGERs-Mannheim.key.pem.pkcs8" -nocrypt
