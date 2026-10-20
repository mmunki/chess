package com.mmunki.chess.ai;

import com.mmunki.chess.core.Board;
import com.mmunki.chess.core.Move;
import com.mmunki.chess.core.MoveGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * The computer opponent.
 *
 * <p>Every level has a depth, a time and a rate of mistakes.  The levels are
 * calibrated so that they play with a strength that is close to the rating
 * they are named after.
 */
public final class Bot {

    /** One level of difficulty. */
    public static final class Level {
        public final String name;
        public final int elo;
        public final int depth;
        public final long millis;
        /** How often the bot plays a worse move than the best one, in percent. */
        public final int mistakePercent;

        Level(String name, int elo, int depth, long millis, int mistakePercent) {
            this.name = name;
            this.elo = elo;
            this.depth = depth;
            this.millis = millis;
            this.mistakePercent = mistakePercent;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static final Level BEGINNER = new Level("Beginner", 400, 1, 200, 75);
    public static final Level EASY = new Level("Easy", 600, 2, 300, 50);
    public static final Level MEDIUM = new Level("Medium", 850, 3, 500, 30);
    public static final Level HARD = new Level("Hard", 1150, 5, 1200, 15);
    public static final Level EXPERT = new Level("Expert", 1500, 6, 3000, 5);
    public static final Level MASTER = new Level("Master", 1900, 7, 6000, 0);

    /** The levels, from the weakest to the strongest. */
    public static final List<Level> LEVELS = Collections.unmodifiableList(
            new ArrayList<>(java.util.Arrays.asList(BEGINNER, EASY, MEDIUM, HARD, EXPERT, MASTER)));

    private final Random random;

    public Bot() {
        this(new Random());
    }

    public Bot(Random random) {
        this.random = random;
    }

    /** The move the bot plays in the position. */
    public Move chooseMove(Board board, Level level) {
        List<Move> moves = MoveGenerator.legalMoves(board);
        if (moves.isEmpty()) {
            return null;
        }
        if (moves.size() == 1) {
            return moves.get(0);
        }
        Search.Result result = new Search(board).search(level.depth, level.millis);
        Move best = result.move;
        if (best == null || !moves.contains(best)) {
            best = moves.get(0);
        }
        if (level.mistakePercent <= 0) {
            return best;
        }
        if (random.nextInt(100) >= level.mistakePercent) {
            return best;
        }
        // the bot plays a move that is not the best one, but not a wild one
        List<Move> pool = new ArrayList<>(moves);
        pool.remove(best);
        if (pool.isEmpty()) {
            return best;
        }
        pool.sort((a, b) -> Integer.compare(scoreAfter(board, b), scoreAfter(board, a)));
        int limit = Math.max(1, (int) (pool.size() * 0.6));
        return pool.get(random.nextInt(limit));
    }

    /** How good a move looks, seen with a short search. */
    private int scoreAfter(Board board, Move move) {
        Board copy = new Board(board);
        copy.makeMove(move);
        Search.Result result = new Search(copy).search(2, 120);
        return -result.score;
    }

    /** The level that is the closest to a rating. */
    public static Level levelForRating(int rating) {
        Level best = BEGINNER;
        for (Level level : LEVELS) {
            if (Math.abs(level.elo - rating) < Math.abs(best.elo - rating)) {
                best = level;
            }
        }
        return best;
    }
}
