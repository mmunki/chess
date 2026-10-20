package com.mmunki.chess.rating;

/**
 * The rating of a player.
 *
 * <p>Beside the rating itself it keeps the rating deviation, the number of
 * games and the results, so that the rating can be shown the way chess.com
 * shows it.
 */
public final class Rating {

    private double rating = Glicko.DEFAULT_RATING;
    private double deviation = Glicko.DEFAULT_DEVIATION;
    private int games;
    private int wins;
    private int losses;
    private int draws;

    public Rating() {
    }

    public Rating(double rating, double deviation) {
        this.rating = rating;
        this.deviation = deviation;
    }

    public double rating() {
        return rating;
    }

    public double deviation() {
        return deviation;
    }

    public int games() {
        return games;
    }

    public int wins() {
        return wins;
    }

    public int losses() {
        return losses;
    }

    public int draws() {
        return draws;
    }

    /** The rating that is shown, the deviation is not part of it. */
    public int display() {
        return (int) Math.round(rating);
    }

    /** The confidence of the rating, in percent. */
    public int confidence() {
        double factor = (Glicko.DEFAULT_DEVIATION - deviation) / Glicko.DEFAULT_DEVIATION;
        return (int) Math.round(Math.max(0, Math.min(1, factor)) * 100);
    }

    public void record(double score, double opponentRating, double opponentDeviation) {
        double[] updated = Glicko.update(rating, deviation, opponentRating, opponentDeviation, score);
        rating = updated[0];
        deviation = updated[1];
        games++;
        if (score > 0.75) {
            wins++;
        } else if (score < 0.25) {
            losses++;
        } else {
            draws++;
        }
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public void setDeviation(double deviation) {
        this.deviation = deviation;
    }

    public void setGames(int games) {
        this.games = games;
    }

    public void setResults(int wins, int losses, int draws) {
        this.wins = wins;
        this.losses = losses;
        this.draws = draws;
    }

    @Override
    public String toString() {
        return display() + " (" + games + " games, " + wins + "/" + draws + "/" + losses + ")";
    }
}
