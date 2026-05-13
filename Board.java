package ProyectoVideojuegoBBDD;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.*;

public class Board extends JPanel {
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

    private String normalizePlayerName(String playerName, String defaultName) {
        if (playerName == null || playerName.trim().isEmpty()) {
            return defaultName;
        }
        return playerName.trim();
    }

    @Override
    protected void paintComponent(Graphics g) { // Dibuja el tablero y las piezas.
        super.paintComponent(g);
        int boardWidth = columns * squareSize;
        int boardHeight = rows * squareSize;
        int xOffset = (getWidth() - boardWidth) / 2;
        int yOffset = getYOffset();
        String letters = "ABCDEFGH";

        // Dibujar el tablero.
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                if ((row + col) % 2 == 0) {
                    g.setColor(Color.WHITE);
                } else {
                    g.setColor(Color.BLACK);
                }
                g.fillRect(xOffset + col * squareSize, yOffset + row * squareSize, squareSize, squareSize);
            }
        }

        if (!highlightedSquares.isEmpty()) { // Resaltar casillas legales.
            Graphics2D g2 = (Graphics2D) g.create();

            for (Point p : highlightedSquares) {
                int hx = xOffset + p.x * squareSize;
                int hy = yOffset + p.y * squareSize;
                Chesspiece target = getPieceAt(p.y, p.x);

                if (target != null && selectedPiece != null && target.isWhite() != selectedPiece.isWhite()) {
                    g2.setColor(new Color(220, 40, 40, 150));
                } else {
                    g2.setColor(new Color(0, 255, 0, 130));
                }

                g2.fillRect(hx, hy, squareSize, squareSize);
            }

            if (selectedPiece != null) {
                g2.setColor(new Color(0, 255, 0, 200));
                int sx = xOffset + selectedPiece.getCol() * squareSize;
                int sy = yOffset + selectedPiece.getRow() * squareSize;
                g2.setStroke(new BasicStroke(3));
                g2.drawRect(sx + 1, sy + 1, squareSize - 2, squareSize - 2);
            }
            g2.dispose();
        }

        g.setFont(new Font("Arial", Font.PLAIN, 16));
        // Letras superiores e inferiores.
        for (int i = 0; i < columns; i++) {
            g.setColor(Color.BLACK);
            g.drawString(String.valueOf(letters.charAt(i)), xOffset + i * squareSize + squareSize / 2 - 5, yOffset - 10);
            g.drawString(String.valueOf(letters.charAt(i)), xOffset + i * squareSize + squareSize / 2 - 5, yOffset + boardHeight + 20);
        }

        // Numeros laterales.
        for (int i = 0; i < rows; i++) {
            g.setColor(Color.BLACK);
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
