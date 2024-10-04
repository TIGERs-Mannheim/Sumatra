#!/usr/bin/env bash

#
# Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
#

# stop on error
set -e

INTERACTIVE=false

EXEC_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
SUMATRA_DIR="$EXEC_DIR/build/sumatra"
AUTO_REF_DIR="$EXEC_DIR/build/autoref"

if [[ -d "$SUMATRA_DIR" ]]; then
    rm -rf "$SUMATRA_DIR"
fi
git clone . "$SUMATRA_DIR"

if [[ -d "$AUTO_REF_DIR" ]]; then
    rm -rf "$AUTO_REF_DIR"
fi
git clone git@gitlab.tigers-mannheim.de:open-source/AutoReferee.git "$AUTO_REF_DIR"

cd "$AUTO_REF_DIR"
sumatraCommit=$(git log -100 --pretty=%B | grep -m1 "sumatra-commit: " | sed -e 's/sumatra-commit: //')
if [[ -z "$sumatraCommit" ]]; then
    echo "Using default sumatra commit"
    sumatraCommit="879ba08dc1b6cab480a54341796eaa739fd44168"
fi

echo "Latest sumatra commit: $sumatraCommit"

cd "$SUMATRA_DIR"
commits=$(git rev-list --merges --first-parent --reverse HEAD ^${sumatraCommit})

for rev in ${commits}; do
    cd "$SUMATRA_DIR"
    echo "Processing rev: ${rev}"
    git checkout "$rev" &> /dev/null
    ./scripts/synchronizeAutoReferee.sh "$AUTO_REF_DIR"
    git log -1 --pretty=%B | tail -n +3 > /tmp/message
    echo >> /tmp/message
    echo "sumatra-commit: ${rev}" >> /tmp/message
    authorName=$(git log -1 --pretty=%an)
    authorEmail=$(git log -1 --pretty=%ae)
    authorDate=$(git log -1 --pretty=%ad)
    cd "$AUTO_REF_DIR"
    git add . &>/dev/null
    if ! git diff-index --quiet HEAD --; then
        echo "Change detected. Using $authorName <$authorEmail> ${authorDate}"
        ${INTERACTIVE} && git diff HEAD
        if ${INTERACTIVE}; then GIT_COMMIT_EDIT="-e"; fi
        git commit ${GIT_COMMIT_EDIT} -F /tmp/message --author="$authorName <$authorEmail>" --date="${authorDate}"
    fi
done

echo "Sync done. After reviewing the changes, press enter to push changes to remote repo"
${INTERACTIVE} && read -r

cd "${AUTO_REF_DIR}"
git push

rm -rf "${AUTO_REF_DIR}" "$SUMATRA_DIR"
