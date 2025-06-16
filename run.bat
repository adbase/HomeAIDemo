@echo off
echo Starting Spring Boot Application...

REM Check if Java is installed
java -version >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo Java is not installed or not in PATH.
    echo Please install Java from https://www.oracle.com/java/technologies/downloads/
    pause
    exit /b 1
)

REM Check if Maven is installed
where mvn >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo Maven is not installed or not in PATH.
    echo Please install Maven from https://maven.apache.org/download.cgi
    echo or use an IDE like IntelliJ IDEA or Eclipse to run the application.
    pause
    exit /b 1
)

REM Run the application using Maven
mvn spring-boot:run

echo Application started. Press Ctrl+C to stop. 