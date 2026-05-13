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
            """
            No estas solo.
            Cada casilla que abras...
            ellos tambien avanzan.

            No puedes verlos,
            pero ellos si pueden verte.

            Y cuando el tiempo se agote...

            ya sera demasiado tarde."""
        );

        JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        optionsPanel.setBackground(Color.BLACK);

        JLabel difficultyLabel = new JLabel("Dificultad:");
        difficultyLabel.setForeground(Color.WHITE);
        difficultyLabel.setFont(new Font("Arial", Font.BOLD, 16));

        JComboBox<CatHunterBoard.Difficulty> difficultySelector = new JComboBox<>(CatHunterBoard.Difficulty.values());
        difficultySelector.setSelectedItem(CatHunterBoard.Difficulty.EASY);
        difficultySelector.setFont(new Font("Arial", Font.PLAIN, 16));

        JButton startButton = new JButton("Comenzar");
        startButton.setFont(new Font("Arial", Font.BOLD, 20));
        startButton.addActionListener(e -> {
            // Al hacer clic en Comenzar, se muestra el panel del juego.
            CatHunterBoard.Difficulty selectedDifficulty =
                (CatHunterBoard.Difficulty) difficultySelector.getSelectedItem();
            CatHunterBoard board = new CatHunterBoard(selectedDifficulty, playerName, Main.createReturnButton(window));
            Main.showGameWithoutMenuBar(window, board);
        });

        JPanel bottom = new JPanel();
        bottom.setBackground(Color.BLACK);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        optionsPanel.add(difficultyLabel);
        optionsPanel.add(difficultySelector);
        bottom.add(optionsPanel);
        bottom.add(startButton);

        add(playerLabel, BorderLayout.NORTH);
        add(text, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }
}
