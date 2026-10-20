package com.mmunki.chess.android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

/**
 * A button in the style of the game: a rounded rectangle that changes its
 * colour when it is pressed.
 */
public final class FlatButton extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int normal;
    private final int pressed;
    private String label = "";

    public FlatButton(Context context, String label, int normal, int pressed) {
        super(context);
        this.label = label;
        this.normal = normal;
        this.pressed = pressed;
        text.setAntiAlias(true);
        text.setFakeBoldText(true);
        text.setTextSize(dp(14));
        text.setColor(Theme.TEXT);
        setClickable(true);
        setFocusable(true);
    }

    public void setLabel(String label) {
        this.label = label;
        invalidate();
    }

    public String label() {
        return label;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }
        float radius = height / 2f;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(isPressed() ? pressed : normal);
        canvas.drawRoundRect(new RectF(0, 0, width, height), radius, radius, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1f);
        paint.setColor(0x3C000000);
        canvas.drawRoundRect(new RectF(0, 0, width - 1, height - 1), radius, radius, paint);

        float x = (width - text.measureText(label)) / 2f;
        float y = (height + text.descent() - text.ascent()) / 2f;
        canvas.drawText(label, x, y, text);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
