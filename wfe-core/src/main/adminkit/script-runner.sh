#!/bin/sh
########Configuration###############
SCRIPT_PATH="scripts/deploy-samples-script.xml"
LOGIN="Administrator"
PASSWORD="wf"
########End of Configuration###############

CLASSPATH="./conf:./lib/wfe-service.jar:./lib/wfe-core.jar:./lib/jbossall-client.jar:./lib/commons-logging-1.1.0.jar:./lib/commons-io-1.2.jar:./lib/guava-14.0.1.jar"

if [ "$JAVA_HOME" != "" ]
then
	JAVA=$JAVA_HOME/bin/java
else
	JAVA=java
fi

$JAVA -cp ${CLASSPATH} ru.runa.wfe.service.client.AdminScriptClient ${SCRIPT_PATH} ${LOGIN} ${PASSWORD}
