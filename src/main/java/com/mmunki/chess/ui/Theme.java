package com.mmunki.chess.ui;

import java.awt.Color;
import java.awt.Font;

/**
 * The colours and the fonts of the game.
 *
 * <p>The palette is the dark one of chess.com: a warm dark grey around the
 * board, a green board with pale squares and a green accent for the buttons.
 */
public final class Theme {

    public static final Color BACKGROUND = new Color(0x26, 0x24, 0x21);
    public static final Color PANEL = new Color(0x30, 0x2E, 0x2C);
    public static final Color PANEL_LIGHT = new Color(0x3B, 0x39, 0x37);
    public static final Color BORDER = new Color(0x43, 0x40, 0x3D);

    public static final Color TEXT = new Color(0xF2, 0xF2, 0xF0);
    public static final Color TEXT_MUTED = new Color(0x9B, 0x99, 0x95);

    public static final Color ACCENT = new Color(0x81, 0xB6, 0x4C);
    public static final Color ACCENT_DARK = new Color(0x63, 0x8F, 0x38);
    public static final Color DANGER = new Color(0xE2, 0x56, 0x4C);
    public static final Color BLUE = new Color(0x5C, 0x9D, 0xD6);

    public static final Color BOARD_LIGHT = new Color(0xEE, 0xEE, 0xD2);
    public static final Color BOARD_DARK = new Color(0x76, 0x92, 0x56);
    public static final Color HIGHLIGHT_LIGHT = new Color(0xF7, 0xEC, 0x74);
    public static final Color HIGHLIGHT_DARK = new Color(0xD6, 0xD6, 0x4A);
    public static final Color CHECK = new Color(0xE2, 0x56, 0x4C);
    public static final Color MOVE_DOT = new Color(0x40, 0x40, 0x40, 120);
    public static final Color CAPTURE_RING = new Color(0x40, 0x40, 0x40, 110);

    public static final Color PIECE_WHITE = new Color(0xFA, 0xFA, 0xF7);
    public static final Color PIECE_WHITE_OUTLINE = new Color(0x3A, 0x38, 0x36);
    public static final Color PIECE_BLACK = new Color(0x4C, 0x4C, 0x4C);
    public static final Color PIECE_BLACK_OUTLINE = new Color(0x12, 0x12, 0x12);

    public static final String FONT = "SansSerif";

    private Theme() {
    }

    public static Font font(int style, float size) {
        return new Font(FONT, style, Math.round(size));
    }

    /** The colour of a square of the board. */
    public static Color squareColor(int file, int rank, boolean light) {
        return light ? BOARD_LIGHT : BOARD_DARK;
    }

    /** The colour a square gets when the move that was played went over it. */
    public static Color highlight(Color base) {
        return base.equals(BOARD_LIGHT) ? HIGHLIGHT_LIGHT : HIGHLIGHT_DARK;
    }
}
