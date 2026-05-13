IF OBJECT_ID('jugador', 'U') IS NULL
CREATE TABLE jugador (
    id INT IDENTITY(1,1) PRIMARY KEY,
    nombre NVARCHAR(100) NOT NULL UNIQUE,
    fecha_creacion DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
);

IF OBJECT_ID('configuracion', 'U') IS NULL
CREATE TABLE configuracion (
    clave NVARCHAR(100) PRIMARY KEY,
    valor NVARCHAR(500) NOT NULL
);

IF OBJECT_ID('partida_cathunter', 'U') IS NULL
CREATE TABLE partida_cathunter (
    id INT IDENTITY(1,1) PRIMARY KEY,
    jugador_id INT NOT NULL,
    dificultad NVARCHAR(30) NOT NULL,
    filas INT NOT NULL,
    columnas INT NOT NULL,
    minas INT NOT NULL,
    banderas_usadas INT NOT NULL,
    duracion_segundos INT NOT NULL,
    ganada BIT NOT NULL,
    estado_tablero NVARCHAR(MAX) NOT NULL,
    fecha_partida DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_partida_cathunter_jugador
        FOREIGN KEY (jugador_id) REFERENCES jugador(id)
);

IF OBJECT_ID('partida_ajedrez', 'U') IS NULL
CREATE TABLE partida_ajedrez (
    id INT IDENTITY(1,1) PRIMARY KEY,
    jugador_blancas_id INT NULL,
    jugador_negras_id INT NULL,
    turno_actual NVARCHAR(30) NOT NULL,
    estado_tablero NVARCHAR(MAX) NOT NULL,
    resultado NVARCHAR(50) NULL,
    fecha_guardado DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_partida_ajedrez_blancas
        FOREIGN KEY (jugador_blancas_id) REFERENCES jugador(id),
    CONSTRAINT FK_partida_ajedrez_negras
        FOREIGN KEY (jugador_negras_id) REFERENCES jugador(id)
);
