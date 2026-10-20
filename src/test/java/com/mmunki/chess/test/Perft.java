package com.mmunki.chess.test;

import com.mmunki.chess.core.Board;
import com.mmunki.chess.core.Move;
import com.mmunki.chess.core.MoveGenerator;

import java.util.List;

/**
 * Perft, the count of the legal move trees of a position.
 *
 * <p>Perft is the classical test of a chess engine: it counts every legal move
 * of every position that can be reached in a given number of moves.  A single
 * wrong rule in the move generator changes the numbers.
 */
public final class Perft {

    private Perft() {
    }

    /** Counts the leaves of the move tree. */
    public static long perft(Board board, int depth) {
        if (depth == 0) {
            return 1;
        }
        List<Move> moves = MoveGenerator.legalMoves(board);
        if (depth == 1) {
            return moves.size();
        }
        long nodes = 0;
        for (Move move : moves) {
            Board.Undo undo = board.makeMove(move);
            nodes += perft(board, depth - 1);
            board.undoMove(undo);
        }
        return nodes;
    }

    /** Counts the leaves and divides them by the number of legal moves. */
    public static void divide(Board board, int depth) {
        List<Move> moves = MoveGenerator.legalMoves(board);
        for (Move move : moves) {
            Board.Undo undo = board.makeMove(move);
            long count = perft(board, depth - 1);
            board.undoMove(undo);
            System.out.println(move + ": " + count);
        }
        System.out.println("nodes: " + perft(board, depth));
    }
}
