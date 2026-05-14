package ProyectoVideojuegoBBDD;

import java.awt.*;
import java.net.URL;
import javax.swing.*;

public class Main {

    private static final Color MENU_TOP = new Color(19, 27, 36);
    private static final Color MENU_BOTTOM = new Color(42, 47, 58);
    private static final Color MENU_ACCENT = new Color(232, 190, 96);
    private static final Color BUTTON_PRIMARY = new Color(75, 113, 145);
    private static final Color BUTTON_SECONDARY = new Color(86, 124, 94);
    private static final Color BUTTON_EXIT = new Color(108, 73, 78);

    public static void main(String[] args) {
        DatabaseManager.initializeDatabase();

        JFrame window = new JFrame("Proyecto Videojuego");
        setWindowIcon(window);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(800, 800);
        window.setLayout(new BorderLayout());

        showMenu(window);

        window.setVisible(true);
    }

    private static void setWindowIcon(JFrame window) {
        URL iconUrl = Main.class.getResource("/ProyectoVideojuego.png");

        if (iconUrl == null) {
            iconUrl = Main.class.getResource("/ProyectoVideojuegoBBDD/ProyectoVideojuego.png");
        }

        if (iconUrl != null) {
            window.setIconImage(new ImageIcon(iconUrl).getImage());
        }
    }

    private static JButton createButton(String text) { // Metodo para crear botones con estilo uniforme
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFont(new Font("Arial", Font.BOLD, 22));
        button.setMaximumSize(new Dimension(300, 56));
        button.setFocusPainted(false);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 65), 1),
            BorderFactory.createEmptyBorder(10, 18, 10, 18)
        ));
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
        JPanel menuPanel = new MainMenuPanel();
        menuPanel.setLayout(new GridBagLayout());

        JLabel title = new JLabel("PROYECTO VIDEOJUEGO");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("Serif", Font.BOLD, 40));
        title.setForeground(MENU_ACCENT);

        JLabel hint = new JLabel("Elige una experiencia para comenzar");
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        hint.setFont(new Font("Arial", Font.PLAIN, 14));
        hint.setForeground(new Color(164, 178, 178));

        // Crear botones para los juegos.
        JButton chessButton = createButton("Ajedrez");
        JButton catHunterButton = createButton("Buscagatos");
        JButton exitButton = createButton("Salir");
        chessButton.setBackground(BUTTON_PRIMARY);
        catHunterButton.setBackground(BUTTON_SECONDARY);
        exitButton.setBackground(BUTTON_EXIT);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(42, 54, 44, 54));
        content.setPreferredSize(new Dimension(660, 520));

        content.add(title);
        content.add(Box.createVerticalStrut(44));
        content.add(hint);
        content.add(Box.createVerticalStrut(24));
        content.add(chessButton);
        content.add(Box.createVerticalStrut(16));
        content.add(catHunterButton);
        content.add(Box.createVerticalStrut(16));
        content.add(exitButton);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.insets = new Insets(24, 24, 24, 24);
        menuPanel.add(content, constraints);

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
            Board board = new Board(whitePlayer, blackPlayer);
            showGame(
                window,
                board,
                "Blancas: " + whitePlayer + " | Negras: " + blackPlayer,
                board::resetGame,
                "Historial",
                board::showHistoryDialog
            );
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

    public static void showGame(JFrame window, JPanel gamePanel) {
        showGame(window, gamePanel, null, null);
    }

    public static void showGameWithoutMenuBar(JFrame window, JPanel gamePanel) {
        window.getContentPane().removeAll();
        window.add(gamePanel, BorderLayout.CENTER);
        window.revalidate();
        window.repaint();
    }

    public static void showGame(JFrame window, JPanel gamePanel, String infoText, Runnable resetAction) {
        showGame(window, gamePanel, infoText, resetAction, null, null);
    }

    public static void showGame(
        JFrame window,
        JPanel gamePanel,
        String infoText,
        Runnable resetAction,
        String extraActionText,
        Runnable extraAction
    ) {
        JPanel screen = new JPanel(new BorderLayout());
        screen.add(createReturnPanel(window, infoText, resetAction, extraActionText, extraAction), BorderLayout.NORTH);
        screen.add(gamePanel, BorderLayout.CENTER);

        window.getContentPane().removeAll();
        window.add(screen, BorderLayout.CENTER);
        window.revalidate();
        window.repaint();
    }

    public static JButton createReturnButton(JFrame window) {
        JButton returnButton = new JButton("Volver al menu");
        returnButton.addActionListener(e -> {
            window.getContentPane().removeAll();
            showMenu(window);
            window.revalidate();
            window.repaint();
        });

        return returnButton;
    }

    private static JPanel createReturnPanel(
        JFrame window,
        String infoText,
        Runnable resetAction,
        String extraActionText,
        Runnable extraAction
    ) {
        JPanel topPanel = new JPanel(new BorderLayout(10, 5));
        topPanel.setBackground(Color.DARK_GRAY);
        topPanel.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        JLabel infoLabel = new JLabel(infoText == null ? "" : infoText);
        infoLabel.setForeground(Color.WHITE);
        infoLabel.setFont(new Font("Arial", Font.BOLD, 16));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonPanel.setOpaque(false);

        if (resetAction != null) {
            JButton resetButton = new JButton("Reiniciar");
            resetButton.addActionListener(e -> resetAction.run());
            buttonPanel.add(resetButton);
        }

        if (extraActionText != null && extraAction != null) {
            JButton extraButton = new JButton(extraActionText);
            extraButton.addActionListener(e -> extraAction.run());
            buttonPanel.add(extraButton);
        }

        buttonPanel.add(createReturnButton(window));
        topPanel.add(infoLabel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.EAST);
        return topPanel;
    }

    private static class MainMenuPanel extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint background = new GradientPaint(0, 0, MENU_TOP, 0, getHeight(), MENU_BOTTOM);
            g2.setPaint(background);
            g2.fillRect(0, 0, getWidth(), getHeight());

            paintBoardPattern(g2);
            paintGameSymbols(g2);
            paintCenterPanel(g2);

            g2.dispose();
        }

        private void paintBoardPattern(Graphics2D g2) {
            int size = 64;
            for (int y = 0; y < getHeight(); y += size) {
                for (int x = 0; x < getWidth(); x += size) {
                    boolean alternate = ((x / size) + (y / size)) % 2 == 0;
                    g2.setColor(alternate ? new Color(255, 255, 255, 10) : new Color(0, 0, 0, 14));
                    g2.fillRect(x, y, size, size);
                }
            }
        }

        private void paintGameSymbols(Graphics2D g2) {
            g2.setFont(new Font("Serif", Font.BOLD, 92));
            g2.setColor(new Color(232, 190, 96, 48));
            g2.drawString("♞", 86, 150);
            g2.drawString("♛", getWidth() - 170, getHeight() - 84);

            g2.setColor(new Color(155, 210, 170, 58));
            g2.fillOval(getWidth() - 180, 86, 46, 18);
            g2.fillOval(getWidth() - 98, 86, 46, 18);
            g2.setColor(new Color(20, 28, 28, 190));
            g2.fillOval(getWidth() - 160, 88, 8, 14);
            g2.fillOval(getWidth() - 78, 88, 8, 14);
        }

        private void paintCenterPanel(Graphics2D g2) {
            int panelWidth = 700;
            int panelHeight = 560;
            int x = (getWidth() - panelWidth) / 2;
            int y = (getHeight() - panelHeight) / 2;

            g2.setColor(new Color(0, 0, 0, 95));
            g2.fillRoundRect(x - 16, y - 16, panelWidth + 32, panelHeight + 32, 24, 24);
            g2.setColor(new Color(18, 27, 33, 220));
            g2.fillRoundRect(x, y, panelWidth, panelHeight, 18, 18);
            g2.setColor(new Color(255, 255, 255, 48));
            g2.drawRoundRect(x, y, panelWidth, panelHeight, 18, 18);
        }
    }
}
