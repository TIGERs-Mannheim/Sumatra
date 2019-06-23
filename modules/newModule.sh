#!/bin/sh

base=module-template
new=$1

cp -r $base $new

find $new -type f -exec sed -i "s/$base/$new/g" {} \;
