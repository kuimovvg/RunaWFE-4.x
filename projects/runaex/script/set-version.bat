@if a%1 == a goto usage

call mvn -f ..\pom.xml versions:set -DgenerateBackupPoms=false -DnewVersion=%1

@goto end

:usage
@echo Usage example: %0 1.0.2

:end