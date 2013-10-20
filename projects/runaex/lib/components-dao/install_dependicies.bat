set BASEDIR=%~dp0
set LIB=%BASEDIR%
@echo BASEDIR=%BASEDIR%
@echo LIB=%LIB%
call mvn install:install-file -DgroupId=postgresql-jdbc4 -DartifactId=postgresql-jdbc4 -Dversion=9.1 -Dpackaging=jar -Dfile=%LIB%/postgresql-jdbc4-9.1.jar