@echo off

for /R %1 %%i in (*.jar) do (
	for %%j in ("%2\%%~ni*.jar") do (
		echo "%%~nj" | findstr /R /C:"\"%%~ni[-]*[0-9]*[\.]*[0-9]*[\.]*[0-9]*[\.]*[0-9]*\"" >nul
		if not errorlevel 1 (
			copy /Y "%%j" "%%i"
		)
	)
)

echo "Copy commons-codec.jar if found"
for %%j in ("%2\commons-codec*.jar") do (
	echo "%%~nj" | findstr /R /C:"\"commons-codec[-]*[0-9]*[\.]*[0-9]*[\.]*[0-9]*[\.]*[0-9]*\"" >nul
	if not errorlevel 1 (
		copy /Y "%%j" "%1\cactus\commons-codec.jar"
	)
)

