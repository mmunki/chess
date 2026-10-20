package com.mmunki.chess.ui;

import com.mmunki.chess.ai.Bot;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * The window that is opened before a new game.
 *
 * <p>The player chooses against whom the game is played, how strong the
 * computer is and how much time everybody gets.
 */
public final class NewGameDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final JRadioButton computer = new JRadioButton("Computer", true);
    private final JRadioButton friend = new JRadioButton("Friend on this computer");
    private final JComboBox<Bot.Level> level = new JComboBox<>(Bot.LEVELS.toArray(new Bot.Level[0]));
    private final JComboBox<Integer> minutes = new JComboBox<>(new Integer[]{1, 2, 3, 5, 10, 15, 30});
    private final JComboBox<Integer> increment = new JComboBox<>(new Integer[]{0, 2, 3, 5, 10});

    private boolean accepted;

    public NewGameDialog(Frame owner, Bot.Level currentLevel, int currentMinutes, int currentIncrement) {
        super(owner, "New game", true);
        setBackground(Theme.PANEL);

        level.setSelectedItem(currentLevel);
        minutes.setSelectedItem(currentMinutes);
        increment.setSelectedItem(currentIncrement);

        ButtonGroup group = new ButtonGroup();
        group.add(computer);
        group.add(friend);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.PANEL);
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 10, 24));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        form.add(title("Opponent"), c);
        c.gridwidth = 1;
        c.gridy = 1;
        form.add(computer, c);
        c.gridx = 1;
        form.add(level, c);
        c.gridx = 0;
        c.gridy = 2;
        form.add(friend, c);
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;
        form.add(title("Time"), c);
        c.gridwidth = 1;
        c.gridy = 4;
        form.add(new JLabel("Minutes per player"), c);
        c.gridx = 1;
        form.add(minutes, c);
        c.gridx = 0;
        c.gridy = 5;
        form.add(new JLabel("Seconds after a move"), c);
        c.gridx = 1;
        form.add(increment, c);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttons.setBackground(Theme.PANEL);
        JButton start = FlatButton.primary("Start");
        JButton cancel = FlatButton.secondary("Cancel");
        start.addActionListener(e -> {
            accepted = true;
            dispose();
        });
        cancel.addActionListener(e -> dispose());
        buttons.add(start);
        buttons.add(cancel);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.PANEL);
        panel.add(form, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        setContentPane(panel);
        getRootPane().setDefaultButton(start);
        setPreferredSize(new Dimension(400, 340));
        pack();
        setLocationRelativeTo(owner);
    }

    private static JLabel title(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.font(Font.BOLD, 16));
        label.setForeground(Theme.TEXT);
        return label;
    }

    public boolean accepted() {
        return accepted;
    }

    public boolean againstComputer() {
        return computer.isSelected();
    }

    public Bot.Level level() {
        Bot.Level selected = (Bot.Level) level.getSelectedItem();
        return selected == null ? Bot.MEDIUM : selected;
    }

    public int minutes() {
        Integer value = (Integer) minutes.getSelectedItem();
        return value == null ? 5 : value;
    }

    public int increment() {
        Integer value = (Integer) increment.getSelectedItem();
        return value == null ? 0 : value;
    }
}
