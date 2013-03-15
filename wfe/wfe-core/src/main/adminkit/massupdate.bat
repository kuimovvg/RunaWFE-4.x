@echo off

REM ########Configuration###############

set LOGIN="Administrator"
set PASSWORD="wf"

REM ########End of Configuration###############


set CLASSPATH=".\conf;.\lib\wfe-service.jar;.\lib\wfe-core.jar;.\lib\jbossall-client.jar;.\lib\commons-logging-1.1.0.jar;.\lib\commons-io-1.2.jar;.\lib\guava-12.0.jar;./lib/dom4j-1.6.1.jar"

java -cp %CLASSPATH% ru.runa.wfe.service.client.ProcessDefinitionsMassUpdate massupdate %LOGIN% %PASSWORD%



