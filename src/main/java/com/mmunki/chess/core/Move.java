package com.mmunki.chess.core;

/** One move of a chess game. */
public final class Move {

    public final int from;
    public final int to;
    /** The piece the pawn becomes, null when the move is not a promotion. */
    public final Piece promotion;
    public final boolean castle;
    public final boolean enPassant;

    public Move(int from, int to) {
        this(from, to, null, false, false);
    }

    public Move(int from, int to, Piece promotion, boolean castle, boolean enPassant) {
        this.from = from;
        this.to = to;
        this.promotion = promotion;
        this.castle = castle;
        this.enPassant = enPassant;
    }

    /** The square a pawn is captured on, the square behind the target for en passant. */
    public int captureSquare(Color mover) {
        if (!enPassant) {
            return to;
        }
        return mover == Color.WHITE ? to - 8 : to + 8;
    }

    /** A stable key, used to look moves up in a hash map. */
    public int key() {
        return (from << 10) | (to << 2) | (promotion == null ? 0 : promotion.ordinal() + 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Move)) {
            return false;
        }
        Move m = (Move) o;
        return from == m.from && to == m.to && promotion == m.promotion
                && castle == m.castle && enPassant == m.enPassant;
    }

    @Override
    public int hashCode() {
        return key();
    }

    @Override
    public String toString() {
        return Square.name(from) + Square.name(to) + (promotion == null ? "" : "=" + promotion.letter());
    }
}
