@echo off
setlocal
set "JAVA_HOME="
for /d %%D in ("%~dp0.tools\jdk25\jdk-*") do set "JAVA_HOME=%%~fD"
if not defined JAVA_HOME (
    echo JDK 25 was not found in .tools\jdk25.
    echo Install JDK 25 and set JAVA_HOME, or restore the project-local JDK.
    exit /b 1
)
call "%~dp0gradlew.bat" %*
exit /b %errorlevel%
