#!/bin/sh

########Configuration###############
LOGIN="Administrator"
PASSWORD="wf"
########End of Configuration###############

CLASSPATH="./conf:./lib/runa-common.jar:./lib/af.delegate.jar:./lib/wf.delegate.jar:./lib/wf.core.jar:./lib/af.core.jar:./lib/commons-logging-1.1.0.jar:./lib/commons-io-1.2.jar"

java -cp ${CLASSPATH} ru.runa.af.delegate.impl.LDAPImporterClient ${LOGIN} ${PASSWORD}
