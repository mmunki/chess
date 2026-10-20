package com.mmunki.chess.ui;

import com.mmunki.chess.ai.Bot;
import com.mmunki.chess.ai.Search;
import com.mmunki.chess.audio.SoundEngine;
import com.mmunki.chess.core.Board;
import com.mmunki.chess.core.Color;
import com.mmunki.chess.core.Game;
import com.mmunki.chess.core.Move;
import com.mmunki.chess.core.Piece;
import com.mmunki.chess.rating.Rating;
import com.mmunki.chess.rating.RatingTest;
import com.mmunki.chess.util.Storage;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * The window of the game.
 *
 * <p>It holds the board, the clocks, the list of the moves and the buttons,
 * and it lets the computer play.
 */
public final class ChessFrame extends JFrame implements BoardPanel.Listener {

    private static final long serialVersionUID = 1L;

    private final BoardPanel boardPanel = new BoardPanel();
    private final PlayerBar topBar = new PlayerBar(Theme.ACCENT);
    private final PlayerBar bottomBar = new PlayerBar(Theme.BLUE);
    private final MoveListPanel moveList = new MoveListPanel();
    private final JLabel status = new JLabel(" ");
    private final JLabel ratingLabel = new JLabel(" ");
    private final SoundEngine sound = new SoundEngine();
    private final Storage storage;
    private final Rating rating;
    private final Bot bot = new Bot();

    private Game game = new Game();
    private Bot.Level level = Bot.MEDIUM;
    private boolean againstComputer = true;
    private Color human = Color.WHITE;
    private int minutes = 5;
    private int incrementSeconds = 0;
    private boolean highlights = true;
    private RatingTest ratingTest;
    private volatile boolean botThinking;
    private Thread botThread;
    private JButton soundButton;

    public ChessFrame(Storage storage) {
        super("Chess");
        this.storage = storage;
        this.rating = loadRating(storage);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(980, 640));
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.BACKGROUND);

        JPanel left = new JPanel(new BorderLayout());
        left.setBackground(Theme.BACKGROUND);
        left.add(topBar, BorderLayout.NORTH);
        left.add(boardPanel, BorderLayout.CENTER);
        left.add(bottomBar, BorderLayout.SOUTH);
        add(left, BorderLayout.CENTER);

        add(buildSidebar(), BorderLayout.EAST);

        boardPanel.setListener(this);
        boardPanel.setGame(game);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stopBot();
                dispose();
                System.exit(0);
            }
        });

        loadSettings();
        updateBars();
        startClocks();
        startClockWatcher();
    }

    /** Watches the clocks, the game is over when the time of a player is up. */
    private void startClockWatcher() {
        Timer watcher = new Timer(200, e -> {
            if (game.isOver() || botThinking) {
                return;
            }
            Color side = game.sideToMove();
            if (clockOf(side).remaining() <= 0) {
                clockOf(side).stop();
                game.timeout(side);
                endOfGame();
            }
        });
        watcher.setRepeats(true);
        watcher.start();
    }

    /** The board of the window, used by the tests. */
    public BoardPanel boardPanel() {
        return boardPanel;
    }

    // ------------------------------------------------------------------ layout

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(Theme.PANEL);
        sidebar.setPreferredSize(new Dimension(300, 100));
        sidebar.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        JPanel head = new JPanel(new BorderLayout());
        head.setBackground(Theme.PANEL);
        head.setBorder(BorderFactory.createEmptyBorder(14, 16, 10, 16));
        JLabel title = new JLabel("Game");
        title.setFont(Theme.font(Font.BOLD, 20));
        title.setForeground(Theme.TEXT);
        head.add(title, BorderLayout.WEST);
        status.setFont(Theme.font(Font.PLAIN, 13));
        status.setForeground(Theme.TEXT_MUTED);
        head.add(status, BorderLayout.EAST);
        sidebar.add(head, BorderLayout.NORTH);

        JPanel centre = new JPanel(new BorderLayout());
        centre.setBackground(Theme.PANEL);
        centre.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        centre.add(moveList, BorderLayout.CENTER);
        ratingLabel.setFont(Theme.font(Font.PLAIN, 13));
        ratingLabel.setForeground(Theme.TEXT_MUTED);
        ratingLabel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        centre.add(ratingLabel, BorderLayout.SOUTH);
        sidebar.add(centre, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(4, 2, 10, 10));
        buttons.setBackground(Theme.PANEL);
        buttons.setBorder(BorderFactory.createEmptyBorder(12, 16, 16, 16));
        buttons.add(button("New game", FlatButton.primary("New game"), e -> newGame()));
        buttons.add(button("Undo", FlatButton.secondary("Undo"), e -> undo()));
        buttons.add(button("Hint", FlatButton.secondary("Hint"), e -> hint()));
        buttons.add(button("Resign", FlatButton.danger("Resign"), e -> resign()));
        buttons.add(button("Flip", FlatButton.secondary("Flip board"), e -> flipBoard()));
        buttons.add(button("Settings", FlatButton.secondary("Settings"), e -> settings()));
        buttons.add(button("Rating test", FlatButton.secondary("Rating test"), e -> startRatingTest()));
        soundButton = FlatButton.secondary(sound.isEnabled() ? "Sound: on" : "Sound: off");
        buttons.add(button("Sound", soundButton, e -> toggleSound()));
        sidebar.add(buttons, BorderLayout.SOUTH);
        return sidebar;
    }

    private JButton button(String name, JButton button, java.awt.event.ActionListener action) {
        button.setToolTipText(name);
        button.addActionListener(action);
        return button;
    }

    // ------------------------------------------------------------------ settings

    private void loadSettings() {
        sound.setEnabled(storage.getBoolean(Storage.SOUND, true));
        sound.setVolume((float) storage.getDouble(Storage.VOLUME, 0.7));
        highlights = storage.getBoolean(Storage.HIGHLIGHTS, true);
        minutes = storage.getInt(Storage.MINUTES, 5);
        incrementSeconds = storage.getInt(Storage.INCREMENT, 0);
        String name = storage.get(Storage.PLAYER_NAME, "You");
        String levelName = storage.get(Storage.LEVEL, Bot.MEDIUM.name);
        for (Bot.Level candidate : Bot.LEVELS) {
            if (candidate.name.equals(levelName)) {
                level = candidate;
            }
        }
        bottomBar.setPlayer(name, rating.games() > 0 ? rating.display() : 0);
    }

    private Rating loadRating(Storage storage) {
        Rating loaded = new Rating(storage.getDouble(Storage.RATING, 1500),
                storage.getDouble(Storage.DEVIATION, 200));
        loaded.setGames(storage.getInt(Storage.GAMES, 0));
        loaded.setResults(storage.getInt(Storage.WINS, 0), storage.getInt(Storage.LOSSES, 0),
                storage.getInt(Storage.DRAWS, 0));
        return loaded;
    }

    private void saveRating() {
        storage.set(Storage.RATING, String.valueOf(rating.rating()));
        storage.set(Storage.DEVIATION, String.valueOf(rating.deviation()));
        storage.set(Storage.GAMES, String.valueOf(rating.games()));
        storage.set(Storage.WINS, String.valueOf(rating.wins()));
        storage.set(Storage.LOSSES, String.valueOf(rating.losses()));
        storage.set(Storage.DRAWS, String.valueOf(rating.draws()));
    }

    // ------------------------------------------------------------------ the game

    private void newGame() {
        NewGameDialog dialog = new NewGameDialog(this, level, minutes, incrementSeconds);
        dialog.setVisible(true);
        if (!dialog.accepted()) {
            return;
        }
        againstComputer = dialog.againstComputer();
        level = dialog.level();
        minutes = dialog.minutes();
        incrementSeconds = dialog.increment();
        storage.set(Storage.LEVEL, level.name);
        storage.set(Storage.MINUTES, String.valueOf(minutes));
        storage.set(Storage.INCREMENT, String.valueOf(incrementSeconds));
        ratingTest = null;
        human = Color.WHITE;
        startGame();
    }

    private void startGame() {
        stopBot();
        game = new Game();
        boardPanel.setGame(game);
        boardPanel.setFlipped(againstComputer && human == Color.BLACK);
        boardPanel.setHighlightsEnabled(highlights);
        moveList.setMoves(game.scoreSheet());
        startClocks();
        updateBars();
        if (againstComputer && game.sideToMove() != human) {
            startBot();
        }
    }

    private void startClocks() {
        long millis = minutes * 60_000L;
        topBar.clock().setTime(millis);
        bottomBar.clock().setTime(millis);
        topBar.clock().setIncrement(incrementSeconds * 1000L);
        bottomBar.clock().setIncrement(incrementSeconds * 1000L);
        topBar.clock().stop();
        bottomBar.clock().stop();
        if (!game.isOver()) {
            clockOf(game.sideToMove()).start();
        }
    }

    private com.mmunki.chess.ui.ClockPanel clockOf(Color color) {
        return color == Color.WHITE ? bottomBar.clock() : topBar.clock();
    }

    private void updateBars() {
        String playerName = storage.get(Storage.PLAYER_NAME, "You");
        int playerRating = rating.games() > 0 ? rating.display() : 0;
        bottomBar.setPlayer(playerName, playerRating);
        bottomBar.setActive(game.sideToMove() == human);
        if (againstComputer) {
            Bot.Level opponent = ratingTest != null ? ratingTest.nextLevel() : level;
            topBar.setPlayer(opponent.name + "  (" + opponent.elo + ")", opponent.elo);
        } else {
            topBar.setPlayer("Friend", 0);
        }
        topBar.setActive(game.sideToMove() != human);
        ratingLabel.setText(ratingTest != null ? ratingTest.progress()
                : "Your rating: " + (rating.games() > 0 ? rating.display() : "unrated")
                + "   games " + rating.games());
    }

    // ------------------------------------------------------------------ moves

    @Override
    public void movePicked(Move move) {
        if (game.isOver() || botThinking) {
            return;
        }
        Board board = game.board();
        boolean capture = move.enPassant || board.pieceAt(move.to) != null;
        if (!game.play(move)) {
            sound.play(SoundEngine.Sound.ILLEGAL);
            return;
        }
        afterMove(move, capture);
    }

    @Override
    public void promotionPicked(Move move) {
        PromotionDialog dialog = new PromotionDialog(this, human);
        dialog.setVisible(true);
        Piece piece = dialog.chosen();
        if (piece == null) {
            return;
        }
        Move promotion = new Move(move.from, move.to, piece, false, false);
        Board board = game.board();
        boolean capture = board.pieceAt(move.to) != null;
        if (game.play(promotion)) {
            afterMove(promotion, capture);
        }
    }

    @Override
    public void squarePicked(int square) {
        // nothing to do, the board shows the picked square itself
    }

    private void afterMove(Move last, boolean capture) {
        boardPanel.setHint(null);
        boardPanel.animate(last);
        playSound(last, capture);
        moveList.setMoves(game.scoreSheet());
        moveList.scrollToBottom();

        Color mover = game.board().sideToMove().opposite();
        clockOf(mover).stop();
        clockOf(mover).addIncrement();
        if (!game.isOver()) {
            clockOf(game.sideToMove()).start();
        }
        updateBars();

        if (game.isOver()) {
            endOfGame();
            return;
        }
        if (againstComputer && game.sideToMove() != human) {
            startBot();
        }
    }

    private void playSound(Move last, boolean capture) {
        if (last == null) {
            return;
        }
        Board board = game.board();
        if (last.castle) {
            sound.play(SoundEngine.Sound.CASTLE);
        } else if (capture) {
            sound.play(SoundEngine.Sound.CAPTURE);
        } else {
            sound.play(SoundEngine.Sound.MOVE);
        }
        if (board.inCheck(board.sideToMove())) {
            sound.play(SoundEngine.Sound.CHECK);
        }
    }

    private void startBot() {
        botThinking = true;
        boardPanel.setInteractive(false);
        status.setText("thinking ...");
        Board copy = new Board(game.board());
        Bot.Level botLevel = ratingTest != null ? ratingTest.nextLevel() : level;
        botThread = new Thread(() -> {
            Move move = bot.chooseMove(copy, botLevel);
            SwingUtilities.invokeLater(() -> {
                botThinking = false;
                boardPanel.setInteractive(true);
                status.setText(" ");
                if (move != null && !game.isOver()) {
                    if (game.play(move)) {
                        afterMove(move, false);
                    }
                }
            });
        }, "chess-bot");
        botThread.setDaemon(true);
        botThread.start();
    }

    private void stopBot() {
        if (botThread != null) {
            botThread.interrupt();
            botThread = null;
        }
        botThinking = false;
    }

    // ------------------------------------------------------------------ buttons

    private void undo() {
        if (ratingTest != null) {
            // a move that is taken back would make the test meaningless
            return;
        }
        stopBot();
        if (game.history().isEmpty()) {
            return;
        }
        game.undo();
        if (againstComputer && game.sideToMove() == human && !game.history().isEmpty()) {
            game.undo();
        }
        boardPanel.setGame(game);
        moveList.setMoves(game.scoreSheet());
        startClocks();
        updateBars();
        sound.play(SoundEngine.Sound.CLICK);
    }

    private void hint() {
        if (game.isOver() || botThinking) {
            return;
        }
        status.setText("looking for a good move ...");
        Thread thread = new Thread(() -> {
            Board copy = new Board(game.board());
            Search.Result result = new Search(copy).search(6, 700);
            SwingUtilities.invokeLater(() -> {
                status.setText(" ");
                if (result.move != null) {
                    boardPanel.setHint(result.move);
                }
            });
        }, "chess-hint");
        thread.setDaemon(true);
        thread.start();
    }

    private void resign() {
        if (game.isOver()) {
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this, "Give up the game?", "Resign",
                JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            game.resign(game.sideToMove());
            endOfGame();
        }
    }

    private void flipBoard() {
        boardPanel.setFlipped(!boardPanel.isFlipped());
        sound.play(SoundEngine.Sound.CLICK);
    }

    private void settings() {
        SettingsDialog dialog = new SettingsDialog(this, storage.get(Storage.PLAYER_NAME, "You"),
                sound.isEnabled(), sound.volume(), highlights);
        dialog.setVisible(true);
        sound.setEnabled(dialog.soundOn());
        sound.setVolume(dialog.volume());
        highlights = dialog.highlightsOn();
        storage.set(Storage.SOUND, String.valueOf(dialog.soundOn()));
        storage.set(Storage.VOLUME, String.valueOf(dialog.volume()));
        storage.set(Storage.HIGHLIGHTS, String.valueOf(dialog.highlightsOn()));
        storage.set(Storage.PLAYER_NAME, dialog.playerName());
        updateBars();
    }

    private void toggleSound() {
        sound.setEnabled(!sound.isEnabled());
        storage.set(Storage.SOUND, String.valueOf(sound.isEnabled()));
        sound.play(SoundEngine.Sound.CLICK);
        updateSoundButton();
    }

    private void updateSoundButton() {
        if (soundButton != null) {
            soundButton.setText(sound.isEnabled() ? "Sound: on" : "Sound: off");
        }
    }

    // ------------------------------------------------------------------ the end

    private void endOfGame() {
        stopBot();
        boardPanel.setInteractive(false);
        clockOf(Color.WHITE).stop();
        clockOf(Color.BLACK).stop();

        boolean won = game.winner() == human;
        boolean draw = game.result() == Game.Result.DRAW;
        String headline;
        String message;
        if (draw) {
            headline = "Draw";
            sound.play(SoundEngine.Sound.DRAW);
            message = reasonText();
        } else if (won) {
            headline = "You won";
            sound.play(SoundEngine.Sound.WIN);
            message = reasonText();
        } else {
            headline = "You lost";
            sound.play(SoundEngine.Sound.LOSE);
            message = reasonText();
        }

        int oldRating = rating.display();
        if (againstComputer && ratingTest == null) {
            double score = draw ? 0.5 : won ? 1 : 0;
            rating.record(score, level.elo, 60);
            saveRating();
            updateBars();
        }
        String detail;
        if (againstComputer && ratingTest == null) {
            int change = rating.display() - oldRating;
            detail = "Rating " + oldRating + " -> " + rating.display()
                    + (change > 0 ? "  (+" + change + ")" : change < 0 ? "  (" + change + ")" : "");
        } else {
            detail = "";
        }

        new GameOverDialog(this, headline, message, detail).setVisible(true);

        if (ratingTest != null) {
            double score = draw ? 0.5 : won ? 1 : 0;
            ratingTest.record(score, ratingTest.nextLevel());
            saveRating();
            updateBars();
            if (ratingTest.finished()) {
                new GameOverDialog(this, "Rating test finished",
                        "Your rating is now " + rating.display(),
                        ratingTest.progress()).setVisible(true);
                ratingTest = null;
                updateBars();
            } else {
                human = human == Color.WHITE ? Color.BLACK : Color.WHITE;
                startGame();
            }
        }
    }

    private String reasonText() {
        switch (game.end()) {
            case CHECKMATE:
                return "by checkmate";
            case STALEMATE:
                return "by stalemate";
            case FIFTY_MOVE:
                return "by the fifty move rule";
            case REPETITION:
                return "by repetition";
            case INSUFFICIENT_MATERIAL:
                return "there was not enough material left";
            case RESIGNATION:
                return "by resignation";
            case TIME:
                return "on time";
            case AGREEMENT:
                return "by agreement";
            default:
                return "";
        }
    }

    // ------------------------------------------------------------------ rating test

    private void startRatingTest() {
        againstComputer = true;
        ratingTest = new RatingTest(rating);
        human = Color.WHITE;
        startGame();
        updateBars();
    }
}
