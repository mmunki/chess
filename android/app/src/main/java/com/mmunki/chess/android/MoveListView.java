package com.mmunki.chess.android;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;

/**
 * The list of the moves that were played, the way a score sheet shows them.
 */
public final class MoveListView extends LinearLayout {

    public MoveListView(Context context) {
        super(context);
        setOrientation(VERTICAL);
    }

    /** Shows the moves of the game. */
    public void setMoves(List<String> scoreSheet) {
        removeAllViews();
        String number = "";
        String white = "";
        String black = "";
        for (String token : scoreSheet) {
            if (token.endsWith(".")) {
                if (!number.isEmpty()) {
                    addRow(number, white, black);
                }
                number = token;
                white = "";
                black = "";
            } else if (white.isEmpty()) {
                white = token;
            } else {
                black = token;
            }
        }
        if (!number.isEmpty()) {
            addRow(number, white, black);
        }
    }

    /** Scrolls to the last move. */
    public void scrollToBottom() {
        post(() -> {
            ViewParent parent = getParent();
            while (parent != null) {
                if (parent instanceof ScrollView) {
                    ((ScrollView) parent).fullScroll(View.FOCUS_DOWN);
                    return;
                }
                parent = parent.getParent();
            }
        });
    }

    private void addRow(String number, String white, String black) {        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(HORIZONTAL);
        row.addView(cell(number, false), new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.4f));
        row.addView(cell(white, true), new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.8f));
        row.addView(cell(black, true), new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.8f));
        addView(row, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private TextView cell(String value, boolean bold) {
        TextView view = new TextView(getContext());
        view.setText(value);
        view.setTextColor(bold ? Theme.TEXT : Theme.TEXT_MUTED);
        view.setTextSize(14f);
        view.setTypeface(Typeface.DEFAULT, bold ? Typeface.BOLD : Typeface.NORMAL);
        view.setPadding(dp(6), dp(4), dp(6), dp(4));
        return view;
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
