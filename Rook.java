package ProyectoVideojuegoBBDD;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Rook extends Chesspiece {
    public Rook(int row, int col, boolean isWhite) {
        super(row, col, isWhite, isWhite ? "rook-white.png" : "rook-black.png");
    }

    /**
     * Movimientos legales básicos de la torre:
     * - Se mueve cualquier número de casillas en vertical u horizontal.
     * - No puede saltar piezas.
     * - Puede capturar la primera pieza rival que encuentre en una dirección.
     *
     */
    @Override
    public List<Point> getLegalMoves(Board board) {
        List<Point> moves = new ArrayList<>();

        // Direcciones: arriba, abajo, izquierda, derecha
        int[][] dirs = {
            {-1, 0}, // arriba
            { 1, 0}, // abajo
            { 0,-1}, // izquierda
            { 0, 1} // derecha
        };

        for (int[] d : dirs) {
            int dr = d[0];
            int dc = d[1];

            int r = row + dr;
            int c = col + dc;

            while (board.isInsideBoard(r, c)) {
                Chesspiece target = board.getPieceAt(r, c);

                if (target == null) {
                    // Casilla libre: se puede seguir avanzando
                    moves.add(new Point(c, r));
                } else {
                    // Casilla ocupada: si es rival, se puede capturar; en ambos casos se detiene
                    if (target.isWhite() != this.isWhite) {
                        moves.add(new Point(c, r));
                    }
                    break;
                }

                r += dr;
                c += dc;
            }
        }

        return moves;
    }
}
