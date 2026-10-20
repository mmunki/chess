package com.mmunki.chess.rating;

import com.mmunki.chess.ai.Bot;

/**
 * The rating test.
 *
 * <p>The player plays a number of games against the computer.  The computer
 * gets stronger while the test goes on, so that the rating that comes out is
 * the rating of the player and not only the rating of one game.
 */
public final class RatingTest {

    /** The number of games of a rating test. */
    public static final int GAMES = 10;

    private final Rating rating;
    private int played;
    private int wins;
    private int losses;
    private int draws;

    public RatingTest(Rating rating) {
        this.rating = rating;
    }

    public Rating rating() {
        return rating;
    }

    public int played() {
        return played;
    }

    public boolean finished() {
        return played >= GAMES;
    }

    /** The strength of the computer in the next game. */
    public Bot.Level nextLevel() {
        int game = played + 1;
        if (game <= 2) {
            return Bot.BEGINNER;
        }
        if (game <= 5) {
            return Bot.EASY;
        }
        if (game <= 8) {
            return Bot.MEDIUM;
        }
        return Bot.HARD;
    }

    /**
     * Writes the result of a game into the rating.
     *
     * @param score 1 for a win, 0.5 for a draw, 0 for a loss
     * @param level the strength of the computer that was played against
     */
    public void record(double score, Bot.Level level) {
        rating.record(score, level.elo, 60);
        played++;
        if (score > 0.75) {
            wins++;
        } else if (score < 0.25) {
            losses++;
        } else {
            draws++;
        }
    }

    /** The text that tells how far the test is. */
    public String progress() {
        return "Rating test  " + played + "/" + GAMES + "   " + wins + " won  " + draws
                + " drawn  " + losses + " lost   rating " + rating.display();
    }
}
