package com.mmunki.chess.android;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * The bar of a player: the picture, the name, the rating and the clock.
 */
public final class PlayerBar extends LinearLayout {

    private final AvatarView avatar = new AvatarView(getContext());
    private final TextView name = new TextView(getContext());
    private final TextView rating = new TextView(getContext());
    private final ClockView clock;

    public PlayerBar(Context context, int accent) {
        super(context);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        int pad = dp(12);
        setPadding(pad, dp(6), pad, dp(6));

        addView(avatar, new LinearLayout.LayoutParams(dp(44), dp(44)));

        LinearLayout texts = new LinearLayout(context);
        texts.setOrientation(VERTICAL);
        name.setTextColor(Theme.TEXT);
        name.setTextSize(15f);
        name.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        rating.setTextColor(Theme.TEXT_MUTED);
        rating.setTextSize(13f);
        texts.addView(name, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        texts.addView(rating, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        addView(texts, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        clock = new ClockView(context, accent);
        addView(clock, new LinearLayout.LayoutParams(dp(130), dp(56)));
    }

    public void setPlayer(String playerName, int ratingValue) {
        name.setText(playerName);
        rating.setText(ratingValue > 0 ? "Rating " + ratingValue : "Unrated");
        avatar.setInitials(initials(playerName));
    }

    public void setActive(boolean active) {
        name.setTextColor(active ? Theme.TEXT : Theme.TEXT_MUTED);
    }

    public ClockView clock() {
        return clock;
    }

    private static String initials(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "?";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            String part = parts[0];
            return part.substring(0, 1).toUpperCase();
        }
        return (parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
