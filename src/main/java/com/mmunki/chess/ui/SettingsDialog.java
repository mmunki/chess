package com.mmunki.chess.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * The window with the settings of the game.
 */
public final class SettingsDialog extends JDialog {

    private final JCheckBox sound = new JCheckBox("Sound", true);
    private final JSlider volume = new JSlider(0, 100, 70);
    private final JCheckBox highlights = new JCheckBox("Show the possible moves", true);
    private final JTextField name = new JTextField(14);

    public SettingsDialog(Frame owner, String playerName, boolean soundOn, float volumeValue,
                          boolean highlightsOn) {
        super(owner, "Settings", true);
        setBackground(Theme.PANEL);

        sound.setSelected(soundOn);
        volume.setValue((int) Math.round(volumeValue * 100));
        highlights.setSelected(highlightsOn);
        name.setText(playerName);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.PANEL);
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 10, 24));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy = 0;
        form.add(label("Name"), c);
        c.gridx = 1;
        form.add(name, c);
        c.gridx = 0;
        c.gridy = 1;
        form.add(sound, c);
        c.gridx = 1;
        form.add(label("Volume"), c);
        c.gridy = 2;
        form.add(volume, c);
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;
        form.add(highlights, c);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttons.setBackground(Theme.PANEL);
        JButton save = FlatButton.primary("Save");
        JButton cancel = FlatButton.secondary("Cancel");
        save.addActionListener(e -> dispose());
        cancel.addActionListener(e -> dispose());
        buttons.add(save);
        buttons.add(cancel);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.PANEL);
        panel.add(form, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        setContentPane(panel);
        getRootPane().setDefaultButton(save);
        setPreferredSize(new Dimension(400, 300));
        pack();
        setLocationRelativeTo(owner);
    }

    private static JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.font(Font.PLAIN, 14));
        label.setForeground(Theme.TEXT);
        return label;
    }

    public String playerName() {
        String value = name.getText().trim();
        return value.isEmpty() ? "You" : value;
    }

    public boolean soundOn() {
        return sound.isSelected();
    }

    public float volume() {
        return volume.getValue() / 100f;
    }

    public boolean highlightsOn() {
        return highlights.isSelected();
    }
}
