# Diccionario de datos

El diccionario de datos describe la informacion que necesita manejar el sistema. En esta fase, el modelo es estable y sencillo, por lo que se ha elegido una base de datos relacional SQLite.

### Tabla `jugador`

| Campo | Tipo | Restricciones | Descripcion |
| --- | --- | --- | --- |
| `id` | INTEGER | PK, AUTOINCREMENT | Identificador unico del jugador. |
| `nombre` | TEXT | NOT NULL, UNIQUE | Nombre introducido por el usuario antes de jugar. |
| `fecha_creacion` | TEXT | DEFAULT CURRENT_TIMESTAMP | Fecha en la que se registra el jugador. |

### Tabla `configuracion`

| Campo | Tipo | Restricciones | Descripcion |
| --- | --- | --- | --- |
| `clave` | TEXT | PK | Nombre del ajuste o preferencia. |
| `valor` | TEXT | NOT NULL | Valor asociado a la clave. |

### Tabla `partida_cathunter`

| Campo | Tipo | Restricciones | Descripcion |
| --- | --- | --- | --- |
| `id` | INTEGER | PK, AUTOINCREMENT | Identificador unico de la partida. |
| `jugador_id` | INTEGER | FK, NOT NULL | Jugador que realiza la partida. |
| `dificultad` | TEXT | NOT NULL | Nivel de dificultad: facil, medio o dificil. |
| `filas` | INTEGER | NOT NULL | Numero de filas del tablero. |
| `columnas` | INTEGER | NOT NULL | Numero de columnas del tablero. |
| `minas` | INTEGER | NOT NULL | Numero total de minas/gatos ocultos. |
| `banderas_usadas` | INTEGER | NOT NULL | Numero de banderas colocadas por el jugador. |
| `duracion_segundos` | INTEGER | NOT NULL | Duracion de la partida en segundos. |
| `ganada` | INTEGER | NOT NULL | Resultado de la partida: 1 si gana, 0 si pierde. |
| `estado_tablero` | TEXT | NOT NULL | Representacion serializada del tablero. |
| `fecha_partida` | TEXT | DEFAULT CURRENT_TIMESTAMP | Fecha de la partida. |

### Tabla `partida_ajedrez`

| Campo | Tipo | Restricciones | Descripcion |
| --- | --- | --- | --- |
| `id` | INTEGER | PK, AUTOINCREMENT | Identificador unico de la partida. |
| `jugador_blancas_id` | INTEGER | FK | Jugador que utiliza piezas blancas. |
| `jugador_negras_id` | INTEGER | FK | Jugador que utiliza piezas negras. |
| `turno_actual` | TEXT | NOT NULL | Color o jugador al que le corresponde mover. |
| `estado_tablero` | TEXT | NOT NULL | Representacion serializada de piezas y posiciones. |
| `resultado` | TEXT | NULL | Resultado final o estado de la partida. |
| `fecha_guardado` | TEXT | DEFAULT CURRENT_TIMESTAMP | Fecha en la que se guarda la partida. |
