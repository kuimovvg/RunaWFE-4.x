set BASEDIR=%~dp0
set LIB=%BASEDIR%..\ws
@echo BASEDIR=%BASEDIR%
@echo LIB=%LIB%
call mvn install:install-file -DgroupId=runawfe-ws-client -DartifactId=runawfe-ws-client -Dversion=3.4.2  -Dpackaging=jar -Dfile=%LIB%/runawfe-ws-client.3.4.2.jar
call mvn install:install-file -DgroupId=wf.core -DartifactId=wf.core -Dversion=1.0 -Dpackaging=jar -Dfile=%LIB%/wf.core-1.0.jar
call mvn install:install-file -DgroupId=wf.delegate -DartifactId=wf.delegate -Dversion=1.0 -Dpackaging=jar -Dfile=%LIB%/wf.delegate-1.0.jar
call mvn install:install-file -DgroupId=af.delegate -DartifactId=af.delegate -Dversion=1.0 -Dpackaging=jar -Dfile=%LIB%/af.delegate-1.0.jar
call mvn install:install-file -DgroupId=jcifs -DartifactId=jcifs -Dversion=1.0 -Dpackaging=jar -Dfile=%LIB%/jcifs-1.0.jar
call mvn install:install-file -DgroupId=af.core -DartifactId=af.core -Dversion=1.0 -Dpackaging=jar -Dfile=%LIB%/af.core-1.0.jar
call mvn install:install-file -DgroupId=af.logic -DartifactId=af.logic -Dversion=1.0 -Dpackaging=jar -Dfile=%LIB%/af.logic-1.0.jar
call mvn install:install-file -DgroupId=locommons-logging -DartifactId=commons-logging -Dversion=1.0 -Dpackaging=jar -Dfile=%LIB%/commons-logging-1.0.jar
call mvn install:install-file -DgroupId=gson -DartifactId=gson -Dversion=2.1 -Dpackaging=jar -Dfile=%LIB%/gson-2.1.jar
call mvn install:install-file -DgroupId=el-api -DartifactId=el-api -Dversion=1.0 -Dpackaging=jar -Dfile=%LIB%/el-api-1.0.jar


