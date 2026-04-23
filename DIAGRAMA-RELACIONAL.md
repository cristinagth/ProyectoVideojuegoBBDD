# Diagrama Relacional

Este archivo recoge el diagrama relacional actual de la base de datos del proyecto.
Se ha representado en formato `Mermaid` para poder visualizarlo en GitHub, VS Code o Mermaid Live.

```mermaid
erDiagram
    JUGADOR {
        INTEGER id PK
        TEXT nombre UK
        TEXT fecha_creacion
    }

    CONFIGURACION {
        TEXT clave PK
        TEXT valor
    }

    PARTIDA_CATHUNTER {
        INTEGER id PK
        INTEGER jugador_id FK
        TEXT dificultad
        INTEGER filas
        INTEGER columnas
        INTEGER minas
        INTEGER banderas_usadas
        INTEGER duracion_segundos
        INTEGER ganada
        TEXT estado_tablero
        TEXT fecha_partida
    }

    PARTIDA_AJEDREZ {
        INTEGER id PK
        INTEGER jugador_blancas_id FK
        INTEGER jugador_negras_id FK
        TEXT turno_actual
        TEXT estado_tablero
        TEXT resultado
        TEXT fecha_guardado
    }

    JUGADOR ||--o{ PARTIDA_CATHUNTER : juega
    JUGADOR ||--o{ PARTIDA_AJEDREZ : blancas
    JUGADOR ||--o{ PARTIDA_AJEDREZ : negras
```

## Explicacion breve

- `jugador` almacena los perfiles de jugadores.
- `configuracion` guarda pares simples `clave -> valor` para ajustes globales.
- `partida_cathunter` guarda partidas de Buscagatos y referencia a un jugador.
- `partida_ajedrez` guarda partidas de Ajedrez y referencia a los jugadores de blancas y negras.

## Notas

- `configuracion` no tiene relaciones con otras tablas porque actua como tabla de preferencias globales.
- `partida_ajedrez` tiene dos claves foraneas hacia `jugador`:
  - una para el jugador de blancas
  - otra para el jugador de negras
- El campo `estado_tablero` se guarda como `TEXT` para permitir una serializacion sencilla del estado del juego.
