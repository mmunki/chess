package com.mmunki.chess.ui;

import com.mmunki.chess.core.Board;
import com.mmunki.chess.core.Color;
import com.mmunki.chess.core.Game;
import com.mmunki.chess.core.Move;
import com.mmunki.chess.core.MoveGenerator;
import com.mmunki.chess.core.Piece;
import com.mmunki.chess.core.Square;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * The board with the pieces on it.
 *
 * <p>The board draws itself, takes the moves of the player and moves the
 * pieces with a short animation.
 */
public final class BoardPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    /** What the board tells the game. */
    public interface Listener {
        /** The player picked a move. */
        void movePicked(Move move);

        /** The player picked a move of a pawn that has to be promoted. */
        void promotionPicked(Move move);

        /** The player picked a square. */
        void squarePicked(int square);
    }

    private static final long ANIMATION_MILLIS = 170;

    /** One piece that is on its way. */
    private static final class Animation {
        final int from;
        final int to;
        final Piece piece;
        final Color color;
        final long start;

        Animation(int from, int to, Piece piece, Color color, long start) {
            this.from = from;
            this.to = to;
            this.piece = piece;
            this.color = color;
            this.start = start;
        }
    }

    private final List<Animation> animations = new ArrayList<>();
    private final Timer timer = new Timer(16, null);
    private Game game;
    private Listener listener;
    private int selected = -1;
    private List<Move> legal = new ArrayList<>();
    private boolean flipped;
    private boolean interactive = true;
    private boolean highlights = true;
    private int hoverSquare = -1;
    private Move hint;

    public BoardPanel() {
        setOpaque(true);
        setBackground(Theme.BACKGROUND);
        setPreferredSize(new Dimension(560, 560));
        timer.addActionListener(e -> {
            if (animations.isEmpty()) {
                timer.stop();
            }
            repaint();
        });
        timer.setRepeats(true);

        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                onPress(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                onRelease(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hoverSquare = -1;
                repaint();
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int square = squareAt(e.getX(), e.getY());
                if (square != hoverSquare) {
                    hoverSquare = square;
                    repaint();
                }
            }
        });
    }

    public void setGame(Game game) {
        this.game = game;
        this.selected = -1;
        this.legal = new ArrayList<>();
        this.animations.clear();
        repaint();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setFlipped(boolean flipped) {
        this.flipped = flipped;
        repaint();
    }

    public boolean isFlipped() {
        return flipped;
    }

    public void setInteractive(boolean interactive) {
        this.interactive = interactive;
    }

    /** Shows or hides the dots on the squares a piece can go to. */
    public void setHighlightsEnabled(boolean highlights) {
        this.highlights = highlights;
        repaint();
    }

    /** Shows the move that the computer would play. */
    public void setHint(Move move) {
        this.hint = move;
        repaint();
    }

    /** The square that is picked right now. */
    public int selectedSquare() {
        return selected;
    }

    /** Shows the move that was played with a short animation. */
    public void animate(Move move) {
        if (move == null || game == null) {
            return;
        }
        Board board = game.board();
        Color color = board.sideToMove().opposite();
        Piece piece = board.pieceAt(move.to);
        if (piece != null) {
            animations.add(new Animation(move.from, move.to, piece, color, System.currentTimeMillis()));
        }
        if (move.castle) {
            int rookFrom = move.to > move.from ? move.to + 1 : move.to - 2;
            int rookTo = move.to > move.from ? move.to - 1 : move.to + 1;
            animations.add(new Animation(rookFrom, rookTo, Piece.ROOK, color,
                    System.currentTimeMillis()));
        }
        if (!animations.isEmpty()) {
            timer.start();
        }
    }

    // ------------------------------------------------------------------ input

    private void onPress(MouseEvent e) {
        if (!interactive || game == null || game.isOver()) {
            return;
        }
        int square = squareAt(e.getX(), e.getY());
        if (square < 0) {
            return;
        }
        if (selected >= 0) {
            Move move = findMove(selected, square);
            if (move != null) {
                selected = -1;
                legal.clear();
                if (move.promotion != null) {
                    if (listener != null) {
                        listener.promotionPicked(move);
                    }
                } else if (listener != null) {
                    listener.movePicked(move);
                }
                repaint();
                return;
            }
        }
        select(square);
    }

    private void onRelease(MouseEvent e) {
        if (!interactive || game == null || game.isOver()) {
            return;
        }
        int square = squareAt(e.getX(), e.getY());
        if (square < 0 || selected < 0) {
            return;
        }
        Move move = findMove(selected, square);
        if (move != null) {
            selected = -1;
            legal.clear();
            if (move.promotion != null) {
                if (listener != null) {
                    listener.promotionPicked(move);
                }
            } else if (listener != null) {
                listener.movePicked(move);
            }
            repaint();
        }
    }

    private void select(int square) {
        selected = square;
        legal = new ArrayList<>();
        if (game != null) {
            Board board = game.board();
            if (board.isOccupiedBy(square, board.sideToMove())) {
                legal = game.legalMoves();
            }
        }
        if (listener != null) {
            listener.squarePicked(square);
        }
        repaint();
    }

    /** The move that goes from one square to the other, null when there is none. */
    private Move findMove(int from, int to) {
        for (Move move : legal) {
            if (move.from == from && move.to == to) {
                return move;
            }
        }
        return null;
    }

    /** The square under the mouse. */
    private int squareAt(int x, int y) {
        int size = squareSize();
        if (size <= 0) {
            return -1;
        }
        int file = x / size;
        int rank = y / size;
        if (file < 0 || file > 7 || rank < 0 || rank > 7) {
            return -1;
        }
        if (flipped) {
            return Square.of(7 - file, 7 - rank);
        }
        return Square.of(file, 7 - rank);
    }

    private int squareSize() {
        return Math.min(getWidth(), getHeight()) / 8;
    }

    // ------------------------------------------------------------------ drawing

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int size = squareSize();
        if (size <= 0 || game == null) {
            g2.dispose();
            return;
        }
        int offsetX = (getWidth() - size * 8) / 2;
        int offsetY = (getHeight() - size * 8) / 2;

        Board board = game.board();
        int lastFrom = -1;
        int lastTo = -1;
        if (!game.history().isEmpty()) {
            Move last = game.history().get(game.history().size() - 1);
            lastFrom = last.from;
            lastTo = last.to;
        }
        int checkedKing = board.inCheck(board.sideToMove())
                ? board.kingSquare(board.sideToMove()) : -1;

        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                boolean light = (file + rank) % 2 == 0;
                int square = Square.of(file, rank);
                java.awt.Color color = Theme.squareColor(file, rank, light);
                if (square == lastFrom || square == lastTo) {
                    color = Theme.highlight(color);
                }
                if (square == selected) {
                    color = new java.awt.Color(0xF7, 0xEC, 0x74);
                }
                int x = offsetX + (flipped ? 7 - file : file) * size;
                int y = offsetY + (flipped ? rank : 7 - rank) * size;
                g2.setColor(color);
                g2.fillRect(x, y, size, size);
                if (square == checkedKing) {
                    g2.setColor(new java.awt.Color(0xE2, 0x56, 0x4C, 170));
                    g2.fillOval(x + size / 12, y + size / 12, size * 5 / 6, size * 5 / 6);
                }
            }
        }

        drawCoordinates(g2, offsetX, offsetY, size);
        drawTargets(g2, offsetX, offsetY, size);
        drawPieces(g2, offsetX, offsetY, size);
        drawAnimations(g2, offsetX, offsetY, size);
        drawHint(g2, offsetX, offsetY, size);

        g2.setColor(new java.awt.Color(0, 0, 0, 120));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRect(offsetX, offsetY, size * 8 - 1, size * 8 - 1);
        g2.dispose();
    }

    private void drawCoordinates(Graphics2D g2, int offsetX, int offsetY, int size) {
        g2.setFont(Theme.font(Font.BOLD, Math.max(9, size / 9)));
        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                boolean light = (file + rank) % 2 == 0;
                int square = Square.of(file, rank);
                int x = offsetX + (flipped ? 7 - file : file) * size;
                int y = offsetY + (flipped ? 7 - rank : rank) * size;
                g2.setColor(light ? new java.awt.Color(0x76, 0x92, 0x56, 190) : new java.awt.Color(0xEE, 0xEE, 0xD2, 200));
                if (rank == 0) {
                    String letter = "" + (char) ('a' + file);
                    g2.drawString(letter, x + 4, y + size - 5);
                }
                if (file == 0) {
                    String number = "" + (rank + 1);
                    g2.drawString(number, x + 4, y + 14);
                }
            }
        }
    }

    /** The dots on the squares the piece can go to. */
    private void drawTargets(Graphics2D g2, int offsetX, int offsetY, int size) {
        if (selected < 0 || legal.isEmpty() || game == null || !highlights) {
            return;
        }
        Board board = game.board();
        for (Move move : legal) {
            if (move.from != selected) {
                continue;
            }
            int x = offsetX + (flipped ? 7 - Square.file(move.to) : Square.file(move.to)) * size;
            int y = offsetY + (flipped ? Square.rank(move.to) : 7 - Square.rank(move.to)) * size;
            boolean capture = board.pieceAt(move.to) != null || move.enPassant;
            g2.setColor(Theme.MOVE_DOT);
            if (capture) {
                g2.setStroke(new BasicStroke(Math.max(2f, size / 12f)));
                g2.drawOval(x + size / 6, y + size / 6, size * 2 / 3, size * 2 / 3);
            } else {
                int dot = size / 6;
                g2.fill(new Ellipse2D.Double(x + (size - dot) / 2.0, y + (size - dot) / 2.0, dot, dot));
            }
        }
    }

    private void drawPieces(Graphics2D g2, int offsetX, int offsetY, int size) {
        Board board = game.board();
        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                int square = Square.of(file, rank);
                Piece piece = board.pieceAt(square);
                if (piece == null || isAnimating(square)) {
                    continue;
                }
                int x = offsetX + (flipped ? 7 - file : file) * size;
                int y = offsetY + (flipped ? rank : 7 - rank) * size;
                PieceRenderer.draw(g2, piece, board.colorAt(square), x, y, size);
            }
        }
    }

    private boolean isAnimating(int square) {
        for (Animation animation : animations) {
            if (animation.to == square) {
                return true;
            }
        }
        return false;
    }

    /** A green circle around the square the hint points at. */
    private void drawHint(Graphics2D g2, int offsetX, int offsetY, int size) {
        if (hint == null) {
            return;
        }
        int x = offsetX + (flipped ? 7 - Square.file(hint.to) : Square.file(hint.to)) * size;
        int y = offsetY + (flipped ? Square.rank(hint.to) : 7 - Square.rank(hint.to)) * size;
        g2.setColor(new java.awt.Color(0x81, 0xB6, 0x4C, 170));
        g2.setStroke(new BasicStroke(Math.max(3f, size / 10f)));
        g2.drawOval(x + size / 8, y + size / 8, size * 3 / 4, size * 3 / 4);
    }

    private void drawAnimations(Graphics2D g2, int offsetX, int offsetY, int size) {
        long now = System.currentTimeMillis();
        List<Animation> done = new ArrayList<>();
        for (Animation animation : animations) {
            double progress = (now - animation.start) / (double) ANIMATION_MILLIS;
            if (progress >= 1.0) {
                done.add(animation);
                continue;
            }
            // a small ease so that the piece slows down at the end
            double eased = 1 - Math.pow(1 - progress, 3);
            double fromX = offsetX + (flipped ? 7 - Square.file(animation.from) : Square.file(animation.from)) * size;
            double fromY = offsetY + (flipped ? Square.rank(animation.from) : 7 - Square.rank(animation.from)) * size;
            double toX = offsetX + (flipped ? 7 - Square.file(animation.to) : Square.file(animation.to)) * size;
            double toY = offsetY + (flipped ? Square.rank(animation.to) : 7 - Square.rank(animation.to)) * size;
            PieceRenderer.draw(g2, animation.piece, animation.color,
                    fromX + (toX - fromX) * eased, fromY + (toY - fromY) * eased, size);
        }
        animations.removeAll(done);
    }
}
