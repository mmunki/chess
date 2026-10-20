package com.mmunki.chess.ui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * The bar of a player: the picture, the name, the rating and the clock.
 */
public final class PlayerBar extends JPanel {

    private static final long serialVersionUID = 1L;

    private final Avatar avatar = new Avatar();
    private final JLabel name = new JLabel();
    private final JLabel rating = new JLabel();
    private final ClockPanel clock;

    public PlayerBar(Color accent) {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

        avatar.setPreferredSize(new Dimension(44, 44));
        avatar.setAlignmentY(CENTER_ALIGNMENT);
        add(avatar);

        add(Box.createHorizontalStrut(10));

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        name.setFont(Theme.font(Font.BOLD, 15));
        name.setForeground(Theme.TEXT);
        name.setAlignmentX(LEFT_ALIGNMENT);
        rating.setFont(Theme.font(Font.PLAIN, 13));
        rating.setForeground(Theme.TEXT_MUTED);
        rating.setAlignmentX(LEFT_ALIGNMENT);
        texts.add(name);
        texts.add(rating);
        texts.setAlignmentY(CENTER_ALIGNMENT);
        add(texts);

        add(Box.createHorizontalGlue());

        clock = new ClockPanel(accent);
        clock.setAlignmentY(CENTER_ALIGNMENT);
        add(clock);
    }

    public void setPlayer(String playerName, int ratingValue) {
        name.setText(playerName);
        rating.setText(ratingValue > 0 ? "Rating " + ratingValue : "Unrated");
        avatar.setInitials(initials(playerName));
    }

    public void setClockVisible(boolean visible) {
        clock.setVisible(visible);
    }

    public ClockPanel clock() {
        return clock;
    }

    public void setActive(boolean active) {
        name.setForeground(active ? Theme.TEXT : Theme.TEXT_MUTED);
    }

    private static String initials(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "?";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            String part = parts[0];
            return part.substring(0, 1).toUpperCase();
        }
        return (parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    /** The round picture with the letters of the player. */
    private static final class Avatar extends JPanel {
        private static final long serialVersionUID = 1L;
        private String initials = "?";

        Avatar() {
            setOpaque(false);
        }

        void setInitials(String initials) {
            this.initials = initials;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int size = Math.min(getWidth(), getHeight());
            g2.setColor(Theme.PANEL_LIGHT);
            g2.fillOval(0, 0, size, size);
            g2.setColor(Theme.ACCENT);
            g2.setStroke(new java.awt.BasicStroke(2f));
            g2.drawOval(0, 0, size - 1, size - 1);
            g2.setFont(Theme.font(Font.BOLD, 16));
            g2.setColor(Theme.TEXT);
            int width = g2.getFontMetrics().stringWidth(initials);
            g2.drawString(initials, (size - width) / 2,
                    (size + g2.getFontMetrics().getAscent()) / 2 - 2);
            g2.dispose();
        }
    }
}
