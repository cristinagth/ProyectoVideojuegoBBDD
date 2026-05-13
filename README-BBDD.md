# Base de datos del proyecto

## Estado actual

La aplicacion usa **Azure SQL Database** como base de datos online.

SQLite fue usado como primera aproximacion local, pero ya no es el modo activo del proyecto.

## Guia principal

La configuracion actual esta documentada en:

```text
README-AZURE.md
```

Ese archivo incluye:

- Recurso creado en Azure.
- Servidor y base de datos.
- Driver JDBC necesario.
- Variables de entorno.
- Comandos de compilacion y ejecucion.

## Resumen de ejecucion

Desde la raiz del proyecto:

```powershell
$env:AZURE_SQL_PASSWORD="tu-contrasena"
javac -cp ".;lib\mssql-jdbc-13.4.0.jre11.jar" -d . *.java
java -cp ".;lib\mssql-jdbc-13.4.0.jre11.jar" ProyectoVideojuegoBBDD.Main
```

## Esquema

El esquema compatible con Azure SQL esta en:

```text
schema-azure.sql
```

Tablas principales:

- `jugador`
- `configuracion`
- `partida_cathunter`
- `partida_ajedrez`

## Persistencia implementada

- Buscagatos guarda el resultado de la partida al ganar o perder.
- Ajedrez guarda la partida al finalizar por jaque mate.
- El historial de ajedrez se puede consultar desde el boton `Historial` del panel superior.
