#!/bin/sh

export LANG=en_US.UTF8
export LC_ALL=en_US.UTF8

if [ ! -d ~/.runawfe/simulation ]; then
    mkdir -p ~/.runawfe/simulation/data
    mkdir -p ~/.runawfe/simulation/log
    mkdir -p ~/.runawfe/simulation/tmp
    mkdir -p ~/.runawfe/simulation/work
    cp -Rf /usr/share/runawfe-jboss/server/runawfe-simulation/data/* ~/.runawfe/simulation/data/
fi

(sleep 180 && cd /usr/share/runawfe-jboss/adminkit && bash bot-invoker.sh start) &

cd /usr/share/runawfe-jboss/bin && exec ./run.sh -Djboss.server.log.dir=$HOME/.runawfe/simulation/log -Djboss.server.temp.dir=$HOME/.runawfe/simulation/tmp -Djboss.server.data.dir=$HOME/.runawfe/simulation/data -Djboss.server.home.dir=$HOME/.runawfe/simulation -c runawfe-simulation -b 0.0.0.0

