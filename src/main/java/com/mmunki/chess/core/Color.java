package com.mmunki.chess.core;

/** The two sides of a chess game. */
public enum Color {
    WHITE,
    BLACK;

    public Color opposite() {
        return this == WHITE ? BLACK : WHITE;
    }

    /** "w" or "b", the way a FEN spells it. */
    public String fen() {
        return this == WHITE ? "w" : "b";
    }

    public static Color fromFen(String s) {
        return "w".equalsIgnoreCase(s) ? WHITE : BLACK;
    }
}
