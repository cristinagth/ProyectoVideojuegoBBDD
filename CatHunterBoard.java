package ProyectoVideojuegoBBDD;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;
import javax.swing.*;

public class CatHunterBoard extends JPanel {

    public enum Difficulty {
        EASY(8, 8, 10),
        MEDIUM(16, 16, 40),
        HARD(16, 30, 99);

        public final int rows;
        public final int cols;
        public final int mines;

        Difficulty(int r, int c, int m) {
            rows = r;
            cols = c;
            mines = m;
        }
    }

    private final String playerName;
    private int rows, cols, mines;
    private Cell[][] grid;
    private int cellSize = 30;
    private boolean firstClick = true;
    private boolean gameOver = false;
    private int flagsUsed = 0;
    private JButton resetButton;
    private JLabel infoLabel;
    private int topOffset = 40;

    public CatHunterBoard(Difficulty difficulty) {
        this(difficulty, "Jugador");
    }

    public CatHunterBoard(Difficulty difficulty, String playerName) {
        this.playerName = normalizePlayerName(playerName);
        this.rows = difficulty.rows;
        this.cols = difficulty.cols;
        this.mines = difficulty.mines;

        setPreferredSize(new Dimension(cols * cellSize, rows * cellSize));
        setLayout(new BorderLayout());
        add(createTopPanel(), BorderLayout.NORTH);
        initBoard();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int c = e.getX() / cellSize;
                int r = (e.getY() - topOffset) / cellSize;

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

    private void initBoard() {
        grid = new Cell[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = new Cell();
            }
        }
    }

    private boolean isInside(int r, int c) {
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }

    private void placeMines(int safeRow, int safeCol) {
        Random rand = new Random();
        int placed = 0;

        while (placed < mines) {
            int r = rand.nextInt(rows);
            int c = rand.nextInt(cols);

            if (Math.abs(r - safeRow) <= 1 && Math.abs(c - safeCol) <= 1) {
                continue;
            }

            if (!grid[r][c].hasMine()) {
                grid[r][c].setMine(true);
                placed++;
            }
        }
    }

    private void calculateNumbers() {
        int[] dr = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dc = {-1, 0, 1, -1, 1, -1, 0, 1};

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (grid[r][c].hasMine()) {
                    continue;
                }

                int count = 0;
                for (int i = 0; i < 8; i++) {
                    int nr = r + dr[i];
                    int nc = c + dc[i];

                    if (isInside(nr, nc) && grid[nr][nc].hasMine()) {
                        count++;
                    }
                }
                grid[r][c].setAdjacentMines(count);
            }
        }
    }

    private void reveal(int r, int c) {
        if (!isInside(r, c) || grid[r][c].isRevealed() || grid[r][c].isFlagged()) {
            return;
        }

        grid[r][c].setRevealed(true);

        if (grid[r][c].hasMine()) {
            gameOver = true;
            JOptionPane.showMessageDialog(this, "Has perdido. Te has encontrado un gato.");
            return;
        }

        if (grid[r][c].getAdjacentMines() == 0) {
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    reveal(r + dr, c + dc);
                }
            }
        }

        checkWin();
    }

    private void toggleFlag(int r, int c) {
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
        firstClick = true;
        gameOver = false;
        flagsUsed = 0;

        initBoard();
        updateInfo();
        repaint();
    }

    private void checkWin() {
        int unrevealed = 0;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (!grid[r][c].isRevealed()) {
                    unrevealed++;
                }
            }
        }

        if (unrevealed == mines) {
            gameOver = true;
            JOptionPane.showMessageDialog(this, "Has ganado. Encontraste todos los gatos.");
        }
    }

    private JPanel createTopPanel() {
        JPanel top = new JPanel();
        top.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 5));
        top.setBackground(Color.DARK_GRAY);

        infoLabel = new JLabel();
        infoLabel.setForeground(Color.WHITE);
        infoLabel.setFont(new Font("Arial", Font.BOLD, 16));

        resetButton = new JButton("Reiniciar");
        resetButton.addActionListener(e -> resetGame());

        top.add(infoLabel);
        top.add(resetButton);

        updateInfo();

        return top;
    }

    private void updateInfo() {
        int remaining = mines - flagsUsed;
        infoLabel.setText("Jugador: " + playerName + " | Minas: " + mines + " | Banderas: " + flagsUsed + " | Restantes: " + remaining);
    }

    private String normalizePlayerName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "Jugador";
        }

        return name.trim();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int x = c * cellSize;
                int y = r * cellSize + topOffset;

                Cell cell = grid[r][c];

                if (!cell.isRevealed()) {
                    g.setColor(Color.GRAY);
                    g.fillRect(x, y, cellSize, cellSize);

                    if (cell.isFlagged()) {
                        g.setColor(Color.RED);
                        g.drawString("F", x + 10, y + 20);
                    }
                } else {
                    g.setColor(Color.LIGHT_GRAY);
                    g.fillRect(x, y, cellSize, cellSize);

                    if (cell.hasMine()) {
                        g.setColor(Color.BLACK);
                        g.fillOval(x + 5, y + 5, 20, 20);
                    } else if (cell.getAdjacentMines() > 0) {
                        g.setColor(Color.BLUE);
                        g.drawString(String.valueOf(cell.getAdjacentMines()), x + 10, y + 20);
                    }
                }

                g.setColor(Color.DARK_GRAY);
                g.drawRect(x, y, cellSize, cellSize);
            }
        }
    }
}
