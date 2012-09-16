function BotstationTune
  ReplaceInFile Session.Property("TARGETDIR") + "server\default\conf\af_delegate.properties", "main_wfe_server", Session.Property("MAIN_WFE_SERVER_IP")
end function

function ServerTune
  ReplaceInFile Session.Property("TARGETDIR") + "server\default\deploy\http-invoker.sar\META-INF\jboss-service.xml", "8080", Session.Property("MAIN_WFE_SERVER_PORT")
  ReplaceInFile Session.Property("TARGETDIR") + "server\default\deploy\jbossweb-tomcat55.sar\server.xml", "8080", Session.Property("MAIN_WFE_SERVER_PORT")
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


