package ProyectoVideojuegoBBDD;

import java.awt.*;
import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        JFrame window = new JFrame("Proyecto Videojuego");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(800, 800);
        window.setLayout(new BorderLayout());

        showMenu(window);

        window.setVisible(true);
    }

    private static JButton createButton(String text) { // Metodo para crear botones con estilo uniforme
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFont(new Font("Arial", Font.BOLD, 22));
        button.setMaximumSize(new Dimension(250, 50));
        button.setFocusPainted(false);
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        return button;
    }

    private static String requestPlayerName(Component parent, String message, String defaultName) {
        // Si el usuario cancela, devolvemos null para quedarnos en el menu.
        String input = JOptionPane.showInputDialog(parent, message, defaultName);

        if (input == null) {
            return null;
        }

        String trimmed = input.trim();
        return trimmed.isEmpty() ? defaultName : trimmed;
    }

    public static void showMenu(JFrame window) {
        // Panel para el menu principal.
        JPanel menuPanel = new JPanel();
        menuPanel.setBackground(new Color(30, 30, 30));
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("PROYECTO VIDEOJUEGO");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("Arial", Font.BOLD, 36));
        title.setForeground(Color.WHITE);

        // Crear botones para los juegos.
        JButton chessButton = createButton("Ajedrez");
        JButton catHunterButton = createButton("Buscagatos");
        JButton exitButton = createButton("Salir");

        menuPanel.add(Box.createVerticalStrut(120));
        menuPanel.add(title);
        menuPanel.add(Box.createVerticalStrut(80));
        menuPanel.add(chessButton);
        menuPanel.add(Box.createVerticalStrut(20));
        menuPanel.add(catHunterButton);
        menuPanel.add(Box.createVerticalStrut(20));
        menuPanel.add(exitButton);

        window.add(menuPanel, BorderLayout.CENTER);

        chessButton.addActionListener(e -> {
            // Al hacer clic en Ajedrez, se piden dos jugadores antes de mostrar el tablero.
            String whitePlayer = requestPlayerName(window, "Introduce el nombre del jugador de blancas:", "Jugador blancas");
            if (whitePlayer == null) {
                return;
            }

            String blackPlayer = requestPlayerName(window, "Introduce el nombre del jugador de negras:", "Jugador negras");
            if (blackPlayer == null) {
                return;
            }

            window.getContentPane().removeAll();
            window.add(new Board(window, whitePlayer, blackPlayer), BorderLayout.CENTER);
            window.revalidate();
            window.repaint();
        });

        catHunterButton.addActionListener(e -> {
            // Al hacer clic en Buscagatos, se pide un unico jugador antes de la introduccion.
            String playerName = requestPlayerName(window, "Introduce el nombre del jugador:", "Jugador");
            if (playerName == null) {
                return;
            }

            window.getContentPane().removeAll();
            window.add(new CatHunterIntro(window, playerName), BorderLayout.CENTER);
            window.revalidate();
            window.repaint();
        });

        exitButton.addActionListener(e -> System.exit(0)); // Al hacer clic en Salir, se cierra la aplicacion.
    }
}
