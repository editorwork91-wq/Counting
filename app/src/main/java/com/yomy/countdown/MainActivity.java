package com.yomy.countdown;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends Activity {
    private CountdownView countdownView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.setStatusBarColor(Color.rgb(8, 10, 18));
        window.setNavigationBarColor(Color.rgb(8, 10, 18));
        countdownView = new CountdownView();
        setContentView(countdownView);
    }

    private final class CountdownView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF rect = new RectF();
        private final Handler handler = new Handler(Looper.getMainLooper());
        private final TimeZone cairo = TimeZone.getTimeZone("Africa/Cairo");

        // Change these four values to move the countdown window.
        private final long startMillis = dateMillis(2026, 9, 17, 0, 0);
        private final long targetMillis = dateMillis(2026, 9, 24, 0, 0);

        private final Runnable ticker = new Runnable() {
            @Override public void run() {
                invalidate();
                handler.postDelayed(this, 250);
            }
        };

        CountdownView() {
            super(MainActivity.this);
            setBackground(new ColorDrawable(Color.rgb(8, 10, 18)));
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            setOnApplyWindowInsetsListener((v, insets) -> {
                setPadding(getPaddingLeft(), insets.getSystemWindowInsetTop(),
                        getPaddingRight(), insets.getSystemWindowInsetBottom());
                return insets;
            });
            handler.post(ticker);
        }

        @Override protected void onDetachedFromWindow() {
            handler.removeCallbacks(ticker);
            super.onDetachedFromWindow();
        }

        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            final float w = getWidth();
            final float h = getHeight() - getPaddingTop() - getPaddingBottom();
            final float top = getPaddingTop();
            final long now = System.currentTimeMillis();

            final long remaining = Math.max(0L, targetMillis - now);
            long totalSeconds = remaining / 1000L;
            final long days = totalSeconds / 86400L;
            totalSeconds %= 86400L;
            final long hours = totalSeconds / 3600L;
            totalSeconds %= 3600L;
            final long minutes = totalSeconds / 60L;
            final long seconds = totalSeconds % 60L;

            drawBackdrop(canvas, w, h, top);
            drawHeader(canvas, w, top);
            drawEventCard(canvas, w, top, now);

            final float side = dp(22);
            final float gap = dp(10);
            final float gridTop = top + dp(198);
            final float progressY = top + h - dp(56);
            final float usableGrid = Math.max(dp(150), progressY - gridTop - dp(14));
            final float cardH = Math.min(dp(118), Math.max(dp(72), (usableGrid - gap) / 2f));
            final float cardW = (w - side * 2 - gap) / 2f;

            drawUnit(canvas, side, gridTop, cardW, cardH, days, "DAYS");
            drawUnit(canvas, side + cardW + gap, gridTop, cardW, cardH, hours, "HOURS");
            drawUnit(canvas, side, gridTop + cardH + gap, cardW, cardH, minutes, "MINUTES");
            drawUnit(canvas, side + cardW + gap, gridTop + cardH + gap, cardW, cardH, seconds, "SECONDS");

            drawProgress(canvas, w, h, top, now);
        }

        private void drawBackdrop(Canvas c, float w, float h, float top) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.rgb(8, 10, 18));
            c.drawRect(0, top, w, top + h, paint);
            paint.setColor(Color.argb(28, 90, 120, 255));
            c.drawCircle(w * 0.86f, top + h * 0.13f, w * 0.42f, paint);
            paint.setColor(Color.argb(18, 180, 90, 255));
            c.drawCircle(w * 0.10f, top + h * 0.52f, w * 0.30f, paint);
        }

        private void drawHeader(Canvas c, float w, float top) {
            text(c, "YOMY", w / 2f, top + dp(58), sp(34), true, Color.WHITE);
            text(c, "THE COUNTDOWN", w / 2f, top + dp(82), sp(11), true, Color.rgb(154, 163, 185));
        }

        private void drawEventCard(Canvas c, float w, float top, long now) {
            float l = dp(22), r = w - dp(22), t = top + dp(102), b = t + dp(82);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.rgb(18, 22, 34));
            rect.set(l, t, r, b);
            c.drawRoundRect(rect, dp(20), dp(20), paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(1));
            paint.setColor(Color.rgb(44, 50, 70));
            c.drawRoundRect(rect, dp(20), dp(20), paint);

            paint.setStyle(Paint.Style.FILL);
            text(c, "LAUNCH DAY", l + dp(20), t + dp(31), sp(11), true,
                    Color.rgb(129, 144, 173), Paint.Align.LEFT);
            text(c, "24 SEPTEMBER 2026", l + dp(20), t + dp(56), sp(17), true,
                    Color.WHITE, Paint.Align.LEFT);
            text(c, String.format(Locale.US, "%d%%", Math.round(progress(now) * 100f)),
                    r - dp(20), t + dp(44), sp(13), true, Color.WHITE, Paint.Align.RIGHT);
        }

        private void drawUnit(Canvas c, float x, float y, float cardW, float cardH,
                              long value, String label) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.rgb(17, 21, 32));
            rect.set(x, y, x + cardW, y + cardH);
            c.drawRoundRect(rect, dp(22), dp(22), paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(1));
            paint.setColor(Color.rgb(39, 45, 63));
            c.drawRoundRect(rect, dp(22), dp(22), paint);

            paint.setStyle(Paint.Style.FILL);
            text(c, String.format(Locale.US, "%02d", value),
                    x + cardW / 2f, y + cardH * 0.59f, sp(40), true, Color.WHITE);
            text(c, label, x + cardW / 2f, y + cardH - dp(18),
                    sp(10), true, Color.rgb(124, 136, 164));
        }

        private void drawProgress(Canvas c, float w, float h, float top, long now) {
            float y = top + h - dp(56);
            text(c, "COUNTDOWN STARTED  •  17 SEPTEMBER",
                    dp(22), y, sp(10), true, Color.rgb(117, 130, 157), Paint.Align.LEFT);

            float x1 = dp(22), x2 = w - dp(22), barY = y + dp(17);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.rgb(35, 41, 57));
            rect.set(x1, barY, x2, barY + dp(6));
            c.drawRoundRect(rect, dp(4), dp(4), paint);

            paint.setColor(Color.WHITE);
            rect.right = x1 + (x2 - x1) * progress(now);
            c.drawRoundRect(rect, dp(4), dp(4), paint);
        }

        private float progress(long now) {
            if (now <= startMillis) return 0f;
            if (now >= targetMillis) return 1f;
            return (float) ((now - startMillis) / (double) (targetMillis - startMillis));
        }

        private long dateMillis(int year, int month, int day, int hour, int minute) {
            Calendar c = Calendar.getInstance(cairo, Locale.US);
            c.set(year, month - 1, day, hour, minute, 0);
            c.set(Calendar.MILLISECOND, 0);
            return c.getTimeInMillis();
        }

        private float dp(float value) {
            return value * getResources().getDisplayMetrics().density;
        }

        private float sp(float value) {
            return value * getResources().getDisplayMetrics().scaledDensity;
        }

        private void text(Canvas c, String value, float x, float y, float size,
                          boolean bold, int color) {
            text(c, value, x, y, size, bold, color, Paint.Align.CENTER);
        }

        private void text(Canvas c, String value, float x, float y, float size,
                          boolean bold, int color, Paint.Align align) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(color);
            paint.setTextSize(size);
            paint.setTextAlign(align);
            paint.setTypeface(Typeface.create("sans", bold ? Typeface.BOLD : Typeface.NORMAL));
            c.drawText(value, x, y, paint);
        }
    }
}
