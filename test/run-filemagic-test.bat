@echo off

set "POI3_DIR=C:\IBM\NotesR9\jvm\lib\ext"
set "SHADED_JAR=..\target\poi4-shaded-4.1.1.jar"
set "CP=%POI3_DIR%\poi-3.16.jar;%POI3_DIR%\poi-ooxml-3.16.jar;%POI3_DIR%\poi-ooxml-schemas-3.16.jar;%SHADED_JAR%;."

echo [1/2] Compiling FileMagicTest.java ...
javac -cp "%CP%" FileMagicTest.java
if %errorlevel% neq 0 (
    echo Compile FAILED!
    pause
    exit /b 1
)

echo [2/2] Running test ...
echo.

if "%~1"=="" (
    java -cp "%CP%" FileMagicTest
) else (
    java -cp "%CP%" FileMagicTest "%~1"
)

echo.
pause
