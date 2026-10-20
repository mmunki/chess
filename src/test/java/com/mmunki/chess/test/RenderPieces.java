package com.mmunki.chess.test;

import com.mmunki.chess.core.Board;
import com.mmunki.chess.core.Piece;
import com.mmunki.chess.core.Square;
import com.mmunki.chess.ui.PieceRenderer;
import com.mmunki.chess.ui.Theme;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Draws the pieces into a picture, so that the shapes can be looked at.
 */
public final class RenderPieces {

    private RenderPieces() {
    }

    public static void main(String[] args) throws Exception {
        int size = 110;
        BufferedImage image = new BufferedImage(size * 6, size * 2, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new java.awt.Color(0xEE, 0xEE, 0xD2));
        g.fillRect(0, 0, image.getWidth(), image.getHeight());

        Piece[] pieces = {Piece.ROOK, Piece.KNIGHT, Piece.BISHOP, Piece.QUEEN, Piece.KING, Piece.PAWN};
        for (int i = 0; i < pieces.length; i++) {
            PieceRenderer.draw(g, pieces[i], com.mmunki.chess.core.Color.WHITE, i * size, 0, size);
            PieceRenderer.draw(g, pieces[i], com.mmunki.chess.core.Color.BLACK, i * size, size, size);
        }
        g.dispose();
        File out = new File(args.length > 0 ? args[0] : "pieces.png");
        ImageIO.write(image, "png", out);
        System.out.println("wrote " + out.getAbsolutePath());

        // the same pieces on a board
        Board board = new Board();
        BufferedImage boardImage = new BufferedImage(560, 560, BufferedImage.TYPE_INT_RGB);
        Graphics2D bg = boardImage.createGraphics();
        bg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (int rank = 0; rank < 8; rank++) {
            for (int column = 0; column < 8; column++) {
                bg.setColor((column + rank) % 2 == 0 ? Theme.BOARD_LIGHT : Theme.BOARD_DARK);
                bg.fillRect(column * 70, (7 - rank) * 70, 70, 70);
            }
        }
        for (int s = 0; s < 64; s++) {
            Piece piece = board.pieceAt(s);
            if (piece == null) {
                continue;
            }
            int column = Square.file(s);
            int rank = Square.rank(s);
            PieceRenderer.draw(bg, piece, board.colorAt(s), column * 70, (7 - rank) * 70, 70);
        }
        bg.dispose();
        File boardFile = new File(out.getParentFile(), "board.png");
        ImageIO.write(boardImage, "png", boardFile);
        System.out.println("wrote " + boardFile.getAbsolutePath());
    }
}
