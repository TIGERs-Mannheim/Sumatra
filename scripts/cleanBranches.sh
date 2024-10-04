#!/usr/bin/env bash

if [[ "$1" == "-h" ]] || [[ "$1" == "--help" ]]; then
    echo "Remove all local branches that do not have any upstream branch"
    echo "Be careful! You may delete your own private local branches, too!"
    echo "For security, you have to pass -x to actually execute the commands."
    exit 0
fi

DRY_RUN="echo"
if [[ "$1" == "-x" ]]; then
	DRY_RUN=
fi

git fetch -p 
for branch in $(git branch -vv | grep ': gone]' | gawk '{print $1}')
do 
	${DRY_RUN} git branch -D "${branch}"
done

if [[ -n "$DRY_RUN" ]]; then
    echo "Note: This was only a dry-run. The printed commands will be executed when you pass -x to this script"
fi