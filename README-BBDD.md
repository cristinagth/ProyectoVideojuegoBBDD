# Base de datos SQLite para ProyectoVideojuegoBBDD

## Objetivo

Este documento explica, paso a paso, como ejecutar el proyecto con la infraestructura de base de datos SQLite.
Esta guia esta pensada para alguien que va a arrancar el programa por primera vez.

## Donde se crea la base de datos

Cuando la aplicacion arranca correctamente con SQLite, intentara crear este fichero:

```text
data/proyecto_videojuego.db
```

## Requisitos previos

Antes de ejecutar el proyecto necesitas:

- Tener Java instalado.
- Abrir una terminal PowerShell.
- Situarte en la raiz del proyecto.

La raiz del proyecto, será similar a:

```powershell
D:\ProyectoBBDD\ProyectoVideojuegoBBDD
```

## Paso 1. Descargar el driver JDBC de SQLite

Desde la raiz del proyecto, ejecuta:

```powershell
mkdir lib
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.51.2.0/sqlite-jdbc-3.51.2.0.jar" -OutFile "lib\sqlite-jdbc-3.51.2.0.jar"
```

Al terminar, deberias tener este fichero:

```text
lib/sqlite-jdbc-3.51.2.0.jar
```

## Paso 2. Compilar el proyecto

Desde la misma carpeta raiz del proyecto, compila asi:

```powershell
javac -cp ".;lib\sqlite-jdbc-3.51.2.0.jar" -d . *.java
```

### Que hace este comando

- `-cp` anade el driver SQLite al classpath.
- `-d .` genera las clases compiladas respetando el paquete Java actual.
- `*.java` compila todos los archivos fuente del proyecto.

## Paso 3. Ejecutar el programa

```powershell
java -cp ".;lib\sqlite-jdbc-3.51.2.0.jar" ProyectoVideojuegoBBDD.Main
```

## Sinopsis

Si quieres la secuencia minima correcta para una primera ejecucion con BBDD:

```powershell
cd D:\ProyectoBBDD\ProyectoVideojuegoBBDD
mkdir lib
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.51.2.0/sqlite-jdbc-3.51.2.0.jar" -OutFile "lib\sqlite-jdbc-3.51.2.0.jar"
javac -cp ".;lib\sqlite-jdbc-3.51.2.0.jar" -d . *.java
java -cp ".;lib\sqlite-jdbc-3.51.2.0.jar" ProyectoVideojuegoBBDD.Main
```
