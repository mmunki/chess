package com.mmunki.chess.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;

/**
 * The window that tells how the game ended.
 */
public final class GameOverDialog extends JDialog {

    public GameOverDialog(Frame owner, String title, String message, String detail) {
        super(owner, title, true);
        setBackground(Theme.PANEL);

        JPanel content = new JPanel(new GridLayout(0, 1, 0, 10));
        content.setBackground(Theme.PANEL);
        content.setBorder(BorderFactory.createEmptyBorder(24, 32, 20, 32));

        JLabel headline = new JLabel(title, JLabel.CENTER);
        headline.setFont(Theme.font(Font.BOLD, 26));
        headline.setForeground(Theme.TEXT);
        content.add(headline);

        JLabel text = new JLabel(message, JLabel.CENTER);
        text.setFont(Theme.font(Font.PLAIN, 15));
        text.setForeground(Theme.TEXT_MUTED);
        content.add(text);

        if (detail != null && !detail.isEmpty()) {
            JLabel info = new JLabel(detail, JLabel.CENTER);
            info.setFont(Theme.font(Font.PLAIN, 14));
            info.setForeground(Theme.TEXT_MUTED);
            content.add(info);
        }

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttons.setBackground(Theme.PANEL);
        JButton close = FlatButton.secondary("Close");
        close.addActionListener(e -> dispose());
        buttons.add(close);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.PANEL);
        panel.add(content, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        setContentPane(panel);
        getRootPane().setDefaultButton(close);
        setPreferredSize(new Dimension(380, 220));
        pack();
        setLocationRelativeTo(owner);
    }

    /** The colour of a result, green for a win, red for a loss. */
    public static Color resultColor(boolean won, boolean draw) {
        if (draw) {
            return Theme.TEXT;
        }
        return won ? Theme.ACCENT : Theme.DANGER;
    }
}
