#!/bin/bash
# =============================================================================
# install.sh — Script de instalación de pdfcli para Linux y macOS
# =============================================================================
# Uso:
#   ./install.sh              Compila e instala pdfcli
#   ./install.sh --skip-build Instala sin recompilar (usa JAR existente)
#   ./install.sh --uninstall  Desinstala pdfcli del sistema
# =============================================================================

set -e  # Detener inmediatamente si cualquier comando falla

# ── Colores para output ──────────────────────────────────────────────────────
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
BOLD='\033[1m'
RESET='\033[0m'

# ── Configuración ────────────────────────────────────────────────────────────
INSTALL_DIR="$HOME/.local/bin"
JAR_NAME="pdfcli.jar"
CMD_NAME="pdfcli"
JAR_SOURCE="target/$JAR_NAME"

# ── Funciones de utilidad ────────────────────────────────────────────────────
print_step()    { echo -e "\n${CYAN}${BOLD}▸ $1${RESET}"; }
print_success() { echo -e "${GREEN}✓ $1${RESET}"; }
print_warning() { echo -e "${YELLOW}⚠ $1${RESET}"; }
print_error()   { echo -e "${RED}✗ $1${RESET}"; }

# ── Función: desinstalar ─────────────────────────────────────────────────────
uninstall() {
    print_step "Desinstalando pdfcli..."

    local removed=0

    if [ -f "$INSTALL_DIR/$JAR_NAME" ]; then
        rm "$INSTALL_DIR/$JAR_NAME"
        print_success "JAR eliminado: $INSTALL_DIR/$JAR_NAME"
        removed=1
    fi

    if [ -f "$INSTALL_DIR/$CMD_NAME" ]; then
        rm "$INSTALL_DIR/$CMD_NAME"
        print_success "Comando eliminado: $INSTALL_DIR/$CMD_NAME"
        removed=1
    fi

    if [ $removed -eq 0 ]; then
        print_warning "pdfcli no estaba instalado."
    else
        echo -e "\n${GREEN}${BOLD}pdfcli desinstalado correctamente.${RESET}"
        print_warning "La línea del PATH en tu .bashrc/.zshrc no fue eliminada."
        print_warning "Puedes eliminarla manualmente si lo deseas."
    fi

    exit 0
}

# ── Función: verificar dependencias ─────────────────────────────────────────
check_dependencies() {
    print_step "Verificando dependencias..."

    # Java es el único requisito obligatorio para el usuario final
    if ! command -v java &> /dev/null; then
        print_error "Java no está instalado o no está en el PATH."
        echo "  Instálalo desde: https://adoptium.net"
        exit 1
    fi

    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -lt 17 ] 2>/dev/null; then
        print_error "Se requiere Java 17 o superior. Versión encontrada: $JAVA_VERSION"
        exit 1
    fi
    print_success "Java encontrado: $(java -version 2>&1 | head -n1)"

    # Maven Wrapper solo se necesita si vamos a compilar
    if [ "$SKIP_BUILD" = false ]; then
        if [ ! -f "./mvnw" ]; then
            print_error "No se encontró el Maven Wrapper (mvnw)."
            print_error "Asegúrate de ejecutar este script desde la raíz del proyecto."
            exit 1
        fi
        # Garantizar permisos de ejecución
        chmod +x ./mvnw
        print_success "Maven Wrapper encontrado (Maven no necesario — se descarga solo)."
    fi
}

# ── Función: compilar con Maven Wrapper ─────────────────────────────────────
build() {
    print_step "Compilando pdfcli con Maven..."
    echo ""

    if ! ./mvnw package -q; then
        print_error "La compilación falló. Revisa los errores arriba."
        exit 1
    fi

    if [ ! -f "$JAR_SOURCE" ]; then
        print_error "El JAR no fue generado en $JAR_SOURCE"
        exit 1
    fi

    print_success "Compilación exitosa: $JAR_SOURCE"
}

# ── Función: instalar ────────────────────────────────────────────────────────
install() {
    print_step "Preparando directorio de instalación: $INSTALL_DIR"
    mkdir -p "$INSTALL_DIR"
    print_success "Directorio listo."

    print_step "Instalando JAR..."
    cp "$JAR_SOURCE" "$INSTALL_DIR/$JAR_NAME"
    print_success "JAR instalado en: $INSTALL_DIR/$JAR_NAME"

    print_step "Creando script wrapper..."
    cat > "$INSTALL_DIR/$CMD_NAME" << EOF
#!/bin/bash
exec java -jar "$INSTALL_DIR/$JAR_NAME" "\$@"
EOF
    chmod +x "$INSTALL_DIR/$CMD_NAME"
    print_success "Wrapper creado: $INSTALL_DIR/$CMD_NAME"

    configure_path
}

# ── Función: configurar PATH ─────────────────────────────────────────────────
configure_path() {
    print_step "Verificando configuración del PATH..."

    if echo "$PATH" | grep -q "$INSTALL_DIR"; then
        print_success "~/.local/bin ya está en el PATH."
        return
    fi

    SHELL_NAME=$(basename "$SHELL")
    case "$SHELL_NAME" in
        bash) SHELL_RC="$HOME/.bashrc" ;;
        zsh)  SHELL_RC="$HOME/.zshrc" ;;
        fish)
            print_warning "Shell 'fish' detectado."
            print_warning "Agrega manualmente: set -Ux fish_user_paths ~/.local/bin \$fish_user_paths"
            return
            ;;
        *)    SHELL_RC="$HOME/.profile" ;;
    esac

    if grep -q 'local/bin' "$SHELL_RC" 2>/dev/null; then
        print_warning "La entrada del PATH ya existe en $SHELL_RC pero no está activa aún."
        print_warning "Ejecuta: source $SHELL_RC"
        return
    fi

    echo '' >> "$SHELL_RC"
    echo '# pdfcli — agregado por install.sh' >> "$SHELL_RC"
    echo 'export PATH="$HOME/.local/bin:$PATH"' >> "$SHELL_RC"

    print_success "PATH configurado en $SHELL_RC"
    print_warning "Para activarlo en esta sesión ejecuta: source $SHELL_RC"
}

# ── Función: verificación final ──────────────────────────────────────────────
verify_installation() {
    print_step "Verificando instalación..."

    export PATH="$INSTALL_DIR:$PATH"

    if command -v pdfcli &> /dev/null; then
        print_success "pdfcli encontrado en PATH: $(which pdfcli)"
        echo ""
        pdfcli --version
    else
        print_warning "pdfcli aún no está en el PATH de esta sesión."
        print_warning "Ejecuta: source ~/.bashrc  (o ~/.zshrc según tu shell)"
        print_warning "Luego verifica con: pdfcli --version"
    fi
}

# ── Main ─────────────────────────────────────────────────────────────────────
SKIP_BUILD=false

for arg in "$@"; do
    case $arg in
        --skip-build) SKIP_BUILD=true ;;
        --uninstall)  uninstall ;;
        --help|-h)
            echo "Uso: ./install.sh [OPCIÓN]"
            echo ""
            echo "Opciones:"
            echo "  (ninguna)        Compila e instala pdfcli"
            echo "  --skip-build     Instala sin recompilar (usa JAR existente en target/)"
            echo "  --uninstall      Desinstala pdfcli del sistema"
            echo "  --help           Muestra esta ayuda"
            echo ""
            echo "Requisitos:"
            echo "  - Java 17 o superior"
            echo "  - Maven NO es necesario (Maven Wrapper lo descarga automáticamente)"
            exit 0
            ;;
        *)
            print_error "Opción desconocida: $arg"
            echo "Usa --help para ver las opciones disponibles."
            exit 1
            ;;
    esac
done

echo -e "\n${BOLD}╔══════════════════════════════════════╗"
echo -e "║        Instalador de pdfcli          ║"
echo -e "╚══════════════════════════════════════╝${RESET}"

check_dependencies

if [ "$SKIP_BUILD" = false ]; then
    build
else
    print_warning "Omitiendo compilación (--skip-build)."
    if [ ! -f "$JAR_SOURCE" ]; then
        print_error "No se encontró el JAR en $JAR_SOURCE"
        print_error "Compila primero con: ./mvnw package"
        exit 1
    fi
fi

install
verify_installation

echo -e "\n${GREEN}${BOLD}✓ ¡Instalación completada!${RESET}"
echo -e "  Usa ${BOLD}pdfcli --help${RESET} para ver los comandos disponibles.\n"