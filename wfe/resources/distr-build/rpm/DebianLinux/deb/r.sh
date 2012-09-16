#!/bin/sh
mkdir -p `pwd`/debian/tmp/usr
mkdir -p `pwd`/debian/tmp/etc

cp -Rf usr `pwd`/debian/tmp/
cp -Rf etc `pwd`/debian/tmp/
