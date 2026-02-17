@echo off
setlocal

REM Build a runnable JAR (no external dependencies)

if not exist dist mkdir dist
if not exist out mkdir out

echo Compiling...
javac -d out @sources.txt
if errorlevel 1 (
  echo Build failed.
  exit /b 1
)

echo Creating JAR...
echo Main-Class: Main> manifest.txt
jar cfm dist\HotelBooker.jar manifest.txt -C out .
del manifest.txt

echo Done!
echo Run with: java -jar dist\HotelBooker.jar
endlocal
