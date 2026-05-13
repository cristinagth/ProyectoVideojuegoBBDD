package ProyectoVideojuegoBBDD;

import java.awt.*;
import javax.swing.*;

public class CatHunterIntro extends JPanel {

    private static final Color BACKGROUND_TOP = new Color(15, 23, 27);
    private static final Color BACKGROUND_BOTTOM = new Color(34, 47, 51);
    private static final Color PANEL_BACKGROUND = new Color(18, 29, 33, 220);
    private static final Color TEXT_COLOR = new Color(226, 232, 228);
    private static final Color MUTED_TEXT_COLOR = new Color(174, 188, 184);
    private static final Color ACCENT_COLOR = new Color(242, 203, 110);

    public CatHunterIntro(JFrame window) {
        this(window, "Jugador");
    }

    public CatHunterIntro(JFrame window, String playerName) {
        setLayout(new GridBagLayout());
        setBackground(BACKGROUND_TOP);

        JPanel content = createContentPanel(window, playerName);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.insets = new Insets(24, 24, 24, 24);
        add(content, constraints);
    }

    private JPanel createContentPanel(JFrame window, String playerName) {
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(85, 104, 101), 1),
            BorderFactory.createEmptyBorder(28, 34, 30, 34)
        ));
        content.setPreferredSize(new Dimension(560, 520));

        JLabel title = new JLabel("CATHUNTER");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setForeground(ACCENT_COLOR);
        title.setFont(new Font("Serif", Font.BOLD, 42));

        JLabel subtitle = new JLabel("La casa esta a oscuras. Algo se mueve bajo las baldosas.");
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setForeground(MUTED_TEXT_COLOR);
        subtitle.setFont(new Font("Arial", Font.PLAIN, 15));

        JLabel playerLabel = new JLabel("Jugador: " + playerName);
        playerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        playerLabel.setForeground(TEXT_COLOR);
        playerLabel.setFont(new Font("Arial", Font.BOLD, 17));

        JTextArea text = new JTextArea();
        text.setEditable(false);
        text.setFocusable(false);
        text.setOpaque(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setForeground(TEXT_COLOR);
        text.setFont(new Font("Serif", Font.PLAIN, 19));
        text.setText(
            """
            No estas solo.

            Cada casilla que abras puede despertar un maullido, un cascabel o un par de ojos escondidos entre las sombras.

            Marca con ovillos los lugares sospechosos, conserva la calma y escucha las pistas. A veces la suerte ronronea. A veces araña.

            Cuando el tiempo se agote, la casa volvera a quedar en silencio."""
        );
        text.setMaximumSize(new Dimension(490, 260));

        JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        optionsPanel.setOpaque(false);
        optionsPanel.setMaximumSize(new Dimension(490, 42));

        JLabel difficultyLabel = new JLabel("Dificultad");
        difficultyLabel.setForeground(TEXT_COLOR);
        difficultyLabel.setFont(new Font("Arial", Font.BOLD, 16));

        JComboBox<CatHunterBoard.Difficulty> difficultySelector = new JComboBox<>(CatHunterBoard.Difficulty.values());
        difficultySelector.setSelectedItem(CatHunterBoard.Difficulty.EASY);
        difficultySelector.setFont(new Font("Arial", Font.PLAIN, 16));
        difficultySelector.setFocusable(false);

        JCheckBox eventsCheckBox = new JCheckBox("Eventos gatunos");
        eventsCheckBox.setSelected(true);
        eventsCheckBox.setOpaque(false);
        eventsCheckBox.setForeground(TEXT_COLOR);
        eventsCheckBox.setFont(new Font("Arial", Font.BOLD, 16));
        eventsCheckBox.setFocusPainted(false);

        JButton startButton = new JButton("Entrar en la casa");
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startButton.setFont(new Font("Arial", Font.BOLD, 20));
        startButton.setFocusPainted(false);
        startButton.setBackground(new Color(94, 120, 98));
        startButton.setForeground(Color.WHITE);
        startButton.setMaximumSize(new Dimension(240, 46));
        startButton.addActionListener(e -> {
            CatHunterBoard.Difficulty selectedDifficulty =
                (CatHunterBoard.Difficulty) difficultySelector.getSelectedItem();
            CatHunterBoard board = new CatHunterBoard(
                selectedDifficulty,
                playerName,
                Main.createReturnButton(window),
                eventsCheckBox.isSelected()
            );
            Main.showGameWithoutMenuBar(window, board);
        });

        optionsPanel.add(difficultyLabel);
        optionsPanel.add(difficultySelector);
        optionsPanel.add(eventsCheckBox);

        content.add(title);
        content.add(Box.createVerticalStrut(8));
        content.add(subtitle);
        content.add(Box.createVerticalStrut(22));
        content.add(playerLabel);
        content.add(Box.createVerticalStrut(24));
        content.add(text);
        content.add(Box.createVerticalStrut(22));
        content.add(optionsPanel);
        content.add(Box.createVerticalStrut(18));
        content.add(startButton);
        return content;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint background = new GradientPaint(
            0,
            0,
            BACKGROUND_TOP,
            0,
            getHeight(),
            BACKGROUND_BOTTOM
        );
        g2.setPaint(background);
        g2.fillRect(0, 0, getWidth(), getHeight());

        paintTiles(g2);
        paintCatEyes(g2);
        paintContentGlow(g2);

        g2.dispose();
    }

    private void paintTiles(Graphics2D g2) {
        int tileSize = 74;
        for (int y = 0; y < getHeight(); y += tileSize) {
            for (int x = 0; x < getWidth(); x += tileSize) {
                boolean alternate = ((x / tileSize) + (y / tileSize)) % 2 == 0;
                g2.setColor(alternate ? new Color(255, 255, 255, 10) : new Color(0, 0, 0, 12));
                g2.fillRect(x, y, tileSize, tileSize);
            }
        }
    }

    private void paintCatEyes(Graphics2D g2) {
        int centerX = getWidth() / 2;
        int eyeY = Math.max(70, getHeight() / 5);
        int eyeWidth = 44;
        int eyeHeight = 18;

        g2.setColor(new Color(242, 203, 110, 170));
        g2.fillOval(centerX - 88, eyeY, eyeWidth, eyeHeight);
        g2.fillOval(centerX + 44, eyeY, eyeWidth, eyeHeight);

        g2.setColor(new Color(20, 28, 28, 210));
        g2.fillOval(centerX - 68, eyeY + 2, 8, eyeHeight - 4);
        g2.fillOval(centerX + 64, eyeY + 2, 8, eyeHeight - 4);
    }

    private void paintContentGlow(Graphics2D g2) {
        int panelWidth = 600;
        int panelHeight = 560;
        int x = (getWidth() - panelWidth) / 2;
        int y = (getHeight() - panelHeight) / 2;

        g2.setColor(new Color(0, 0, 0, 90));
        g2.fillRoundRect(x - 18, y - 18, panelWidth + 36, panelHeight + 36, 22, 22);
        g2.setColor(PANEL_BACKGROUND);
        g2.fillRoundRect(x, y, panelWidth, panelHeight, 18, 18);
    }
}
