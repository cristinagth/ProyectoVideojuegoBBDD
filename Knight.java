package ProyectoVideojuegoBBDD;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Knight extends Chesspiece {
    public Knight(int row, int col, boolean isWhite) {
        super(row, col, isWhite, isWhite ? "knight-white.png" : "knight-black.png");
    }

    /**
     * Movimientos legales básicos del caballo:
     * - Se mueve en "L" (2+1) y puede saltar piezas.
     * - Puede capturar una pieza rival en la casilla destino.
     *
     */
    @Override
    public List<Point> getLegalMoves(Board board) {
        List<Point> moves = new ArrayList<>();

        int[][] jumps = {
                {-2, -1}, {-2,  1}, // arriba 2 e izquierda/derecha 1
                {-1, -2}, {-1,  2}, // arriba 1 e izquierda/derecha 2
                { 1, -2}, { 1,  2}, // abajo 1 e izquierda/derecha 2
                { 2, -1}, { 2,  1}  // abajo 2 e izquierda/derecha 1
        };

        for (int[] j : jumps) {
            int r = row + j[0];
            int c = col + j[1];

            if (!board.isInsideBoard(r, c)) continue;

            Chesspiece target = board.getPieceAt(r, c);
            if (target == null || target.isWhite() != this.isWhite) {
                moves.add(new Point(c, r));
            }
        }

        return moves;
    }
}
