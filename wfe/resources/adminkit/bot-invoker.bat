@echo off

set CLASSPATH=".\conf;.\lib\af.core.jar;.\lib\wfe-bot.jar;.\lib\af.delegate.jar;.\lib\runa-common.jar;.\lib\jbossall-client.jar;.\lib\commons-logging-1.1.0.jar;.\lib\commons-io-1.2.jar"

java -cp %CLASSPATH% ru.runa.af.delegate.bot.impl.BotInvokerServiceDelegateRemoteImpl %1
EXIT /B ERRORLEVEL
