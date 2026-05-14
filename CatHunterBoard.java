package ProyectoVideojuegoBBDD;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.*;

public class CatHunterBoard extends JPanel {

    private static final Color HIDDEN_CELL_LIGHT = new Color(49, 67, 74);
    private static final Color HIDDEN_CELL_DARK = new Color(42, 57, 64);
    private static final Color REVEALED_CELL_LIGHT = new Color(215, 221, 217);
    private static final Color REVEALED_CELL_DARK = new Color(201, 209, 205);
    private static final Color CAT_CELL_BACKGROUND = new Color(222, 184, 184);
    private static final Color GRID_COLOR = new Color(31, 42, 46);
    private static final Color EVENT_POSITIVE = new Color(95, 135, 98, 230);
    private static final Color EVENT_NEGATIVE = new Color(132, 75, 80, 230);
    private static final Color EVENT_NEUTRAL = new Color(55, 76, 86, 230);
    private static final Color EVENT_BORDER = new Color(242, 203, 110, 210);
    private static final int EVENT_INTERVAL_MS = 30000;
    private static final int EVENT_PROBABILITY_PERCENT = 50;
    private static final int MAX_EVENT_LOG_LINES = 2;

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
    private final boolean eventsEnabled;
    private int rows, cols, mines;
    private Cell[][] grid;
    private int cellSize = 30;
    private boolean firstClick = true;
    private boolean gameOver = false; // Variable para controlar si el juego ha terminado.
    private int flagsUsed = 0;
    private int remainingSeconds;
    private int elapsedSeconds = 0;
    private Timer gameTimer;
    private Timer eventTimer;
    private Timer eventMessageTimer;
    private Timer pauseResumeTimer;
    private boolean timerPaused = false;
    private String eventTitle = "";
    private String eventMessage = "";
    private Color eventMessageColor = EVENT_NEUTRAL;
    private String gameOverTitle = "";
    private String gameOverMessage = "";
    private Color gameOverColor = EVENT_NEUTRAL;
    private boolean gameSaved = false;
    private final List<String> eventLog = new ArrayList<>();
    private final Random random = new Random();
    private JButton resetButton;
    private JButton menuButton;
    private JButton rankingButton;
    private JLabel infoLabel;
    private int topOffset = 64;
    private int bottomLogHeight = 128;
    private final BufferedImage catImage;
    private final BufferedImage yarnImage;

    public CatHunterBoard(Difficulty difficulty) {
        this(difficulty, "Jugador");
    }

    public CatHunterBoard(Difficulty difficulty, String playerName) {
        this(difficulty, playerName, null);
    }

    public CatHunterBoard(Difficulty difficulty, String playerName, JButton menuButton) {
        this(difficulty, playerName, menuButton, true);
    }

    public CatHunterBoard(Difficulty difficulty, String playerName, JButton menuButton, boolean eventsEnabled) {
        this.menuButton = menuButton;
        this.difficulty = difficulty;
        this.eventsEnabled = eventsEnabled;
        this.playerName = normalizePlayerName(playerName);
        this.catImage = loadImage("gato.png");
        this.yarnImage = loadImage("Ovillo.png");
        // Configuracion segun dificultad.
        this.rows = difficulty.rows;
        this.cols = difficulty.cols;
        this.mines = difficulty.mines;
        this.cellSize = difficulty.cellSize;
        this.remainingSeconds = difficulty.timeLimitMinutes * 60;

        setPreferredSize(new Dimension(cols * cellSize, rows * cellSize + topOffset + bottomLogHeight)); // Tamano del panel segun el numero de celdas.
        setLayout(new BorderLayout());
        add(createTopPanel(), BorderLayout.NORTH);
        initBoard();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int boardX = getBoardXOffset();
                int c = (e.getX() - boardX) / cellSize; // Columna clickeada.
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
                        showEventMessage("La casa despierta", "Los gatos ya han encontrado sus escondites.", EVENT_NEUTRAL);
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

    private int getBoardXOffset() {
        return Math.max(0, (getWidth() - cols * cellSize) / 2);
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
            finishGame("Has perdido", "Te has encontrado un gato.", EVENT_NEGATIVE, false);
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
        timerPaused = false;
        stopPauseResumeTimer();
        elapsedSeconds = 0;
        gameSaved = false;
        gameOverTitle = "";
        gameOverMessage = "";
        eventTitle = "";
        eventMessage = "";
        eventLog.clear();
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
            finishGame("Has ganado", "Encontraste todos los gatos.", EVENT_POSITIVE, true);
        }
    }

    private JPanel createTopPanel() { // Crea el panel superior con informacion del juego y boton de reinicio.
        JPanel top = new JPanel(new BorderLayout(10, 5));
        top.setBackground(Color.DARK_GRAY);
        top.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        top.setPreferredSize(new Dimension(cols * cellSize, topOffset));

        infoLabel = new JLabel();
        infoLabel.setForeground(Color.WHITE);
        infoLabel.setFont(new Font("Arial", Font.BOLD, 14));

        resetButton = new JButton("Reiniciar");
        resetButton.addActionListener(e -> resetGame());

        rankingButton = new JButton("Ranking");
        rankingButton.addActionListener(e -> showRankingDialog());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(resetButton);
        buttonPanel.add(rankingButton);
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
            "<html>" +
                "Jugador: " + playerName + " | Dificultad: " + difficulty.displayName +
                " | Eventos: " + (eventsEnabled ? "Si" : "No") +
                "<br>" +
                "Tiempo: " + formatTime(remainingSeconds) +
                " | Minas: " + mines +
                " | Banderas: " + flagsUsed +
                " | Restantes: " + remaining +
            "</html>"
        );
    }

    private void showRankingDialog() {
        try {
            List<CatHunterRepository.RankingEntry> ranking = CatHunterRepository.getTopRanking(10);

            if (ranking.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Todavia no hay partidas guardadas.", "Ranking CatHunter", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String[] columns = {"#", "Jugador", "Dificultad", "Tiempo", "Resultado", "Fecha"};
            Object[][] data = new Object[ranking.size()][columns.length];

            for (int i = 0; i < ranking.size(); i++) {
                CatHunterRepository.RankingEntry entry = ranking.get(i);
                data[i][0] = i + 1;
                data[i][1] = entry.getPlayerName();
                data[i][2] = entry.getDifficulty();
                data[i][3] = formatTime(entry.getDurationSeconds());
                data[i][4] = entry.isWon() ? "Victoria" : "Derrota";
                data[i][5] = entry.getDate();
            }

            JTable table = new JTable(data, columns);
            table.setEnabled(false);
            table.setRowHeight(24);
            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setPreferredSize(new Dimension(650, 260));

            JOptionPane.showMessageDialog(this, scrollPane, "Ranking CatHunter", JOptionPane.PLAIN_MESSAGE);
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(
                this,
                "No se pudo cargar el ranking desde Azure SQL.",
                "Ranking CatHunter",
                JOptionPane.ERROR_MESSAGE
            );
            System.err.println("No se pudo cargar el ranking de CatHunter: " + exception.getMessage());
        }
    }

    private void startTimer() {
        stopTimer();
        gameTimer = new Timer(1000, e -> {
            if (gameOver) {
                stopTimer();
                return;
            }

            elapsedSeconds++;
            remainingSeconds--;
            updateInfo();

            if (remainingSeconds <= 0) {
                finishGame("Se ha agotado el tiempo", "La casa vuelve a quedar en silencio.", EVENT_NEGATIVE, false);
            }
        });
        gameTimer.start();
    }

    private void startEventTimer() {
        stopEventTimer();
        if (!eventsEnabled) {
            return;
        }

        eventTimer = new Timer(EVENT_INTERVAL_MS, e -> {
            if (gameOver) {
                stopEventTimer();
                return;
            }

            if (firstClick) {
                return;
            }

            if (random.nextInt(100) < EVENT_PROBABILITY_PERCENT) {
                triggerRandomEvent();
            }
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

    private void stopPauseResumeTimer() {
        if (pauseResumeTimer != null) {
            pauseResumeTimer.stop();
            pauseResumeTimer = null;
        }
    }

    private void finishGame(String title, String message, Color color, boolean won) {
        if (gameOver) {
            return;
        }

        gameOver = true;
        stopTimer();
        stopEventTimer();
        timerPaused = false;
        stopPauseResumeTimer();
        gameOverTitle = title;
        gameOverMessage = message;
        gameOverColor = color;
        if (!won) {
            revealAllCats();
        }
        saveFinishedGame(won);
        updateInfo();
        repaint();
    }

    private void revealAllCats() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (grid[r][c].hasMine()) {
                    grid[r][c].setRevealed(true);
                }
            }
        }
    }

    private void saveFinishedGame(boolean won) {
        if (gameSaved) {
            return;
        }

        gameSaved = true;

        try {
            CatHunterRepository.saveGame(
                playerName,
                difficulty.displayName,
                rows,
                cols,
                mines,
                flagsUsed,
                elapsedSeconds,
                won,
                serializeBoard()
            );
        } catch (SQLException exception) {
            showEventMessage("Guardado fallido", "No se pudo guardar la partida en Azure SQL.", EVENT_NEGATIVE);
            System.err.println("No se pudo guardar la partida de CatHunter: " + exception.getMessage());
        }
    }

    private String serializeBoard() {
        StringBuilder builder = new StringBuilder(rows * cols * 4);

        for (int r = 0; r < rows; r++) {
            if (r > 0) {
                builder.append('/');
            }

            for (int c = 0; c < cols; c++) {
                Cell cell = grid[r][c];
                builder.append(cell.hasMine() ? 'M' : 'S');
                builder.append(cell.isRevealed() ? 'R' : 'H');
                builder.append(cell.isFlagged() ? 'F' : 'N');
                builder.append(cell.getAdjacentMines());

                if (c < cols - 1) {
                    builder.append(',');
                }
            }
        }

        return builder.toString();
    }

    private void triggerRandomEvent() {
        switch (random.nextInt(9)) {
            case 0:
                placeHelpfulFlagEvent();
                break;
            case 1:
                addTimeEvent();
                break;
            case 2:
                removeTimeEvent();
                break;
            case 3:
                removeRandomFlagEvent();
                break;
            case 4:
                revealSafeCellEvent();
                break;
            case 5:
                pauseTimerEvent();
                break;
            case 6:
                removeWrongFlagEvent();
                break;
            case 7:
                moveHiddenCatEvent();
                break;
            default:
                neutralStoryEvent();
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
        showEventMessage("Pista", "El gato ha pisado un cascabel. Una bandera aparece sobre una casilla sospechosa.", EVENT_POSITIVE);
    }

    private void addTimeEvent() {
        int seconds = getRandomTimeBonus();
        remainingSeconds += seconds;
        updateInfo();
        showEventMessage("Tiempo ganado", "Has encontrado un ovillo brillante. Ganas " + seconds + " segundos.", EVENT_POSITIVE);
    }

    private void removeTimeEvent() {
        int seconds = getRandomTimeBonus();
        remainingSeconds = Math.max(0, remainingSeconds - seconds);
        updateInfo();

        if (remainingSeconds == 0) {
            finishGame("Te has quedado sin tiempo", "Un gato ha tirado un reloj al suelo.", EVENT_NEGATIVE, false);
            return;
        }

        showEventMessage("Tiempo perdido", "Un gato tira un vaso al suelo. Pierdes " + seconds + " segundos.", EVENT_NEGATIVE);
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
        showEventMessage("Ovillo travieso", "Un ovillo rueda por el tablero y arrastra una de tus banderas.", EVENT_NEGATIVE);
    }

    private void revealSafeCellEvent() {
        CellPosition position = findRandomCell(false, false, false);
        if (position == null) {
            showEventMessage("Un maullido lejano intenta ayudarte, pero ya no quedan casillas seguras ocultas.");
            return;
        }

        reveal(position.row, position.col);
        repaint();
        showEventMessage("Maullido guia", "Un maullido lejano te guia. Una casilla segura se revela sola.", EVENT_POSITIVE);
    }

    private void pauseTimerEvent() {
        if (timerPaused) {
            showEventMessage("El gato bosteza, pero el reloj ya estaba tomando aire.");
            return;
        }

        timerPaused = true;
        stopTimer();
        updateInfo();
        showEventMessage("Pausa felina", "El gato se duerme al sol. El tiempo se pausa durante 10 segundos.", EVENT_POSITIVE);

        stopPauseResumeTimer();
        pauseResumeTimer = new Timer(10000, e -> {
            timerPaused = false;
            if (!gameOver) {
                startTimer();
                updateInfo();
            }
        });
        pauseResumeTimer.setRepeats(false);
        pauseResumeTimer.start();
    }

    private void removeWrongFlagEvent() {
        CellPosition position = findRandomWrongFlag();
        if (position == null) {
            showEventMessage("Un gato curioso revisa tus ovillos, pero no encuentra ninguna bandera mal puesta.");
            return;
        }

        grid[position.row][position.col].setFlagged(false);
        flagsUsed--;
        updateInfo();
        repaint();
        showEventMessage("Ayuda inesperada", "Un gato curioso empuja un ovillo mal colocado. Recuperas una bandera.", EVENT_POSITIVE);
    }

    private void moveHiddenCatEvent() {
        CellPosition origin = findRandomCell(true, false, false);
        CellPosition destination = findRandomCell(false, false, false);

        if (origin == null || destination == null) {
            showEventMessage("Un gato intenta escabullirse, pero no encuentra un escondite nuevo.");
            return;
        }

        grid[origin.row][origin.col].setMine(false);
        grid[destination.row][destination.col].setMine(true);
        calculateNumbers();
        repaint();
        showEventMessage("Gato escurridizo", "Un gato escurridizo cambia de escondite. El tablero ya no suena igual.", EVENT_NEGATIVE);
    }

    private void neutralStoryEvent() {
        String[] messages = {
            "Dos ojos brillan bajo una silla. No ocurre nada... por ahora.",
            "Oyes un ronroneo detras de la pared. La casa sigue observando.",
            "Un cascabel suena en otra habitacion. Respiras hondo y continuas."
        };
        showEventMessage(messages[random.nextInt(messages.length)]);
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

    private CellPosition findRandomWrongFlag() {
        CellPosition[] positions = new CellPosition[rows * cols];
        int count = 0;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = grid[r][c];

                if (cell.isFlagged() && !cell.hasMine()) {
                    positions[count] = new CellPosition(r, c);
                    count++;
                }
            }
        }

        if (count == 0) {
            return null;
        }

        return positions[random.nextInt(count)];
    }

    private void showEventMessage(String message) {
        showEventMessage("Evento gatuno", message, EVENT_NEUTRAL);
    }

    private void showEventMessage(String title, String message, Color color) {
        eventTitle = title;
        eventMessage = message;
        eventMessageColor = color;
        addEventLog(title + ": " + message);

        if (eventMessageTimer != null) {
            eventMessageTimer.stop();
        }

        eventMessageTimer = new Timer(4200, e -> {
            eventTitle = "";
            eventMessage = "";
            repaint();
        });
        eventMessageTimer.setRepeats(false);
        eventMessageTimer.start();
        repaint();
    }

    private void addEventLog(String message) {
        eventLog.add(0, message);

        while (eventLog.size() > MAX_EVENT_LOG_LINES) {
            eventLog.remove(eventLog.size() - 1);
        }
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

        int boardX = getBoardXOffset();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int x = boardX + c * cellSize;
                int y = r * cellSize + topOffset;

                Cell cell = grid[r][c];
                boolean alternate = (r + c) % 2 == 0;

                if (!cell.isRevealed()) { // Si la celda no esta revelada, se dibuja gris.
                    g.setColor(alternate ? HIDDEN_CELL_LIGHT : HIDDEN_CELL_DARK);
                    g.fillRect(x, y, cellSize, cellSize);

                    if (cell.isFlagged()) { // Si tiene bandera, se dibuja una F roja.
                        g.drawImage(yarnImage, x + 4, y + 4, cellSize - 8, cellSize - 8, null);
                    }
                } else {
                    if (cell.hasMine()) {
                        g.setColor(CAT_CELL_BACKGROUND);
                    } else {
                        g.setColor(alternate ? REVEALED_CELL_LIGHT : REVEALED_CELL_DARK);
                    }
                    g.fillRect(x, y, cellSize, cellSize);

                    if (cell.hasMine()) { // Si tiene mina, se dibuja un circulo.
                        g.drawImage(catImage, x + 4, y + 4, cellSize - 8, cellSize - 8, null);
                    } else if (cell.getAdjacentMines() > 0) { // Si no tiene mina, se dibuja el numero.
                        drawAdjacentMineNumber(g, cell.getAdjacentMines(), x, y);
                    }
                }

                g.setColor(GRID_COLOR); // Se dibujan las lineas del tablero.
                g.drawRect(x, y, cellSize, cellSize); // Dibuja el borde de cada celda.
            }
        }

        drawBottomLogArea(g);
        drawEventBanner(g);
        drawEventLog(g);
        drawGameOverOverlay(g);
    }

    private void drawBottomLogArea(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        int y = topOffset + rows * cellSize;

        g2.setColor(new Color(18, 29, 33));
        g2.fillRect(0, y, getWidth(), bottomLogHeight);
        g2.setColor(new Color(31, 42, 46));
        g2.drawLine(0, y, getWidth(), y);
        g2.dispose();
    }

    private void drawEventLog(Graphics g) {
        if (eventLog.isEmpty()) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int panelWidth = Math.min(getWidth() - 36, 680);
        int lineHeight = 15;
        int panelHeight = bottomLogHeight - 16;
        int x = (getWidth() - panelWidth) / 2;
        int y = topOffset + rows * cellSize + 8;

        g2.setColor(new Color(18, 29, 33, 205));
        g2.fillRoundRect(x, y, panelWidth, panelHeight, 14, 14);
        g2.setColor(new Color(242, 203, 110, 120));
        g2.drawRoundRect(x, y, panelWidth, panelHeight, 14, 14);

        g2.setFont(new Font("Arial", Font.BOLD, 12));
        g2.setColor(EVENT_BORDER);
        g2.drawString("ULTIMOS EVENTOS", x + 12, y + 20);

        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        g2.setColor(Color.WHITE);
        int currentY = y + 40;

        for (int i = 0; i < eventLog.size(); i++) {
            currentY = drawLogEntry(g2, eventLog.get(i), x + 12, currentY, panelWidth - 24, lineHeight);
            currentY += 5;

            if (currentY > y + panelHeight - 8) {
                break;
            }
        }

        g2.dispose();
    }

    private int drawLogEntry(Graphics2D g2, String text, int x, int y, int maxWidth, int lineHeight) {
        FontMetrics metrics = g2.getFontMetrics();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        int currentY = y;

        for (String word : words) {
            String candidate = line.length() == 0 ? word : line + " " + word;

            if (metrics.stringWidth(candidate) > maxWidth && line.length() > 0) {
                g2.drawString(line.toString(), x, currentY);
                line = new StringBuilder(word);
                currentY += lineHeight;
            } else {
                line = new StringBuilder(candidate);
            }
        }

        if (line.length() > 0) {
            g2.drawString(line.toString(), x, currentY);
        }

        return currentY + lineHeight;
    }

    private void drawGameOverOverlay(Graphics g) {
        if (!gameOver || gameOverMessage.isEmpty()) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(0, 0, 0, 135));
        g2.fillRect(0, topOffset, getWidth(), getHeight() - topOffset);

        int panelWidth = Math.min(getWidth() - 56, 520);
        int panelHeight = 190;
        int x = (getWidth() - panelWidth) / 2;
        int y = topOffset + Math.max(32, ((rows * cellSize) - panelHeight) / 2);

        g2.setColor(new Color(0, 0, 0, 110));
        g2.fillRoundRect(x + 6, y + 7, panelWidth, panelHeight, 22, 22);

        g2.setColor(gameOverColor);
        g2.fillRoundRect(x, y, panelWidth, panelHeight, 22, 22);
        g2.setColor(EVENT_BORDER);
        g2.drawRoundRect(x, y, panelWidth, panelHeight, 22, 22);

        g2.setFont(new Font("Serif", Font.BOLD, 36));
        g2.setColor(EVENT_BORDER);
        FontMetrics titleMetrics = g2.getFontMetrics();
        int titleX = x + (panelWidth - titleMetrics.stringWidth(gameOverTitle)) / 2;
        g2.drawString(gameOverTitle, titleX, y + 58);

        g2.setFont(new Font("Arial", Font.PLAIN, 18));
        g2.setColor(Color.WHITE);
        FontMetrics messageMetrics = g2.getFontMetrics();
        int messageX = x + (panelWidth - messageMetrics.stringWidth(gameOverMessage)) / 2;
        g2.drawString(gameOverMessage, messageX, y + 98);

        String instruction = "Usa Ranking, Reiniciar o Volver al menu para continuar";
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        FontMetrics instructionMetrics = g2.getFontMetrics();
        int instructionX = x + (panelWidth - instructionMetrics.stringWidth(instruction)) / 2;
        g2.setColor(new Color(235, 240, 236));
        g2.drawString(instruction, instructionX, y + 142);

        g2.dispose();
    }

    private void drawEventBanner(Graphics g) {
        if (eventMessage.isEmpty()) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int margin = Math.max(18, cellSize / 2);
        int bannerWidth = Math.min(getWidth() - margin * 2, 620);
        int bannerHeight = 76;
        int x = (getWidth() - bannerWidth) / 2;
        int y = topOffset + 12;

        g2.setColor(new Color(0, 0, 0, 95));
        g2.fillRoundRect(x + 5, y + 6, bannerWidth, bannerHeight, 18, 18);

        g2.setColor(eventMessageColor);
        g2.fillRoundRect(x, y, bannerWidth, bannerHeight, 18, 18);
        g2.setColor(EVENT_BORDER);
        g2.drawRoundRect(x, y, bannerWidth, bannerHeight, 18, 18);

        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.setColor(EVENT_BORDER);
        g2.drawString(eventTitle.toUpperCase(), x + 18, y + 25);

        g2.setFont(new Font("Arial", Font.PLAIN, 15));
        g2.setColor(Color.WHITE);
        drawWrappedText(g2, eventMessage, x + 18, y + 48, bannerWidth - 36, 18);

        g2.dispose();
    }

    private void drawWrappedText(Graphics2D g2, String text, int x, int y, int maxWidth, int lineHeight) {
        FontMetrics metrics = g2.getFontMetrics();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        int currentY = y;

        for (String word : words) {
            String candidate = line.length() == 0 ? word : line + " " + word;
            if (metrics.stringWidth(candidate) > maxWidth && line.length() > 0) {
                g2.drawString(line.toString(), x, currentY);
                line = new StringBuilder(word);
                currentY += lineHeight;
            } else {
                line = new StringBuilder(candidate);
            }
        }

        if (line.length() > 0) {
            g2.drawString(line.toString(), x, currentY);
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
        stopPauseResumeTimer();
        if (eventMessageTimer != null) {
            eventMessageTimer.stop();
            eventMessageTimer = null;
        }
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
