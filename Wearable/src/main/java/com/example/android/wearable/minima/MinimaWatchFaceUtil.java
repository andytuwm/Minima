package com.example.android.wearable.minima;

import android.graphics.Color;

/**
 * Created by Andy on 2015-08-22.
 */

public final class MinimaWatchFaceUtil {
    private static final String TAG = "MinimaWatchFaceUtil";

    public static final String COLOR_NAME_DEFAULT_AND_AMBIENT_HOUR_DIGITS = "White";
    public static final int COLOR_VALUE_DEFAULT_AND_AMBIENT_HOUR_DIGITS =
            parseColor(COLOR_NAME_DEFAULT_AND_AMBIENT_HOUR_DIGITS);

    private static int parseColor(String colorName) {
        return Color.parseColor(colorName.toLowerCase());
    }
}
