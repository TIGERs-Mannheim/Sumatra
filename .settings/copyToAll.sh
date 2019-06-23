#!/bin/bash

for folder in `ls ../modules`
do
	cp *.prefs ../modules/$folder/.settings
done
