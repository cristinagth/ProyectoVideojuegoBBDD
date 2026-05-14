package ProyectoVideojuegoBBDD;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseManager {

    private static final String DEFAULT_AZURE_SQL_SERVER = "proyectovideojuegoserver-pedro";
    private static final String DEFAULT_AZURE_SQL_DATABASE = "ProyectoVideojuegoBBDD";
    private static final String DEFAULT_AZURE_SQL_USER = "azureadmin";

    private DatabaseManager() {
    }

    /**
     * Prepara la base de datos online de Azure SQL Database.
     * Crea las tablas del proyecto si todavia no existen.
     */
    public static void initializeDatabase() {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            createAzureTables(statement);
        } catch (Exception exception) {
            showDatabaseWarning(exception);
        }
    }

    /**
     * Abre una conexion JDBC contra Azure SQL Database.
     * Cada metodo que consulte o escriba datos debe cerrar su conexion al terminar.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(getAzureJdbcUrl());
    }

    private static String getAzureJdbcUrl() {
        String customUrl = getConfigValue("db.url", "DB_URL", null);
        if (customUrl != null && !customUrl.isBlank()) {
            return customUrl;
        }

        String server = getConfigValue("azure.sql.server", "AZURE_SQL_SERVER", DEFAULT_AZURE_SQL_SERVER);
        String database = getConfigValue("azure.sql.database", "AZURE_SQL_DATABASE", DEFAULT_AZURE_SQL_DATABASE);
        String user = getConfigValue("azure.sql.user", "AZURE_SQL_USER", DEFAULT_AZURE_SQL_USER);
        String password = getRequiredConfigValue("azure.sql.password", "AZURE_SQL_PASSWORD");
        String azureUser = user.contains("@") ? user : user + "@" + server;

        return "jdbc:sqlserver://" + server + ".database.windows.net:1433;" +
            "database=" + database + ";" +
            "user=" + azureUser + ";" +
            "password=" + password + ";" +
            "encrypt=true;" +
            "trustServerCertificate=false;" +
            "hostNameInCertificate=*.database.windows.net;" +
            "loginTimeout=30;";
    }

    private static String getConfigValue(String propertyName, String environmentName, String defaultValue) {
        String propertyValue = System.getProperty(propertyName);
        if (propertyValue != null && !propertyValue.isBlank()) {
            return propertyValue;
        }

        String environmentValue = System.getenv(environmentName);
        if (environmentValue != null && !environmentValue.isBlank()) {
            return environmentValue;
        }

        return defaultValue;
    }

    private static String getRequiredConfigValue(String propertyName, String environmentName) {
        String value = getConfigValue(propertyName, environmentName, null);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                "Falta configurar " + environmentName + " o la propiedad -D" + propertyName
            );
        }
        return value;
    }

    private static void createAzureTables(Statement statement) throws SQLException {
        statement.executeUpdate(
            "IF OBJECT_ID('jugador', 'U') IS NULL " +
                "CREATE TABLE jugador (" +
                    "id INT IDENTITY(1,1) PRIMARY KEY, " +
                    "nombre NVARCHAR(100) NOT NULL UNIQUE, " +
                    "fecha_creacion DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()" +
                ")"
        );

        statement.executeUpdate(
            "IF OBJECT_ID('configuracion', 'U') IS NULL " +
                "CREATE TABLE configuracion (" +
                    "clave NVARCHAR(100) PRIMARY KEY, " +
                    "valor NVARCHAR(500) NOT NULL" +
                ")"
        );

        statement.executeUpdate(
            "IF OBJECT_ID('partida_cathunter', 'U') IS NULL " +
                "CREATE TABLE partida_cathunter (" +
                    "id INT IDENTITY(1,1) PRIMARY KEY, " +
                    "jugador_id INT NOT NULL, " +
                    "dificultad NVARCHAR(30) NOT NULL, " +
                    "filas INT NOT NULL, " +
                    "columnas INT NOT NULL, " +
                    "minas INT NOT NULL, " +
                    "banderas_usadas INT NOT NULL, " +
                    "duracion_segundos INT NOT NULL, " +
                    "ganada BIT NOT NULL, " +
                    "estado_tablero NVARCHAR(MAX) NOT NULL, " +
                    "fecha_partida DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(), " +
                    "CONSTRAINT FK_partida_cathunter_jugador " +
                        "FOREIGN KEY (jugador_id) REFERENCES jugador(id)" +
                ")"
        );

        statement.executeUpdate(
            "IF OBJECT_ID('partida_ajedrez', 'U') IS NULL " +
                "CREATE TABLE partida_ajedrez (" +
                    "id INT IDENTITY(1,1) PRIMARY KEY, " +
                    "jugador_blancas_id INT NULL, " +
                    "jugador_negras_id INT NULL, " +
                    "turno_actual NVARCHAR(30) NOT NULL, " +
                    "estado_tablero NVARCHAR(MAX) NOT NULL, " +
                    "resultado NVARCHAR(200) NULL, " +
                    "fecha_guardado DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(), " +
                    "CONSTRAINT FK_partida_ajedrez_blancas " +
                        "FOREIGN KEY (jugador_blancas_id) REFERENCES jugador(id), " +
                    "CONSTRAINT FK_partida_ajedrez_negras " +
                        "FOREIGN KEY (jugador_negras_id) REFERENCES jugador(id)" +
                ")"
        );

        statement.executeUpdate(
            "IF COL_LENGTH('partida_ajedrez', 'resultado') IS NOT NULL " +
                "ALTER TABLE partida_ajedrez ALTER COLUMN resultado NVARCHAR(200) NULL"
        );
    }

    /**
     * Muestra un aviso no bloqueante si Azure SQL no esta disponible.
     * Esto permite que la aplicacion siga abriendo y deje claro que falta configurar la conexion.
     */
    private static void showDatabaseWarning(Exception exception) {
        System.err.println("No se ha podido acceder a Azure SQL Database: " + exception.getMessage());
        System.err.println("Comprueba AZURE_SQL_PASSWORD y el driver JDBC de SQL Server en el classpath.");
    }
}
