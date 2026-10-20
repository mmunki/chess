package com.mmunki.chess.test;

import com.mmunki.chess.ai.Bot;
import com.mmunki.chess.ai.Search;
import com.mmunki.chess.core.Board;
import com.mmunki.chess.core.Game;
import com.mmunki.chess.core.Move;
import com.mmunki.chess.core.Square;

import java.util.List;
import java.util.Random;

/**
 * The checks of the computer: it has to find the mate in one and it has to
 * play a whole game without getting stuck.
 */
public final class EngineCheck2 {

    private static int failures = 0;

    private EngineCheck2() {
    }

    public static void main(String[] args) {
        mateInOne("6k1/5ppp/8/8/8/8/5PPP/R5K1 w - - 0 1", "a1a8");
        mateInOne("r1bqkbnr/pppp1ppp/2n5/4p3/2B1P3/5Q2/PPPP1PPP/RNB1K1NR w KQkq - 4 4", "f3f7");
        mateInTwo("6k1/5ppp/8/8/8/8/5PPP/4R1K1 w - - 0 1");

        // the search has to see that a queen is taken
        Board board = new Board("rnbqkbnr/ppp1pppp/8/3p4/4P3/8/PPPP1PPP/RNBQKBNR w KQkq d6 0 2");
        Search.Result result = new Search(board).search(4, 1000);
        check("the search finds the capture of the pawn", result.move != null && result.move.to == 35);

        // a game between two computers has to end
        Game game = new Game();
        Bot white = new Bot(new Random(1));
        Bot black = new Bot(new Random(2));
        int moves = 0;
        while (!game.isOver() && moves < 300) {
            Move move = game.sideToMove() == com.mmunki.chess.core.Color.WHITE
                    ? white.chooseMove(game.board(), Bot.MEDIUM)
                    : black.chooseMove(game.board(), Bot.MEDIUM);
            if (move == null || !game.play(move)) {
                break;
            }
            moves++;
        }
        check("a game ends (" + game.end() + " after " + moves + " moves)", game.isOver());

        // the moves of the game can be written down and read again
        Game second = new Game();
        for (Move move : game.history()) {
            second.play(move);
        }
        check("the history can be replayed", second.board().fen().equals(game.board().fen()));

        System.out.println(failures == 0 ? "ALL ENGINE CHECKS PASSED" : failures + " CHECKS FAILED");
        if (failures > 0) {
            System.exit(1);
        }
    }

    private static void mateInOne(String fen, String target) {
        Board board = new Board(fen);
        Search.Result result = new Search(board).search(3, 1000);
        String found = result.move == null ? "none" : Square.name(result.move.from)
                + Square.name(result.move.to);
        check("mate in one in " + fen + " (found " + found + ")", target.equals(found));
    }

    private static void mateInTwo(String fen) {
        Board board = new Board(fen);
        Search.Result result = new Search(board).search(4, 2000);
        check("mate in two in " + fen, result.score > Search.MATE - 10);
    }

    private static void check(String what, boolean ok) {
        if (!ok) {
            failures++;
        }
        System.out.println((ok ? "ok      " : "FAILED  ") + what);
    }
}
