#!/bin/sh

export LANG=en_US.UTF8
export LC_ALL=en_US.UTF8

cd /usr/share/runawfe-jboss/bin && exec ./run.sh -c runawfe-server -b 0.0.0.0 1>run.log 2>run.log
