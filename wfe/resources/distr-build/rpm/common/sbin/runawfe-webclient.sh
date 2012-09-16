#!/bin/sh

CONF_FILE
if [ -f ~/.runawfe-client.conf ]; then
    CONF_FILE=~/.runawfe-client.conf
else
    CONF_FILE=/etc/runawfe-client.conf
fi


ReplaceableURLHandler http://`grep WFEServer_webaddress $CONF_FILE | cut -f2- -d=`/wfe
