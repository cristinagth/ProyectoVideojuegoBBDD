package ProyectoVideojuegoBBDD;

import java.awt.*;
import java.net.URL;
import java.util.List;
import javax.swing.*;

public abstract class Chesspiece {
    protected int row;
    protected int col;
    protected boolean isWhite;
    protected Image image;

    public Chesspiece(int row, int col, boolean isWhite, String imageName) {
        this.row = row;
        this.col = col;
        this.isWhite = isWhite;

        this.image = new ImageIcon(getImageUrl(imageName)).getImage();// cargar la imagen desde la carpeta figures
    }

    private URL getImageUrl(String imageName) {
        URL imageUrl = getClass().getResource("/figures/" + imageName);

        if (imageUrl == null) {
            imageUrl = getClass().getResource("/ProyectoVideojuegoBBDD/figures/" + imageName);
        }

        if (imageUrl == null) {
            throw new IllegalArgumentException("No se encontro la imagen: " + imageName);
        }

        return imageUrl;
    }

    public void draw(Graphics g, int x, int y, int size) {
        g.drawImage(image, x, y, size, size, null); // dibujar la imagen en las coordenadas (x,y) con el tamaño size x size
    }

    public void setPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() { return row; }
    public int getCol() { return col; }
    public boolean isWhite() { return isWhite; }
    public abstract List<Point> getLegalMoves(Board board);
}
