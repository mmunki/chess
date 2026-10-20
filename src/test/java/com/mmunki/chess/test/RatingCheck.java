package com.mmunki.chess.test;

import com.mmunki.chess.rating.Glicko;
import com.mmunki.chess.rating.Rating;
import com.mmunki.chess.rating.RatingTest;
import com.mmunki.chess.ai.Bot;

/**
 * The checks of the rating system.
 *
 * <p>The numbers of the first check are the ones of the example of the
 * Glicko-2 paper by Mark Glickman.
 */
public final class RatingCheck {

    private static int failures = 0;

    private RatingCheck() {
    }

    public static void main(String[] args) {
        // the example of the paper: 1500 with a deviation of 200 against
        // three opponents, the numbers of the paper are 1464.06 and 151.52
        double[] updated = Glicko.update(1500, 200,
                new double[]{1400, 1550, 1700},
                new double[]{30, 100, 300},
                new double[]{1, 0, 0});
        check("the rating of the paper", close(updated[0], 1464.06, 0.01), updated[0]);
        check("the deviation of the paper", close(updated[1], 151.52, 0.01), updated[1]);

        // a win against a player of the same strength does not change the
        // rating much, the deviation becomes smaller
        // a win against a player of the same strength raises the rating, the
        // deviation is big, so there is a lot to learn
        double[] win = Glicko.update(1500, 200, 1500, 200, 1);
        check("a win raises the rating", win[0] > 1500, win[0]);
        check("the deviation becomes smaller", win[1] < 200, win[1]);

        // a loss against a stronger player costs rating
        double[] loss = Glicko.update(1500, 200, 2000, 50, 0);
        check("a loss against a stronger player costs rating", loss[0] < 1500, loss[0]);

        // a win against a much weaker player gains rating, but only a little
        double[] winAgainstWeak = Glicko.update(1500, 200, 1000, 50, 1);
        check("a win against a weaker player gains rating", winAgainstWeak[0] > 1500,
                winAgainstWeak[0]);
        check("the win against a weaker player gains little",
                winAgainstWeak[0] - 1500 < win[0] - 1500, winAgainstWeak[0]);

        // the deviation of a player that does not play grows
        check("the deviation grows while the player does not play",
                Glicko.inflate(100, 6) > 100 && Glicko.inflate(100, 6) <= 350,
                Glicko.inflate(100, 6));

        // the rating of the player is counted
        Rating rating = new Rating();
        rating.record(1, 1200, 60);
        check("a win is counted", rating.wins() == 1 && rating.games() == 1, rating);
        rating.record(0.5, 1200, 60);
        rating.record(0, 1200, 60);
        check("the results are counted", rating.draws() == 1 && rating.losses() == 1, rating);

        // the rating test counts ten games and gets stronger
        RatingTest test = new RatingTest(new Rating());
        check("the test has ten games", RatingTest.GAMES == 10, RatingTest.GAMES);
        check("the computer gets stronger", test.nextLevel().elo < Bot.HARD.elo, test.nextLevel());
        for (int i = 0; i < RatingTest.GAMES; i++) {
            test.record(i % 2 == 0 ? 1 : 0, test.nextLevel());
        }
        check("the test is over", test.finished(), test.progress());

        System.out.println(failures == 0 ? "ALL RATING CHECKS PASSED" : failures + " CHECKS FAILED");
        if (failures > 0) {
            System.exit(1);
        }
    }

    private static boolean close(double value, double expected, double tolerance) {
        return Math.abs(value - expected) <= tolerance;
    }

    private static void check(String what, boolean ok, Object value) {
        if (!ok) {
            failures++;
        }
        System.out.println((ok ? "ok      " : "FAILED  ") + what + " (" + value + ")");
    }
}
