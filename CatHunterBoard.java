package ProyectoVideojuegoBBDD;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.*;

public class CatHunterBoard extends JPanel {

    public enum Difficulty {
        EASY("Facil", 8, 8, 10, 10, 60),
        MEDIUM("Medio", 16, 16, 40, 20, 36),
        HARD("Dificil", 16, 30, 99, 30, 26);

        public final String displayName;
        public final int rows;
        public final int cols;
        public final int mines;
        public final int timeLimitMinutes;
        public final int cellSize;

        Difficulty(String displayName, int r, int c, int m, int timeLimitMinutes, int cellSize) {
            this.displayName = displayName;
            rows = r;
            cols = c;
            mines = m;
            this.timeLimitMinutes = timeLimitMinutes;
            this.cellSize = cellSize;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    private final String playerName;
    private final Difficulty difficulty;
    private int rows, cols, mines;
    private Cell[][] grid;
    private int cellSize = 30;
    private boolean firstClick = true;
    private boolean gameOver = false; // Variable para controlar si el juego ha terminado.
    private int flagsUsed = 0;
    private int remainingSeconds;
    private Timer gameTimer;
    private Timer eventTimer;
    private final Random random = new Random();
    private JButton resetButton;
    private JButton menuButton;
    private JLabel infoLabel;
    private int topOffset = 40;
    private final BufferedImage catImage;
    private final BufferedImage yarnImage;

    public CatHunterBoard(Difficulty difficulty) {
        this(difficulty, "Jugador");
    }

    public CatHunterBoard(Difficulty difficulty, String playerName) {
        this(difficulty, playerName, null);
    }

    public CatHunterBoard(Difficulty difficulty, String playerName, JButton menuButton) {
        this.menuButton = menuButton;
        this.difficulty = difficulty;
        this.playerName = normalizePlayerName(playerName);
        this.catImage = loadImage("gato.png");
        this.yarnImage = loadImage("Ovillo.png");
        // Configuracion segun dificultad.
        this.rows = difficulty.rows;
        this.cols = difficulty.cols;
        this.mines = difficulty.mines;
        this.cellSize = difficulty.cellSize;
        this.remainingSeconds = difficulty.timeLimitMinutes * 60;

        setPreferredSize(new Dimension(cols * cellSize, rows * cellSize + topOffset)); // Tamano del panel segun el numero de celdas.
        setLayout(new BorderLayout());
        add(createTopPanel(), BorderLayout.NORTH);
        initBoard();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int c = e.getX() / cellSize; // Columna clickeada.
                int r = (e.getY() - topOffset) / cellSize; // Fila clickeada ajustada por el panel superior.

                if (!isInside(r, c) || gameOver) {
                    return;
                }

                if (SwingUtilities.isRightMouseButton(e)) {
                    toggleFlag(r, c);
                } else {
                    if (firstClick) {
                        placeMines(r, c);
                        calculateNumbers();
                        firstClick = false;
                    }
                    reveal(r, c);
                }
                repaint();
            }
        });
    }

    // Metodo para inicializar el tablero.
    private void initBoard() {
        grid = new Cell[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = new Cell();
            }
        }
    }

    private boolean isInside(int r, int c) { // Metodo para verificar si una celda esta dentro de los limites del tablero.
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }

    // Coloca minas en el tablero asegurando que la primera casilla y sus alrededores esten libres.
    private void placeMines(int safeRow, int safeCol) {
        int placed = 0;

        while (placed < mines) { // Mientras no se hayan colocado todas las minas.
            int r = random.nextInt(rows);
            int c = random.nextInt(cols);

            if (Math.abs(r - safeRow) <= 1 && Math.abs(c - safeCol) <= 1) {
                continue;
            }

            if (!grid[r][c].hasMine()) { // Si no hay mina en esa celda, colocarla.
                grid[r][c].setMine(true);
                placed++;
            }
        }
    }

    // Calcula el numero de minas adyacentes a cada celda.
    private void calculateNumbers() {
        int[] dr = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dc = {-1, 0, 1, -1, 1, -1, 0, 1};

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (grid[r][c].hasMine()) { // Si la celda tiene mina, no se calcula el numero.
                    continue;
                }

                int count = 0;
                for (int i = 0; i < 8; i++) {
                    int nr = r + dr[i];
                    int nc = c + dc[i];

                    if (isInside(nr, nc) && grid[nr][nc].hasMine()) { // Si la celda vecina tiene mina, incrementar contador.
                        count++;
                    }
                }
                grid[r][c].setAdjacentMines(count); // Dice a la celda cuantas minas tiene alrededor.
            }
        }
    }

    private void reveal(int r, int c) { // Metodo para revelar una celda.
        if (!isInside(r, c) || grid[r][c].isRevealed() || grid[r][c].isFlagged()) {
            return;
        }

        grid[r][c].setRevealed(true); // Revelar la celda actual.

        if (grid[r][c].hasMine()) { // Si la celda tiene una mina, el juego termina.
            finishGame("Has perdido. Te has encontrado un gato.");
            return;
        }

        if (grid[r][c].getAdjacentMines() == 0) { // Si no tiene minas alrededor, revelar las celdas vecinas.
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    reveal(r + dr, c + dc);
                }
            }
        }

        checkWin(); // Verificar si el jugador ha ganado despues de revelar la celda.
    }

    private void toggleFlag(int r, int c) { // Metodo para colocar o quitar una bandera en una celda.
        if (!grid[r][c].isRevealed()) {
            grid[r][c].setFlagged(!grid[r][c].isFlagged());

            if (grid[r][c].isFlagged()) {
                flagsUsed++;
            } else {
                flagsUsed--;
            }

            updateInfo();
        }
    }

    private void resetGame() {
        // Pone los valores iniciales para reiniciar el juego.
        firstClick = true;
        gameOver = false;
        flagsUsed = 0;
        remainingSeconds = difficulty.timeLimitMinutes * 60;

        initBoard();
        updateInfo();
        startTimer();
        startEventTimer();
        repaint();
    }

    private void checkWin() { // Metodo para verificar si el jugador ha ganado.
        int unrevealed = 0;

        for (int r = 0; r < rows; r++) { // Recorre filas y columnas para contar celdas no reveladas.
            for (int c = 0; c < cols; c++) {
                if (!grid[r][c].isRevealed()) {
                    unrevealed++;
                }
            }
        }

        if (unrevealed == mines) { // Si las celdas no reveladas coinciden con las minas, gana.
            finishGame("Has ganado. Encontraste todos los gatos.");
        }
    }

    private JPanel createTopPanel() { // Crea el panel superior con informacion del juego y boton de reinicio.
        JPanel top = new JPanel(new BorderLayout(10, 5));
        top.setBackground(Color.DARK_GRAY);
        top.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));

        infoLabel = new JLabel();
        infoLabel.setForeground(Color.WHITE);
        infoLabel.setFont(new Font("Arial", Font.BOLD, 16));

        resetButton = new JButton("Reiniciar");
        resetButton.addActionListener(e -> resetGame());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(resetButton);
        if (menuButton != null) {
            buttonPanel.add(menuButton);
        }

        top.add(infoLabel, BorderLayout.CENTER);
        top.add(buttonPanel, BorderLayout.EAST);

        updateInfo();

        return top;
    }

    private void updateInfo() { // Actualiza minas, banderas usadas y minas restantes.
        int remaining = mines - flagsUsed;
        infoLabel.setText(
            "Jugador: " + playerName +
                " | Dificultad: " + difficulty.displayName +
                " | Tiempo: " + formatTime(remainingSeconds) +
                " | Minas: " + mines +
                " | Banderas: " + flagsUsed +
                " | Restantes: " + remaining
        );
    }

    private void startTimer() {
        stopTimer();
        gameTimer = new Timer(1000, e -> {
            if (gameOver) {
                stopTimer();
                return;
            }

            remainingSeconds--;
            updateInfo();

            if (remainingSeconds <= 0) {
                finishGame("Se ha agotado el tiempo. Has perdido.");
            }
        });
        gameTimer.start();
    }

    private void startEventTimer() {
        stopEventTimer();
        eventTimer = new Timer(5000, e -> { // Cada 5 segundos, hay una posibilidad de que ocurra un evento aleatorio.
            if (gameOver) {
                stopEventTimer();
                return;
            }

            if (firstClick) {
                return;
            }

            // if (random.nextBoolean()) {
                triggerRandomEvent();
            // }
        });
        eventTimer.start();
    }

    private void stopTimer() {
        if (gameTimer != null) {
            gameTimer.stop();
            gameTimer = null;
        }
    }

    private void stopEventTimer() {
        if (eventTimer != null) {
            eventTimer.stop();
            eventTimer = null;
        }
    }

    private void finishGame(String message) {
        if (gameOver) {
            return;
        }

        gameOver = true;
        stopTimer();
        stopEventTimer();
        updateInfo();
        JOptionPane.showMessageDialog(this, message);
    }

    private void triggerRandomEvent() {
        switch (random.nextInt(4)) {
            case 0:
                placeHelpfulFlagEvent();
                break;
            case 1:
                addTimeEvent();
                break;
            case 2:
                removeTimeEvent();
                break;
            default:
                removeRandomFlagEvent();
                break;
        }
    }

    private void placeHelpfulFlagEvent() {
        CellPosition position = findRandomCell(true, false, false);
        if (position == null) {
            showEventMessage("Un gato ha pisado un cascabel, pero ya no quedan pistas nuevas.");
            return;
        }

        grid[position.row][position.col].setFlagged(true);
        flagsUsed++;
        updateInfo();
        repaint();
        showEventMessage("El gato ha pisado un cascabel. Una bandera aparece sobre una casilla sospechosa.");
    }

    private void addTimeEvent() {
        int seconds = getRandomTimeBonus();
        remainingSeconds += seconds;
        updateInfo();
        showEventMessage("Has encontrado un ovillo brillante. Ganas " + seconds + " segundos.");
    }

    private void removeTimeEvent() {
        int seconds = getRandomTimeBonus();
        remainingSeconds = Math.max(0, remainingSeconds - seconds);
        updateInfo();

        if (remainingSeconds == 0) {
            finishGame("Un gato ha tirado un reloj al suelo. Te has quedado sin tiempo.");
            return;
        }

        showEventMessage("Un gato tira un vaso al suelo. Pierdes " + seconds + " segundos.");
    }

    private void removeRandomFlagEvent() {
        CellPosition position = findRandomCell(null, false, true);
        if (position == null) {
            showEventMessage("Un ovillo rueda por el tablero, pero no encuentra ninguna bandera que mover.");
            return;
        }

        grid[position.row][position.col].setFlagged(false);
        flagsUsed--;
        updateInfo();
        repaint();
        showEventMessage("Un ovillo rueda por el tablero y arrastra una de tus banderas.");
    }

    private int getRandomTimeBonus() {
        int[] options = {15, 30, 45};
        return options[random.nextInt(options.length)];
    }

    private CellPosition findRandomCell(Boolean mustHaveMine, boolean mustBeRevealed, boolean mustBeFlagged) {
        CellPosition[] positions = new CellPosition[rows * cols];
        int count = 0;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = grid[r][c];

                if (mustHaveMine != null && cell.hasMine() != mustHaveMine.booleanValue()) {
                    continue;
                }

                if (cell.isRevealed() != mustBeRevealed || cell.isFlagged() != mustBeFlagged) {
                    continue;
                }

                positions[count] = new CellPosition(r, c);
                count++;
            }
        }

        if (count == 0) {
            return null;
        }

        return positions[random.nextInt(count)];
    }

    private void showEventMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Evento gatuno", JOptionPane.INFORMATION_MESSAGE);
    }

    private String formatTime(int seconds) {
        int safeSeconds = Math.max(0, seconds);
        int minutes = safeSeconds / 60;
        int remaining = safeSeconds % 60;
        return String.format("%02d:%02d", minutes, remaining);
    }

    private String normalizePlayerName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "Jugador";
        }

        return name.trim();
    }

    private URL getImageUrl(String imageName) {
        URL imageUrl = getClass().getResource("/figuresCatHunter/" + imageName);

        if (imageUrl == null) {
            imageUrl = getClass().getResource("/ProyectoVideojuegoBBDD/figuresCatHunter/" + imageName);
        }

        if (imageUrl == null) {
            throw new IllegalArgumentException("No se encontro la imagen de CatHunter: " + imageName);
        }

        return imageUrl;
    }

    private BufferedImage loadImage(String imageName) {
        try {
            return ImageIO.read(getImageUrl(imageName));
        } catch (IOException exception) {
            throw new IllegalArgumentException("No se pudo cargar la imagen de CatHunter: " + imageName, exception);
        }
    }

    @Override
    protected void paintComponent(Graphics g) { // Metodo para dibujar el tablero y las celdas.
        super.paintComponent(g);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int x = c * cellSize;
                int y = r * cellSize + topOffset;

                Cell cell = grid[r][c];

                if (!cell.isRevealed()) { // Si la celda no esta revelada, se dibuja gris.
                    g.setColor(Color.GRAY);
                    g.fillRect(x, y, cellSize, cellSize);

                    if (cell.isFlagged()) { // Si tiene bandera, se dibuja una F roja.
                        g.drawImage(yarnImage, x + 4, y + 4, cellSize - 8, cellSize - 8, null);
                    }
                } else {
                    g.setColor(Color.LIGHT_GRAY);
                    g.fillRect(x, y, cellSize, cellSize);

                    if (cell.hasMine()) { // Si tiene mina, se dibuja un circulo.
                        g.drawImage(catImage, x + 4, y + 4, cellSize - 8, cellSize - 8, null);
                    } else if (cell.getAdjacentMines() > 0) { // Si no tiene mina, se dibuja el numero.
                        drawAdjacentMineNumber(g, cell.getAdjacentMines(), x, y);
                    }
                }

                g.setColor(Color.DARK_GRAY); // Se dibujan las lineas del tablero.
                g.drawRect(x, y, cellSize, cellSize); // Dibuja el borde de cada celda.
            }
        }
    }

    private void drawAdjacentMineNumber(Graphics g, int number, int x, int y) {
        Graphics2D g2 = (Graphics2D) g;
        Font previousFont = g2.getFont();
        Font numberFont = previousFont.deriveFont(Font.BOLD, Math.max(16f, cellSize * 0.55f));
        String text = String.valueOf(number);

        g2.setFont(numberFont);
        g2.setColor(getNumberColor(number));

        FontMetrics metrics = g2.getFontMetrics();
        int textX = x + (cellSize - metrics.stringWidth(text)) / 2;
        int textY = y + ((cellSize - metrics.getHeight()) / 2) + metrics.getAscent();
        g2.drawString(text, textX, textY);

        g2.setFont(previousFont);
    }

    private Color getNumberColor(int number) {
        switch (number) {
            case 1:
                return new Color(25, 80, 200);
            case 2:
                return new Color(20, 130, 60);
            case 3:
                return new Color(210, 40, 40);
            case 4:
                return new Color(80, 45, 150);
            case 5:
                return new Color(140, 50, 30);
            case 6:
                return new Color(20, 140, 145);
            case 7:
                return Color.BLACK;
            case 8:
                return Color.DARK_GRAY;
            default:
                return Color.BLUE;
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        startTimer();
        startEventTimer();
    }

    @Override
    public void removeNotify() {
        stopTimer();
        stopEventTimer();
        super.removeNotify();
    }

    private static final class CellPosition {
        private final int row;
        private final int col;

        private CellPosition(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }
}
