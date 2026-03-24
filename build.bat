@echo off
echo [1/2] Building shaded JAR ...
call mvn clean package -q
if %errorlevel% neq 0 (
    echo Build FAILED!
    pause
    exit /b 1
)

echo [2/2] Patching .xsb files ...
python patch_xsb.py
if %errorlevel% neq 0 (
    echo Patch FAILED!
    pause
    exit /b 1
)

echo.
echo Build complete: target\poi4-shaded-4.1.1.jar
pause
