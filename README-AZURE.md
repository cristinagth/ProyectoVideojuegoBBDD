# Persistencia online con Azure SQL Database

## Recurso creado

Se ha creado una base de datos Azure SQL Database.

| Recurso | Valor |
| --- | --- |
| Resource group | `rg-proyecto-videojuego` |
| Region del servidor SQL | `northeurope` |
| SQL server | `proyectovideojuegoserver-pedro` |
| Host | `proyectovideojuegoserver-pedro.database.windows.net` |
| Database | `ProyectoVideojuegoBBDD` |
| Admin user | `azureadmin` |
| SKU | `GeneralPurpose Serverless GP_S_Gen5_2` |
| Free offer | `true` |
| Si se agota el limite gratis | `AutoPause` |
| Backup storage redundancy | `Local` |
| Firewall | Permitida la IP local configurada en Azure |

## Arquitectura minima viable

La aplicacion usa Azure SQL Database como unica base de datos.

- `DatabaseManager` abre siempre conexion contra Azure SQL Database.
- El servidor, la base de datos y el usuario tienen valores por defecto del recurso creado.
- La contrasena se mantiene fuera del codigo mediante `AZURE_SQL_PASSWORD` o `-Dazure.sql.password`.

Esta decision permite justificar que el proyecto ya usa persistencia online real manteniendo el modelo relacional.

## Descargar los drivers JDBC

Desde la raiz del proyecto:

```powershell
mkdir lib
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/com/microsoft/sqlserver/mssql-jdbc/13.4.0.jre11/mssql-jdbc-13.4.0.jre11.jar" -OutFile "lib\mssql-jdbc-13.4.0.jre11.jar"
```

## Ejecutar con Azure SQL Database

Configura las variables de entorno en la misma terminal:

```powershell
$env:AZURE_SQL_SERVER="proyectovideojuegoserver-pedro"
$env:AZURE_SQL_DATABASE="ProyectoVideojuegoBBDD"
$env:AZURE_SQL_USER="azureadmin"
$env:AZURE_SQL_PASSWORD="tu-contrasena"
```

`AZURE_SQL_SERVER`, `AZURE_SQL_DATABASE` y `AZURE_SQL_USER` son opcionales porque el codigo ya contiene los valores del recurso creado. `AZURE_SQL_PASSWORD` si es obligatoria.

Compila y ejecuta:

```powershell
javac -cp ".;lib\mssql-jdbc-13.4.0.jre11.jar" -d . *.java
java -cp ".;lib\mssql-jdbc-13.4.0.jre11.jar" ProyectoVideojuegoBBDD.Main
```

