package ProyectoVideojuegoBBDD;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Pawn extends Chesspiece {
    public Pawn(int row, int col, boolean isWhite) {
        super(row, col, isWhite, isWhite ? "pawn-white.png" : "pawn-black.png");
    }
    /**
     * Movimientos legales basicos del peon:
     * - Avanza 1 casilla hacia delante si esta libre.
     * - Desde la posicion inicial, puede avanzar 2 si ambas casillas estan libres.
     * - Captura en diagonal 1 casilla si hay pieza rival.
     *
     */
    @Override
    public List<Point> getLegalMoves(Board board) {
        List<Point> moves = new ArrayList<>(); 

        // Tenemos en cuenta que row=0 es arriba, row crece hacia abajo.
        // Blancas empiezan en row=6 y suben (direccion -1)
        // Negras empiezan en row=1 y bajan (direccion +1)
        int direction = isWhite ? -1 : 1; // direccion de movimiento si es blanca avanza hacia arriba (-1), si es negra hacia abajo (+1)
        int startRow = isWhite ? 6 : 1; // fila inicial si es blanca es la 6, si es negra es la 1

        // 1) Avance de 1
        int oneStepRow = row + direction;
        if (board.isInsideBoard(oneStepRow, col) && board.isEmpty(oneStepRow, col)) {
            moves.add(new Point(col, oneStepRow));

            // 2) Avance de 2 desde la fila inicial (solo si el avance de 1 es posible)
            int twoStepRow = row + (2 * direction);
            if (row == startRow && board.isInsideBoard(twoStepRow, col) && board.isEmpty(twoStepRow, col)) {
                moves.add(new Point(col, twoStepRow));
            }
        }

        // 3) Capturas diagonales
        int captureRow = row + direction;
        int[] dCols = {-1, 1}; 
        for (int dc : dCols) {
            int captureCol = col + dc;
            if (!board.isInsideBoard(captureRow, captureCol)) {
                continue;
            }
            Chesspiece target = board.getPieceAt(captureRow, captureCol);
            if (target != null && target.isWhite() != this.isWhite) {
                moves.add(new Point(captureCol, captureRow));
            }
        }

        return moves;
    }
}
