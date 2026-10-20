package com.mmunki.chess.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A chess game: the position, the moves that were played and the result.
 *
 * <p>The game knows the history, so it can take moves back, tell the moves
 * apart in the notation of a score sheet and recognise a repetition.
 */
public final class Game {

    public enum Result {
        ONGOING,
        WHITE_WIN,
        BLACK_WIN,
        DRAW
    }

    /** Why the game ended. */
    public enum End {
        NONE,
        CHECKMATE,
        STALEMATE,
        FIFTY_MOVE,
        REPETITION,
        INSUFFICIENT_MATERIAL,
        RESIGNATION,
        TIME,
        AGREEMENT
    }

    private final Board board;
    private final List<Move> history = new ArrayList<>();
    private final List<Board.Undo> undos = new ArrayList<>();
    private final List<String> notations = new ArrayList<>();
    private final List<Long> positionHashes = new ArrayList<>();
    private End end = End.NONE;
    private Result result = Result.ONGOING;
    private Color winner = null;

    public Game() {
        this(new Board());
    }

    public Game(String fen) {
        this(new Board(fen));
    }

    public Game(Board board) {
        this.board = board;
        positionHashes.add(board.hash());
    }

    public Board board() {
        return board;
    }

    public List<Move> history() {
        return history;
    }

    public Color sideToMove() {
        return board.sideToMove();
    }

    public End end() {
        return end;
    }

    public Result result() {
        return result;
    }

    public Color winner() {
        return winner;
    }

    public boolean isOver() {
        return end != End.NONE;
    }

    /** All moves that are legal in the current position. */
    public List<Move> legalMoves() {
        return MoveGenerator.legalMoves(board);
    }

    /** Plays a move, returns false when the move is not legal. */
    public boolean play(Move move) {
        if (isOver()) {
            return false;
        }
        if (!legalMoves().contains(move)) {
            return false;
        }
        String notation = san(move);
        undos.add(board.makeMove(move));
        history.add(move);
        notations.add(notation);
        positionHashes.add(board.hash());
        updateResult();
        return true;
    }

    /** Takes the last move back, returns false when there is nothing to take back. */
    public boolean undo() {
        if (history.isEmpty()) {
            return false;
        }
        history.remove(history.size() - 1);
        Board.Undo undo = undos.remove(undos.size() - 1);
        notations.remove(notations.size() - 1);
        positionHashes.remove(positionHashes.size() - 1);
        board.undoMove(undo);
        end = End.NONE;
        result = Result.ONGOING;
        winner = null;
        return true;
    }

    /** The game is given up by the given player. */
    public void resign(Color color) {
        end = End.RESIGNATION;
        winner = color.opposite();
        result = winner == Color.WHITE ? Result.WHITE_WIN : Result.BLACK_WIN;
    }

    /** The game is lost by the given player because the time ran out. */
    public void timeout(Color color) {
        end = End.TIME;
        winner = color.opposite();
        result = winner == Color.WHITE ? Result.WHITE_WIN : Result.BLACK_WIN;
    }

    /** Both players agree that the game is a draw. */
    public void agreeDraw() {
        end = End.AGREEMENT;
        result = Result.DRAW;
    }

    /** The number of times the current position was on the board. */
    public int repetitions() {
        long hash = board.hash();
        int count = 0;
        for (long h : positionHashes) {
            if (h == hash) {
                count++;
            }
        }
        return count;
    }

    private void updateResult() {
        end = End.NONE;
        result = Result.ONGOING;
        winner = null;
        if (legalMoves().isEmpty()) {
            if (board.inCheck(board.sideToMove())) {
                end = End.CHECKMATE;
                winner = board.sideToMove().opposite();
                result = winner == Color.WHITE ? Result.WHITE_WIN : Result.BLACK_WIN;
            } else {
                end = End.STALEMATE;
                result = Result.DRAW;
            }
            return;
        }
        if (board.halfmoveClock() >= 100) {
            end = End.FIFTY_MOVE;
            result = Result.DRAW;
            return;
        }
        if (insufficientMaterial()) {
            end = End.INSUFFICIENT_MATERIAL;
            result = Result.DRAW;
            return;
        }
        if (repetitions() >= 3) {
            end = End.REPETITION;
            result = Result.DRAW;
            return;
        }
    }

    /** True when neither side has enough material to mate. */
    public boolean insufficientMaterial() {
        int whitePieces = 0;
        int blackPieces = 0;
        int whiteMinor = 0;
        int blackMinor = 0;
        List<Integer> whiteBishops = new ArrayList<>();
        List<Integer> blackBishops = new ArrayList<>();
        for (int s = 0; s < 64; s++) {
            Piece piece = board.pieceAt(s);
            if (piece == null) {
                continue;
            }
            if (board.colorAt(s) == Color.WHITE) {
                whitePieces++;
                if (piece == Piece.BISHOP) {
                    whiteMinor++;
                    whiteBishops.add(s);
                } else if (piece == Piece.KNIGHT) {
                    whiteMinor++;
                }
            } else {
                blackPieces++;
                if (piece == Piece.BISHOP) {
                    blackMinor++;
                    blackBishops.add(s);
                } else if (piece == Piece.KNIGHT) {
                    blackMinor++;
                }
            }
        }
        if (whitePieces <= 1 && blackPieces <= 1) {
            return true;
        }
        if (whitePieces == 1 && blackPieces == 2 && blackMinor == 1 && blackBishops.isEmpty()) {
            return true;
        }
        if (blackPieces == 1 && whitePieces == 2 && whiteMinor == 1 && whiteBishops.isEmpty()) {
            return true;
        }
        if (whitePieces == 2 && blackPieces == 2 && whiteBishops.size() == 1 && blackBishops.size() == 1) {
            // two bishops on the same colour of square
            int a = (Square.file(whiteBishops.get(0)) + Square.rank(whiteBishops.get(0))) % 2;
            int b = (Square.file(blackBishops.get(0)) + Square.rank(blackBishops.get(0))) % 2;
            return a == b;
        }
        return false;
    }

    // ------------------------------------------------------------------ notation

    /** The moves of the game in the notation of a score sheet. */
    public List<String> scoreSheet() {
        List<String> out = new ArrayList<>();
        for (int i = 0; i < notations.size(); i++) {
            if (i % 2 == 0) {
                out.add((i / 2 + 1) + ".");
            }
            out.add(notations.get(i));
        }
        return out;
    }

    /** The name of a move, for example "Nf3", "exd5", "O-O" or "Qh5+". */
    public String san(Move move) {
        Piece piece = board.pieceAt(move.from);
        Color mover = board.colorAt(move.from);
        boolean capture = board.pieceAt(move.captureSquare(mover)) != null || move.enPassant;
        String disambiguation = piece == Piece.PAWN || piece == Piece.KING
                ? "" : disambiguation(move, piece);
        StringBuilder sb = new StringBuilder();

        Board.Undo undo = board.makeMove(move);
        try {
            if (move.castle) {
                sb.append(move.to > move.from ? "O-O" : "O-O-O");
            } else if (piece == Piece.PAWN) {
                if (capture) {
                    sb.append((char) ('a' + Square.file(move.from))).append('x');
                }
                sb.append(Square.name(move.to));
                if (move.promotion != null) {
                    sb.append('=').append(move.promotion.letter());
                }
            } else {
                sb.append(piece.letter()).append(disambiguation);
                if (capture) {
                    sb.append('x');
                }
                sb.append(Square.name(move.to));
            }
            if (board.inCheck(board.sideToMove())) {
                sb.append(MoveGenerator.legalMoves(board).isEmpty() ? '#' : '+');
            }
        } finally {
            board.undoMove(undo);
        }
        return sb.toString();
    }

    /** The part of the name of a move that tells the pieces apart. */
    private String disambiguation(Move move, Piece piece) {
        Map<Integer, Boolean> candidates = new HashMap<>();
        for (Move other : legalMoves()) {
            if (other.from == move.from || other.to != move.to) {
                continue;
            }
            if (board.pieceAt(other.from) != piece) {
                continue;
            }
            candidates.put(other.from, Boolean.TRUE);
        }
        if (candidates.isEmpty()) {
            return "";
        }
        boolean sameFile = false;
        boolean sameRank = false;
        for (int from : candidates.keySet()) {
            if (Square.file(from) == Square.file(move.from)) {
                sameFile = true;
            }
            if (Square.rank(from) == Square.rank(move.from)) {
                sameRank = true;
            }
        }
        if (!sameFile) {
            return "" + (char) ('a' + Square.file(move.from));
        }
        if (!sameRank) {
            return "" + (Square.rank(move.from) + 1);
        }
        return Square.name(move.from);
    }
}
