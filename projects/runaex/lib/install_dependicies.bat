set BASEDIR=%~dp0
set LIB=%BASEDIR%..\lib
@echo BASEDIR=%BASEDIR%
@echo LIB=%LIB%
call mvn install:install-file -DgroupId=freemarker -DartifactId=freemarker -Dversion=2.3.11  -Dpackaging=jar -Dfile=%LIB%/freemarker-2.3.11.jar
call mvn install:install-file -DgroupId=wfe -DartifactId=wfe -Dversion=1.0 -Dpackaging=jar -Dfile=%LIB%/wfe-1.0.jar
call mvn install:install-file -DgroupId=wfe-custom -DartifactId=wfe-custom -Dversion=1.0 -Dpackaging=jar -Dfile=%LIB%/wfe-custom-1.0.jar
call mvn install:install-file -DgroupId=jsp-api -DartifactId=jsp-api -Dversion=1.0 -Dpackaging=jar -Dfile=%LIB%/jsp-api-1.0.jar
call mvn install:install-file -DgroupId=ecs -DartifactId=ecs -Dversion=1.4.2 -Dpackaging=jar -Dfile=%LIB%/ecs-1.4.2.jar
call mvn install:install-file -DgroupId=runa-common -DartifactId=runa-common -Dversion=1.0 -Dpackaging=jar -Dfile=%LIB%/runa-common-1.0.jar

call mvn install:install-file -DgroupId=runawfe-ws-client -DartifactId=runawfe-ws-client -Dversion=3.4.2  -Dpackaging=jar -Dfile=%LIB%/runawfe-ws-client.3.4.2.jar
call mvn install:install-file -DgroupId=wf.service -DartifactId=wf.service -Dversion=1.0 -Dpackaging=jar -Dfile=%LIB%/wf.service-1.0.jar
call mvn install:install-file -DgroupId=jbpm.core -DartifactId=jbpm.core -Dversion=1.0 -Dpackaging=jar -Dfile=%LIB%/jbpm.core-1.0.jar
call mvn install:install-file -DgroupId=wf.logic -DartifactId=wf.logic -Dversion=1.0 -Dpackaging=jar -Dfile=%LIB%/wf.logic-1.0.jar

call mvn install:install-file -DgroupId=liquibase -DartifactId=liquibase -Dversion=2.0.5 -Dpackaging=jar -Dfile=%LIB%/liquibase-2.0.5.jar