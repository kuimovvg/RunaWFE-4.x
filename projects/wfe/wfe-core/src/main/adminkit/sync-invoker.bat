@echo off

set CLASSPATH=".\conf;.\lib\runawfe-specific.jar;.\lib\wfe-service.jar;.\lib\wfe-core.jar;.\lib\jbossall-client.jar;.\lib\commons-logging-1.1.0.jar;.\lib\commons-io-1.2.jar;.\lib\guava-12.0.jar"

java -cp %CLASSPATH% ru.runa.wfe.synchronizer.AlfSynchronizerBeanClient login=Administrator password=wf url=jnp://localhost:10099 synchronizeActors
EXIT /B ERRORLEVEL
