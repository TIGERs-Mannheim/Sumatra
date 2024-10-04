#!/usr/bin/env bash

set -euo pipefail

shopt -s nullglob
shopt -s globstar
for report in **/jacocoTestReport.xml
do
  echo "Processing $report"
  reportDir="$(dirname "$report")"
  source="$reportDir/../../../../src/main/java"
  source=$(realpath "$source")
  python "./scripts/cover2cover/cover2cover.py" "$report" "$source" > "$reportDir/cobertura.xml"
done
