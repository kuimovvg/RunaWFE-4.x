!include "AddToPathEnvironment.nsh"

!define JAVA_VERSION "1.5.0"

Var InstallJava
Var JavaPath
Var IsJDKRequired

Var JdkInstaller

ReserveFile "InstallJDKPage.ini"

Function checkJDKinit
  call CheckInstalledJava
  StrCmp $InstallJava "yes" +2
  Abort
  ${if} "$IsJDKRequired" == 0
    !insertmacro MUI_INSTALLOPTIONS_WRITE "InstallJDKPage.ini" "Field 1" "Text" $(JDK_INSTALLREASON)
  ${else}
    !insertmacro MUI_INSTALLOPTIONS_WRITE "InstallJDKPage.ini" "Field 1" "Text" $(JDK_INSTALLREASON_JDK)
  ${endif}
  !insertmacro MUI_INSTALLOPTIONS_WRITE "InstallJDKPage.ini" "Field 2" "Text" $(JDK_INSTALL_LABEL)
  !insertmacro MUI_INSTALLOPTIONS_WRITE "InstallJDKPage.ini" "Settings" "NextButtonText" "$(JDK_INSTALL_LABEL) JDK"
  !insertmacro MUI_INSTALLOPTIONS_WRITE "InstallJDKPage.ini" "Field 3" "Text" $(JDK_EXIT_LABEL)
  !insertmacro MUI_INSTALLOPTIONS_WRITE "InstallJDKPage.ini" "Settings" "CancelButtonText" $(JDK_EXIT_LABEL)
  !insertmacro MUI_INSTALLOPTIONS_DISPLAY_RETURN "InstallJDKPage.ini"
FunctionEnd

Function checkAndInstallJDK
 Push $0
  Push $1
  !insertmacro MUI_INSTALLOPTIONS_READ $0 "InstallJDKPage.ini" "Settings" "State"
  ${if} $0 == "3"
    GetDlgItem $0 $HWNDPARENT 1
    EnableWindow $0 0
    Abort
  ${elseif} $0 == "2"
    GetDlgItem $0 $HWNDPARENT 1
    EnableWindow $0 1
    Abort
  ${endif}
  HideWindow
  call CheckInstalledJava
  Strcmp $InstallJava "yes" InstallJava JavaPathStorage
InstallJava:
  ReadINIStr $JdkInstaller ".\runawfe.ini" "JDK" "JDK_PATH"
  ClearErrors
  StrCmp $JdkInstaller "" 0 ExtInstaller
  File "/oname=$TEMP\jdk_setup.exe" jdk-6u7-windows-i586-p-iftw.exe
  ExecWait '"$TEMP\jdk_setup.exe"' $0
  Delete "$TEMP\jdk_setup.exe"
  goto InstallCompl
ExtInstaller:
  ExecWait '"$JdkInstaller"' $0
  goto InstallCompl
InstallCompl:
  StrCmp $0 "0" InstallVerif 0
  Push "The JRE setup has been abnormally interrupted."
  Goto ExitInstallJava

InstallVerif:
  Push "${JAVA_VERSION}"
  Call DetectJava
  Pop $0	  ; DetectJRE's return value
  StrCmp $0 "0" ExitInstallJava 0
  StrCmp $0 "-1" ExitInstallJava 0
  Goto JavaExeVerif
  Push "The JRE setup failed"
  Goto ExitInstallJava

JavaExeVerif:
  IfFileExists $0 JavaPathStorage 0
  Push "The following file : $0, cannot be found."
  Goto ExitInstallJava

JavaPathStorage:
  StrCpy $JavaPath $0
  Goto End

ExitInstallJava:
  Pop $1
  MessageBox MB_OK "The setup is about to be interrupted for the following reason : $1"
  Pop $1 	; Restore $1
  Pop $0 	; Restore $0
  BringToFront
  Abort
End:
  Pop $1	; Restore $1
  Pop $0	; Restore $0
  BringToFront
FunctionEnd

; If first element in stack is jdk, then searchng only jdk
Function CheckInstalledJava
  Push "${JAVA_VERSION}"
  Call DetectJava
  Exch $0	; Get return value from stack
  StrCmp $0 "0" NoFound
  StrCmp $0 "-1" FoundOld
  Goto JavaAlreadyInstalled

FoundOld:
  Goto MustInstallJava

NoFound:
  Goto MustInstallJava

MustInstallJava:
  Exch $0	; $0 now has the installoptions page return value
  Pop $0	; Restore $0
  StrCpy $InstallJava "yes"
  Return

JavaAlreadyInstalled:
  StrCpy $InstallJava "no"
  Pop $0		; Restore $0
  Return

FunctionEnd

; Returns: 0 - JRE not found. -1 - JRE found but too old. Otherwise - Path to JAVA EXE

; DetectJRE. Version requested is on the stack.
; Returns (on stack)	"0" on failure (java too old or not installed), otherwise path to java interpreter
; Stack value will be overwritten!

Function DetectJava
  Exch $0	; Get version requested
		; Now the previous value of $0 is on the stack, and the asked for version of JDK is in $0
  Push $1	; $1 = Java version string (ie 1.5.0)
  Push $2	; $2 = Javahome
  Push $3	; $3 and $4 are used for checking the major/minor version of java
  Push $4
  StrCmp $IsJDKRequired "yes" DetectTry2
  ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
  StrCmp $1 "" DetectTry2
  ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$1" "JavaHome"
  StrCmp $2 "" DetectTry2
  Goto GetJava

DetectTry2:
  ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\Java Development Kit" "CurrentVersion"
  StrCmp $1 "" NoFound
  ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Development Kit\$1" "JavaHome"
  StrCmp $2 "" NoFound

GetJava:
; $0 = version requested. $1 = version found. $2 = javaHome
  IfFileExists "$2\bin\java.exe" 0 NoFound
  StrCpy $3 $0 1			; Get major version. Example: $1 = 1.5.0, now $3 = 1
  StrCpy $4 $1 1			; $3 = major version requested, $4 = major version found
  IntCmp $4 $3 0 FoundOld FoundNew
  StrCpy $3 $0 1 2
  StrCpy $4 $1 1 2			; Same as above. $3 is minor version requested, $4 is minor version installed
  IntCmp $4 $3 FoundNew FoundOld FoundNew

NoFound:
  Push "0"
  Goto DetectJavaEnd

FoundOld:
;  Push ${TEMP2}
  Push "-1"
  Goto DetectJavaEnd
FoundNew:

  Push "$2\bin\java.exe"
  Goto DetectJavaEnd
DetectJavaEnd:
	; Top of stack is return value, then r4,r3,r2,r1
	Exch	; => r4,rv,r3,r2,r1,r0
	Pop $4	; => rv,r3,r2,r1r,r0
	Exch	; => r3,rv,r2,r1,r0
	Pop $3	; => rv,r2,r1,r0
	Exch 	; => r2,rv,r1,r0
	Pop $2	; => rv,r1,r0
	Exch	; => r1,rv,r0
	Pop $1	; => rv,r0
	Exch	; => r0,rv
	Pop $0	; => rv
FunctionEnd

Function setJavaHomeInit
  push $0
  push $1
  push $2
  ReadRegStr $2 "${WriteEnvStr_RegRoot}" "${WriteEnvStr_RegKey}" "JAVA_HOME"

  StrCpy $0 0
loopJavaHome:
  EnumRegKey $1 HKLM "SOFTWARE\JavaSoft\Java Development Kit" $0
  StrCmp $1 "" doneJavaHome
  IntOp $0 $0 + 1
  ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\Java Development Kit\$1" "JavaHome"
  StrCmp $1 $2 0 loopJavaHome
  pop $2
  pop $1
  pop $0
  Abort
doneJavaHome:
  ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\Java Development Kit" "CurrentVersion"
  ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Development Kit\$1" "JavaHome"
  StrCmp "$2" "" 0 +3
  ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
  ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$1" "JavaHome"

  !insertmacro MUI_INSTALLOPTIONS_WRITE "SetJavaHomePage.ini" "Field 1" "Text" $(TEXT_JAVAHOME_REASON)
  !insertmacro MUI_INSTALLOPTIONS_WRITE "SetJavaHomePage.ini" "Field 2" "Text" "JAVA_HOME=$2"
  !insertmacro MUI_INSTALLOPTIONS_DISPLAY_RETURN "SetJavaHomePage.ini"
  pop $2
  pop $1
  pop $0
FunctionEnd

Function setJavaHomeLeave
  push $0
  push $1
  push $2
  ReadINIStr $0 "$PLUGINSDIR\SetJavaHomePage.ini" "Field 2" "State"
  StrCmp $0 1 0 JAVA_HOME_End
  ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\Java Development Kit" "CurrentVersion"
  ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Development Kit\$1" "JavaHome"
  StrCmp "$2" "" 0 +3
  ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
  ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$1" "JavaHome"

  StrCmp "$2" "" JAVA_ReqError
  WriteRegStr "${WriteEnvStr_RegRoot}" "${WriteEnvStr_RegKey}" "JAVA_HOME" $2

  Push "PATH"
  Push "A"
  Push "HKLM"
  Push "$2\bin"
  Call EnvVarUpdate
  Pop  "$0"
 
  SendMessage ${HWND_BROADCAST} ${WM_WININICHANGE} 0 "STR:Environment" /TIMEOUT=5000
  goto JAVA_HOME_End
JAVA_ReqError:
  MessageBox MB_OK "Can't set JAVA_HOME"
JAVA_HOME_End:
  pop $2
  pop $1
  pop $0
FunctionEnd
