package com.mmunki.chess.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates the moves of a position.
 *
 * <p>The moves are generated in two steps.  First the moves that only follow
 * the movement rules of the pieces are generated, then every one of them is
 * played on a copy of the position and dropped when it leaves the king of the
 * mover in check.  That way the rules of check, mate and stalemate come out
 * of the same code.
 */
public final class MoveGenerator {

    private static final int[] KNIGHT_TARGETS = {
            1, 2, 2, 1, 2, -1, 1, -2, -1, -2, -2, -1, -2, 1, -1, 2};

    private static final int[] DIAGONALS = {1, 1, 1, -1, -1, 1, -1, -1};
    private static final int[] STRAIGHT = {1, 0, -1, 0, 0, 1, 0, -1};
    private static final int[] KING_TARGETS = {1, 1, 1, 0, 1, -1, 0, 1, 0, -1, -1, 1, -1, 0, -1, -1};

    private MoveGenerator() {
    }

    /** All moves that are legal in the position. */
    public static List<Move> legalMoves(Board board) {
        List<Move> legal = new ArrayList<>();
        for (Move move : pseudoLegalMoves(board)) {
            Board.Undo undo = board.makeMove(move);
            int king = board.kingSquare(undo.sideToMove);
            boolean safe = king < 0 || !board.isAttacked(king, undo.sideToMove.opposite());
            board.undoMove(undo);
            if (safe) {
                legal.add(move);
            }
        }
        return legal;
    }

    /** All moves that only follow the movement rules of the pieces. */
    public static List<Move> pseudoLegalMoves(Board board) {
        List<Move> moves = new ArrayList<>();
        Color us = board.sideToMove();
        Color them = us.opposite();

        for (int from = 0; from < 64; from++) {
            Piece piece = board.pieceAt(from);
            if (piece == null || board.colorAt(from) != us) {
                continue;
            }
            switch (piece) {
                case PAWN:
                    pawnMoves(board, moves, from, us, them);
                    break;
                case KNIGHT:
                    for (int i = 0; i < KNIGHT_TARGETS.length; i += 2) {
                        int to = target(from, KNIGHT_TARGETS[i], KNIGHT_TARGETS[i + 1]);
                        if (to < 0 || board.isOccupiedBy(to, us)) {
                            continue;
                        }
                        moves.add(new Move(from, to));
                    }
                    break;
                case BISHOP:
                case ROOK:
                case QUEEN:
                    slide(board, moves, from, us, piece == Piece.BISHOP ? DIAGONALS
                            : piece == Piece.ROOK ? STRAIGHT : ALL_DIRECTIONS);
                    break;
                case KING:
                    for (int i = 0; i < KING_TARGETS.length; i += 2) {
                        int to = target(from, KING_TARGETS[i], KING_TARGETS[i + 1]);
                        if (to < 0 || board.isOccupiedBy(to, us)) {
                            continue;
                        }
                        moves.add(new Move(from, to));
                    }
                    break;
                default:
                    break;
            }
        }
        castleMoves(board, moves, us, them);
        return moves;
    }

    private static final int[] ALL_DIRECTIONS = {1, 1, 1, 0, 1, -1, 0, 1, 0, -1, -1, 1, -1, 0, -1, -1};

    private static void pawnMoves(Board board, List<Move> moves, int from, Color us, Color them) {
        int file = Square.file(from);
        int rank = Square.rank(from);
        int direction = us == Color.WHITE ? 1 : -1;
        int startRank = us == Color.WHITE ? 1 : 6;
        int lastRank = us == Color.WHITE ? 7 : 0;

        int nextRank = rank + direction;
        if (nextRank >= 0 && nextRank < 8) {
            int straight = Square.of(file, nextRank);
            if (!board.isOccupied(straight)) {
                addPawnMove(moves, from, straight, lastRank);
                int doubleRank = rank + 2 * direction;
                if (rank == startRank && doubleRank >= 0 && doubleRank < 8) {
                    int doubleSquare = Square.of(file, doubleRank);
                    if (!board.isOccupied(doubleSquare)) {
                        moves.add(new Move(from, doubleSquare));
                    }
                }
            }
            // captures
            for (int df : new int[]{-1, 1}) {
                int f = file + df;
                if (f < 0 || f > 7) {
                    continue;
                }
                int to = Square.of(f, nextRank);
                if (board.isOccupiedBy(to, them)) {
                    addPawnMove(moves, from, to, lastRank);
                } else if (to == board.enPassantSquare()) {
                    moves.add(new Move(from, to, null, false, true));
                }
            }
        }
    }

    private static void addPawnMove(List<Move> moves, int from, int to, int lastRank) {
        if (Square.rank(to) == lastRank) {
            for (Piece promotion : new Piece[]{Piece.QUEEN, Piece.ROOK, Piece.BISHOP, Piece.KNIGHT}) {
                moves.add(new Move(from, to, promotion, false, false));
            }
        } else {
            moves.add(new Move(from, to));
        }
    }

    private static void slide(Board board, List<Move> moves, int from, Color us, int[] directions) {
        int file = Square.file(from);
        int rank = Square.rank(from);
        for (int i = 0; i < directions.length; i += 2) {
            int df = directions[i];
            int dr = directions[i + 1];
            int f = file + df;
            int r = rank + dr;
            while (Square.onBoard(f, r)) {
                int to = Square.of(f, r);
                if (board.isOccupiedBy(to, us)) {
                    break;
                }
                moves.add(new Move(from, to));
                if (board.isOccupied(to)) {
                    break;
                }
                f += df;
                r += dr;
            }
        }
    }

    private static void castleMoves(Board board, List<Move> moves, Color us, Color them) {
        int kingSquare = us == Color.WHITE ? 4 : 60;
        int rookKing = us == Color.WHITE ? 7 : 63;
        int rookQueen = us == Color.WHITE ? 0 : 56;
        int rightKing = us == Color.WHITE ? Board.CASTLE_WK : Board.CASTLE_BK;
        int rightQueen = us == Color.WHITE ? Board.CASTLE_WQ : Board.CASTLE_BQ;

        if (board.pieceAt(kingSquare) != Piece.KING || board.colorAt(kingSquare) != us) {
            return;
        }
        if (board.isAttacked(kingSquare, them)) {
            return;
        }

        if (board.hasCastlingRight(rightKing) && board.pieceAt(rookKing) == Piece.ROOK
                && board.colorAt(rookKing) == us) {
            int empty1 = kingSquare + 1;
            int empty2 = kingSquare + 2;
            if (!board.isOccupied(empty1) && !board.isOccupied(empty2)
                    && !board.isAttacked(empty1, them) && !board.isAttacked(empty2, them)) {
                moves.add(new Move(kingSquare, empty2, null, true, false));
            }
        }
        if (board.hasCastlingRight(rightQueen) && board.pieceAt(rookQueen) == Piece.ROOK
                && board.colorAt(rookQueen) == us) {
            int empty1 = kingSquare - 1;
            int empty2 = kingSquare - 2;
            int empty3 = kingSquare - 3;
            if (!board.isOccupied(empty1) && !board.isOccupied(empty2) && !board.isOccupied(empty3)
                    && !board.isAttacked(empty1, them) && !board.isAttacked(empty2, them)) {
                moves.add(new Move(kingSquare, empty2, null, true, false));
            }
        }
    }

    private static int target(int from, int df, int dr) {
        int f = Square.file(from) + df;
        int r = Square.rank(from) + dr;
        if (!Square.onBoard(f, r)) {
            return -1;
        }
        return Square.of(f, r);
    }
}
