package ProyectoVideojuegoBBDD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class CatHunterRepository {

    private CatHunterRepository() {
    }

    public static void saveGame(
        String playerName,
        String difficulty,
        int rows,
        int columns,
        int mines,
        int flagsUsed,
        int durationSeconds,
        boolean won,
        String boardState
    ) throws SQLException {
        try (Connection connection = DatabaseManager.getConnection()) {
            int playerId = PlayerRepository.findOrCreatePlayer(connection, playerName);

            String sql =
                "INSERT INTO partida_cathunter " +
                    "(jugador_id, dificultad, filas, columnas, minas, banderas_usadas, duracion_segundos, ganada, estado_tablero) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, playerId);
                statement.setString(2, difficulty);
                statement.setInt(3, rows);
                statement.setInt(4, columns);
                statement.setInt(5, mines);
                statement.setInt(6, flagsUsed);
                statement.setInt(7, durationSeconds);
                statement.setBoolean(8, won);
                statement.setString(9, boardState);
                statement.executeUpdate();
            }
        }
    }

    public static List<RankingEntry> getTopRanking(int limit) throws SQLException {
        List<RankingEntry> ranking = new ArrayList<>();

        String sql =
            "SELECT TOP (?) j.nombre, p.dificultad, p.duracion_segundos, p.ganada, p.fecha_partida " +
                "FROM partida_cathunter p " +
                "INNER JOIN jugador j ON p.jugador_id = j.id " +
                "ORDER BY p.ganada DESC, p.duracion_segundos ASC, p.fecha_partida DESC";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ranking.add(new RankingEntry(
                        resultSet.getString("nombre"),
                        resultSet.getString("dificultad"),
                        resultSet.getInt("duracion_segundos"),
                        resultSet.getBoolean("ganada"),
                        resultSet.getString("fecha_partida")
                    ));
                }
            }
        }

        return ranking;
    }

    public static final class RankingEntry {
        private final String playerName;
        private final String difficulty;
        private final int durationSeconds;
        private final boolean won;
        private final String date;

        private RankingEntry(String playerName, String difficulty, int durationSeconds, boolean won, String date) {
            this.playerName = playerName;
            this.difficulty = difficulty;
            this.durationSeconds = durationSeconds;
            this.won = won;
            this.date = date;
        }

        public String getPlayerName() {
            return playerName;
        }

        public String getDifficulty() {
            return difficulty;
        }

        public int getDurationSeconds() {
            return durationSeconds;
        }

        public boolean isWon() {
            return won;
        }

        public String getDate() {
            return date;
        }
    }
}
