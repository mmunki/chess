package com.mmunki.chess.android;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * The settings of the game.
 *
 * <p>The settings are kept in the preferences of the phone, so that the sound,
 * the board and the rating survive a restart of the game.
 */
public final class Settings {

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

    private final SharedPreferences preferences;

    public Settings(Context context) {
        this.preferences = context.getSharedPreferences("chess", Context.MODE_PRIVATE);
    }

    public String get(String key, String fallback) {
        return preferences.getString(key, fallback);
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

    /** Writes the settings. */
    public void set(String key, String value) {
        preferences.edit().putString(key, value).apply();
    }
}
