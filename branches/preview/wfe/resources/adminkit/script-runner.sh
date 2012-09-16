#!/bin/sh
########Configuration###############
SCRIPT_PATH="scripts/deploy-samples-script.xml"
LOGIN="Administrator"
PASSWORD="wf"
########End of Configuration###############

CLASSPATH="./conf:./lib/runa-common.jar:./lib/af.delegate.jar:./lib/wf.delegate.jar:./lib/wf.core.jar:./lib/af.core.jar:./lib/jbossall-client.jar:./lib/commons-logging-1.1.0.jar:./lib/commons-io-1.2.jar"

if [ "$JAVA_HOME" != "" ]
then
	JAVA=$JAVA_HOME/bin/java
else
	JAVA=java
fi

$JAVA -cp ${CLASSPATH} ru.runa.wf.delegate.impl.WfeScriptClient ${SCRIPT_PATH} ${LOGIN} ${PASSWORD}
