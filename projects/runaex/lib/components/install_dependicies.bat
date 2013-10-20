set BASEDIR=%~dp0
set LIB=%BASEDIR%
@echo BASEDIR=%BASEDIR%
@echo LIB=%LIB%
call mvn install:install-file -DgroupId=commons-logging -DartifactId=commons-logging -Dversion=1.0 -Dpackaging=jar -Dfile=%LIB%/commons-logging-1.0.jar