#!/usr/bin/env bash

set -e

readonly target_folder="${1:-../Sumatra-oss}"

# clone to a clean folder to avoid local files in the release
rm -rf release
git clone . release

cd release

# Remove private keys from release
find modules -name "TIGERs*.pem*" -exec rm {} +

# remove GIT
rm -rf .git

# remove some files
rm -rf .mailmap ./.gitlab-ci.yml ./CONTRIBUTING.md gitlab-known-hosts

# remove gradle cache
rm -rf  .gradle

# create archive
tar czf ../Sumatra.tar.gz .

# Copy to OSS repository
cd ..
if [[ -d "${target_folder}" ]]; then
  cd "${target_folder}"
  rm -rf ./*
  tar xf ../Sumatra/Sumatra.tar.gz .
fi
