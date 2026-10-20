package com.mmunki.chess.test;

import com.mmunki.chess.core.Board;

/** Runs the checks of the engine and prints what it found. */
public final class EngineCheck {

    private static int failures = 0;

    private EngineCheck() {
    }

    public static void main(String[] args) {
        perft("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
                new long[]{20, 400, 8902, 197281, 4865609});
        perft("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1",
                new long[]{48, 2039, 97862, 4085603});
        perft("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1",
                new long[]{14, 191, 2812, 43238, 674624});
        perft("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1",
                new long[]{6, 264, 9467, 422333});
        perft("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8",
                new long[]{44, 1486, 62379, 2103487});
        perft("r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10",
                new long[]{46, 2079, 89890, 3894594});

        fen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        fen("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1");
        fen("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 b - - 3 17");
        fen("rnbqkbnr/pp1ppppp/8/2p5/4P3/8/PPPP1PPP/RNBQKBNR w KQkq c6 0 2");

        System.out.println(failures == 0 ? "ALL ENGINE CHECKS PASSED" : failures + " CHECKS FAILED");
        if (failures > 0) {
            System.exit(1);
        }
    }

    private static void perft(String fen, long[] expected) {
        Board board = new Board(fen);
        for (int depth = 1; depth <= expected.length; depth++) {
            long nodes = Perft.perft(board, depth);
            boolean ok = nodes == expected[depth - 1];
            if (!ok) {
                failures++;
            }
            System.out.printf("perft %-70s depth %d: %-10d expected %-10d %s%n",
                    fen, depth, nodes, expected[depth - 1], ok ? "ok" : "FAILED");
        }
    }

    private static void fen(String fen) {
        Board board = new Board(fen);
        String back = board.fen();
        boolean ok = back.equals(fen);
        if (!ok) {
            failures++;
        }
        System.out.printf("fen  %-70s -> %-70s %s%n", fen, back, ok ? "ok" : "FAILED");
    }
}
