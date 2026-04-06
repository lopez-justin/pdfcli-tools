@echo off
REM =============================================================================
REM install.bat — Script de instalación de pdfcli para Windows
REM =============================================================================
REM Uso (desde CMD o PowerShell):
REM   install.bat               Compila e instala pdfcli
REM   install.bat --skip-build  Instala sin recompilar (usa JAR existente)
REM   install.bat --uninstall   Desinstala pdfcli
REM
REM Requisitos:
REM   - Java 17 o superior
REM   - Maven NO es necesario (Maven Wrapper lo descarga automáticamente)
REM =============================================================================

setlocal EnableDelayedExpansion

REM ── Configuración ────────────────────────────────────────────────────────────
set INSTALL_DIR=%USERPROFILE%\.local\bin
set JAR_NAME=pdfcli.jar
set CMD_NAME=pdfcli.bat
set JAR_SOURCE=target\%JAR_NAME%
set SKIP_BUILD=false

REM ── Parsear argumentos ───────────────────────────────────────────────────────
if "%1"=="--uninstall"  goto :uninstall
if "%1"=="--skip-build" set SKIP_BUILD=true
if "%1"=="--help"       goto :show_help
if "%1"=="-h"           goto :show_help

REM ── Encabezado ───────────────────────────────────────────────────────────────
echo.
echo ╔══════════════════════════════════════╗
echo ║        Instalador de pdfcli          ║
echo ╚══════════════════════════════════════╝
echo.

REM ── Verificar Java ───────────────────────────────────────────────────────────
echo [1/5] Verificando Java...
java -version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Java no esta instalado o no esta en el PATH.
    echo         Descargalo desde: https://adoptium.net
    exit /b 1
)
for /f "tokens=3" %%v in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVA_VER=%%v
)
echo [OK] Java encontrado: !JAVA_VER!

REM ── Verificar Maven Wrapper (si vamos a compilar) ────────────────────────────
if "%SKIP_BUILD%"=="false" (
    echo.
    echo [2/5] Verificando Maven Wrapper...

    if not exist "mvnw.cmd" (
        echo [ERROR] No se encontro el Maven Wrapper ^(mvnw.cmd^).
        echo         Asegurate de ejecutar este script desde la raiz del proyecto.
        exit /b 1
    )

    echo [OK] Maven Wrapper encontrado ^(Maven se descargara automaticamente si es necesario^).
) else (
    echo.
    echo [2/5] Omitiendo verificacion de Maven Wrapper ^(--skip-build^).
)

REM ── Compilar con Maven Wrapper ───────────────────────────────────────────────
if "%SKIP_BUILD%"=="false" (
    echo.
    echo [3/5] Compilando pdfcli con Maven Wrapper...
    echo.

    call mvnw.cmd package -q
    if !ERRORLEVEL! neq 0 (
        echo [ERROR] La compilacion fallo. Revisa los errores arriba.
        exit /b 1
    )

    if not exist "%JAR_SOURCE%" (
        echo [ERROR] El JAR no fue generado en %JAR_SOURCE%
        exit /b 1
    )
    echo [OK] Compilacion exitosa.
) else (
    echo.
    echo [3/5] Omitiendo compilacion ^(--skip-build^).
    if not exist "%JAR_SOURCE%" (
        echo [ERROR] No se encontro el JAR en %JAR_SOURCE%
        echo         Compila primero con: mvnw.cmd package
        exit /b 1
    )
)

REM ── Crear directorio de instalación ──────────────────────────────────────────
echo.
echo [4/5] Instalando archivos...

if not exist "%INSTALL_DIR%" (
    mkdir "%INSTALL_DIR%"
    echo [OK] Directorio creado: %INSTALL_DIR%
) else (
    echo [OK] Directorio ya existe: %INSTALL_DIR%
)

REM Copiar el JAR
copy /Y "%JAR_SOURCE%" "%INSTALL_DIR%\%JAR_NAME%" >nul
echo [OK] JAR instalado en: %INSTALL_DIR%\%JAR_NAME%

REM Crear script wrapper .bat
(
    echo @echo off
    echo REM Wrapper para pdfcli — generado por install.bat
    echo java -jar "%INSTALL_DIR%\%JAR_NAME%" %%*
) > "%INSTALL_DIR%\%CMD_NAME%"
echo [OK] Wrapper creado: %INSTALL_DIR%\%CMD_NAME%

REM ── Configurar PATH ───────────────────────────────────────────────────────────
echo.
echo [5/5] Configurando PATH...

echo %PATH% | findstr /i ".local\bin" >nul
if %ERRORLEVEL% equ 0 (
    echo [OK] %INSTALL_DIR% ya esta en el PATH.
    goto :verify
)

for /f "tokens=2*" %%a in (
    'reg query "HKCU\Environment" /v PATH 2^>nul'
) do set CURRENT_PATH=%%b

if defined CURRENT_PATH (
    setx PATH "%INSTALL_DIR%;!CURRENT_PATH!" >nul
) else (
    setx PATH "%INSTALL_DIR%" >nul
)

if %ERRORLEVEL% equ 0 (
    echo [OK] PATH actualizado correctamente.
    echo [!] Cierra y vuelve a abrir la terminal para que el PATH se active.
) else (
    echo [AVISO] No se pudo actualizar el PATH automaticamente.
    echo         Agregalo manualmente:
    echo         1. Inicio -^> "Variables de entorno"
    echo         2. Variables de usuario -^> Path -^> Editar
    echo         3. Nuevo -^> %INSTALL_DIR%
)

:verify
REM ── Verificación final ────────────────────────────────────────────────────────
echo.
echo Verificando instalacion...

set PATH=%INSTALL_DIR%;%PATH%

where pdfcli >nul 2>&1
if %ERRORLEVEL% equ 0 (
    echo [OK] pdfcli encontrado en PATH.
    echo.
    pdfcli --version
) else (
    echo [AVISO] pdfcli aun no esta activo en esta sesion.
    echo         Cierra y vuelve a abrir la terminal, luego ejecuta:
    echo         pdfcli --version
)

echo.
echo ╔══════════════════════════════════════════════╗
echo ║   Instalacion completada exitosamente!       ║
echo ║   Usa  pdfcli --help  para comenzar.         ║
echo ╚══════════════════════════════════════════════╝
echo.
exit /b 0

REM ── Desinstalar ───────────────────────────────────────────────────────────────
:uninstall
echo.
echo Desinstalando pdfcli...
echo.

set REMOVED=0

if exist "%INSTALL_DIR%\%JAR_NAME%" (
    del "%INSTALL_DIR%\%JAR_NAME%"
    echo [OK] JAR eliminado: %INSTALL_DIR%\%JAR_NAME%
    set REMOVED=1
)

if exist "%INSTALL_DIR%\%CMD_NAME%" (
    del "%INSTALL_DIR%\%CMD_NAME%"
    echo [OK] Wrapper eliminado: %INSTALL_DIR%\%CMD_NAME%
    set REMOVED=1
)

if "%REMOVED%"=="0" (
    echo [AVISO] pdfcli no estaba instalado.
) else (
    echo.
    echo pdfcli desinstalado correctamente.
    echo [!] El PATH no fue modificado. Puedes limpiarlo manualmente
    echo     desde las variables de entorno del sistema si lo deseas.
)
exit /b 0

REM ── Ayuda ─────────────────────────────────────────────────────────────────────
:show_help
echo.
echo Uso: install.bat [OPCION]
echo.
echo Opciones:
echo   (ninguna)        Compila e instala pdfcli
echo   --skip-build     Instala sin recompilar ^(usa JAR existente en target/^)
echo   --uninstall      Desinstala pdfcli del sistema
echo   --help           Muestra esta ayuda
echo.
echo Requisitos:
echo   - Java 17 o superior
echo   - Maven NO es necesario ^(Maven Wrapper lo descarga automaticamente^)
echo.
exit /b 0