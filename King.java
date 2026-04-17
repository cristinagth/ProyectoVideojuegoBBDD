package ProyectoVideojuegoBBDD;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class King extends Chesspiece {
    public King(int row, int col, boolean isWhite) {
        super(row, col, isWhite, isWhite ? "king-white.png" : "king-black.png");
    }
    
    /**
     * Movimientos legales básicos del rey:
     * - Se mueve 1 casilla en cualquier dirección (8 direcciones).
     * - Puede capturar una pieza rival en la casilla destino.
     * - No incluye enroque ni validación de "no quedar en jaque".
     *
     */
   @Override
   public  List<Point> getLegalMoves(Board board) {
        List<Point> moves = new ArrayList<>();

        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) { // todas las direcciones
                if (dr == 0 && dc == 0) continue; // no quedarse en el mismo lugar

                int r = row + dr;
                int c = col + dc;

                if (!board.isInsideBoard(r, c)) continue;

                Chesspiece target = board.getPieceAt(r, c);
                if (target == null || target.isWhite() != this.isWhite) {
                    moves.add(new Point(c, r));
                }
            }
        }

        return moves;
    }
}
