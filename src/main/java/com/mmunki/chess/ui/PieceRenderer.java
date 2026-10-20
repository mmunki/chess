package com.mmunki.chess.ui;

import com.mmunki.chess.core.Color;
import com.mmunki.chess.core.Piece;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.RoundRectangle2D;

/**
 * Draws the pieces.
 *
 * <p>The pieces are drawn as vectors, they do not need an image or a font.
 * Every piece is built from a few shapes that are put together, the outline
 * is drawn around the whole piece.
 */
public final class PieceRenderer {

    private PieceRenderer() {
    }

    /** Draws a piece in the square, the square is from x to x + size. */
    public static void draw(Graphics2D g, Piece piece, Color color,
                            double x, double y, double size) {
        Graphics2D copy = (Graphics2D) g.create();
        copy.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        copy.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        double scale = size / 100.0;
        // a small margin, so that the piece does not touch the edge of the square
        double inset = size * 0.025;
        copy.translate(x + inset, y + inset);
        copy.scale(scale * 0.95, scale * 0.95);

        Area area = build(piece);
        Path2D.Double outline = toPath(area);

        java.awt.Color fill = color == Color.WHITE ? Theme.PIECE_WHITE : Theme.PIECE_BLACK;
        java.awt.Color line = color == Color.WHITE ? Theme.PIECE_WHITE_OUTLINE : Theme.PIECE_BLACK_OUTLINE;

        copy.setColor(fill);
        copy.fill(area);

        // a soft shade on the lower half of the piece
        copy.setColor(new java.awt.Color(0, 0, 0, color == Color.WHITE ? 12 : 40));
        copy.fill(new Area(new java.awt.geom.Rectangle2D.Double(0, 60, 100, 40)));

        copy.setColor(line);
        copy.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        copy.draw(outline);

        if (piece == Piece.BISHOP) {
            copy.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Path2D.Double slit = new Path2D.Double();
            slit.moveTo(50, 22);
            slit.quadTo(45, 34, 44, 46);
            copy.draw(slit);
        }
        if (piece == Piece.KNIGHT) {
            copy.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Path2D.Double mouth = new Path2D.Double();
            mouth.moveTo(24, 47);
            mouth.quadTo(33, 50, 41, 46);
            copy.draw(mouth);
            copy.setColor(new java.awt.Color(0, 0, 0, 180));
            Ellipse2D.Double eye = new Ellipse2D.Double(38, 31, 6, 6);
            copy.fill(eye);
        }
        copy.dispose();
    }

    private static Area build(Piece piece) {
        Area area = new Area();
        switch (piece) {
            case PAWN:
                area.add(new Area(foot(20, 76)));
                area.add(new Area(stem()));
                area.add(new Area(new Ellipse2D.Double(37, 44, 26, 15)));
                area.add(new Area(new Ellipse2D.Double(37, 17, 26, 27)));
                break;
            case ROOK:
                area.add(new Area(foot(16, 76)));
                area.add(new Area(body(35, 66, 40, 74)));
                area.add(new Area(new java.awt.geom.Rectangle2D.Double(31, 40, 38, 7)));
                area.add(new Area(merlons()));
                break;
            case KNIGHT:
                area.add(new Area(foot(20, 78)));
                area.add(new Area(knight()));
                break;
            case BISHOP:
                area.add(new Area(foot(18, 76)));
                area.add(new Area(body(36, 64, 42, 74)));
                area.add(new Area(new Ellipse2D.Double(34, 34, 32, 12)));
                area.add(new Area(new Ellipse2D.Double(36, 18, 28, 22)));
                area.add(new Area(new Ellipse2D.Double(45, 10, 10, 10)));
                break;
            case QUEEN:
                area.add(new Area(foot(16, 78)));
                area.add(new Area(queenBody()));
                area.add(new Area(new Ellipse2D.Double(30, 34, 40, 12)));
                area.add(new Area(crown()));
                area.add(new Area(new Ellipse2D.Double(24, 8, 11, 11)));
                area.add(new Area(new Ellipse2D.Double(44, 4, 11, 11)));
                area.add(new Area(new Ellipse2D.Double(65, 8, 11, 11)));
                break;
            case KING:
            default:
                area.add(new Area(foot(16, 78)));
                area.add(new Area(kingBody()));
                area.add(new Area(new Ellipse2D.Double(28, 32, 44, 12)));
                area.add(new Area(new java.awt.geom.Rectangle2D.Double(33, 24, 34, 9)));
                area.add(new Area(new java.awt.geom.Rectangle2D.Double(45, 8, 10, 18)));
                area.add(new Area(new java.awt.geom.Rectangle2D.Double(38, 14, 24, 10)));
                break;
        }
        return area;
    }

    /** The wide foot that every piece stands on. */
    private static RoundRectangle2D.Double foot(double width, double top) {
        return new RoundRectangle2D.Double(50 - width / 2, top, width, 22, 10, 10);
    }

    /** The body between the foot and the head. */
    private static Path2D.Double body(double left, double right, double top, double bottom) {
        Path2D.Double path = new Path2D.Double();
        path.moveTo(left, bottom);
        path.quadTo(left + 2, top + 12, left + 6, top);
        path.lineTo(right - 6, top);
        path.quadTo(right - 2, top + 12, right, bottom);
        path.closePath();
        return path;
    }

    private static Path2D.Double stem() {
        Path2D.Double path = new Path2D.Double();
        path.moveTo(40, 76);
        path.quadTo(41, 64, 44, 56);
        path.lineTo(56, 56);
        path.quadTo(59, 64, 60, 76);
        path.closePath();
        return path;
    }

    /** The top of a rook with its three merlons. */
    private static Path2D.Double merlons() {
        Path2D.Double path = new Path2D.Double();
        path.moveTo(25, 40);
        path.lineTo(25, 16);
        path.lineTo(36, 16);
        path.lineTo(36, 25);
        path.lineTo(43, 25);
        path.lineTo(43, 16);
        path.lineTo(57, 16);
        path.lineTo(57, 25);
        path.lineTo(64, 25);
        path.lineTo(64, 16);
        path.lineTo(75, 16);
        path.lineTo(75, 40);
        path.closePath();
        return path;
    }

    /** The head of the knight, it looks to the left. */
    private static Path2D.Double knight() {
        Path2D.Double path = new Path2D.Double();
        path.moveTo(38, 78);
        path.quadTo(34, 62, 34, 52);
        path.quadTo(30, 48, 22, 46);
        path.quadTo(19, 45, 20, 42);
        path.quadTo(23, 38, 31, 36);
        path.quadTo(35, 34, 37, 30);
        path.lineTo(33, 12);
        path.quadTo(45, 16, 50, 26);
        path.quadTo(55, 18, 62, 14);
        path.quadTo(72, 26, 73, 42);
        path.quadTo(74, 60, 66, 78);
        path.closePath();
        return path;
    }

    private static Path2D.Double queenBody() {
        Path2D.Double path = new Path2D.Double();
        path.moveTo(34, 78);
        path.quadTo(38, 60, 42, 46);
        path.lineTo(58, 46);
        path.quadTo(62, 60, 66, 78);
        path.closePath();
        return path;
    }

    /** The crown of the queen with its five points. */
    private static Path2D.Double crown() {
        Path2D.Double path = new Path2D.Double();
        path.moveTo(28, 34);
        path.lineTo(24, 14);
        path.quadTo(30, 20, 36, 26);
        path.lineTo(42, 6);
        path.quadTo(50, 16, 58, 6);
        path.lineTo(64, 26);
        path.quadTo(70, 20, 76, 14);
        path.lineTo(72, 34);
        path.closePath();
        return path;
    }

    private static Path2D.Double kingBody() {
        Path2D.Double path = new Path2D.Double();
        path.moveTo(34, 78);
        path.quadTo(38, 58, 40, 44);
        path.lineTo(60, 44);
        path.quadTo(62, 58, 66, 78);
        path.closePath();
        return path;
    }

    /** The outline of a group of shapes. */
    private static Path2D.Double toPath(Area area) {
        Path2D.Double path = new Path2D.Double();
        PathIterator iterator = area.getPathIterator(null);
        double[] coordinates = new double[6];
        while (!iterator.isDone()) {
            int type = iterator.currentSegment(coordinates);
            switch (type) {
                case PathIterator.SEG_MOVETO:
                    path.moveTo(coordinates[0], coordinates[1]);
                    break;
                case PathIterator.SEG_LINETO:
                    path.lineTo(coordinates[0], coordinates[1]);
                    break;
                case PathIterator.SEG_QUADTO:
                    path.quadTo(coordinates[0], coordinates[1], coordinates[2], coordinates[3]);
                    break;
                case PathIterator.SEG_CUBICTO:
                    path.curveTo(coordinates[0], coordinates[1], coordinates[2], coordinates[3],
                            coordinates[4], coordinates[5]);
                    break;
                case PathIterator.SEG_CLOSE:
                    path.closePath();
                    break;
                default:
                    break;
            }
            iterator.next();
        }
        return path;
    }
}
