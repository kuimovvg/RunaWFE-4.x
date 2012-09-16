#!/bin/sh

export LANG=en_US.UTF8
export LC_ALL=en_US.UTF8

(sleep 180 && cd /usr/share/runawfe-jboss/adminkit && bash bot-invoker.sh start) &

cd /usr/share/runawfe-jboss/bin && exec ./run.sh -c runawfe-botstation -b 0.0.0.0 1>/dev/null 2>/dev/null
