CALL mvn install:install-file -Dfile=jcifs-1.0.0.jar -DgroupId=jcifs -DartifactId=jcifs -Dversion=1.0.0 -Dpackaging=jar -DgeneratePom=true
CALL mvn install:install-file -Dfile=freemarker.patched-2.3.11.jar -DgroupId=freemarker -DartifactId=freemarker.patched -Dversion=2.3.11 -Dpackaging=jar -DgeneratePom=true
CALL mvn install:install-file -Dfile=jacob-1.0.jar -DgroupId=com.activex -DartifactId=jacob -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true
CALL mvn install:install-file -Dfile=jcom-1.0.jar -DgroupId=com.activex -DartifactId=jcom -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true

pause