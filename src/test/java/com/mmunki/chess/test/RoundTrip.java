package com.mmunki.chess.test;

import com.mmunki.chess.core.Board;
import com.mmunki.chess.core.Move;
import com.mmunki.chess.core.MoveGenerator;

import java.util.List;
import java.util.Random;

/**
 * Walks through random games and checks that every move can be taken back
 * without leaving a different position behind.
 */
public final class RoundTrip {

    private RoundTrip() {
    }

    public static int check(String fen, int games, int maxPlies, long seed) {
        Random random = new Random(seed);
        int failures = 0;
        for (int game = 0; game < games; game++) {
            Board board = new Board(fen);
            for (int ply = 0; ply < maxPlies; ply++) {
                List<Move> moves = MoveGenerator.legalMoves(board);
                if (moves.isEmpty()) {
                    break;
                }
                Move move = moves.get(random.nextInt(moves.size()));
                String before = board.fen();
                long hashBefore = board.hash();
                Board.Undo undo = board.makeMove(move);
                board.undoMove(undo);
                if (!before.equals(board.fen()) || hashBefore != board.hash()
                        || board.hash() != board.recalculatedHash()) {
                    failures++;
                    System.out.println("FAILED " + before + " after " + move + " -> " + board.fen());
                    return failures;
                }
                board.makeMove(move);
            }
        }
        return failures;
    }
}
