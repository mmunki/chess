package com.mmunki.chess.core;

import java.util.Random;

/**
 * Zobrist hashing.
 *
 * <p>Every position is reduced to a single 64 bit number, so that the search
 * can recognise a position it has seen before.
 */
public final class Zobrist {

    private static final long[][][] PIECES = new long[6][2][64];
    private static final long[] CASTLING = new long[16];
    private static final long[] EP_FILE = new long[8];
    private static final long SIDE;

    static {
        Random random = new Random(0x5EEDCAFE);
        for (int p = 0; p < 6; p++) {
            for (int c = 0; c < 2; c++) {
                for (int s = 0; s < 64; s++) {
                    PIECES[p][c][s] = random.nextLong();
                }
            }
        }
        for (int i = 0; i < CASTLING.length; i++) {
            CASTLING[i] = random.nextLong();
        }
        for (int i = 0; i < EP_FILE.length; i++) {
            EP_FILE[i] = random.nextLong();
        }
        SIDE = random.nextLong();
    }

    private Zobrist() {
    }

    public static long piece(Piece piece, Color color, int square) {
        return PIECES[piece.ordinal()][color == Color.WHITE ? 0 : 1][square];
    }

    public static long castling(int rights) {
        long key = 0;
        for (int i = 0; i < 4; i++) {
            if ((rights & (1 << i)) != 0) {
                key ^= CASTLING[i];
            }
        }
        return key;
    }

    public static long enPassant(int square) {
        return square < 0 ? 0 : EP_FILE[Square.file(square)];
    }

    public static long side() {
        return SIDE;
    }
}
