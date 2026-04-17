package ProyectoVideojuegoBBDD;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Queen extends Chesspiece {
    public Queen(int row, int col, boolean isWhite) {
        super(row, col, isWhite, isWhite ? "queen-white.png" : "queen-black.png");
    }
    /**
     * Movimientos legales básicos de la reina:
     * - Combina torre + alfil: se mueve cualquier número de casillas en vertical, horizontal y diagonal.
     * - No puede saltar piezas.
     * - Puede capturar la primera pieza rival que encuentre en cada dirección.
     *
     */
    @Override
    public List<Point> getLegalMoves(Board board) {
        List<Point> moves = new ArrayList<>();

        // 8 direcciones: (torre + alfil)
        int[][] dirs = {
            {-1, 0},  // arriba
            { 1, 0},  // abajo
            { 0,-1},  // izquierda
            { 0, 1},  // derecha
            {-1,-1},  // diagonal izquierda arriba 
            {-1, 1},  // diagonal derecha arriba
            { 1,-1},  // diagonal izquierda abajo
            { 1, 1}   // diagonal derecha abajo
        };

        for (int[] d : dirs) {
            int dr = d[0];
            int dc = d[1];

            int r = row + dr;
            int c = col + dc;

            while (board.isInsideBoard(r, c)) { // mientras estemos dentro del tablero dejamos movernos en esa direccion
                Chesspiece target = board.getPieceAt(r, c);

                if (target == null) {
                    moves.add(new Point(c, r));
                } else {
                    if (target.isWhite() != this.isWhite) { // pieza rival
                        moves.add(new Point(c, r)); // captura
                    }
                    break; // bloquea el camino en cualquier caso
                }

                r += dr;
                c += dc;
            }
        }

        return moves;
    }
}
