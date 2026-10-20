package com.mmunki.chess.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.mmunki.chess.ai.Bot;
import com.mmunki.chess.ai.Search;
import com.mmunki.chess.core.Board;
import com.mmunki.chess.core.Color;
import com.mmunki.chess.core.Game;
import com.mmunki.chess.core.Move;
import com.mmunki.chess.core.Piece;
import com.mmunki.chess.rating.Rating;
import com.mmunki.chess.rating.RatingTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * The window of the game.
 *
 * <p>It holds the board, the clocks, the list of the moves and the buttons,
 * and it lets the computer play.
 */
public final class MainActivity extends Activity implements BoardView.Listener {

    private static final int[] MINUTES = {1, 2, 3, 5, 10, 15, 30};
    private static final int[] INCREMENTS = {0, 1, 2, 3, 5, 10};

    private final Bot bot = new Bot();
    private final AudioEngine sound = new AudioEngine();
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService worker = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "chess-bot");
        thread.setDaemon(true);
        return thread;
    });
    private final Runnable clockWatcher = new Runnable() {
        @Override
        public void run() {
            watchTheClock();
            main.postDelayed(this, 200);
        }
    };

    private Settings settings;
    private Rating rating;
    private Game game = new Game();
    private Bot.Level level = Bot.MEDIUM;
    private boolean againstComputer = true;
    private Color human = Color.WHITE;
    private int minutes = 5;
    private int incrementSeconds = 0;
    private boolean highlights = true;
    private RatingTest ratingTest;
    private volatile boolean botThinking;
    private Future<?> botTask;
    private int generation;
    private boolean flipped;

    private AlertDialog promotionDialog;
    private BoardView boardView;
    private PlayerBar opponentBar;
    private PlayerBar playerBar;
    private TextView status;
    private TextView ratingLabel;
    private MoveListView moveList;
    private FlatButton soundButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = new Settings(this);
        rating = loadRating(settings);
        loadSettings();
        buildUi();
        boardView.setListener(this);
        boardView.setGame(game);
        updateBars();
        startClocks();
        main.post(clockWatcher);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // the game keeps its state, only the windows are built again
        buildUi();
        boardView.setListener(this);
        boardView.setGame(game);
        boardView.setFlipped(flipped);
        boardView.setHighlightsEnabled(highlights);
        moveList.setMoves(game.scoreSheet());
        updateBars();
        startClocks();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        main.removeCallbacks(clockWatcher);
        worker.shutdownNow();
    }

    // ------------------------------------------------------------------ layout

    private void buildUi() {
        boolean landscape = getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;

        boardView = new BoardView(this);
        opponentBar = new PlayerBar(this, Theme.ACCENT);
        playerBar = new PlayerBar(this, Theme.BLUE);
        status = text(13, Theme.TEXT_MUTED);
        ratingLabel = text(13, Theme.TEXT_MUTED);
        moveList = new MoveListView(this);

        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.addView(opponentBar, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        int boardSize = boardSize(landscape);
        column.addView(boardView, new LinearLayout.LayoutParams(boardSize, boardSize));
        column.addView(playerBar, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout sidebar = buildSidebar();

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Theme.BACKGROUND);
        if (landscape) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.addView(column, new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.MATCH_PARENT, 1f));
            ScrollView scroll = new ScrollView(this);
            scroll.addView(sidebar);
            row.addView(scroll, new LinearLayout.LayoutParams(dp(300),
                    ViewGroup.LayoutParams.MATCH_PARENT));
            root.addView(row, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        } else {
            ScrollView scroll = new ScrollView(this);
            LinearLayout all = new LinearLayout(this);
            all.setOrientation(LinearLayout.VERTICAL);
            all.addView(column, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            all.addView(sidebar, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            scroll.addView(all);
            root.addView(scroll, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
        setContentView(root);
    }

    /** The side of the board, it is under the board when the phone stands. */
    private LinearLayout buildSidebar() {
        LinearLayout sidebar = new LinearLayout(this);
        sidebar.setOrientation(LinearLayout.VERTICAL);
        sidebar.setPadding(dp(12), dp(8), dp(12), dp(12));

        status.setPadding(0, dp(4), 0, dp(4));
        sidebar.addView(status, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ratingLabel.setPadding(0, dp(4), 0, dp(4));
        sidebar.addView(ratingLabel, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Theme.PANEL);
        scroll.addView(moveList);
        sidebar.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(150)));

        GridLayout buttons = new GridLayout(this);
        buttons.setColumnCount(2);
        addButton(buttons, "New game", Theme.ACCENT, Theme.ACCENT_DARK, v -> newGame());
        addButton(buttons, "Undo", Theme.PANEL_LIGHT, 0xFF2A2826, v -> undo());
        addButton(buttons, "Hint", Theme.PANEL_LIGHT, 0xFF2A2826, v -> hint());
        addButton(buttons, "Resign", 0xFF8A322B, Theme.DANGER, v -> resign());
        addButton(buttons, "Flip board", Theme.PANEL_LIGHT, 0xFF2A2826, v -> flipBoard());
        addButton(buttons, "Settings", Theme.PANEL_LIGHT, 0xFF2A2826, v -> settings());
        addButton(buttons, "Rating test", Theme.PANEL_LIGHT, 0xFF2A2826, v -> startRatingTest());
        soundButton = new FlatButton(this, sound.isEnabled() ? "Sound: on" : "Sound: off",
                Theme.PANEL_LIGHT, 0xFF2A2826);
        soundButton.setOnClickListener(v -> toggleSound());
        addButton(buttons, soundButton);
        sidebar.addView(buttons, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return sidebar;
    }

    private void addButton(GridLayout grid, String label, int normal, int pressed,
                           View.OnClickListener action) {
        FlatButton button = new FlatButton(this, label, normal, pressed);
        button.setOnClickListener(action);
        addButton(grid, button);
    }

    private void addButton(GridLayout grid, FlatButton button) {
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = dp(44);
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
        params.setMargins(dp(4), dp(4), dp(4), dp(4));
        grid.addView(button, params);
    }

    private TextView text(float size, int color) {
        TextView view = new TextView(this);
        view.setTextSize(size);
        view.setTextColor(color);
        return view;
    }

    /** The side of the board that fits on the screen. */
    private int boardSize(boolean landscape) {
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        if (landscape) {
            return Math.min(height - dp(140), (int) (width * 0.62f));
        }
        return width - dp(16);
    }

    // ------------------------------------------------------------------ settings

    private void loadSettings() {
        sound.setEnabled(settings.getBoolean(Settings.SOUND, true));
        sound.setVolume((float) settings.getDouble(Settings.VOLUME, 0.7));
        highlights = settings.getBoolean(Settings.HIGHLIGHTS, true);
        minutes = settings.getInt(Settings.MINUTES, 5);
        incrementSeconds = settings.getInt(Settings.INCREMENT, 0);
        String levelName = settings.get(Settings.LEVEL, Bot.MEDIUM.name);
        for (Bot.Level candidate : Bot.LEVELS) {
            if (candidate.name.equals(levelName)) {
                level = candidate;
            }
        }
    }

    private Rating loadRating(Settings settings) {
        Rating loaded = new Rating(settings.getDouble(Settings.RATING, 1500),
                settings.getDouble(Settings.DEVIATION, 200));
        loaded.setGames(settings.getInt(Settings.GAMES, 0));
        loaded.setResults(settings.getInt(Settings.WINS, 0), settings.getInt(Settings.LOSSES, 0),
                settings.getInt(Settings.DRAWS, 0));
        return loaded;
    }

    private void saveRating() {
        settings.set(Settings.RATING, String.valueOf(rating.rating()));
        settings.set(Settings.DEVIATION, String.valueOf(rating.deviation()));
        settings.set(Settings.GAMES, String.valueOf(rating.games()));
        settings.set(Settings.WINS, String.valueOf(rating.wins()));
        settings.set(Settings.LOSSES, String.valueOf(rating.losses()));
        settings.set(Settings.DRAWS, String.valueOf(rating.draws()));
    }

    // ------------------------------------------------------------------ the game

    private void newGame() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(24), dp(12), dp(24), dp(4));

        RadioGroup opponent = new RadioGroup(this);
        RadioButton computer = new RadioButton(this);
        computer.setText("Play against the computer");
        RadioButton friend = new RadioButton(this);
        friend.setText("Play against a friend on this phone");
        opponent.addView(computer);
        opponent.addView(friend);
        computer.setChecked(againstComputer);
        friend.setChecked(!againstComputer);
        layout.addView(opponent);

        List<String> levels = new ArrayList<>();
        for (Bot.Level candidate : Bot.LEVELS) {
            levels.add(candidate.name + "  (" + candidate.elo + ")");
        }
        Spinner levelSpinner = spinner(levels, levelIndex(level));
        layout.addView(label("The computer plays"));
        layout.addView(levelSpinner);

        List<String> minuteList = new ArrayList<>();
        for (int value : MINUTES) {
            minuteList.add(value + " minutes");
        }
        Spinner minutesSpinner = spinner(minuteList, indexOf(MINUTES, minutes));
        layout.addView(label("Every player gets"));
        layout.addView(minutesSpinner);

        List<String> incrementList = new ArrayList<>();
        for (int value : INCREMENTS) {
            incrementList.add(value + " seconds after a move");
        }
        Spinner incrementSpinner = spinner(incrementList, indexOf(INCREMENTS, incrementSeconds));
        layout.addView(label("The clock gets"));
        layout.addView(incrementSpinner);

        new AlertDialog.Builder(this)
                .setTitle("New game")
                .setView(layout)
                .setPositiveButton("Start", (dialog, which) -> {
                    againstComputer = computer.isChecked();
                    level = Bot.LEVELS.get(levelSpinner.getSelectedItemPosition());
                    minutes = MINUTES[minutesSpinner.getSelectedItemPosition()];
                    incrementSeconds = INCREMENTS[incrementSpinner.getSelectedItemPosition()];
                    settings.set(Settings.LEVEL, level.name);
                    settings.set(Settings.MINUTES, String.valueOf(minutes));
                    settings.set(Settings.INCREMENT, String.valueOf(incrementSeconds));
                    ratingTest = null;
                    human = Color.WHITE;
                    startGame();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private TextView label(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextColor(Theme.TEXT_MUTED);
        view.setTextSize(13f);
        view.setPadding(dp(4), dp(10), dp(4), 0);
        return view;
    }

    private Spinner spinner(List<String> values, int selection) {
        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, values);
        spinner.setAdapter(adapter);
        spinner.setSelection(Math.max(0, Math.min(values.size() - 1, selection)));
        return spinner;
    }

    private int levelIndex(Bot.Level level) {
        for (int i = 0; i < Bot.LEVELS.size(); i++) {
            if (Bot.LEVELS.get(i) == level) {
                return i;
            }
        }
        return 0;
    }

    private int indexOf(int[] values, int value) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] == value) {
                return i;
            }
        }
        return 0;
    }

    private void startGame() {
        stopBot();
        game = new Game();
        boardView.setGame(game);
        flipped = againstComputer && human == Color.BLACK;
        boardView.setFlipped(flipped);
        boardView.setHighlightsEnabled(highlights);
        moveList.setMoves(game.scoreSheet());
        startClocks();
        updateBars();
        if (againstComputer && game.sideToMove() != human) {
            startBot();
        }
    }

    private void startClocks() {
        long millis = minutes * 60_000L;
        opponentBar.clock().setTime(millis);
        playerBar.clock().setTime(millis);
        opponentBar.clock().setIncrement(incrementSeconds * 1000L);
        playerBar.clock().setIncrement(incrementSeconds * 1000L);
        opponentBar.clock().stop();
        playerBar.clock().stop();
        if (!game.isOver()) {
            clockOf(game.sideToMove()).start();
        }
    }

    private ClockView clockOf(Color color) {
        return color == Color.WHITE ? playerBar.clock() : opponentBar.clock();
    }

    private void updateBars() {
        String playerName = settings.get(Settings.PLAYER_NAME, "You");
        int playerRating = rating.games() > 0 ? rating.display() : 0;
        playerBar.setPlayer(playerName, playerRating);
        playerBar.setActive(game.sideToMove() == human);
        if (againstComputer) {
            Bot.Level opponent = ratingTest != null ? ratingTest.nextLevel() : level;
            opponentBar.setPlayer(opponent.name + "  (" + opponent.elo + ")", opponent.elo);
        } else {
            opponentBar.setPlayer("Friend", 0);
        }
        opponentBar.setActive(game.sideToMove() != human);
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
            sound.play(AudioEngine.Sound.ILLEGAL);
            return;
        }
        afterMove(move, capture);
    }

    @Override
    public void promotionPicked(Move move) {
        showPromotion(move);
    }

    @Override
    public void squarePicked(int square) {
        // nothing to do, the board shows the picked square itself
    }

    /** The player chooses the piece that the pawn becomes. */
    private void showPromotion(Move move) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(dp(16), dp(16), dp(16), dp(16));

        final Piece[] pieces = {Piece.QUEEN, Piece.ROOK, Piece.BISHOP, Piece.KNIGHT};
        final String[] glyphs = {"♛", "♜", "♝", "♞"};
        for (int i = 0; i < pieces.length; i++) {
            final Piece piece = pieces[i];
            TextView button = new TextView(this);
            button.setText(glyphs[i]);
            button.setTextColor(Theme.TEXT);
            button.setTextSize(36f);
            button.setGravity(Gravity.CENTER);
            button.setPadding(dp(16), dp(8), dp(16), dp(8));
            button.setOnClickListener(v -> {
                Move promotion = new Move(move.from, move.to, piece, false, false);
                Board board = game.board();
                boolean capture = board.pieceAt(move.to) != null;
                if (game.play(promotion)) {
                    afterMove(promotion, capture);
                }
                promotionDialog.dismiss();
            });
            layout.addView(button, new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        }

        promotionDialog = new AlertDialog.Builder(this)
                .setTitle("The pawn is promoted")
                .setView(layout)
                .setNegativeButton("Cancel", (dialog, which) -> boardView.invalidate())
                .create();
        promotionDialog.show();
    }

    private void afterMove(Move last, boolean capture) {
        boardView.setHint(null);
        boardView.animate(last);
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
            sound.play(AudioEngine.Sound.CASTLE);
        } else if (capture) {
            sound.play(AudioEngine.Sound.CAPTURE);
        } else {
            sound.play(AudioEngine.Sound.MOVE);
        }
        if (board.inCheck(board.sideToMove())) {
            sound.play(AudioEngine.Sound.CHECK);
        }
    }

    private void startBot() {
        botThinking = true;
        boardView.setInteractive(false);
        status.setText("thinking ...");
        Board copy = new Board(game.board());
        Bot.Level botLevel = ratingTest != null ? ratingTest.nextLevel() : level;
        int stamp = ++generation;
        botTask = worker.submit(() -> {
            Move move = bot.chooseMove(copy, botLevel);
            main.post(() -> {
                if (stamp != generation) {
                    // the game went on without this move
                    return;
                }
                botThinking = false;
                boardView.setInteractive(true);
                status.setText(" ");
                if (move != null && !game.isOver() && game.play(move)) {
                    afterMove(move, false);
                }
            });
        });
    }

    private void stopBot() {
        generation++;
        if (botTask != null) {
            botTask.cancel(true);
            botTask = null;
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
        boardView.setGame(game);
        moveList.setMoves(game.scoreSheet());
        startClocks();
        updateBars();
        sound.play(AudioEngine.Sound.CLICK);
    }

    private void hint() {
        if (game.isOver() || botThinking) {
            return;
        }
        status.setText("looking for a good move ...");
        Board copy = new Board(game.board());
        worker.submit(() -> {
            Search.Result result = new Search(copy).search(6, 700);
            main.post(() -> {
                status.setText(" ");
                if (result.move != null) {
                    boardView.setHint(result.move);
                }
            });
        });
    }

    private void resign() {
        if (game.isOver()) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Resign")
                .setMessage("Give up the game?")
                .setPositiveButton("Give up", (dialog, which) -> {
                    game.resign(game.sideToMove());
                    endOfGame();
                })
                .setNegativeButton("Play on", null)
                .show();
    }

    private void flipBoard() {
        flipped = !flipped;
        boardView.setFlipped(flipped);
        sound.play(AudioEngine.Sound.CLICK);
    }

    private void settings() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(24), dp(12), dp(24), dp(4));

        EditText name = new EditText(this);
        name.setInputType(InputType.TYPE_CLASS_TEXT);
        name.setText(settings.get(Settings.PLAYER_NAME, "You"));
        layout.addView(label("Your name"));
        layout.addView(name);

        CheckBox soundBox = new CheckBox(this);
        soundBox.setText("Sound");
        soundBox.setChecked(sound.isEnabled());
        layout.addView(soundBox);

        layout.addView(label("Volume"));
        SeekBar volume = new SeekBar(this);
        volume.setMax(100);
        volume.setProgress((int) (sound.volume() * 100));
        layout.addView(volume);

        CheckBox highlightBox = new CheckBox(this);
        highlightBox.setText("Show the squares a piece can go to");
        highlightBox.setChecked(highlights);
        layout.addView(highlightBox);

        new AlertDialog.Builder(this)
                .setTitle("Settings")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    sound.setEnabled(soundBox.isChecked());
                    sound.setVolume(volume.getProgress() / 100f);
                    highlights = highlightBox.isChecked();
                    boardView.setHighlightsEnabled(highlights);
                    settings.set(Settings.SOUND, String.valueOf(soundBox.isChecked()));
                    settings.set(Settings.VOLUME, String.valueOf(volume.getProgress() / 100f));
                    settings.set(Settings.HIGHLIGHTS, String.valueOf(highlightBox.isChecked()));
                    settings.set(Settings.PLAYER_NAME, name.getText().toString());
                    updateSoundButton();
                    updateBars();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void toggleSound() {
        sound.setEnabled(!sound.isEnabled());
        settings.set(Settings.SOUND, String.valueOf(sound.isEnabled()));
        sound.play(AudioEngine.Sound.CLICK);
        updateSoundButton();
    }

    private void updateSoundButton() {
        if (soundButton != null) {
            soundButton.setLabel(sound.isEnabled() ? "Sound: on" : "Sound: off");
        }
    }

    // ------------------------------------------------------------------ the end

    /** Watches the clocks, the game is over when the time of a player is up. */
    private void watchTheClock() {
        if (game.isOver() || botThinking) {
            return;
        }
        Color side = game.sideToMove();
        if (clockOf(side).remaining() <= 0) {
            clockOf(side).stop();
            game.timeout(side);
            endOfGame();
        }
    }

    private void endOfGame() {
        stopBot();
        boardView.setInteractive(false);
        clockOf(Color.WHITE).stop();
        clockOf(Color.BLACK).stop();

        boolean won = game.winner() == human;
        boolean draw = game.result() == Game.Result.DRAW;
        String headline;
        String message;
        if (draw) {
            headline = "Draw";
            sound.play(AudioEngine.Sound.DRAW);
            message = reasonText();
        } else if (won) {
            headline = "You won";
            sound.play(AudioEngine.Sound.WIN);
            message = reasonText();
        } else {
            headline = "You lost";
            sound.play(AudioEngine.Sound.LOSE);
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

        showMessage(headline, message, detail);

        if (ratingTest != null) {
            double score = draw ? 0.5 : won ? 1 : 0;
            ratingTest.record(score, ratingTest.nextLevel());
            saveRating();
            updateBars();
            if (ratingTest.finished()) {
                showMessage("Rating test finished", "Your rating is now " + rating.display(),
                        ratingTest.progress());
                ratingTest = null;
                updateBars();
            } else {
                human = human == Color.WHITE ? Color.BLACK : Color.WHITE;
                startGame();
            }
        }
    }

    private void showMessage(String headline, String message, String detail) {
        String text = detail.isEmpty() ? message : message + "\n\n" + detail;
        new AlertDialog.Builder(this)
                .setTitle(headline)
                .setMessage(text.trim().isEmpty() ? headline : text)
                .setPositiveButton("Ok", null)
                .show();
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

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
