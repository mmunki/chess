package com.mmunki.chess.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * The settings of the game.
 *
 * <p>The settings are kept in a small file in the home directory of the
 * player, so that the sound, the board and the rating survive a restart.
 */
public final class Storage {

    public static final String SOUND = "sound";
    public static final String VOLUME = "volume";
    public static final String PLAYER_NAME = "player.name";
    public static final String RATING = "rating.value";
    public static final String DEVIATION = "rating.deviation";
    public static final String GAMES = "rating.games";
    public static final String WINS = "rating.wins";
    public static final String LOSSES = "rating.losses";
    public static final String DRAWS = "rating.draws";
    public static final String LEVEL = "bot.level";
    public static final String MINUTES = "clock.minutes";
    public static final String INCREMENT = "clock.increment";
    public static final String HIGHLIGHTS = "board.highlights";

    private final File file;

    public Storage() {
        this(new File(new File(System.getProperty("user.home"), ".chess"), "chess.properties"));
    }

    public Storage(File file) {
        this.file = file;
    }

    public File file() {
        return file;
    }

    private Properties properties() {
        Properties properties = new Properties();
        if (file.exists()) {
            try (InputStream in = new FileInputStream(file)) {
                properties.load(in);
            } catch (IOException e) {
                // a broken file is not a reason to stop the game
            }
        }
        return properties;
    }

    public String get(String key, String fallback) {
        String value = properties().getProperty(key);
        return value == null ? fallback : value;
    }

    public int getInt(String key, int fallback) {
        try {
            return Integer.parseInt(get(key, String.valueOf(fallback)).trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public double getDouble(String key, double fallback) {
        try {
            return Double.parseDouble(get(key, String.valueOf(fallback)).trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public boolean getBoolean(String key, boolean fallback) {
        return Boolean.parseBoolean(get(key, String.valueOf(fallback)).trim());
    }

    /** Writes the settings, nothing happens when the file cannot be written. */
    public void set(String key, String value) {
        Properties properties = properties();
        properties.setProperty(key, value);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            return;
        }
        try (OutputStream out = new FileOutputStream(file)) {
            properties.store(out, "chess");
        } catch (IOException e) {
            // the settings are not important enough to bother the player
        }
    }
}
