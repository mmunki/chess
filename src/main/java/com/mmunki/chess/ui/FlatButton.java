package com.mmunki.chess.ui;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A button in the style of a phone: a rounded rectangle that changes its
 * colour when the mouse is over it and when it is pressed.
 */
public final class FlatButton extends JButton {

    private final Color normal;
    private final Color hover;
    private final Color pressed;
    private boolean over;
    private boolean down;

    public FlatButton(String text, Color normal, Color hover, Color pressed) {
        super(text);
        this.normal = normal;
        this.hover = hover;
        this.pressed = pressed;
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setForeground(Theme.TEXT);
        setFont(Theme.font(java.awt.Font.BOLD, 14));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(140, 40));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                over = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                over = false;
                down = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                down = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                down = false;
                repaint();
            }
        });
    }

    /** A button with the colours of the game. */
    public static FlatButton primary(String text) {
        return new FlatButton(text, Theme.ACCENT, Theme.ACCENT.brighter(), Theme.ACCENT_DARK);
    }

    /** A button that does not stand out. */
    public static FlatButton secondary(String text) {
        return new FlatButton(text, Theme.PANEL_LIGHT, new Color(0x4A, 0x47, 0x44),
                new Color(0x2A, 0x28, 0x26));
    }

    /** A button that ends the game. */
    public static FlatButton danger(String text) {
        return new FlatButton(text, new Color(0x8A, 0x32, 0x2B), Theme.DANGER, new Color(0x6E, 0x28, 0x22));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color fill = down ? pressed : over ? hover : normal;
        g2.setColor(fill);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
        g2.setColor(new Color(0, 0, 0, 60));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
        g2.dispose();
        super.paintComponent(g);
    }
}
