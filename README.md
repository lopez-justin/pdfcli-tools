# PDFCLI

> Herramienta de línea de comandos (CLI) desarrollada en Java para la manipulación de archivos PDF. 
> Actualmente implementa la funcionalidad de unión de múltiples PDFs (merge) en un solo archivo.

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=openjdk)
![Maven](https://img.shields.io/badge/Maven-3.6+-blue?style=flat-square&logo=apachemaven)
![PDFBox](https://img.shields.io/badge/PDFBox-3.0.7-red?style=flat-square)
![Picocli](https://img.shields.io/badge/Picocli-4.7.7-green?style=flat-square)

---

## Tabla de contenidos

- [Descripción](#descripción)
- [Funcionalidades](#funcionalidades)
- [Requisitos](#requisitos)
- [Instalación](#instalación)
  - [Linux / macOS](#linux--macos)
  - [Windows](#windows)
- [Uso y ejemplos](#uso-y-ejemplos)
    - [Comando merge](#comando-merge)
- [Roadmap](#roadmap)
- [Tecnologías](#tecnologías)

---

## Descripción

`pdfcli` es una herramienta CLI profesional para manipulación de archivos PDF desde la terminal. Inspirada en herramientas como iLovePDF, pero ejecutándose completamente de forma local — sin subir tus archivos a ningún servidor, sin conexión a internet requerida.

Diseñada con una arquitectura modular y escalable que permite agregar nuevas funcionalidades de forma sencilla.

---

## Funcionalidades

| Comando    | Descripción                        | Estado          |
|------------|------------------------------------|-----------------|
| `merge`    | Une múltiples PDFs en uno solo     | ✅ Disponible    |
| `split`    | Divide un PDF por páginas o rangos | 🔜 Próximamente |
| `compress` | Reduce el tamaño de un PDF         | 🔜 Próximamente |
| `convert`  | Convierte imágenes a PDF           | 🔜 Próximamente | 

---

## Requisitos

- **[Git](https://git-scm.com/)** para clonar el repositorio
- **[Java](https://openjdk.org/)** 17 o superior instalado y en el PATH

Verifica tu instalación de Java:

```bash
java -version
```

---

## Instalación

### Linux / macOS

**1. Clona el repositorio:**

```bash
git clone https://github.com/lopezjustin/pdfcli.git
```
```bash
cd pdfcli
```

**2. Da permisos de ejecución al instalador y ejecútalo:**

```bash
chmod +x install.sh
````
```bash
./install.sh
```

**3. Recarga tu terminal:**

```bash
source ~/.bashrc   # Si usas bash
source ~/.zshrc    # Si usas zsh
```

**4. Verifica la instalación:**

```bash
pdfcli --version
```

---

### Windows

**1. Clona el repositorio:**

```bash
git clone https://github.com/lopezjustin/pdfcli.git
```
```bash
cd pdfcli
```

**2. Ejecuta el instalador en PowerShell:**

```powershell
install.bat
```

**3. Abre una nueva terminal y verifica:**

```powershell
pdfcli --version
```

---

## Uso y ejemplos

### Comando merge
```bash
# Unir dos PDFs
pdfcli merge capitulo1.pdf capitulo2.pdf -o libro.pdf
 
# Unir tres PDFs
pdfcli merge portada.pdf contenido.pdf anexos.pdf -o documento_final.pdf
 
# Unir usando rutas absolutas
pdfcli merge ~/docs/a.pdf ~/docs/b.pdf -o ~/Desktop/resultado.pdf
 
# Ver ayuda de un subcomando
pdfcli merge --help
``` 

---

## Roadmap

- [x] MVP — Comando `merge`
- [ ] Comando `split` — dividir PDF por páginas o rangos
- [ ] Comando `compress` — reducir tamaño de archivo
- [ ] Comando `convert` — imágenes a PDF y viceversa
- [ ] Comando `rotate` — rotar páginas
- [ ] Distribución como binario nativo con GraalVM

---

## Tecnologías

| Tecnología                                  | Versión | Uso                  |
|---------------------------------------------|---------|----------------------|
| [Java](https://openjdk.org/)                | 17 LTS  | Lenguaje principal   |
| [Apache Maven](https://maven.apache.org/)   | 3.6+    | Build y dependencias |
| [Picocli](https://picocli.info/)            | 4.7.7   | Framework CLI        |
| [Apache PDFBox](https://pdfbox.apache.org/) | 3.0.7   | Manipulación de PDFs |
| [SLF4J](https://www.slf4j.org/)             | 2.0.13  | Logging              |
