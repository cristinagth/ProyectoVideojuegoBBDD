CREATE TABLE IF NOT EXISTS jugador (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL UNIQUE,
    fecha_creacion TEXT DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS configuracion (
    clave TEXT PRIMARY KEY,
    valor TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS partida_cathunter (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    jugador_id INTEGER NOT NULL,
    dificultad TEXT NOT NULL,
    filas INTEGER NOT NULL,
    columnas INTEGER NOT NULL,
    minas INTEGER NOT NULL,
    banderas_usadas INTEGER NOT NULL,
    duracion_segundos INTEGER NOT NULL,
    ganada INTEGER NOT NULL,
    estado_tablero TEXT NOT NULL,
    fecha_partida TEXT DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (jugador_id) REFERENCES jugador(id)
);

CREATE TABLE IF NOT EXISTS partida_ajedrez (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    jugador_blancas_id INTEGER,
    jugador_negras_id INTEGER,
    turno_actual TEXT NOT NULL,
    estado_tablero TEXT NOT NULL,
    resultado TEXT,
    fecha_guardado TEXT DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (jugador_blancas_id) REFERENCES jugador(id),
    FOREIGN KEY (jugador_negras_id) REFERENCES jugador(id)
);
