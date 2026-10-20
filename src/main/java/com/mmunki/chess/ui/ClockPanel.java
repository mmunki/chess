package com.mmunki.chess.ui;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * The clock of a player.
 *
 * <p>The clock shows the time that is left and a bar that becomes shorter
 * while the player thinks.
 */
public final class ClockPanel extends JPanel {

    private final Timer timer;
    private long remainingMillis;
    private long incrementMillis;
    private long totalMillis;
    private boolean running;
    private boolean warning;
    private final Color accent;

    public ClockPanel(Color accent) {
        this.accent = accent;
        setOpaque(false);
        setPreferredSize(new Dimension(130, 56));
        setMaximumSize(new Dimension(130, 56));
        setMinimumSize(new Dimension(130, 56));
        timer = new Timer(100, e -> {
            if (running) {
                remainingMillis -= 100;
                if (remainingMillis <= 0) {
                    remainingMillis = 0;
                    running = false;
                }
                repaint();
            }
        });
        timer.setRepeats(true);
    }

    public void setTime(long millis) {
        this.totalMillis = millis;
        this.remainingMillis = millis;
        repaint();
    }

    public void setIncrement(long millis) {
        this.incrementMillis = millis;
    }

    public void start() {
        running = true;
        timer.start();
        repaint();
    }

    public void stop() {
        running = false;
        timer.stop();
        repaint();
    }

    public boolean isRunning() {
        return running;
    }

    /** The time that is left. */
    public long remaining() {
        return remainingMillis;
    }

    /** The time that is added after a move. */
    public void addIncrement() {
        remainingMillis += incrementMillis;
        repaint();
    }

    public void reset() {
        remainingMillis = totalMillis;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        g2.setColor(new Color(0, 0, 0, 90));
        g2.fillRoundRect(0, 0, width, height, 10, 10);

        double fraction = totalMillis > 0 ? (double) remainingMillis / totalMillis : 0;
        g2.setColor(accent);
        g2.fillRoundRect(0, 0, (int) (width * fraction), height, 10, 10);

        boolean low = remainingMillis < 30000;
        g2.setFont(Theme.font(Font.BOLD, 22));
        g2.setColor(low ? new Color(0x30, 0x20, 0x20) : Color.WHITE);
        String text = format(remainingMillis);
        FontMetrics metrics = g2.getFontMetrics();
        int x = (width - metrics.stringWidth(text)) / 2;
        int y = (height - metrics.getHeight()) / 2 + metrics.getAscent();
        g2.drawString(text, x, y);

        if (warning) {
            g2.setColor(new Color(0, 0, 0, 120));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1, width - 2, height - 2, 10, 10);
        }
        g2.dispose();
    }

    private static String format(long millis) {
        long seconds = Math.max(0, millis) / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        long hours = minutes / 60;
        minutes = minutes % 60;
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%d:%02d", minutes, seconds);
    }
}
