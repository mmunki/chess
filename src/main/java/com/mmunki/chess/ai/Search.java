package com.mmunki.chess.ai;

import com.mmunki.chess.core.Board;
import com.mmunki.chess.core.Color;
import com.mmunki.chess.core.Move;
import com.mmunki.chess.core.MoveGenerator;
import com.mmunki.chess.core.Piece;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The search of the computer.
 *
 * <p>Negamax with the alpha beta cut off, a table of transpositions, a search
 * of the captures when the position is quiet (quiescence) and a deeper search
 * of every position again and again (iterative deepening).
 */
public final class Search {

    public static final int MATE = 30000;
    public static final int INFINITY = 40000;

    /** What the search found. */
    public static final class Result {
        public final Move move;
        public final int score;
        public final int depth;
        public final long nodes;
        public final long millis;
        /** The score of every move of the position, the best one first. */
        public final Map<Integer, Integer> rootScores;

        Result(Move move, int score, int depth, long nodes, long millis,
               Map<Integer, Integer> rootScores) {
            this.move = move;
            this.score = score;
            this.depth = depth;
            this.nodes = nodes;
            this.millis = millis;
            this.rootScores = rootScores;
        }
    }

    private static final class Entry {
        final int depth;
        final int score;
        final int flag;
        final Move move;

        Entry(int depth, int score, int flag, Move move) {
            this.depth = depth;
            this.score = score;
            this.flag = flag;
            this.move = move;
        }
    }

    private static final int FLAG_EXACT = 0;
    private static final int FLAG_LOWER = 1;
    private static final int FLAG_UPPER = 2;

    private final Board board;
    private final Map<Long, Entry> table = new HashMap<>();
    private final long[][] killers = new long[64][2];
    private final Map<Integer, Integer> history = new HashMap<>();
    private long nodes;
    private long deadline;
    private boolean stopped;
    private Move ttMove;

    public Search(Board board) {
        this.board = board;
    }

    /**
     * Searches the position.
     *
     * @param maxDepth how deep the search may go
     * @param millis   how long the search may take, 0 means no limit
     */
    public Result search(int maxDepth, long millis) {
        long start = System.currentTimeMillis();
        deadline = millis > 0 ? start + millis : Long.MAX_VALUE;
        nodes = 0;
        stopped = false;
        table.clear();
        history.clear();
        Map<Integer, Integer> rootScores = new HashMap<>();

        int alpha = -INFINITY;
        int beta = INFINITY;
        Move bestMove = null;
        Move previous = null;
        int lastScore = 0;
        int reachedDepth = 0;

        for (int depth = 1; depth <= maxDepth; depth++) {
            // every round starts with a full window, a narrow one would only
            // give the bounds of the moves and not their real values
            alpha = -INFINITY;
            beta = INFINITY;
            Map<Integer, Integer> scores = new HashMap<>();
            Move move = rootSearch(depth, alpha, beta, scores, previous);
            if (stopped || move == null) {
                break;
            }
            bestMove = move;
            previous = move;
            lastScore = scores.getOrDefault(move.key(), 0);
            rootScores = scores;
            reachedDepth = depth;
            if (Math.abs(lastScore) > MATE - 1000) {
                break;
            }
            if (millis > 0 && System.currentTimeMillis() - start > millis / 2) {
                break;
            }
        }
        if (bestMove == null) {
            List<Move> moves = MoveGenerator.legalMoves(board);
            if (!moves.isEmpty()) {
                bestMove = moves.get(0);
            }
        }
        return new Result(bestMove, lastScore, reachedDepth, nodes,
                System.currentTimeMillis() - start, rootScores);
    }

    /** The search of the position itself, the moves of the root are scored. */
    private Move rootSearch(int depth, int alpha, int beta, Map<Integer, Integer> scores,
                            Move previous) {
        List<Move> moves = MoveGenerator.legalMoves(board);
        if (moves.isEmpty()) {
            return null;
        }
        order(moves, previous, 0);

        Move bestMove = null;
        int bestScore = -INFINITY;
        for (int i = 0; i < moves.size(); i++) {
            Move move = moves.get(i);
            Board.Undo undo = board.makeMove(move);
            // every move of the position is searched with the full window, so
            // that the score of every move is the real one and not only a bound
            int score = -negamax(depth - 1, -beta, -alpha, 1);
            board.undoMove(undo);
            if (stopped) {
                break;
            }
            scores.put(move.key(), score);
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
                if (score > alpha) {
                    alpha = score;
                }
                if (alpha >= beta) {
                    break;
                }
            }
        }
        if (bestMove != null) {
            // the score of the best move is the real one
            scores.put(bestMove.key(), bestScore);
        }
        return bestMove;
    }

    private int negamax(int depth, int alpha, int beta, int ply) {
        if ((nodes & 1023) == 0 && System.currentTimeMillis() > deadline) {
            stopped = true;
            return 0;
        }
        nodes++;

        boolean isPv = beta - alpha > 1;
        long hash = board.hash();
        Entry entry = table.get(hash);
        Move tt = entry == null ? null : entry.move;
        if (entry != null && !isPv && entry.depth >= depth) {
            int score = fromTable(entry.score, ply);
            if (entry.flag == FLAG_EXACT) {
                return score;
            }
            if (entry.flag == FLAG_LOWER && score >= beta) {
                return score;
            }
            if (entry.flag == FLAG_UPPER && score <= alpha) {
                return score;
            }
        }

        boolean inCheck = board.inCheck(board.sideToMove());
        if (inCheck) {
            depth++;
        }
        if (depth <= 0) {
            return quiescence(alpha, beta, ply);
        }

        List<Move> moves = MoveGenerator.legalMoves(board);
        if (moves.isEmpty()) {
            return inCheck ? -MATE + ply : 0;
        }

        order(moves, tt, ply);

        int bestScore = -INFINITY;
        Move bestMove = null;
        int originalAlpha = alpha;

        for (int i = 0; i < moves.size(); i++) {
            Move move = moves.get(i);
            Board.Undo undo = board.makeMove(move);
            int score = -negamax(depth - 1, -beta, -alpha, ply + 1);
            board.undoMove(undo);
            if (stopped) {
                return bestScore == -INFINITY ? 0 : bestScore;
            }
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
                if (score > alpha) {
                    alpha = score;
                }
                if (alpha >= beta) {
                    if (!inCheck && move.promotion == null) {
                        // the move that was refused is remembered
                        if (killers[ply][0] != move.key()) {
                            killers[ply][1] = killers[ply][0];
                            killers[ply][0] = move.key();
                        }
                        history.merge(move.key(), depth * depth, Integer::sum);
                    }
                    break;
                }
            }
        }

        int flag = bestScore <= originalAlpha ? FLAG_UPPER
                : bestScore >= beta ? FLAG_LOWER : FLAG_EXACT;
        table.put(hash, new Entry(depth, toTable(bestScore, ply), flag, bestMove));
        return bestScore;
    }

    /** The captures of the position, searched until the position is quiet again. */
    private int quiescence(int alpha, int beta, int ply) {
        if ((nodes & 1023) == 0 && System.currentTimeMillis() > deadline) {
            stopped = true;
            return 0;
        }
        nodes++;

        int standPat = Evaluator.evaluate(board);
        if (standPat >= beta) {
            return standPat;
        }
        if (standPat > alpha) {
            alpha = standPat;
        }
        if (ply > 24) {
            return standPat;
        }

        List<Move> captures = new ArrayList<>();
        for (Move move : MoveGenerator.legalMoves(board)) {
            if (isCapture(move)) {
                captures.add(move);
            }
        }
        captures.sort((a, b) -> Integer.compare(captureValue(b), captureValue(a)));

        for (Move move : captures) {
            int gain = captureValue(move);
            if (standPat + gain + 200 < alpha) {
                continue;
            }
            Board.Undo undo = board.makeMove(move);
            int score = -quiescence(-beta, -alpha, ply + 1);
            board.undoMove(undo);
            if (stopped) {
                return standPat;
            }
            if (score >= beta) {
                return score;
            }
            if (score > alpha) {
                alpha = score;
            }
        }
        return alpha;
    }

    private boolean isCapture(Move move) {
        if (move.enPassant) {
            return true;
        }
        return board.pieceAt(move.to) != null;
    }

    /** How much a capture is worth, the most valuable victim first. */
    private int captureValue(Move move) {
        Piece victim = move.enPassant ? Piece.PAWN : board.pieceAt(move.to);
        if (victim == null) {
            return 0;
        }
        Piece attacker = board.pieceAt(move.from);
        return victim.value() * 10 - (attacker == null ? 0 : attacker.value());
    }

    /** Puts the moves in the order the search likes them. */
    private void order(List<Move> moves, Move tt, int ply) {
        ttMove = tt;
        final int level = ply;
        moves.sort((a, b) -> Integer.compare(orderScore(b, level), orderScore(a, level)));
    }

    private int orderScore(Move move, int ply) {
        if (ttMove != null && move.equals(ttMove)) {
            return 10_000_000;
        }
        if (move.enPassant) {
            return 9_000_000 + Piece.PAWN.value();
        }
        Piece victim = board.pieceAt(move.to);
        if (victim != null) {
            Piece attacker = board.pieceAt(move.from);
            return 8_000_000 + victim.value() * 10 - (attacker == null ? 0 : attacker.value());
        }
        if (move.promotion != null) {
            return 7_000_000 + move.promotion.value();
        }
        if (killers[ply][0] == move.key()) {
            return 6_000_000;
        }
        if (killers[ply][1] == move.key()) {
            return 5_000_000;
        }
        return history.getOrDefault(move.key(), 0);
    }

    private static int toTable(int score, int ply) {
        if (score > MATE - 1000) {
            return score + ply;
        }
        if (score < -MATE + 1000) {
            return score - ply;
        }
        return score;
    }

    private static int fromTable(int score, int ply) {
        if (score > MATE - 1000) {
            return score - ply;
        }
        if (score < -MATE + 1000) {
            return score + ply;
        }
        return score;
    }

    /** The evaluation of the position in the point of view of white. */
    public static int whiteScore(Board board) {
        int score = Evaluator.evaluate(board);
        return board.sideToMove() == Color.WHITE ? score : -score;
    }
}
