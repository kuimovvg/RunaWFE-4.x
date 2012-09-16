@echo off

REM ########Configuration###############

set SCRIPT_PATH="scripts/deploy-samples-script.xml"

set LOGIN="Administrator"
set PASSWORD="wf"

REM ########End of Configuration###############


set CLASSPATH=".\conf;.\lib\runa-common.jar;.\lib\af.delegate.jar;.\lib\wf.delegate.jar;.\lib\wf.core.jar;.\lib\af.core.jar;.\lib\jbossall-client.jar;.\lib\commons-logging-1.1.0.jar;.\lib\commons-io-1.2.jar"

java -cp %CLASSPATH% ru.runa.wf.delegate.impl.WfeScriptClient %SCRIPT_PATH% %LOGIN% %PASSWORD%



