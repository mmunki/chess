package com.mmunki.chess.android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import java.util.Locale;

/**
 * The clock of a player.
 *
 * <p>The clock shows the time that is left and a bar that becomes shorter
 * while the player thinks.
 */
public final class ClockView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            if (running) {
                remainingMillis -= 100;
                if (remainingMillis <= 0) {
                    remainingMillis = 0;
                    running = false;
                }
                invalidate();
            }
            if (running) {
                handler.postDelayed(this, 100);
            }
        }
    };

    private long remainingMillis;
    private long incrementMillis;
    private long totalMillis;
    private boolean running;
    private final int accent;

    public ClockView(Context context, int accent) {
        super(context);
        this.accent = accent;
        text.setAntiAlias(true);
        text.setFakeBoldText(true);
        text.setTextSize(dp(22));
        text.setColor(Theme.TEXT);
    }

    public void setTime(long millis) {
        this.totalMillis = millis;
        this.remainingMillis = millis;
        invalidate();
    }

    public void setIncrement(long millis) {
        this.incrementMillis = millis;
    }

    public void start() {
        running = true;
        handler.removeCallbacks(tick);
        handler.postDelayed(tick, 100);
        invalidate();
    }

    public void stop() {
        running = false;
        handler.removeCallbacks(tick);
        invalidate();
    }

    public boolean isRunning() {
        return running;
    }

    /** The time that is left. */
    public long remaining() {
        return remainingMillis;
    }

    /** The time that is added after a move. */
    public void addIncrement() {
        remainingMillis += incrementMillis;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }
        float radius = dp(10);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x5A000000);
        canvas.drawRoundRect(new RectF(0, 0, width, height), radius, radius, paint);

        double fraction = totalMillis > 0 ? (double) remainingMillis / totalMillis : 0;
        paint.setColor(accent);
        canvas.drawRoundRect(new RectF(0, 0, (float) (width * fraction), height), radius, radius, paint);

        boolean low = remainingMillis < 30000;
        text.setColor(low ? 0xFF302020 : Theme.TEXT);
        String value = format(remainingMillis);
        float x = (width - text.measureText(value)) / 2f;
        float y = (height + text.descent() - text.ascent()) / 2f;
        canvas.drawText(value, x, y, text);
    }

    private static String format(long millis) {
        long seconds = Math.max(0, millis) / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        long hours = minutes / 60;
        minutes = minutes % 60;
        if (hours > 0) {
            return String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format(Locale.US, "%d:%02d", minutes, seconds);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
