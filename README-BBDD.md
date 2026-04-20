# Base de datos SQLite para ProyectoVideojuegoBBDD

## Que incluye esta infraestructura

- Inicializacion automatica de SQLite al arrancar la aplicacion.
- Tabla `jugador` para perfiles locales.
- Tabla `configuracion` para futuras preferencias.
- Tabla `partida_cathunter` preparada para resultados de Buscagatos.
- Tabla `partida_ajedrez` preparada para resultados de Ajedrez.

## Archivo de base de datos

La aplicacion intentara crear el fichero:

`data/proyecto_videojuego.db`

## Driver necesario

Para que SQLite funcione en tiempo de ejecucion necesitas anadir el driver JDBC de SQLite al classpath.

Puedes descargar `sqlite-jdbc` y colocarlo en una carpeta local `lib/`.

Ejemplo:

```powershell
mkdir lib
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.51.2.0/sqlite-jdbc-3.51.2.0.jar" -OutFile "lib\sqlite-jdbc-3.51.2.0.jar"
```

## Compilar y ejecutar con SQLite

Desde la raiz del proyecto:

```powershell
javac -cp ".;lib\sqlite-jdbc-3.51.2.0.jar" -d . *.java
java -cp ".;lib\sqlite-jdbc-3.51.2.0.jar" ProyectoVideojuegoBBDD.Main
```

## Nota

Este commit solo prepara la infraestructura de base de datos. El guardado real de partidas se anadira en commits separados.
