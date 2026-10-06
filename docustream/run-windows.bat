@echo off
cd /d "%~dp0"
java -version >nul 2>&1
if errorlevel 1 (
  echo Install Java 25 or newer and reopen this terminal.
  pause
  exit /b 1
)
if exist "docustream-1.0.0.jar" (
  java -jar docustream-1.0.0.jar
) else (
  call mvn spring-boot:run
)
pause
