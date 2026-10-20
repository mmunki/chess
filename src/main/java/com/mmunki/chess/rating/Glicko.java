package com.mmunki.chess.rating;

/**
 * The Glicko-2 rating system.
 *
 * <p>It is the system chess.com uses.  A rating comes with a rating
 * deviation, the number that tells how certain the rating is.  The
 * deviation grows when a player does not play and shrinks when the player
 * plays against opponents of a known strength.
 */
public final class Glicko {

    /** The Glicko-2 scale, 173.7178 points are one standard deviation. */
    public static final double SCALE = 173.7178;
    /** The volatility of the rating, it never changes at the moment. */
    public static final double TAU = 0.5;
    /** The rating a player starts with. */
    public static final double DEFAULT_RATING = 1500;
    /** The rating deviation a player starts with. */
    public static final double DEFAULT_DEVIATION = 200;

    private Glicko() {
    }

    /**
     * The new rating and the new rating deviation after a game.
     *
     * @param rating             the rating of the player
     * @param deviation          the rating deviation of the player
     * @param opponentRating     the rating of the opponent
     * @param opponentDeviation  the rating deviation of the opponent
     * @param score              1 for a win, 0.5 for a draw, 0 for a loss
     */
    public static double[] update(double rating, double deviation,
                                  double opponentRating, double opponentDeviation,
                                  double score) {
        double mu = (rating - DEFAULT_RATING) / SCALE;
        double phi = deviation / SCALE;
        double muJ = (opponentRating - DEFAULT_RATING) / SCALE;
        double phiJ = opponentDeviation / SCALE;

        double g = g(phiJ);
        double e = expected(mu, muJ, phiJ);

        double v = 1.0 / (g * g * e * (1 - e));
        double delta = v * g * (e - score);

        double phiStar = Math.sqrt(phi * phi + TAU * TAU);
        double phiPrime = 1.0 / Math.sqrt(1.0 / (phiStar * phiStar) + 1.0 / v);
        double muPrime = mu + phiPrime * phiPrime * g * (e - score);

        double newRating = SCALE * muPrime + DEFAULT_RATING;
        double newDeviation = SCALE * phiPrime;

        // the deviation never falls below the floor of the system
        newDeviation = Math.max(newDeviation, DEFAULT_DEVIATION * 0.05);
        return new double[]{newRating, newDeviation};
    }

    /** The deviation of a player that did not play for a while. */
    public static double inflate(double rating, double deviation) {
        double phi = deviation / SCALE;
        return SCALE * Math.sqrt(phi * phi + TAU * TAU);
    }

    /** The probability that the player wins against an opponent. */
    public static double expected(double mu, double muJ, double phiJ) {
        return 1.0 / (1.0 + Math.exp(-g(phiJ) * (mu - muJ)));
    }

    private static double g(double phi) {
        return 1.0 / Math.sqrt(1.0 + 3.0 * phi * phi / (Math.PI * Math.PI));
    }
}
