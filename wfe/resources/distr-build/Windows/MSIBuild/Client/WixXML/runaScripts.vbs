function DocumentationLinkTune
  ReplaceInFile Session.Property("ProgramMenuDir") + Session.Property("RunaWFEClientDocumentationLinkProp"), "shortcut_path", Session.Property("TARGETDIR")
end function

function MainWebLinkTune
  ReplaceInFile Session.Property("ProgramMenuDir") + Session.Property("RunaWFEClientMainServerInterfaceProp"), "server", Session.Property("MAIN_WFE_SERVER_IP")
  ReplaceInFile Session.Property("ProgramMenuDir") + Session.Property("RunaWFEClientMainServerInterfaceProp"), "port", Session.Property("MAIN_WFE_SERVER_PORT")
  ReplaceInFile Session.Property("ProgramMenuDir") + Session.Property("RunaWFEClientMainServerInterfaceProp"), "shortcut_path", Session.Property("TARGETDIR")
end function

function SimulatorWebLinkTune
  ReplaceInFile Session.Property("ProgramMenuDir") + Session.Property("RunaWFEClientSimulatorWebLinkProp"), "shortcut_path", Session.Property("TARGETDIR")
end function

function RTNTune
  ReplaceInFile Session.Property("TARGETDIR") + "rtn-2.2.x\af_delegate.properties", "server", Session.Property("MAIN_WFE_SERVER_IP")
  ReplaceInFile Session.Property("TARGETDIR") + "rtn-2.2.x\application.properties", "server", Session.Property("MAIN_WFE_SERVER_IP")
  ReplaceInFile Session.Property("TARGETDIR") + "rtn-2.2.x\application.properties", "port", Session.Property("MAIN_WFE_SERVER_PORT")
end function

function ReplaceInFile(FileName, Replaced, ReplaceWith)
  Dim FileContents, dFileContents
  FileContents = GetFile(FileName)
  dFileContents = replace(FileContents, Replaced, ReplaceWith, 1, -1, 1)

  WriteFile FileName, dFileContents
End Function

'Read text file
function GetFile(FileName)
  If FileName<>"" Then
    Dim FS, FileStream
    Set FS = CreateObject("Scripting.FileSystemObject")
      on error resume Next
      Set FileStream = FS.OpenTextFile(FileName)
      GetFile = FileStream.ReadAll
  End If
End Function

'Write string As a text file.
function WriteFile(FileName, Contents)
  Dim OutStream, FS

  on error resume Next
  Set FS = CreateObject("Scripting.FileSystemObject")
    Set OutStream = FS.OpenTextFile(FileName, 2, True)
    OutStream.Write Contents
End Function


