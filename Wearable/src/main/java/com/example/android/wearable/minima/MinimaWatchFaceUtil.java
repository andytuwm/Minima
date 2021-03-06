package com.example.android.wearable.minima;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

/**
 * Created by Andy on 2015-08-22.
 */

final class MinimaWatchFaceUtil {
    private static final String TAG = "MinimaWatchFaceUtil";

    private static final String COLOR_NAME_DEFAULT_AND_AMBIENT_HOUR_DIGITS = "White";
    private static final String COLOR_NAME_BLACK_BACKGROUND = "Black";
    static final int COLOR_VALUE_DEFAULT_AND_AMBIENT_HOUR_DIGITS =
            parseColor(COLOR_NAME_DEFAULT_AND_AMBIENT_HOUR_DIGITS);
    static final int COLOR_VALUE_BLACK_BACKGROUND = parseColor(COLOR_NAME_BLACK_BACKGROUND);

    private static int parseColor(String colorName) {
        return Color.parseColor(colorName.toLowerCase());
    }

    static Paint createTextPaint(int color, Typeface typeface) {
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setTypeface(typeface);
        paint.setAntiAlias(true);
        return paint;
    }
}
