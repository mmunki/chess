package com.mmunki.chess.android;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import com.mmunki.chess.core.Color;
import com.mmunki.chess.core.Piece;

import java.util.ArrayList;
import java.util.List;

/**
 * Draws the pieces.
 *
 * <p>The pieces are drawn as vectors, they do not need an image or a font.
 * Every piece is built from a few shapes that are put together, the outline
 * is drawn around the whole piece.
 */
public final class PiecePainter {

    private static final Paint FILL = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint LINE = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint SHADE = new Paint(Paint.ANTI_ALIAS_FLAG);

    private PiecePainter() {
    }

    /** Draws a piece in the square, the square is from x to x + size. */
    public static void draw(Canvas canvas, Piece piece, Color color,
                            float x, float y, float size) {
        float scale = size / 100f;
        // a small margin, so that the piece does not touch the edge of the square
        float inset = size * 0.025f;
        int saved = canvas.save();
        canvas.translate(x + inset, y + inset);
        canvas.scale(scale * 0.95f, scale * 0.95f);

        List<Path> shapes = shapes(piece);
        Path area = union(shapes);

        FILL.setStyle(Paint.Style.FILL);
        FILL.setColor(color == Color.WHITE ? Theme.PIECE_WHITE : Theme.PIECE_BLACK);
        if (area != null) {
            canvas.drawPath(area, FILL);
        } else {
            // the shapes are drawn one by one, the colour of all of them is
            // the same, so they look like one piece
            for (Path shape : shapes) {
                canvas.drawPath(shape, FILL);
            }
        }

        // a soft shade on the lower half of the piece
        SHADE.setStyle(Paint.Style.FILL);
        SHADE.setColor(color == Color.WHITE ? 0x0C000000 : 0x28000000);
        int shade = canvas.save();
        if (area != null) {
            canvas.clipPath(area);
        }
        canvas.drawRect(0f, 60f, 100f, 100f, SHADE);
        canvas.restoreToCount(shade);

        LINE.setStyle(Paint.Style.STROKE);
        LINE.setStrokeWidth(2.6f);
        LINE.setStrokeCap(Paint.Cap.ROUND);
        LINE.setStrokeJoin(Paint.Join.ROUND);
        LINE.setColor(color == Color.WHITE ? Theme.PIECE_WHITE_OUTLINE : Theme.PIECE_BLACK_OUTLINE);
        if (area != null) {
            canvas.drawPath(area, LINE);
        } else {
            for (Path shape : shapes) {
                canvas.drawPath(shape, LINE);
            }
        }

        if (piece == Piece.BISHOP) {
            LINE.setStrokeWidth(2.2f);
            Path slit = new Path();
            slit.moveTo(50f, 22f);
            slit.quadTo(45f, 34f, 44f, 46f);
            canvas.drawPath(slit, LINE);
        }
        if (piece == Piece.KNIGHT) {
            LINE.setStrokeWidth(2.0f);
            Path mouth = new Path();
            mouth.moveTo(24f, 47f);
            mouth.quadTo(33f, 50f, 41f, 46f);
            canvas.drawPath(mouth, LINE);
            FILL.setColor(0xB3000000);
            canvas.drawCircle(41f, 34f, 3f, FILL);
        }
        canvas.restoreToCount(saved);
    }

    /**
     * The outline of a piece, the shapes of the piece in one path.
     *
     * <p>The path is null when the shapes could not be merged, then the
     * shapes are drawn one by one.
     */
    private static Path union(List<Path> shapes) {
        Path area = new Path();
        for (Path shape : shapes) {
            if (area.isEmpty()) {
                area.set(shape);
                continue;
            }
            if (!area.op(shape, Path.Op.UNION)) {
                return null;
            }
        }
        return area.isEmpty() ? null : area;
    }

    /** The shapes that a piece is built from. */
    private static List<Path> shapes(Piece piece) {
        List<Path> shapes = new ArrayList<>();
        switch (piece) {
            case PAWN:
                shapes.add(foot(20, 76));
                shapes.add(stem());
                shapes.add(ellipse(37, 44, 26, 15));
                shapes.add(ellipse(37, 17, 26, 27));
                break;
            case ROOK:
                shapes.add(foot(16, 76));
                shapes.add(body(35, 66, 40, 74));
                shapes.add(rect(31, 40, 38, 7));
                shapes.add(merlons());
                break;
            case KNIGHT:
                shapes.add(foot(20, 78));
                shapes.add(knight());
                break;
            case BISHOP:
                shapes.add(foot(18, 76));
                shapes.add(body(36, 64, 42, 74));
                shapes.add(ellipse(34, 34, 32, 12));
                shapes.add(ellipse(36, 18, 28, 22));
                shapes.add(ellipse(45, 10, 10, 10));
                break;
            case QUEEN:
                shapes.add(foot(16, 78));
                shapes.add(queenBody());
                shapes.add(ellipse(30, 34, 40, 12));
                shapes.add(crown());
                shapes.add(ellipse(24, 8, 11, 11));
                shapes.add(ellipse(44, 4, 11, 11));
                shapes.add(ellipse(65, 8, 11, 11));
                break;
            case KING:
            default:
                shapes.add(foot(16, 78));
                shapes.add(kingBody());
                shapes.add(ellipse(28, 32, 44, 12));
                shapes.add(rect(33, 24, 34, 9));
                shapes.add(rect(45, 8, 10, 18));
                shapes.add(rect(38, 14, 24, 10));
                break;
        }
        return shapes;
    }

    /** The wide foot that every piece stands on. */
    private static Path foot(double width, double top) {
        Path path = new Path();
        path.addRoundRect(new RectF((float) (50 - width / 2), (float) top,
                (float) (50 + width / 2), (float) (top + 22)), 10f, 10f, Path.Direction.CW);
        return path;
    }

    /** The body between the foot and the head. */
    private static Path body(double left, double right, double top, double bottom) {
        Path path = new Path();
        path.moveTo((float) left, (float) bottom);
        path.quadTo((float) (left + 2), (float) (top + 12), (float) (left + 6), (float) top);
        path.lineTo((float) (right - 6), (float) top);
        path.quadTo((float) (right - 2), (float) (top + 12), (float) right, (float) bottom);
        path.close();
        return path;
    }

    private static Path stem() {
        Path path = new Path();
        path.moveTo(40f, 76f);
        path.quadTo(41f, 64f, 44f, 56f);
        path.lineTo(56f, 56f);
        path.quadTo(59f, 64f, 60f, 76f);
        path.close();
        return path;
    }

    /** The top of a rook with its three merlons. */
    private static Path merlons() {
        Path path = new Path();
        path.moveTo(25f, 40f);
        path.lineTo(25f, 16f);
        path.lineTo(36f, 16f);
        path.lineTo(36f, 25f);
        path.lineTo(43f, 25f);
        path.lineTo(43f, 16f);
        path.lineTo(57f, 16f);
        path.lineTo(57f, 25f);
        path.lineTo(64f, 25f);
        path.lineTo(64f, 16f);
        path.lineTo(75f, 16f);
        path.lineTo(75f, 40f);
        path.close();
        return path;
    }

    /** The head of the knight, it looks to the left. */
    private static Path knight() {
        Path path = new Path();
        path.moveTo(38f, 78f);
        path.quadTo(34f, 62f, 34f, 52f);
        path.quadTo(30f, 48f, 22f, 46f);
        path.quadTo(19f, 45f, 20f, 42f);
        path.quadTo(23f, 38f, 31f, 36f);
        path.quadTo(35f, 34f, 37f, 30f);
        path.lineTo(33f, 12f);
        path.quadTo(45f, 16f, 50f, 26f);
        path.quadTo(55f, 18f, 62f, 14f);
        path.quadTo(72f, 26f, 73f, 42f);
        path.quadTo(74f, 60f, 66f, 78f);
        path.close();
        return path;
    }

    private static Path queenBody() {
        Path path = new Path();
        path.moveTo(34f, 78f);
        path.quadTo(38f, 60f, 42f, 46f);
        path.lineTo(58f, 46f);
        path.quadTo(62f, 60f, 66f, 78f);
        path.close();
        return path;
    }

    /** The crown of the queen with its five points. */
    private static Path crown() {
        Path path = new Path();
        path.moveTo(28f, 34f);
        path.lineTo(24f, 14f);
        path.quadTo(30f, 20f, 36f, 26f);
        path.lineTo(42f, 6f);
        path.quadTo(50f, 16f, 58f, 6f);
        path.lineTo(64f, 26f);
        path.quadTo(70f, 20f, 76f, 14f);
        path.lineTo(72f, 34f);
        path.close();
        return path;
    }

    private static Path kingBody() {
        Path path = new Path();
        path.moveTo(34f, 78f);
        path.quadTo(38f, 58f, 40f, 44f);
        path.lineTo(60f, 44f);
        path.quadTo(62f, 58f, 66f, 78f);
        path.close();
        return path;
    }

    private static Path ellipse(double left, double top, double width, double height) {
        Path path = new Path();
        path.addOval(new RectF((float) left, (float) top,
                (float) (left + width), (float) (top + height)), Path.Direction.CW);
        return path;
    }

    private static Path rect(double left, double top, double width, double height) {
        Path path = new Path();
        path.addRect(new RectF((float) left, (float) top,
                (float) (left + width), (float) (top + height)), Path.Direction.CW);
        return path;
    }
}
