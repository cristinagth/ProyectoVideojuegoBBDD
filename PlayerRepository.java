package ProyectoVideojuegoBBDD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class PlayerRepository {

    private PlayerRepository() {
    }

    public static int findOrCreatePlayer(Connection connection, String playerName) throws SQLException {
        String normalizedName = normalizePlayerName(playerName);

        Integer existingPlayerId = findPlayerId(connection, normalizedName);
        if (existingPlayerId != null) {
            return existingPlayerId;
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

        existingPlayerId = findPlayerId(connection, normalizedName);
        if (existingPlayerId != null) {
            return existingPlayerId;
        }

        throw new SQLException("No se pudo crear ni recuperar el jugador: " + normalizedName);
    }

    private static Integer findPlayerId(Connection connection, String playerName) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT id FROM jugador WHERE nombre = ?")) {
            statement.setString(1, playerName);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                }
            }
        }

        return null;
    }

    private static String normalizePlayerName(String playerName) {
        if (playerName == null || playerName.trim().isEmpty()) {
            return "Jugador";
        }

        return playerName.trim();
    }
}
