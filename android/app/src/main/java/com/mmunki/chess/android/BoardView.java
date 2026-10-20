package com.mmunki.chess.android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

import com.mmunki.chess.core.Board;
import com.mmunki.chess.core.Color;
import com.mmunki.chess.core.Game;
import com.mmunki.chess.core.Move;
import com.mmunki.chess.core.Piece;
import com.mmunki.chess.core.Square;

import java.util.ArrayList;
import java.util.List;

/**
 * The board with the pieces on it.
 *
 * <p>The board draws itself, takes the moves of the player and moves the
 * pieces with a short animation.
 */
public final class BoardView extends View {

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

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<Animation> animations = new ArrayList<>();
    private Game game;
    private Listener listener;
    private int selected = -1;
    private List<Move> legal = new ArrayList<>();
    private boolean flipped;
    private boolean interactive = true;
    private boolean highlights = true;
    private Move hint;

    public BoardView(Context context) {
        super(context);
        text.setAntiAlias(true);
    }

    public void setGame(Game game) {
        this.game = game;
        this.selected = -1;
        this.legal = new ArrayList<>();
        this.animations.clear();
        invalidate();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setFlipped(boolean flipped) {
        this.flipped = flipped;
        invalidate();
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
        invalidate();
    }

    /** Shows the move that the computer would play. */
    public void setHint(Move move) {
        this.hint = move;
        invalidate();
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
            invalidate();
        }
    }

    // ------------------------------------------------------------------ input

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!interactive || game == null || game.isOver()) {
            return false;
        }
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            return true;
        }
        if (action != MotionEvent.ACTION_UP) {
            return true;
        }
        int square = squareAt(event.getX(), event.getY());
        if (square < 0) {
            return true;
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
                invalidate();
                return true;
            }
        }
        select(square);
        return true;
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
        invalidate();
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

    /** The square under the finger. */
    private int squareAt(float x, float y) {
        int size = squareSize();
        if (size <= 0) {
            return -1;
        }
        int offsetX = offsetX();
        int offsetY = offsetY();
        int file = (int) ((x - offsetX) / size);
        int rank = (int) ((y - offsetY) / size);
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

    private int offsetX() {
        int size = squareSize();
        return (getWidth() - size * 8) / 2;
    }

    private int offsetY() {
        int size = squareSize();
        return (getHeight() - size * 8) / 2;
    }

    // ------------------------------------------------------------------ drawing

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int size = squareSize();
        if (size <= 0 || game == null) {
            return;
        }
        int offsetX = offsetX();
        int offsetY = offsetY();

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
                int color = Theme.squareColor(light);
                if (square == lastFrom || square == lastTo) {
                    color = Theme.highlight(color);
                }
                if (square == selected) {
                    color = Theme.HIGHLIGHT_LIGHT;
                }
                int x = offsetX + (flipped ? 7 - file : file) * size;
                int y = offsetY + (flipped ? rank : 7 - rank) * size;
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(color);
                canvas.drawRect(x, y, x + size, y + size, paint);
                if (square == checkedKing) {
                    paint.setColor(0xAAE2564C);
                    canvas.drawOval(x + size / 12f, y + size / 12f,
                            x + size * 11 / 12f, y + size * 11 / 12f, paint);
                }
            }
        }

        drawCoordinates(canvas, offsetX, offsetY, size);
        drawTargets(canvas, offsetX, offsetY, size);
        drawPieces(canvas, offsetX, offsetY, size);
        drawAnimations(canvas, offsetX, offsetY, size);
        drawHint(canvas, offsetX, offsetY, size);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1f);
        paint.setColor(0x78000000);
        canvas.drawRect(offsetX, offsetY, offsetX + size * 8 - 1, offsetY + size * 8 - 1, paint);

        if (!animations.isEmpty()) {
            postInvalidateOnAnimation();
        }
    }

    private void drawCoordinates(Canvas canvas, int offsetX, int offsetY, int size) {
        text.setColor(0xC0769256);
        text.setTextSize(Math.max(9f, size / 9f));
        text.setFakeBoldText(true);
        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                boolean light = (file + rank) % 2 == 0;
                int x = offsetX + (flipped ? 7 - file : file) * size;
                int y = offsetY + (flipped ? rank : 7 - rank) * size;
                text.setColor(light ? 0xBE769256 : 0xC8EEEED2);
                if (rank == 0) {
                    canvas.drawText("" + (char) ('a' + file), x + 4, y + size - 5, text);
                }
                if (file == 0) {
                    canvas.drawText("" + (rank + 1), x + 4, y + 14, text);
                }
            }
        }
    }

    /** The dots on the squares the piece can go to. */
    private void drawTargets(Canvas canvas, int offsetX, int offsetY, int size) {
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
            paint.setStyle(Paint.Style.FILL);
            if (capture) {
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(Math.max(2f, size / 12f));
                paint.setColor(Theme.CAPTURE_RING);
                canvas.drawOval(x + size / 6f, y + size / 6f,
                        x + size * 5 / 6f, y + size * 5 / 6f, paint);
            } else {
                int dot = size / 6;
                paint.setColor(Theme.MOVE_DOT);
                canvas.drawCircle(x + size / 2f, y + size / 2f, dot / 2f, paint);
            }
        }
    }

    private void drawPieces(Canvas canvas, int offsetX, int offsetY, int size) {
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
                PiecePainter.draw(canvas, piece, board.colorAt(square), x, y, size);
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
    private void drawHint(Canvas canvas, int offsetX, int offsetY, int size) {
        if (hint == null) {
            return;
        }
        int x = offsetX + (flipped ? 7 - Square.file(hint.to) : Square.file(hint.to)) * size;
        int y = offsetY + (flipped ? Square.rank(hint.to) : 7 - Square.rank(hint.to)) * size;
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(3f, size / 10f));
        paint.setColor(0xAA81B64C);
        canvas.drawOval(x + size / 8f, y + size / 8f,
                x + size * 7 / 8f, y + size * 7 / 8f, paint);
    }

    private void drawAnimations(Canvas canvas, int offsetX, int offsetY, int size) {
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
            float fromX = offsetX + (flipped ? 7 - Square.file(animation.from) : Square.file(animation.from)) * size;
            float fromY = offsetY + (flipped ? Square.rank(animation.from) : 7 - Square.rank(animation.from)) * size;
            float toX = offsetX + (flipped ? 7 - Square.file(animation.to) : Square.file(animation.to)) * size;
            float toY = offsetY + (flipped ? Square.rank(animation.to) : 7 - Square.rank(animation.to)) * size;
            PiecePainter.draw(canvas, animation.piece, animation.color,
                    (float) (fromX + (toX - fromX) * eased),
                    (float) (fromY + (toY - fromY) * eased), size);
        }
        animations.removeAll(done);
    }
}
