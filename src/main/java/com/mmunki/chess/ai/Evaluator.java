package com.mmunki.chess.ai;

import com.mmunki.chess.core.Board;
import com.mmunki.chess.core.Color;
import com.mmunki.chess.core.Piece;
import com.mmunki.chess.core.Square;

/**
 * The evaluation of a position.
 *
 * <p>The score is a number of centipawns for the side to move.  It is the sum
 * of the material, the place of the pieces on the board, the pawn structure,
 * the mobility of the pieces and the safety of the king.  The middle game and
 * the end game are weighed separately and mixed, so that the king counts less
 * when the queens are off.
 */
public final class Evaluator {

    private static final int[] MG_PAWN = {
            0, 0, 0, 0, 0, 0, 0, 0,
            50, 50, 50, 50, 50, 50, 50, 50,
            10, 10, 20, 30, 30, 20, 10, 10,
            5, 5, 10, 25, 25, 10, 5, 5,
            0, 0, 0, 20, 20, 0, 0, 0,
            5, -5, -10, 0, 0, -10, -5, 5,
            5, 10, 10, -20, -20, 10, 10, 5,
            0, 0, 0, 0, 0, 0, 0, 0};

    private static final int[] MG_KNIGHT = {
            -30, -25, -20, -20, -20, -20, -25, -30,
            -30, -15, 0, 5, 5, 0, -15, -30,
            -30, 0, 10, 15, 15, 10, 0, -30,
            -30, 5, 15, 20, 20, 15, 5, -30,
            -30, 0, 15, 20, 20, 15, 0, -30,
            -30, 5, 10, 15, 15, 10, 5, -30,
            -40, -20, 0, 5, 5, 0, -20, -40,
            -50, -40, -30, -30, -30, -30, -40, -50};

    private static final int[] MG_BISHOP = {
            -20, -10, -10, -10, -10, -10, -10, -20,
            -10, 0, 0, 0, 0, 0, 0, -10,
            -10, 0, 5, 10, 10, 5, 0, -10,
            -10, 5, 5, 10, 10, 5, 5, -10,
            -10, 0, 10, 10, 10, 10, 0, -10,
            -10, 10, 10, 10, 10, 10, 10, -10,
            -10, 5, 0, 0, 0, 0, 5, -10,
            -20, -10, -10, -10, -10, -10, -10, -20};

    private static final int[] MG_ROOK = {
            0, 0, 0, 0, 0, 0, 0, 0,
            5, 10, 10, 10, 10, 10, 10, 5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            0, 0, 0, 5, 5, 0, 0, 0};

    private static final int[] MG_QUEEN = {
            -20, -10, -10, -5, -5, -10, -10, -20,
            -10, 0, 0, 0, 0, 0, 0, -10,
            -10, 0, 5, 5, 5, 5, 0, -10,
            -5, 0, 5, 5, 5, 5, 0, -5,
            0, 0, 5, 5, 5, 5, 0, -5,
            -10, 5, 5, 5, 5, 5, 0, -10,
            -10, 0, 5, 0, 0, 0, 0, -10,
            -20, -10, -10, -5, -5, -10, -10, -20};

    private static final int[] MG_KING = {
            -30, -40, -40, -50, -50, -40, -40, -30,
            -30, -40, -40, -50, -50, -40, -40, -30,
            -30, -40, -40, -50, -50, -40, -40, -30,
            -30, -40, -40, -50, -50, -40, -40, -30,
            -20, -30, -30, -40, -40, -30, -30, -20,
            -10, -20, -20, -20, -20, -20, -20, -10,
            20, 20, 0, 0, 0, 0, 20, 20,
            20, 30, 10, 0, 0, 10, 30, 20};

    private static final int[] EG_PAWN = {
            0, 0, 0, 0, 0, 0, 0, 0,
            90, 90, 90, 90, 90, 90, 90, 90,
            60, 60, 60, 60, 60, 60, 60, 60,
            35, 35, 35, 35, 35, 35, 35, 35,
            20, 20, 20, 20, 20, 20, 20, 20,
            10, 10, 10, 10, 10, 10, 10, 10,
            5, 5, 5, 5, 5, 5, 5, 5,
            0, 0, 0, 0, 0, 0, 0, 0};

    private static final int[] EG_KNIGHT = {
            -30, -25, -20, -20, -20, -20, -25, -30,
            -30, -15, 0, 5, 5, 0, -15, -30,
            -30, 0, 10, 15, 15, 10, 0, -30,
            -30, 5, 15, 20, 20, 15, 5, -30,
            -30, 0, 15, 20, 20, 15, 0, -30,
            -30, 5, 10, 15, 15, 10, 5, -30,
            -40, -20, 0, 5, 5, 0, -20, -40,
            -50, -40, -30, -30, -30, -30, -40, -50};

    private static final int[] EG_BISHOP = {
            -20, -10, -10, -10, -10, -10, -10, -20,
            -10, 0, 0, 0, 0, 0, 0, -10,
            -10, 0, 5, 10, 10, 5, 0, -10,
            -10, 5, 5, 10, 10, 5, 5, -10,
            -10, 0, 10, 10, 10, 10, 0, -10,
            -10, 10, 10, 10, 10, 10, 10, -10,
            -10, 5, 0, 0, 0, 0, 5, -10,
            -20, -10, -10, -10, -10, -10, -10, -20};

    private static final int[] EG_ROOK = {
            0, 0, 0, 0, 0, 0, 0, 0,
            5, 10, 10, 10, 10, 10, 10, 5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            0, 0, 0, 5, 5, 0, 0, 0};

    private static final int[] EG_QUEEN = {
            -20, -10, -10, -5, -5, -10, -10, -20,
            -10, 0, 0, 0, 0, 0, 0, -10,
            -10, 0, 5, 5, 5, 5, 0, -10,
            -5, 0, 5, 5, 5, 5, 0, -5,
            0, 0, 5, 5, 5, 5, 0, -5,
            -10, 5, 5, 5, 5, 5, 0, -10,
            -10, 0, 5, 0, 0, 0, 0, -10,
            -20, -10, -10, -5, -5, -10, -10, -20};

    private static final int[] EG_KING = {
            -50, -40, -30, -20, -20, -30, -40, -50,
            -30, -20, -10, 0, 0, -10, -20, -30,
            -30, -10, 20, 30, 30, 20, -10, -30,
            -30, -10, 30, 40, 40, 30, -10, -30,
            -30, -10, 30, 40, 40, 30, -10, -30,
            -30, -10, 20, 30, 30, 20, -10, -30,
            -30, -30, 0, 0, 0, 0, -30, -30,
            -50, -30, -30, -30, -30, -30, -30, -50};

    private static final int[] PASSED_PAWN = {0, 5, 15, 35, 70, 120, 180, 0};

    private Evaluator() {
    }

    /** The value of the position for the side to move, in centipawns. */
    public static int evaluate(Board board) {
        int mg = 0;
        int eg = 0;
        int phase = 0;

        int whiteBishops = 0;
        int blackBishops = 0;
        int[] whitePawnFiles = new int[8];
        int[] blackPawnFiles = new int[8];
        int[] pawnSquares = new int[16];
        int pawnCount = 0;

        // one walk over the board: the material, the place of the pieces and
        // the number of squares they can reach
        for (int s = 0; s < 64; s++) {
            Piece piece = board.pieceAt(s);
            if (piece == null) {
                continue;
            }
            Color color = board.colorAt(s);
            int sign = color == Color.WHITE ? 1 : -1;
            int file = Square.file(s);
            int rank = Square.rank(s);

            mg += sign * (piece.value() + table(mgTable(piece), s, color));
            eg += sign * (piece.value() + table(egTable(piece), s, color));

            switch (piece) {
                case PAWN:
                    if (color == Color.WHITE) {
                        whitePawnFiles[file]++;
                    } else {
                        blackPawnFiles[file]++;
                    }
                    if (pawnCount < pawnSquares.length) {
                        pawnSquares[pawnCount++] = s;
                    }
                    break;
                case BISHOP:
                    if (color == Color.WHITE) {
                        whiteBishops++;
                    } else {
                        blackBishops++;
                    }
                    break;
                default:
                    break;
            }
            if (piece != Piece.PAWN && piece != Piece.KING) {
                phase += piece == Piece.QUEEN ? 8 : piece == Piece.ROOK ? 4
                        : piece == Piece.BISHOP || piece == Piece.KNIGHT ? 2 : 0;
            }
            if (piece != Piece.PAWN && piece != Piece.KING) {
                int reach = reach(board, s, piece, color);
                int weight = piece == Piece.KNIGHT ? 3 : piece == Piece.BISHOP ? 3
                        : piece == Piece.ROOK ? 2 : 1;
                mg += sign * weight * (reach - 4);
                eg += sign * weight * (reach - 4) / 2;
            }
        }
        phase = Math.min(24, phase);

        // the two bishops are worth more than one
        if (whiteBishops >= 2) {
            mg += 30;
            eg += 45;
        }
        if (blackBishops >= 2) {
            mg -= 30;
            eg -= 45;
        }

        // doubled and isolated pawns
        for (int f = 0; f < 8; f++) {
            if (whitePawnFiles[f] > 1) {
                mg -= 12 * (whitePawnFiles[f] - 1);
                eg -= 22 * (whitePawnFiles[f] - 1);
            }
            if (blackPawnFiles[f] > 1) {
                mg += 12 * (blackPawnFiles[f] - 1);
                eg += 22 * (blackPawnFiles[f] - 1);
            }
            boolean isolated = (f == 0 || whitePawnFiles[f - 1] == 0)
                    && (f == 7 || whitePawnFiles[f + 1] == 0);
            if (isolated && whitePawnFiles[f] > 0) {
                mg -= 14;
                eg -= 16;
            }
            isolated = (f == 0 || blackPawnFiles[f - 1] == 0)
                    && (f == 7 || blackPawnFiles[f + 1] == 0);
            if (isolated && blackPawnFiles[f] > 0) {
                mg += 14;
                eg += 16;
            }
        }

        // the pawns that passed all the others
        for (int i = 0; i < pawnCount; i++) {
            int s = pawnSquares[i];
            Color color = board.colorAt(s);
            int file = Square.file(s);
            int rank = Square.rank(s);
            boolean passed = true;
            for (int df = -1; df <= 1 && passed; df++) {
                int f = file + df;
                if (f < 0 || f > 7) {
                    continue;
                }
                for (int r = 0; r < 8; r++) {
                    int sq = Square.of(f, r);
                    if (board.pieceAt(sq) != Piece.PAWN) {
                        continue;
                    }
                    if (color == Color.WHITE && r > rank) {
                        passed = false;
                        break;
                    }
                    if (color == Color.BLACK && r < rank) {
                        passed = false;
                        break;
                    }
                }
            }
            if (passed) {
                int bonus = PASSED_PAWN[color == Color.WHITE ? rank : 7 - rank];
                if (color == Color.WHITE) {
                    mg += bonus / 2;
                    eg += bonus;
                } else {
                    mg -= bonus / 2;
                    eg -= bonus;
                }
            }
            // the pawns in the centre hold the centre together
            if (file == 3 || file == 4) {
                int sign = color == Color.WHITE ? 1 : -1;
                int distance = color == Color.WHITE ? rank : 7 - rank;
                int bonus = distance <= 3 ? 12 : 0;
                mg += sign * bonus;
                eg += sign * bonus;
            }
        }

        // the rooks like the open files
        for (int s = 0; s < 64; s++) {
            if (board.pieceAt(s) != Piece.ROOK) {
                continue;
            }
            int file = Square.file(s);
            int sign = board.colorAt(s) == Color.WHITE ? 1 : -1;
            boolean ownPawn = false;
            boolean otherPawn = false;
            for (int r = 0; r < 8; r++) {
                int sq = Square.of(file, r);
                if (board.pieceAt(sq) != Piece.PAWN) {
                    continue;
                }
                if (board.colorAt(sq) == board.colorAt(s)) {
                    ownPawn = true;
                } else {
                    otherPawn = true;
                }
            }
            if (!ownPawn) {
                mg += sign * (otherPawn ? 12 : 25);
            }
        }

        // the king behind his pawns is safe, an open file in front of him is not
        for (Color color : Color.values()) {
            int king = board.kingSquare(color);
            if (king < 0) {
                continue;
            }
            int sign = color == Color.WHITE ? 1 : -1;
            int file = Square.file(king);
            int rank = Square.rank(king);
            int shield = 0;
            for (int df = -1; df <= 1; df++) {
                int f = file + df;
                if (f < 0 || f > 7) {
                    continue;
                }
                int r = color == Color.WHITE ? rank + 1 : rank - 1;
                if (r < 0 || r > 7) {
                    continue;
                }
                int sq = Square.of(f, r);
                if (board.pieceAt(sq) == Piece.PAWN && board.colorAt(sq) == color) {
                    shield += 8;
                    continue;
                }
                boolean open = true;
                for (int rr = 0; rr < 8; rr++) {
                    if (board.pieceAt(Square.of(f, rr)) == Piece.PAWN) {
                        open = false;
                        break;
                    }
                }
                shield += open ? -22 : -4;
            }
            mg += sign * shield;

            // in the end game the king goes to the centre
            int centre = 7 - (Math.abs(3 - file) + Math.abs(3 - rank));
            eg += sign * centre * 6;
        }

        int score = (mg * phase + eg * (24 - phase)) / 24;
        // the side to move has the advantage of the first move
        score += 10;
        return board.sideToMove() == Color.WHITE ? score : -score;
    }

    /** The squares a piece can reach. */
    private static int reach(Board board, int square, Piece piece, Color color) {
        int count = 0;
        int file = Square.file(square);
        int rank = Square.rank(square);
        switch (piece) {
            case KNIGHT: {
                int[][] steps = {{1, 2}, {2, 1}, {2, -1}, {1, -2}, {-1, -2}, {-2, -1}, {-2, 1}, {-1, 2}};
                for (int[] step : steps) {
                    if (Square.onBoard(file + step[0], rank + step[1])) {
                        count++;
                    }
                }
                break;
            }
            case BISHOP:
                count += reach(board, file, rank, DIAGONALS, color);
                break;
            case ROOK:
                count += reach(board, file, rank, STRAIGHT, color);
                break;
            case QUEEN:
                count += reach(board, file, rank, ALL, color);
                break;
            default:
                break;
        }
        return count;
    }

    private static final int[][] DIAGONALS = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
    private static final int[][] STRAIGHT = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    private static final int[][] ALL = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1},
            {1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    /** The squares a piece reaches along the given directions. */
    private static int reach(Board board, int file, int rank, int[][] directions, Color color) {
        int count = 0;
        for (int[] direction : directions) {
            int f = file + direction[0];
            int r = rank + direction[1];
            while (f >= 0 && f < 8 && r >= 0 && r < 8) {
                int square = r * 8 + f;
                if (board.isOccupied(square)) {
                    if (board.colorAt(square) != color) {
                        count++;
                    }
                    break;
                }
                count++;
                f += direction[0];
                r += direction[1];
            }
        }
        return count;
    }

    private static int[] mgTable(Piece piece) {
        switch (piece) {
            case PAWN:
                return MG_PAWN;
            case KNIGHT:
                return MG_KNIGHT;
            case BISHOP:
                return MG_BISHOP;
            case ROOK:
                return MG_ROOK;
            case QUEEN:
                return MG_QUEEN;
            case KING:
            default:
                return MG_KING;
        }
    }

    private static int[] egTable(Piece piece) {
        switch (piece) {
            case PAWN:
                return EG_PAWN;
            case KNIGHT:
                return EG_KNIGHT;
            case BISHOP:
                return EG_BISHOP;
            case ROOK:
                return EG_ROOK;
            case QUEEN:
                return EG_QUEEN;
            case KING:
            default:
                return EG_KING;
        }
    }

    /** The place of a piece on the board, in the point of view of its colour. */
    private static int table(int[] table, int square, Color color) {
        int file = Square.file(square);
        int rank = Square.rank(square);
        int index = color == Color.WHITE ? (7 - rank) * 8 + file : rank * 8 + file;
        return table[index];
    }
}
