@echo off
setlocal

cd /d %~dp0

if not exist bin mkdir bin

echo Compiling...
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -d bin @sources.txt
if errorlevel 1 goto :eof

echo Running...
java -cp bin Main

endlocal
