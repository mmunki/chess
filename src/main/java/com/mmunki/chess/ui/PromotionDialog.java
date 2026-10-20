package com.mmunki.chess.ui;

import com.mmunki.chess.core.Color;
import com.mmunki.chess.core.Piece;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * The window that asks which piece a pawn becomes.
 */
public final class PromotionDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private Piece chosen;

    public PromotionDialog(java.awt.Frame owner, Color color) {
        super(owner, "Promote your pawn", true);
        setUndecorated(true);
        setBackground(new java.awt.Color(0, 0, 0, 0));
        setLayout(new BorderLayout());

        JPanel panel = new JPanel(new java.awt.GridLayout(1, 4, 8, 0));
        panel.setBackground(Theme.PANEL);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        for (Piece piece : new Piece[]{Piece.QUEEN, Piece.ROOK, Piece.BISHOP, Piece.KNIGHT}) {
            panel.add(new PieceChoice(piece, color));
        }
        add(panel, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(owner);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                chosen = null;
            }
        });
    }

    /** The piece the player picked, null when the window was closed. */
    public Piece chosen() {
        return chosen;
    }

    /** One of the four pieces. */
    private final class PieceChoice extends JPanel {
        private static final long serialVersionUID = 1L;
        private final Piece piece;
        private final Color color;
        private boolean over;

        PieceChoice(Piece piece, Color color) {
            this.piece = piece;
            this.color = color;
            setPreferredSize(new Dimension(76, 76));
            setOpaque(false);
            setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    chosen = piece;
                    dispose();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    over = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    over = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (over) {
                g2.setColor(Theme.PANEL_LIGHT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            }
            g2.dispose();
            PieceRenderer.draw((Graphics2D) g, piece, color, 4, 4, getWidth() - 8);
        }
    }
}
