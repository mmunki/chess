package com.mmunki.chess.android;

/**
 * The colours of the game.
 *
 * <p>The palette is the dark one of chess.com: a warm dark grey around the
 * board, a green board with pale squares and a green accent for the buttons.
 * It is the same palette as the game on the computer.
 */
public final class Theme {

    public static final int BACKGROUND = 0xFF262421;
    public static final int PANEL = 0xFF302E2C;
    public static final int PANEL_LIGHT = 0xFF3B3937;
    public static final int BORDER = 0xFF43403D;

    public static final int TEXT = 0xFFF2F2F0;
    public static final int TEXT_MUTED = 0xFF9B9995;

    public static final int ACCENT = 0xFF81B64C;
    public static final int ACCENT_DARK = 0xFF638F38;
    public static final int DANGER = 0xFFE2564C;
    public static final int BLUE = 0xFF5C9DD6;

    public static final int BOARD_LIGHT = 0xFFEEEED2;
    public static final int BOARD_DARK = 0xFF769256;
    public static final int HIGHLIGHT_LIGHT = 0xFFF7EC74;
    public static final int HIGHLIGHT_DARK = 0xFFD6D64A;
    public static final int CHECK = 0xFFE2564C;
    public static final int MOVE_DOT = 0x78404040;
    public static final int CAPTURE_RING = 0x6E404040;

    public static final int PIECE_WHITE = 0xFFFAFAF7;
    public static final int PIECE_WHITE_OUTLINE = 0xFF3A3836;
    public static final int PIECE_BLACK = 0xFF4C4C4C;
    public static final int PIECE_BLACK_OUTLINE = 0xFF121212;

    private Theme() {
    }

    /** The colour of a square of the board. */
    public static int squareColor(boolean light) {
        return light ? BOARD_LIGHT : BOARD_DARK;
    }

    /** The colour a square gets when the move that was played went over it. */
    public static int highlight(int base) {
        return base == BOARD_LIGHT ? HIGHLIGHT_LIGHT : HIGHLIGHT_DARK;
    }
}
