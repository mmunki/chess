package com.mmunki.chess.android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

/**
 * The round picture with the letters of the player.
 */
public final class AvatarView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private String initials = "?";

    public AvatarView(Context context) {
        super(context);
        text.setAntiAlias(true);
        text.setFakeBoldText(true);
        text.setTextSize(dp(16));
        text.setColor(Theme.TEXT);
    }

    public void setInitials(String initials) {
        this.initials = initials;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int size = Math.min(getWidth(), getHeight());
        if (size <= 0) {
            return;
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Theme.PANEL_LIGHT);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(2));
        paint.setColor(Theme.ACCENT);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - dp(1), paint);

        float x = (size - text.measureText(initials)) / 2f;
        float y = (size + text.descent() - text.ascent()) / 2f;
        canvas.drawText(initials, x, y, text);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
