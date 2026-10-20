package com.mmunki.chess.core;

/** The six kinds of chess piece. */
public enum Piece {
    PAWN('P'),
    KNIGHT('N'),
    BISHOP('B'),
    ROOK('R'),
    QUEEN('Q'),
    KING('K');

    private static final Piece[] VALUES = values();

    private final char letter;

    Piece(char letter) {
        this.letter = letter;
    }

    /** The upper case letter of the piece, the way a FEN spells it. */
    public char letter() {
        return letter;
    }

    /** The letter of the piece in the FEN, lower case for black. */
    public char fen(Color color) {
        return color == Color.WHITE ? letter : Character.toLowerCase(letter);
    }

    public static Piece fromFen(char c) {
        char u = Character.toUpperCase(c);
        for (Piece p : VALUES) {
            if (p.letter == u) {
                return p;
            }
        }
        return null;
    }

    /** The value of the piece in centipawns, used by the evaluation. */
    public int value() {
        switch (this) {
            case PAWN:
                return 100;
            case KNIGHT:
                return 320;
            case BISHOP:
                return 335;
            case ROOK:
                return 500;
            case QUEEN:
                return 950;
            case KING:
            default:
                return 0;
        }
    }

    /** True for the pieces that slide over other pieces. */
    public boolean isSlider() {
        return this == BISHOP || this == ROOK || this == QUEEN;
    }
}
