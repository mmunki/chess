package com.mmunki.chess.core;

/**
 * A square of the board.
 *
 * <p>The board is stored as an array of 64 squares.  The index of a square is
 * {@code rank * 8 + file}, the file is 0 for the a-file and 7 for the h-file,
 * the rank is 0 for the first rank and 7 for the eighth rank.  The white
 * pieces start at the bottom, so a1 is 0 and h8 is 63.
 */
public final class Square {

    private Square() {
    }

    public static boolean onBoard(int file, int rank) {
        return file >= 0 && file < 8 && rank >= 0 && rank < 8;
    }

    public static int of(int file, int rank) {
        if (!onBoard(file, rank)) {
            throw new IllegalArgumentException("square off the board: " + file + "/" + rank);
        }
        return rank * 8 + file;
    }

    public static int file(int square) {
        return square & 7;
    }

    /** 0 is the first rank, 7 the eighth. */
    public static int rank(int square) {
        return square >>> 3;
    }

    /** The name of the square, for example "e4". */
    public static String name(int square) {
        return "" + (char) ('a' + file(square)) + (rank(square) + 1);
    }

    /** Reads a square name, for example "e4".  Returns -1 when it is not one. */
    public static int fromName(String name) {
        if (name == null || name.length() < 2) {
            return -1;
        }
        int file = Character.toLowerCase(name.charAt(0)) - 'a';
        int rank = Character.digit(name.charAt(1), 10) - 1;
        if (!onBoard(file, rank)) {
            return -1;
        }
        return of(file, rank);
    }

    /** True when the square is on the edge of the board. */
    public static boolean isEdge(int square) {
        int f = file(square);
        int r = rank(square);
        return f == 0 || f == 7 || r == 0 || r == 7;
    }
}
