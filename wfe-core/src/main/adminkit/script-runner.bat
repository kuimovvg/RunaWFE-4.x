@echo off

REM ########Configuration###############

set SCRIPT_PATH="scripts/deploy-samples-script.xml"

set LOGIN="Administrator"
set PASSWORD="wf"

REM ########End of Configuration###############


set CLASSPATH=".\conf;.\lib\wfe.service.jar;.\lib\wfe.core.jar;.\lib\jbossall-client.jar;.\lib\commons-logging-1.1.0.jar;.\lib\commons-io-1.2.jar;.\lib\guava-12.0.jar"

java -cp %CLASSPATH% ru.runa.service.client.AdminScriptClient %SCRIPT_PATH% %LOGIN% %PASSWORD%



