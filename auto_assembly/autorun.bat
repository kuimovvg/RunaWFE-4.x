if "%1"=="" (
echo Version must be specified!!!
exit
)

if "%2"=="" (
echo Arch must be specified!!!
exit
)

rd /S /Q build
rd /S /Q results
mkdir build
mkdir results

copy jboss7.zip build
copy eclipse-3.7.2-with-deltapack.zip build
copy readme build

REM start unrar x eclipse.rar 
REM unrar x -tsm -tsc -tsa trunk.rar

cd /D build
jar xf eclipse-3.7.2-with-deltapack.zip
del eclipse-3.7.2-with-deltapack.zip

mkdir trunk
svn export https://svn.code.sf.net/p/runawfe/code/RunaWFE-4.x/trunk/projects trunk/projects
rem svn export svn://alcomputer/RunaWFE-4.x/trunk/projects

mkdir trunk\docs
mkdir trunk\docs\guides
copy readme trunk\docs\guides\

if "%2"=="64" (
    del trunk\projects\installer\windows\resources\jdk-7u17-windows-i586.exe 
    copy ..\jdk\jdk-7u7-windows-x64.exe trunk\projects\installer\windows\resources\jdk-7u17-windows-i586.exe 
)

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
cd ../../wfe/wfe-cactus-it
call mvn versions:set -DnewVersion=%1

cd ..\installer\windows\

call mvn clean package -Djboss.zip.file=../../../../jboss7.zip -Djboss.zip.folder=jboss7 -Declipse.home.dir=../../../../eclipse -Dappserver=jboss7 -Djdk.dir="%~dp0%jdk"

xcopy /E /Q target\test-result ..\..\..\..\..\results\test-result\
mkdir ..\..\..\..\..\results\Execution
copy target\artifacts\Installer\RunaWFE-Installer.exe ..\..\..\..\..\results\Execution\
mkdir ..\..\..\..\..\results\ISO
copy target\installer.iso ..\..\..\..\..\results\ISO\