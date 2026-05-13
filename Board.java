package ProyectoVideojuegoBBDD;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.*;

public class Board extends JPanel {
    private static final Color BOARD_BACKGROUND = new Color(30, 34, 38);
    private static final Color LIGHT_SQUARE = new Color(221, 207, 180);
    private static final Color DARK_SQUARE = new Color(101, 128, 91);
    private static final Color BOARD_FRAME = new Color(55, 43, 35);
    private static final Color COORDINATE_COLOR = new Color(232, 222, 198);
    private static final Color MOVE_HIGHLIGHT = new Color(246, 211, 91, 115);
    private static final Color CAPTURE_HIGHLIGHT = new Color(205, 55, 55, 150);
    private static final Color SELECTED_BORDER = new Color(242, 203, 110);

    int columns = 8;
    int rows = 8;    // Tamano del tablero en filas.
    int squareSize = 80;
    private List<Chesspiece> pieces;
    private Chesspiece selectedPiece;
    private List<Point> highlightedSquares = new ArrayList<>();
    // Agregamos el control de turnos con el primer movimiento.
    private boolean whiteShift = true; // Se determina que empiezan las blancas.
    private boolean firstMove = true; // Determinamos el primer movimiento.
    private boolean gameOver = false;
    private final String whitePlayerName;
    private final String blackPlayerName;
    private String statusMessage = "";
    private Color statusColor = new Color(30, 80, 130, 215);
    private Timer statusTimer;
    private boolean gameSaved = false;

    public Board() {
        this("Jugador blancas", "Jugador negras");
    }

    public Board(String whitePlayerName, String blackPlayerName) {
        this.whitePlayerName = normalizePlayerName(whitePlayerName, "Jugador blancas");
        this.blackPlayerName = normalizePlayerName(blackPlayerName, "Jugador negras");
        pieces = new ArrayList<>();
        initializePieces(); // Movemos las piezas a un metodo para reiniciar el juego y no duplicar codigo.

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }
        });
    }

    // Metodo para reiniciar el juego o inicializar las piezas en su posicion inicial.
    private void initializePieces() {
        // Piezas blancas.
        pieces.add(new Rook(7, 0, true));
        pieces.add(new Knight(7, 1, true));
        pieces.add(new Bishop(7, 2, true));
        pieces.add(new Queen(7, 3, true));
        pieces.add(new King(7, 4, true));
        pieces.add(new Bishop(7, 5, true));
        pieces.add(new Knight(7, 6, true));
        pieces.add(new Rook(7, 7, true));
        for (int i = 0; i < columns; i++) {
            pieces.add(new Pawn(6, i, true));
        }

        // Piezas negras.
        pieces.add(new Rook(0, 0, false));
        pieces.add(new Knight(0, 1, false));
        pieces.add(new Bishop(0, 2, false));
        pieces.add(new Queen(0, 3, false));
        pieces.add(new King(0, 4, false));
        pieces.add(new Bishop(0, 5, false));
        pieces.add(new Knight(0, 6, false));
        pieces.add(new Rook(0, 7, false));
        for (int i = 0; i < columns; i++) {
            pieces.add(new Pawn(1, i, false));
        }
    }

    // Para crear el mate, verificamos si la casilla del rey esta siendo atacada por una pieza rival.
    public boolean isSquareAttacked(int row, int col, boolean whiteAttacker) {
        for (Chesspiece p : pieces) {
            if (p.isWhite() == whiteAttacker) {
                if (p instanceof King && Math.abs(p.getRow() - row) <= 1 && Math.abs(p.getCol() - col) <= 1) {
                    return true;
                }

                if (p instanceof Pawn) {
                    int direction = p.isWhite() ? -1 : 1;
                    int attackRow = p.getRow() + direction;

                    if (attackRow == row && (p.getCol() - 1 == col || p.getCol() + 1 == col)) {
                        return true;
                    }

                    continue;
                }

                List<Point> moves = p.getLegalMoves(this);
                for (Point move : moves) {
                    if (move.y == row && move.x == col) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // Metodo para encontrar al rey de un color determinado.
    public Chesspiece findKing(boolean white) {
        for (Chesspiece p : pieces) {
            if (p instanceof King && p.isWhite() == white) {
                return p;
            }
        }
        return null;
    }

    private List<Point> getLegalMovesForTurn(Chesspiece piece) {
        List<Point> legalMoves = new ArrayList<>();

        for (Point move : piece.getLegalMoves(this)) {
            Chesspiece target = getPieceAt(move.y, move.x);

            if (target instanceof King) {
                continue;
            }

            if (!wouldLeaveKingInCheck(piece, move.y, move.x)) {
                legalMoves.add(move);
            }
        }

        return legalMoves;
    }

    private boolean wouldLeaveKingInCheck(Chesspiece piece, int targetRow, int targetCol) {
        int originalRow = piece.getRow();
        int originalCol = piece.getCol();
        Chesspiece capturedPiece = getPieceAt(targetRow, targetCol);

        if (capturedPiece != null) {
            pieces.remove(capturedPiece);
        }

        piece.setPosition(targetRow, targetCol);
        Chesspiece king = piece instanceof King ? piece : findKing(piece.isWhite());
        boolean inCheck = king == null || isSquareAttacked(king.getRow(), king.getCol(), !piece.isWhite());

        piece.setPosition(originalRow, originalCol);
        if (capturedPiece != null) {
            pieces.add(capturedPiece);
        }

        return inCheck;
    }

    // Verifica si el jugador del color actual esta en jaque mate.
    public boolean isCheckmate(boolean isWhiteTurn) {
        Chesspiece king = findKing(isWhiteTurn);
        if (king == null) {
            return false;
        }

        if (!isSquareAttacked(king.getRow(), king.getCol(), !isWhiteTurn)) {
            return false;
        }

        // Comprobar el resto de fichas, por si se puede salvar al rey.
        List<Chesspiece> currentPlayerPieces = new ArrayList<>(pieces);
        for (Chesspiece p : currentPlayerPieces) {
            if (p.isWhite() == isWhiteTurn && !getLegalMovesForTurn(p).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    // Devuelve la pieza situada en (row, col) o null si la casilla esta vacia.
    public Chesspiece getPieceAt(int row, int col) {
        for (Chesspiece p : pieces) {
            if (p.getRow() == row && p.getCol() == col) {
                return p;
            }
        }
        return null;
    }

    public boolean isInsideBoard(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < columns;
    }

    public boolean isEmpty(int row, int col) {
        return getPieceAt(row, col) == null;
    }

    public List<Chesspiece> getPieces() {
        return Collections.unmodifiableList(pieces);
    }

    private int getBoardWidthPx() {
        return columns * squareSize;
    }

    private int getBoardHeightPx() {
        return rows * squareSize;
    }

    private int getXOffset() {
        return (getWidth() - getBoardWidthPx()) / 2;
    }

    private int getYOffset() {
        return (getHeight() - getBoardHeightPx()) / 2;
    }

    private void clearSelection() {
        selectedPiece = null;
        highlightedSquares.clear();
    }

    // Metodo para reiniciar el juego.
    public void resetGame() {
        pieces.clear();
        initializePieces();
        whiteShift = true;
        firstMove = true;
        gameOver = false;
        gameSaved = false;
        selectedPiece = null;
        highlightedSquares.clear();
        showStatus("Partida reiniciada. Empiezan las blancas: " + whitePlayerName, new Color(30, 90, 120, 215));

        repaint();
    }

    // Manejo de clicks del raton.
    private void handleClick(int mouseX, int mouseY) {
        if (gameOver) {
            return;
        }

        int xOffset = getXOffset();
        int yOffset = getYOffset();

        // Si click fuera del tablero, limpiar seleccion.
        if (mouseX < xOffset || mouseY < yOffset
                || mouseX >= xOffset + getBoardWidthPx()
                || mouseY >= yOffset + getBoardHeightPx()) {
            clearSelection();
            repaint();
            return;
        }
        int col = (mouseX - xOffset) / squareSize;
        int row = (mouseY - yOffset) / squareSize;

        Chesspiece clicked = getPieceAt(row, col);
        // Si tenemos una pieza seleccionada, intentamos moverla.
        if (selectedPiece != null && isHighlightedSquare(row, col)) {
            Chesspiece target = getPieceAt(row, col);

            // Gestion de capturas.
            if (target != null && target.isWhite() != selectedPiece.isWhite()) {
                pieces.remove(target);
            }

            selectedPiece.setPosition(row, col);
            promotePawnIfNeeded(selectedPiece);
            firstMove = false;
            whiteShift = !whiteShift; // Cambio de turno.

            // Estado de mate o jaque.
            if (isCheckmate(whiteShift)) {
                repaint();
                String winner = (!whiteShift) ? whitePlayerName : blackPlayerName;
                String result = "Jaque mate. Gana " + winner;
                saveFinishedGame(result);
                int response = JOptionPane.showConfirmDialog(this,
                    "JAQUE MATE. Gana " + winner + ". Deseas reiniciar?",
                    "Fin de la partida", JOptionPane.YES_NO_OPTION);

                if (response == JOptionPane.YES_OPTION) {
                    resetGame();
                } else {
                    gameOver = true;
                    clearSelection();
                    repaint();
                }
            } else {
                Chesspiece currentKing = findKing(whiteShift);
                if (currentKing != null && isSquareAttacked(currentKing.getRow(), currentKing.getCol(), !whiteShift)) {
                    String playerInCheck = whiteShift ? whitePlayerName : blackPlayerName;
                    showStatus("Jaque. El rey de " + playerInCheck + " esta en peligro.", new Color(145, 65, 65, 220));
                }
            }

            clearSelection();
            repaint();
            return;
        }

        // Seleccionamos piezas y verificamos que sea el turno correcto.
        if (clicked != null && clicked.isWhite() == whiteShift) {
            selectedPiece = clicked;
            highlightedSquares = getLegalMovesForTurn(clicked);
            if (highlightedSquares.isEmpty()) {
                showStatus("Esa pieza no tiene movimientos legales.", new Color(80, 80, 80, 210));
            }
            repaint();
            return;
        } else {
            if (firstMove && whiteShift && clicked != null && !clicked.isWhite()) {
                showStatus("Empiezan las blancas: " + whitePlayerName, new Color(30, 90, 120, 215));
            }
            clearSelection();
            repaint();
        }

        if (clicked != null && clicked.isWhite() != whiteShift) {
            clearSelection();
            repaint();
            return;
        }

        repaint();
    }

    // Verifica si la casilla (row, col) esta en las casillas resaltadas.
    private boolean isHighlightedSquare(int row, int col) {
        for (Point p : highlightedSquares) {
            if (p.x == col && p.y == row) {
                return true;
            }
        }
        return false;
    }

    private void promotePawnIfNeeded(Chesspiece piece) {
        if (!(piece instanceof Pawn)) {
            return;
        }

        if ((piece.isWhite() && piece.getRow() == 0) || (!piece.isWhite() && piece.getRow() == rows - 1)) {
            pieces.remove(piece);
            pieces.add(new Queen(piece.getRow(), piece.getCol(), piece.isWhite()));
            String player = piece.isWhite() ? whitePlayerName : blackPlayerName;
            showStatus("Promocion. El peon de " + player + " se convierte en reina.", new Color(95, 85, 145, 220));
        }
    }

    private void showStatus(String message, Color color) {
        statusMessage = message;
        statusColor = color;

        if (statusTimer != null) {
            statusTimer.stop();
        }

        statusTimer = new Timer(3500, e -> {
            statusMessage = "";
            repaint();
        });
        statusTimer.setRepeats(false);
        statusTimer.start();
        repaint();
    }

    private void saveFinishedGame(String result) {
        if (gameSaved) {
            return;
        }

        gameSaved = true;

        try {
            ChessRepository.saveGame(
                whitePlayerName,
                blackPlayerName,
                whiteShift ? "Blancas" : "Negras",
                serializeBoard(),
                result
            );
        } catch (SQLException exception) {
            showStatus("No se pudo guardar la partida de ajedrez en Azure SQL.", new Color(145, 65, 65, 220));
            System.err.println("No se pudo guardar la partida de ajedrez: " + exception.getMessage());
        }
    }

    private String serializeBoard() {
        StringBuilder builder = new StringBuilder();

        for (Chesspiece piece : pieces) {
            if (builder.length() > 0) {
                builder.append(';');
            }

            builder.append(piece.getClass().getSimpleName());
            builder.append(',');
            builder.append(piece.isWhite() ? "white" : "black");
            builder.append(',');
            builder.append(piece.getRow());
            builder.append(',');
            builder.append(piece.getCol());
        }

        return builder.toString();
    }

    public void showHistoryDialog() {
        try {
            List<ChessRepository.HistoryEntry> history = ChessRepository.getRecentGames(10);

            if (history.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Todavia no hay partidas de ajedrez guardadas.", "Historial de ajedrez", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String[] columns = {"#", "Blancas", "Negras", "Resultado", "Turno final", "Fecha"};
            Object[][] data = new Object[history.size()][columns.length];

            for (int i = 0; i < history.size(); i++) {
                ChessRepository.HistoryEntry entry = history.get(i);
                data[i][0] = i + 1;
                data[i][1] = entry.getWhitePlayerName();
                data[i][2] = entry.getBlackPlayerName();
                data[i][3] = entry.getResult();
                data[i][4] = entry.getCurrentTurn();
                data[i][5] = entry.getDate();
            }

            JTable table = new JTable(data, columns);
            table.setEnabled(false);
            table.setRowHeight(24);
            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setPreferredSize(new Dimension(720, 260));

            JOptionPane.showMessageDialog(this, scrollPane, "Historial de ajedrez", JOptionPane.PLAIN_MESSAGE);
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(
                this,
                "No se pudo cargar el historial desde Azure SQL.",
                "Historial de ajedrez",
                JOptionPane.ERROR_MESSAGE
            );
            System.err.println("No se pudo cargar el historial de ajedrez: " + exception.getMessage());
        }
    }

    private String normalizePlayerName(String playerName, String defaultName) {
        if (playerName == null || playerName.trim().isEmpty()) {
            return defaultName;
        }
        return playerName.trim();
    }

    @Override
    protected void paintComponent(Graphics g) { // Dibuja el tablero y las piezas.
        super.paintComponent(g);
        Graphics2D base = (Graphics2D) g;
        base.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int boardWidth = columns * squareSize;
        int boardHeight = rows * squareSize;
        int xOffset = (getWidth() - boardWidth) / 2;
        int yOffset = getYOffset();
        String letters = "ABCDEFGH";

        base.setColor(BOARD_BACKGROUND);
        base.fillRect(0, 0, getWidth(), getHeight());

        base.setColor(new Color(0, 0, 0, 80));
        base.fillRoundRect(xOffset - 34, yOffset - 34, boardWidth + 68, boardHeight + 68, 22, 22);
        base.setColor(BOARD_FRAME);
        base.fillRoundRect(xOffset - 28, yOffset - 28, boardWidth + 56, boardHeight + 56, 18, 18);

        // Dibujar el tablero.
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                g.setColor((row + col) % 2 == 0 ? LIGHT_SQUARE : DARK_SQUARE);
                g.fillRect(xOffset + col * squareSize, yOffset + row * squareSize, squareSize, squareSize);
            }
        }

        base.setColor(new Color(0, 0, 0, 130));
        base.setStroke(new BasicStroke(3));
        base.drawRect(xOffset, yOffset, boardWidth, boardHeight);

        if (!highlightedSquares.isEmpty()) { // Resaltar casillas legales.
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            for (Point p : highlightedSquares) {
                int hx = xOffset + p.x * squareSize;
                int hy = yOffset + p.y * squareSize;
                Chesspiece target = getPieceAt(p.y, p.x);

                if (target != null && selectedPiece != null && target.isWhite() != selectedPiece.isWhite()) {
                    g2.setColor(CAPTURE_HIGHLIGHT);
                } else {
                    g2.setColor(MOVE_HIGHLIGHT);
                }

                g2.fillRect(hx, hy, squareSize, squareSize);
            }

            if (selectedPiece != null) {
                g2.setColor(SELECTED_BORDER);
                int sx = xOffset + selectedPiece.getCol() * squareSize;
                int sy = yOffset + selectedPiece.getRow() * squareSize;
                g2.setStroke(new BasicStroke(4));
                g2.drawRoundRect(sx + 4, sy + 4, squareSize - 8, squareSize - 8, 10, 10);
            }
            g2.dispose();
        }

        g.setFont(new Font("Arial", Font.BOLD, 16));
        // Letras superiores e inferiores.
        for (int i = 0; i < columns; i++) {
            g.setColor(COORDINATE_COLOR);
            g.drawString(String.valueOf(letters.charAt(i)), xOffset + i * squareSize + squareSize / 2 - 5, yOffset - 10);
            g.drawString(String.valueOf(letters.charAt(i)), xOffset + i * squareSize + squareSize / 2 - 5, yOffset + boardHeight + 20);
        }

        // Numeros laterales.
        for (int i = 0; i < rows; i++) {
            g.setColor(COORDINATE_COLOR);
            g.drawString(String.valueOf(8 - i), xOffset - 20, yOffset + i * squareSize + squareSize / 2 + 5);
            g.drawString(String.valueOf(8 - i), xOffset + boardWidth + 10, yOffset + i * squareSize + squareSize / 2 + 5);
        }

        // Dibujar las piezas.
        for (Chesspiece piece : pieces) {
            int px = xOffset + piece.getCol() * squareSize;
            int py = yOffset + piece.getRow() * squareSize;
            piece.draw(g, px, py, squareSize);
        }

        drawStatusMessage(g, xOffset, yOffset, boardWidth);
    }

    private void drawStatusMessage(Graphics g, int xOffset, int yOffset, int boardWidth) {
        if (statusMessage.isEmpty()) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int bannerWidth = Math.min(boardWidth - 40, 560);
        int bannerHeight = 46;
        int x = xOffset + (boardWidth - bannerWidth) / 2;
        int y = Math.max(12, yOffset - 64);

        g2.setColor(new Color(0, 0, 0, 90));
        g2.fillRoundRect(x + 4, y + 5, bannerWidth, bannerHeight, 16, 16);
        g2.setColor(statusColor);
        g2.fillRoundRect(x, y, bannerWidth, bannerHeight, 16, 16);
        g2.setColor(new Color(240, 225, 170));
        g2.drawRoundRect(x, y, bannerWidth, bannerHeight, 16, 16);

        g2.setFont(new Font("Arial", Font.BOLD, 15));
        g2.setColor(Color.WHITE);
        FontMetrics metrics = g2.getFontMetrics();
        int textX = x + (bannerWidth - metrics.stringWidth(statusMessage)) / 2;
        int textY = y + ((bannerHeight - metrics.getHeight()) / 2) + metrics.getAscent();
        g2.drawString(statusMessage, textX, textY);

        g2.dispose();
    }
}
