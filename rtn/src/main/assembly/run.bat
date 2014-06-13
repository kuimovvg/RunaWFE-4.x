set DIRNAME=.\
if "%OS%" == "Windows_NT" set DIRNAME=%~dp0%
cd /D "%DIRNAME%"
if "%JAVA_HOME%"=="" start javaw -Drtn.log.dir="%APPDATA%\runawfe" -cp .;rtn.jar ru.runa.notifier.PlatformLoader
if NOT "%JAVA_HOME%"=="" start "wnd" "%JAVA_HOME%\bin\javaw" -Drtn.log.dir="%APPDATA%\runawfe" -cp .;rtn.jar ru.runa.notifier.PlatformLoader
