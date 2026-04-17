package ProyectoVideojuegoBBDD;

import java.awt.*;
import javax.swing.*;

public class CatHunterIntro extends JPanel {

    public CatHunterIntro(JFrame window) {
        this(window, "Jugador");
    }

    public CatHunterIntro(JFrame window, String playerName) {
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);

        // Mostramos el jugador elegido antes de comenzar la partida.
        JLabel playerLabel = new JLabel("Jugador: " + playerName);
        playerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        playerLabel.setForeground(Color.WHITE);
        playerLabel.setFont(new Font("Arial", Font.BOLD, 18));

        JTextArea text = new JTextArea();
        text.setEditable(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setBackground(Color.BLACK);
        text.setForeground(Color.WHITE);
        text.setFont(new Font("Serif", Font.PLAIN, 18));
        text.setText(
            "No estas solo.\n\n" +
            "Cada casilla que abras...\n" +
            "ellos tambien avanzan.\n\n" +
            "No puedes verlos,\n" +
            "pero ellos si pueden verte.\n\n" +
            "Y cuando el tiempo se agote...\n\n" +
            "ya sera demasiado tarde."
        );

        JButton startButton = new JButton("Comenzar");
        startButton.setFont(new Font("Arial", Font.BOLD, 20));
        startButton.addActionListener(e -> {
            // Al hacer clic en Comenzar, se muestra el panel del juego.
            window.getContentPane().removeAll();
            window.add(new CatHunterBoard(window, CatHunterBoard.Difficulty.EASY, playerName), BorderLayout.CENTER);
            window.revalidate();
            window.repaint();
        });

        JPanel bottom = new JPanel();
        bottom.setBackground(Color.BLACK);
        bottom.add(startButton);

        add(playerLabel, BorderLayout.NORTH);
        add(text, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }
}
