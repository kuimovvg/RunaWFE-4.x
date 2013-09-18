set DIRNAME=.\
if "%OS%" == "Windows_NT" set DIRNAME=%~dp0%
cd /D "%DIRNAME%"
if "%JAVA_HOME%"=="" start javaw -cp ".;rtn.jar;swt-win32.jar" -Drtn.log.dir="%APPDATA%\runawfe" ru.runa.notifier.PlatformLoader
if NOT "%JAVA_HOME%"=="" start "wnd" "%JAVA_HOME%\bin\javaw" -cp .;rtn.jar;swt-win32.jar -Drtn.log.dir="%APPDATA%\runawfe" ru.runa.notifier.PlatformLoader
