# ProyectoVideojuegoBBDD

Proyecto academico en Java Swing/AWT con dos juegos:

- Ajedrez
- Buscagatos / CatHunter

La aplicacion usa Azure SQL Database como sistema de persistencia online.

## Funcionalidades principales

### Menu principal

- Portada visual del proyecto.
- Seleccion entre Ajedrez y Buscagatos.
- Peticion de nombres de jugadores antes de iniciar cada juego.

### Ajedrez

- Tablero 8x8 con piezas graficas.
- Dos jugadores locales.
- Control de turnos.
- Movimientos legales por pieza.
- Resaltado de movimientos:
  - dorado para movimientos normales
  - rojo para capturas
- Deteccion de jaque.
- Deteccion de jaque mate.
- Bloqueo de movimientos que dejan al propio rey en jaque.
- Promocion automatica de peon a reina.
- Historial de partidas guardadas en Azure SQL.
- Guardado de partida al finalizar por jaque mate.

No implementado:

- Enroque.
- En passant.
- Tablas por ahogado.
- Eleccion de pieza en promocion.

### Buscagatos / CatHunter

- Version tematizada de Buscaminas.
- Selector de dificultad.
- Temporizador por dificultad.
- Eventos aleatorios gatunos opcionales.
- Log visual de eventos recientes.
- Ranking consultado desde Azure SQL.
- Guardado de partidas al ganar o perder.
- Revelado de gatos al perder.
- Estilo visual personalizado.

Mas detalles en:

```text
README-CATHUNTER.md
```

## Base de datos

La base de datos activa es Azure SQL Database.

Tablas principales:

- `jugador`
- `configuracion`
- `partida_cathunter`
- `partida_ajedrez`

Documentacion:

```text
README-AZURE.md
README-BBDD.md
schema-azure.sql
```

## Requisitos

- Java instalado.
- Driver JDBC de SQL Server en `lib/`.
- Variable de entorno `AZURE_SQL_PASSWORD` configurada.

Driver usado:

```text
lib/mssql-jdbc-13.4.0.jre11.jar
```

## Ejecucion en Windows PowerShell

Desde la raiz del proyecto:

```powershell
cd D:\ProyectoBBDD\ProyectoVideojuegoBBDD
$env:AZURE_SQL_PASSWORD="tu-contrasena"
javac -cp ".;lib\mssql-jdbc-13.4.0.jre11.jar" -d . *.java
java -cp ".;lib\mssql-jdbc-13.4.0.jre11.jar" ProyectoVideojuegoBBDD.Main
```

## Estructura principal

```text
Main.java
Board.java
Chesspiece.java
Pawn.java
Rook.java
Knight.java
Bishop.java
Queen.java
King.java
CatHunterIntro.java
CatHunterBoard.java
Cell.java
DatabaseManager.java
ChessRepository.java
CatHunterRepository.java
schema-azure.sql
README-AZURE.md
README-BBDD.md
README-CATHUNTER.md
```

## Notas

- `.env.azure.local`, `.env.local`, `lib/`, `*.jar`, `*.class` y la carpeta compilada del paquete estan ignorados por Git.
- Azure SQL tarda un poco en arrancar al ser la opción free, la primera consulta puede tardar unos segundos en responder.
