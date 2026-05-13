package ProyectoVideojuegoBBDD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class ChessRepository {

    private ChessRepository() {
    }

    public static void saveGame(
        String whitePlayerName,
        String blackPlayerName,
        String currentTurn,
        String boardState,
        String result
    ) throws SQLException {
        try (Connection connection = DatabaseManager.getConnection()) {
            int whitePlayerId = findOrCreatePlayer(connection, whitePlayerName);
            int blackPlayerId = findOrCreatePlayer(connection, blackPlayerName);

            String sql =
                "INSERT INTO partida_ajedrez " +
                    "(jugador_blancas_id, jugador_negras_id, turno_actual, estado_tablero, resultado) " +
                    "VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, whitePlayerId);
                statement.setInt(2, blackPlayerId);
                statement.setString(3, currentTurn);
                statement.setString(4, boardState);
                statement.setString(5, result);
                statement.executeUpdate();
            }
        }
    }

    public static List<HistoryEntry> getRecentGames(int limit) throws SQLException {
        List<HistoryEntry> history = new ArrayList<>();

        String sql =
            "SELECT TOP (?) " +
                "bw.nombre AS blancas, bn.nombre AS negras, p.resultado, p.turno_actual, p.fecha_guardado " +
                "FROM partida_ajedrez p " +
                "LEFT JOIN jugador bw ON p.jugador_blancas_id = bw.id " +
                "LEFT JOIN jugador bn ON p.jugador_negras_id = bn.id " +
                "ORDER BY p.fecha_guardado DESC";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    history.add(new HistoryEntry(
                        resultSet.getString("blancas"),
                        resultSet.getString("negras"),
                        resultSet.getString("resultado"),
                        resultSet.getString("turno_actual"),
                        resultSet.getString("fecha_guardado")
                    ));
                }
            }
        }

        return history;
    }

    private static int findOrCreatePlayer(Connection connection, String playerName) throws SQLException {
        String normalizedName = normalizePlayerName(playerName);

        try (PreparedStatement statement = connection.prepareStatement("SELECT id FROM jugador WHERE nombre = ?")) {
            statement.setString(1, normalizedName);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                }
            }
        }

        try (PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO jugador (nombre) VALUES (?)",
            Statement.RETURN_GENERATED_KEYS
        )) {
            statement.setString(1, normalizedName);
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }

        throw new SQLException("No se pudo crear el jugador de ajedrez: " + normalizedName);
    }

    private static String normalizePlayerName(String playerName) {
        if (playerName == null || playerName.trim().isEmpty()) {
            return "Jugador";
        }

        return playerName.trim();
    }

    public static final class HistoryEntry {
        private final String whitePlayerName;
        private final String blackPlayerName;
        private final String result;
        private final String currentTurn;
        private final String date;

        private HistoryEntry(String whitePlayerName, String blackPlayerName, String result, String currentTurn, String date) {
            this.whitePlayerName = whitePlayerName;
            this.blackPlayerName = blackPlayerName;
            this.result = result;
            this.currentTurn = currentTurn;
            this.date = date;
        }

        public String getWhitePlayerName() {
            return whitePlayerName;
        }

        public String getBlackPlayerName() {
            return blackPlayerName;
        }

        public String getResult() {
            return result;
        }

        public String getCurrentTurn() {
            return currentTurn;
        }

        public String getDate() {
            return date;
        }
    }
}
