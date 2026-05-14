@echo off
cd /d "%~dp0"

if "%AZURE_SQL_PASSWORD%"=="" (
    echo Falta configurar AZURE_SQL_PASSWORD.
    echo Ejecuta antes:
    echo setx AZURE_SQL_PASSWORD "tu-contrasena"
    pause
    exit /b 1
)

javac -cp ".;lib\mssql-jdbc-13.4.0.jre11.jar" -d . *.java
if errorlevel 1 (
    echo Error compilando el proyecto.
    pause
    exit /b 1
)

java -cp ".;lib\mssql-jdbc-13.4.0.jre11.jar" ProyectoVideojuegoBBDD.Main
