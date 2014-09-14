if "%1"=="" (
echo Version must be specified!!!
exit
)

rd /S /Q build
mkdir build

copy jboss7.zip build
copy eclipse-3.7.2-with-deltapack.zip build
copy run.bat build
copy createIso.bat build
copy readme build

REM start unrar x eclipse.rar 
REM unrar x -tsm -tsc -tsa trunk.rar

cd /D build
jar xf eclipse-3.7.2-with-deltapack.zip
del eclipse-3.7.2-with-deltapack.zip
mkdir trunk
cd trunk
svn co https://svn.code.sf.net/p/runawfe/code/RunaWFE-4.x/trunk/projects
cd ..
mkdir trunk\docs
mkdir trunk\docs\guides
copy readme trunk\docs\guides\

cd trunk\projects\installer\windows\
call mvn versions:set -DnewVersion=%1
cd ../../wfe/wfe-appserver
call mvn versions:set -DnewVersion=%1
cd ../wfe-webservice-client
call mvn versions:set -DnewVersion=%1
cd ../wfe-alfresco
call mvn versions:set -DnewVersion=%1
cd ../wfe-app
call mvn versions:set -DnewVersion=%1
cd ../../rtn
call mvn versions:set -DnewVersion=%1

cd ..\installer\windows\
copy ..\..\..\..\run.bat .
copy ..\..\..\..\createIso.bat .

call run.bat