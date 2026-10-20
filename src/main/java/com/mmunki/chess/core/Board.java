package com.mmunki.chess.core;

import java.util.ArrayList;
import java.util.List;

/**
 * The position of a chess game.
 *
 * <p>The board knows the pieces, the side to move, the castling rights, the
 * en passant target and the clocks.  It does not know the history of the game,
 * that lives in {@link Game}.
 */
public final class Board {

    public static final int CASTLE_WK = 1;
    public static final int CASTLE_WQ = 2;
    public static final int CASTLE_BK = 4;
    public static final int CASTLE_BQ = 8;

    private static final String START_FEN =
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    private final Piece[] squares = new Piece[64];
    private final Color[] colors = new Color[64];
    private Color sideToMove = Color.WHITE;
    private int castling = CASTLE_WK | CASTLE_WQ | CASTLE_BK | CASTLE_BQ;
    private int enPassant = -1;
    private int halfmoveClock;
    private int fullmoveNumber = 1;
    private long hash;

    public Board() {
        setFen(START_FEN);
    }

    public Board(String fen) {
        setFen(fen);
    }

    public Board(Board other) {
        System.arraycopy(other.squares, 0, squares, 0, 64);
        System.arraycopy(other.colors, 0, colors, 0, 64);
        this.sideToMove = other.sideToMove;
        this.castling = other.castling;
        this.enPassant = other.enPassant;
        this.halfmoveClock = other.halfmoveClock;
        this.fullmoveNumber = other.fullmoveNumber;
        this.hash = other.hash;
    }

    // ------------------------------------------------------------------ pieces

    public Piece pieceAt(int square) {
        return squares[square];
    }

    public Color colorAt(int square) {
        return colors[square];
    }

    public boolean isOccupied(int square) {
        return squares[square] != null;
    }

    public boolean isOccupiedBy(int square, Color color) {
        return colors[square] == color && squares[square] != null;
    }

    public void putPiece(int square, Piece piece, Color color) {
        squares[square] = piece;
        colors[square] = piece == null ? null : color;
        recomputeHash();
    }

    public void removePiece(int square) {
        squares[square] = null;
        colors[square] = null;
        recomputeHash();
    }

    // ------------------------------------------------------------------ state

    public Color sideToMove() {
        return sideToMove;
    }

    public void setSideToMove(Color color) {
        this.sideToMove = color;
        recomputeHash();
    }

    public int castlingRights() {
        return castling;
    }

    public int enPassantSquare() {
        return enPassant;
    }

    public int halfmoveClock() {
        return halfmoveClock;
    }

    public int fullmoveNumber() {
        return fullmoveNumber;
    }

    public long hash() {
        return hash;
    }

    /** The square of the king of the given colour, -1 when it is not on the board. */
    public int kingSquare(Color color) {
        for (int s = 0; s < 64; s++) {
            if (squares[s] == Piece.KING && colors[s] == color) {
                return s;
            }
        }
        return -1;
    }

    public boolean hasCastlingRight(int right) {
        return (castling & right) != 0;
    }

    // ------------------------------------------------------------------ FEN

    public void setFen(String fen) {
        String[] parts = fen.trim().split("\\s+");
        if (parts.length < 4) {
            throw new IllegalArgumentException("broken FEN: " + fen);
        }
        for (int s = 0; s < 64; s++) {
            squares[s] = null;
            colors[s] = null;
        }
        int rank = 7;
        int file = 0;
        for (int i = 0; i < parts[0].length(); i++) {
            char c = parts[0].charAt(i);
            if (c == '/') {
                rank--;
                file = 0;
            } else if (Character.isDigit(c)) {
                file += c - '0';
            } else {
                Piece p = Piece.fromFen(c);
                if (p == null) {
                    throw new IllegalArgumentException("broken FEN: " + fen);
                }
                int square = Square.of(file, rank);
                squares[square] = p;
                colors[square] = Character.isUpperCase(c) ? Color.WHITE : Color.BLACK;
                file++;
            }
        }
        sideToMove = Color.fromFen(parts[1]);
        castling = 0;
        if (!"-".equals(parts[2])) {
            for (int i = 0; i < parts[2].length(); i++) {
                switch (parts[2].charAt(i)) {
                    case 'K':
                        castling |= CASTLE_WK;
                        break;
                    case 'Q':
                        castling |= CASTLE_WQ;
                        break;
                    case 'k':
                        castling |= CASTLE_BK;
                        break;
                    case 'q':
                        castling |= CASTLE_BQ;
                        break;
                    default:
                        break;
                }
            }
        }
        enPassant = "-".equals(parts[3]) ? -1 : Square.fromName(parts[3]);
        halfmoveClock = parts.length > 4 ? Integer.parseInt(parts[4]) : 0;
        fullmoveNumber = parts.length > 5 ? Integer.parseInt(parts[5]) : 1;
        recomputeHash();
    }

    public String fen() {
        StringBuilder sb = new StringBuilder();
        for (int rank = 7; rank >= 0; rank--) {
            int empty = 0;
            for (int file = 0; file < 8; file++) {
                int square = Square.of(file, rank);
                if (squares[square] == null) {
                    empty++;
                } else {
                    if (empty > 0) {
                        sb.append(empty);
                        empty = 0;
                    }
                    sb.append(squares[square].fen(colors[square]));
                }
            }
            if (empty > 0) {
                sb.append(empty);
            }
            if (rank > 0) {
                sb.append('/');
            }
        }
        sb.append(' ').append(sideToMove.fen()).append(' ');
        if (castling == 0) {
            sb.append('-');
        } else {
            if ((castling & CASTLE_WK) != 0) {
                sb.append('K');
            }
            if ((castling & CASTLE_WQ) != 0) {
                sb.append('Q');
            }
            if ((castling & CASTLE_BK) != 0) {
                sb.append('k');
            }
            if ((castling & CASTLE_BQ) != 0) {
                sb.append('q');
            }
        }
        sb.append(' ');
        sb.append(enPassant < 0 ? "-" : Square.name(enPassant));
        sb.append(' ').append(halfmoveClock);
        sb.append(' ').append(fullmoveNumber);
        return sb.toString();
    }

    // ------------------------------------------------------------------ moves

    /** Everything that has to be restored when a move is taken back. */
    public static final class Undo {
        public final Move move;
        public final Piece captured;
        public final Piece promoted;
        public final int castling;
        public final int enPassant;
        public final int halfmoveClock;
        public final int fullmoveNumber;
        public final Color sideToMove;
        public final long hash;

        Undo(Move move, Piece captured, Piece promoted, int castling, int enPassant,
             int halfmoveClock, int fullmoveNumber, Color sideToMove, long hash) {
            this.move = move;
            this.captured = captured;
            this.promoted = promoted;
            this.castling = castling;
            this.enPassant = enPassant;
            this.halfmoveClock = halfmoveClock;
            this.fullmoveNumber = fullmoveNumber;
            this.sideToMove = sideToMove;
            this.hash = hash;
        }
    }

    /** Plays a move on the board and returns the information to take it back. */
    public Undo makeMove(Move move) {
        Piece moving = squares[move.from];
        Color mover = colors[move.from];
        Piece captured = squares[move.captureSquare(mover)];
        // when a pawn is promoted the piece that has to come back is the pawn
        Piece promoted = move.promotion == null ? null : moving;

        int oldCastling = castling;
        int oldEnPassant = enPassant;
        int oldHalfmove = halfmoveClock;
        int oldFullmove = fullmoveNumber;
        Color oldSide = sideToMove;
        long oldHash = hash;

        // the hash follows every change, it is never calculated from scratch
        hash ^= Zobrist.piece(moving, mover, move.from);
        if (captured != null) {
            hash ^= Zobrist.piece(captured, mover.opposite(), move.captureSquare(mover));
        }
        hash ^= Zobrist.piece(move.promotion != null ? move.promotion : moving, mover, move.to);

        // remove the captured piece
        if (move.enPassant) {
            int victim = move.captureSquare(mover);
            squares[victim] = null;
            colors[victim] = null;
        } else if (captured != null) {
            squares[move.to] = null;
            colors[move.to] = null;
        }

        // move the piece
        squares[move.to] = moving;
        colors[move.to] = mover;
        squares[move.from] = null;
        colors[move.from] = null;

        // promote
        if (move.promotion != null) {
            squares[move.to] = move.promotion;
        }

        // the rook of a castle
        if (move.castle) {
            int rookFrom = move.to > move.from ? move.to + 1 : move.to - 2;
            int rookTo = move.to > move.from ? move.to - 1 : move.to + 1;
            hash ^= Zobrist.piece(Piece.ROOK, mover, rookFrom);
            hash ^= Zobrist.piece(Piece.ROOK, mover, rookTo);
            if (move.to > move.from) {
                // king side: the rook of h1 goes to f1
                squares[move.to - 1] = squares[move.to + 1];
                colors[move.to - 1] = colors[move.to + 1];
                squares[move.to + 1] = null;
                colors[move.to + 1] = null;
            } else {
                // queen side: the rook of a1 goes to d1
                squares[move.to + 1] = squares[move.to - 2];
                colors[move.to + 1] = colors[move.to - 2];
                squares[move.to - 2] = null;
                colors[move.to - 2] = null;
            }
        }

        // castling rights
        if (moving == Piece.KING) {
            castling &= mover == Color.WHITE ? ~(CASTLE_WK | CASTLE_WQ) : ~(CASTLE_BK | CASTLE_BQ);
        }
        if (move.from == 0 || move.to == 0) {
            castling &= ~CASTLE_WQ;
        }
        if (move.from == 7 || move.to == 7) {
            castling &= ~CASTLE_WK;
        }
        if (move.from == 56 || move.to == 56) {
            castling &= ~CASTLE_BQ;
        }
        if (move.from == 63 || move.to == 63) {
            castling &= ~CASTLE_BK;
        }

        // en passant target
        enPassant = -1;
        if (moving == Piece.PAWN && Math.abs(move.to - move.from) == 16) {
            enPassant = (move.from + move.to) / 2;
        }

        // clocks
        if (moving == Piece.PAWN || captured != null) {
            halfmoveClock = 0;
        } else {
            halfmoveClock++;
        }
        if (mover == Color.BLACK) {
            fullmoveNumber++;
        }
        hash ^= Zobrist.castling(oldCastling) ^ Zobrist.castling(castling);
        hash ^= Zobrist.enPassant(oldEnPassant) ^ Zobrist.enPassant(enPassant);
        hash ^= Zobrist.side();
        sideToMove = mover.opposite();
        return new Undo(move, captured, promoted, oldCastling, oldEnPassant, oldHalfmove,
                oldFullmove, oldSide, oldHash);
    }

    /** Takes a move back. */
    public void undoMove(Undo undo) {
        Move move = undo.move;
        Piece standing = squares[move.to];
        Piece moving = undo.promoted != null ? undo.promoted : standing;
        Color mover = undo.sideToMove;

        hash ^= Zobrist.piece(standing, mover, move.to);
        hash ^= Zobrist.piece(moving, mover, move.from);
        if (undo.captured != null) {
            hash ^= Zobrist.piece(undo.captured, mover.opposite(), move.captureSquare(mover));
        }

        squares[move.from] = moving;
        colors[move.from] = mover;
        squares[move.to] = null;
        colors[move.to] = null;

        if (move.enPassant) {
            int victim = move.captureSquare(mover);
            squares[victim] = undo.captured;
            colors[victim] = mover.opposite();
        } else if (undo.captured != null) {
            squares[move.to] = undo.captured;
            colors[move.to] = mover.opposite();
        }

        if (move.castle) {
            int rookFrom = move.to > move.from ? move.to + 1 : move.to - 2;
            int rookTo = move.to > move.from ? move.to - 1 : move.to + 1;
            hash ^= Zobrist.piece(Piece.ROOK, mover, rookFrom);
            hash ^= Zobrist.piece(Piece.ROOK, mover, rookTo);
            if (move.to > move.from) {
                // the rook of f1 goes back to h1
                squares[move.to + 1] = squares[move.to - 1];
                colors[move.to + 1] = colors[move.to - 1];
                squares[move.to - 1] = null;
                colors[move.to - 1] = null;
            } else {
                // the rook of d1 goes back to a1
                squares[move.to - 2] = squares[move.to + 1];
                colors[move.to - 2] = colors[move.to + 1];
                squares[move.to + 1] = null;
                colors[move.to + 1] = null;
            }
        }

        hash ^= Zobrist.castling(castling) ^ Zobrist.castling(undo.castling);
        hash ^= Zobrist.enPassant(enPassant) ^ Zobrist.enPassant(undo.enPassant);
        hash ^= Zobrist.side();
        castling = undo.castling;
        enPassant = undo.enPassant;
        halfmoveClock = undo.halfmoveClock;
        fullmoveNumber = undo.fullmoveNumber;
        sideToMove = undo.sideToMove;
    }

    // ------------------------------------------------------------------ attacks

    /** True when the square is attacked by a piece of the given colour. */
    public boolean isAttacked(int square, Color by) {
        int file = Square.file(square);
        int rank = Square.rank(square);

        // pawns
        int pawnRank = by == Color.WHITE ? rank - 1 : rank + 1;
        if (pawnRank >= 0 && pawnRank < 8) {
            int[] pawnFiles = {file - 1, file + 1};
            for (int pf : pawnFiles) {
                if (pf < 0 || pf > 7) {
                    continue;
                }
                int sq = Square.of(pf, pawnRank);
                if (squares[sq] == Piece.PAWN && colors[sq] == by) {
                    return true;
                }
            }
        }

        // knights
        int[][] knightSteps = {
                {1, 2}, {2, 1}, {2, -1}, {1, -2}, {-1, -2}, {-2, -1}, {-2, 1}, {-1, 2}};
        for (int[] step : knightSteps) {
            int f = file + step[0];
            int r = rank + step[1];
            if (!Square.onBoard(f, r)) {
                continue;
            }
            int sq = Square.of(f, r);
            if (squares[sq] == Piece.KNIGHT && colors[sq] == by) {
                return true;
            }
        }

        // king
        for (int df = -1; df <= 1; df++) {
            for (int dr = -1; dr <= 1; dr++) {
                if (df == 0 && dr == 0) {
                    continue;
                }
                int f = file + df;
                int r = rank + dr;
                if (!Square.onBoard(f, r)) {
                    continue;
                }
                int sq = Square.of(f, r);
                if (squares[sq] == Piece.KING && colors[sq] == by) {
                    return true;
                }
            }
        }

        // sliders
        int[][] diagonals = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
        for (int[] dir : diagonals) {
            if (isAttackedBySlider(file, rank, dir, by, Piece.BISHOP, Piece.QUEEN)) {
                return true;
            }
        }
        int[][] straight = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] dir : straight) {
            if (isAttackedBySlider(file, rank, dir, by, Piece.ROOK, Piece.QUEEN)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAttackedBySlider(int file, int rank, int[] dir, Color by,
                                      Piece slider, Piece queen) {
        int f = file + dir[0];
        int r = rank + dir[1];
        while (Square.onBoard(f, r)) {
            int sq = Square.of(f, r);
            if (squares[sq] != null) {
                if (colors[sq] == by && (squares[sq] == slider || squares[sq] == queen)) {
                    return true;
                }
                return false;
            }
            f += dir[0];
            r += dir[1];
        }
        return false;
    }

    /** True when the side to move is in check. */
    public boolean inCheck(Color color) {
        int king = kingSquare(color);
        return king >= 0 && isAttacked(king, color.opposite());
    }

    /** True when the side to move has no legal move. */
    public boolean isCheckmate() {
        return inCheck(sideToMove) && MoveGenerator.legalMoves(this).isEmpty();
    }

    /** True when the side to move is not in check and has no legal move. */
    public boolean isStalemate() {
        return !inCheck(sideToMove) && MoveGenerator.legalMoves(this).isEmpty();
    }

    /** Every piece of the given colour on the board. */
    public List<Integer> piecesOf(Color color) {
        List<Integer> list = new ArrayList<>();
        for (int s = 0; s < 64; s++) {
            if (squares[s] != null && colors[s] == color) {
                list.add(s);
            }
        }
        return list;
    }

    /** The hash of the position, calculated from scratch.  Used by the checks. */
    public long recalculatedHash() {
        long h = 0;
        for (int s = 0; s < 64; s++) {
            if (squares[s] != null) {
                h ^= Zobrist.piece(squares[s], colors[s], s);
            }
        }
        h ^= Zobrist.castling(castling);
        h ^= Zobrist.enPassant(enPassant);
        if (sideToMove == Color.BLACK) {
            h ^= Zobrist.side();
        }
        return h;
    }

    private void recomputeHash() {
        long h = 0;
        for (int s = 0; s < 64; s++) {
            if (squares[s] != null) {
                h ^= Zobrist.piece(squares[s], colors[s], s);
            }
        }
        h ^= Zobrist.castling(castling);
        h ^= Zobrist.enPassant(enPassant);
        if (sideToMove == Color.BLACK) {
            h ^= Zobrist.side();
        }
        this.hash = h;
    }
}
