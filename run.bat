@echo off
REM Script para ejecutar ObraTech en Windows

echo.
echo ========================================
echo   ObraTech - Gestor de Proyectos
echo ========================================
echo.

REM Verificar si el JAR existe
if not exist "target\obratech-*.jar" (
    echo [INFO] JAR no encontrado. Compilando proyecto...
    call mvnw.cmd clean package -DskipTests
    if errorlevel 1 (
        echo [ERROR] La compilación falló
        pause
        exit /b 1
    )
)

echo [INFO] Iniciando aplicación...
echo.

REM Buscar y ejecutar el JAR
for /f "delims=" %%i in ('dir /b target\obratech-*.jar 2^>nul') do (
    java -jar "target\%%i"
    if errorlevel 1 (
        echo [ERROR] La aplicación falló
        pause
        exit /b 1
    )
    goto end
)

echo [ERROR] No se encontró el archivo JAR
pause
exit /b 1

:end
echo [INFO] Aplicación finalizada
pause
