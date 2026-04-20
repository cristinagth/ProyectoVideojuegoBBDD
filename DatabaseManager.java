package ProyectoVideojuegoBBDD;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseManager {

    private static final String DB_DIRECTORY = "data";
    private static final String DB_FILE_NAME = "proyecto_videojuego.db";
    private static final String JDBC_PREFIX = "jdbc:sqlite:";

    private DatabaseManager() {
    }

    /**
     * Prepara la base de datos local para que la aplicacion pueda usarla.
     * Crea la carpeta `data/` y las tablas si todavia no existen.
     */
    public static void initializeDatabase() {
        try {
            Files.createDirectories(getDatabasePath().getParent());

            try (Connection connection = getConnection();
                 Statement statement = connection.createStatement()) {
                statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS jugador (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "nombre TEXT NOT NULL UNIQUE, " +
                        "fecha_creacion TEXT DEFAULT CURRENT_TIMESTAMP" +
                    ")"
                );

                statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS configuracion (" +
                        "clave TEXT PRIMARY KEY, " +
                        "valor TEXT NOT NULL" +
                    ")"
                );

                statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS partida_cathunter (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "jugador_id INTEGER NOT NULL, " +
                        "dificultad TEXT NOT NULL, " +
                        "filas INTEGER NOT NULL, " +
                        "columnas INTEGER NOT NULL, " +
                        "minas INTEGER NOT NULL, " +
                        "banderas_usadas INTEGER NOT NULL, " +
                        "duracion_segundos INTEGER NOT NULL, " +
                        "ganada INTEGER NOT NULL, " +
                        "estado_tablero TEXT NOT NULL, " +
                        "fecha_partida TEXT DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY (jugador_id) REFERENCES jugador(id)" +
                    ")"
                );

                statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS partida_ajedrez (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "jugador_blancas_id INTEGER, " +
                        "jugador_negras_id INTEGER, " +
                        "turno_actual TEXT NOT NULL, " +
                        "estado_tablero TEXT NOT NULL, " +
                        "resultado TEXT, " +
                        "fecha_guardado TEXT DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY (jugador_blancas_id) REFERENCES jugador(id), " +
                        "FOREIGN KEY (jugador_negras_id) REFERENCES jugador(id)" +
                    ")"
                );
            }
        } catch (Exception exception) {
            showDatabaseWarning(exception);
        }
    }

    /**
     * Abre una conexion JDBC contra el fichero SQLite local.
     * Cada metodo que consulte o escriba datos debe cerrar su conexion al terminar.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_PREFIX + getDatabasePath());
    }

    /**
     * Devuelve la ruta absoluta del fichero de base de datos.
     * Se usa para mostrar o documentar donde se guardan los datos locales.
     */
    public static String getDatabaseFilePath() {
        return getDatabasePath().toString();
    }

    /**
     * Construye la ruta del fichero SQLite usando la carpeta `data/`.
     * La ruta se calcula como absoluta para evitar dudas segun desde donde se ejecute la app.
     */
    private static Path getDatabasePath() {
        return Paths.get(DB_DIRECTORY, DB_FILE_NAME).toAbsolutePath();
    }

    /**
     * Muestra un aviso no bloqueante si SQLite no esta disponible.
     * Esto permite que la aplicacion siga abriendo aunque falte el driver JDBC.
     */
    private static void showDatabaseWarning(Exception exception) {
        System.err.println("No se ha podido acceder a la base de datos: " + exception.getMessage());
        System.err.println("Comprueba que el driver JDBC de SQLite este en el classpath.");
    }
}
