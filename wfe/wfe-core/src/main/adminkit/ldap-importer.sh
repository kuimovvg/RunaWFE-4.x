#!/bin/sh

########Configuration###############
LOGIN="Administrator"
PASSWORD="wf"
########End of Configuration###############

CLASSPATH="./conf:./lib/wfe.service.jar:./lib/wfe.core.jar:./lib/jbossall-client.jar:./lib/commons-logging-1.1.0.jar:./lib/commons-io-1.2.jar:./lib/guava-12.0.jar"

java -cp ${CLASSPATH} ru.runa.wfe.service.client.LDAPImporterClient ${LOGIN} ${PASSWORD}
