package com.mmunki.chess.rating;

/**
 * The Glicko-2 rating system.
 *
 * <p>It is the system chess.com uses.  A rating comes with a rating
 * deviation, the number that tells how certain the rating is.  The
 * deviation grows when a player does not play and shrinks when the player
 * plays against opponents of a known strength.
 *
 * <p>The steps of the system are the ones of the paper of Mark Glickman:
 * the expected result of every game, the new rating and the new rating
 * deviation.  The volatility of the rating is kept as it is, a rating that
 * is not changed over a long time does not become more uncertain than the
 * system says.
 */
public final class Glicko {

    /** The Glicko-2 scale, 173.7178 points are one standard deviation. */
    public static final double SCALE = 173.7178;
    /** How much the rating of a player wanders around. */
    public static final double SIGMA = 0.06;
    /** The rating a player starts with. */
    public static final double DEFAULT_RATING = 1500;
    /** The rating deviation a player starts with. */
    public static final double DEFAULT_DEVIATION = 200;
    /** The rating deviation of a player that never played. */
    public static final double MAX_DEVIATION = 350;

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
        return update(rating, deviation, new double[]{opponentRating},
                new double[]{opponentDeviation}, new double[]{score});
    }

    /**
     * The new rating and the new rating deviation after a number of games.
     *
     * @param rating             the rating of the player
     * @param deviation          the rating deviation of the player
     * @param opponentRatings    the ratings of the opponents
     * @param opponentDeviations the rating deviations of the opponents
     * @param scores             1 for a win, 0.5 for a draw, 0 for a loss
     */
    public static double[] update(double rating, double deviation,
                                  double[] opponentRatings, double[] opponentDeviations,
                                  double[] scores) {
        double mu = (rating - DEFAULT_RATING) / SCALE;
        double phi = deviation / SCALE;

        double v = 0;
        double sum = 0;
        for (int i = 0; i < opponentRatings.length; i++) {
            double muJ = (opponentRatings[i] - DEFAULT_RATING) / SCALE;
            double phiJ = opponentDeviations[i] / SCALE;
            double g = g(phiJ);
            double e = expected(mu, muJ, phiJ);
            v += g * g * e * (1 - e);
            sum += g * (scores[i] - e);
        }
        v = v > 0 ? 1.0 / v : 1.0;

        double phiStar = Math.sqrt(phi * phi + SIGMA * SIGMA);
        double phiPrime = 1.0 / Math.sqrt(1.0 / (phiStar * phiStar) + 1.0 / v);
        double muPrime = mu + phiPrime * phiPrime * sum;

        double newRating = SCALE * muPrime + DEFAULT_RATING;
        double newDeviation = SCALE * phiPrime;

        // the deviation never falls below the floor of the system
        newDeviation = Math.max(newDeviation, DEFAULT_DEVIATION * 0.05);
        return new double[]{newRating, newDeviation};
    }

    /**
     * The deviation of a player that did not play for a while.
     *
     * @param deviation the rating deviation of the player
     * @param months    the months since the last game
     */
    public static double inflate(double deviation, double months) {
        double c = 34.6;
        double grown = Math.sqrt(deviation * deviation + c * c * months);
        return Math.min(grown, MAX_DEVIATION);
    }

    /** The probability that the player wins against an opponent. */
    public static double expected(double mu, double muJ, double phiJ) {
        return 1.0 / (1.0 + Math.exp(-g(phiJ) * (mu - muJ)));
    }

    private static double g(double phi) {
        return 1.0 / Math.sqrt(1.0 + 3.0 * phi * phi / (Math.PI * Math.PI));
    }
}
