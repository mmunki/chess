package com.mmunki.chess.test;

import com.mmunki.chess.ai.Bot;
import com.mmunki.chess.core.Game;
import com.mmunki.chess.core.Move;

import java.util.Random;

/**
 * Measures how strong the levels of the computer really are.
 *
 * <p>Every level plays a number of games against the level above it, the
 * result tells how far the levels are apart in rating points.
 */
public final class Calibrate {

    private Calibrate() {
    }

    public static void main(String[] args) {
        int games = args.length > 0 ? Integer.parseInt(args[0]) : 6;
        Bot.Level[] levels = Bot.LEVELS.toArray(new Bot.Level[0]);
        System.out.println("games per pair: " + games);
        double base = levels[0].elo;
        for (int i = 0; i + 1 < levels.length; i++) {
            Bot.Level weaker = levels[i];
            Bot.Level stronger = levels[i + 1];
            double score = 0;
            int draws = 0;
            for (int g = 0; g < games; g++) {
                boolean weakerIsWhite = g % 2 == 0;
                Game game = new Game();
                Bot white = new Bot(new Random(1000 + g));
                Bot black = new Bot(new Random(2000 + g));
                int moves = 0;
                while (!game.isOver() && moves < 200) {
                    boolean whiteTurn = game.sideToMove() == com.mmunki.chess.core.Color.WHITE;
                    Bot bot = whiteTurn ? white : black;
                    Bot.Level level = whiteTurn == weakerIsWhite ? weaker : stronger;
                    Move move = bot.chooseMove(game.board(), level);
                    if (move == null || !game.play(move)) {
                        break;
                    }
                    moves++;
                }
                double result;
                if (game.result() == Game.Result.DRAW) {
                    result = 0.5;
                    draws++;
                } else {
                    boolean weakerWon = game.winner() == com.mmunki.chess.core.Color.WHITE
                            ? weakerIsWhite : !weakerIsWhite;
                    result = weakerWon ? 1 : 0;
                }
                score += result;
                System.out.printf("  %s vs %s: %s%s%n", weaker.name, stronger.name,
                        result == 1 ? "win" : result == 0 ? "loss" : "draw", "");
            }
            double average = score / games;
            double difference = average > 0 && average < 1
                    ? -400 * Math.log10(1 / average - 1)
                    : (average >= 1 ? 400 : -400);
            System.out.printf("%-8s vs %-8s score %.2f  difference %.0f rating points%n",
                    weaker.name, stronger.name, average, difference);
        }
    }
}
