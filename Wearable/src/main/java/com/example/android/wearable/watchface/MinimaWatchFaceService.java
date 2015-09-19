/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.wearable.watchface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Minima watch face - combines the classic analog look with the modern digital clock.
 * <p/>
 */
public class MinimaWatchFaceService extends CanvasWatchFaceService {
    private static final String TAG = "MinimaWatchFaceService";

    private static final Typeface BOLD_TYPEFACE =
            Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
    private static final Typeface NORMAL_TYPEFACE =
            Typeface.create("sans-serif-thin", Typeface.NORMAL);

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private class Engine extends CanvasWatchFaceService.Engine {
        static final float TWO_PI = (float) Math.PI * 2f;

        Paint mHourPaint;
        Paint mMinutePaint;
        Paint mSecondPaint;
        Paint mTickPaint;
        Paint mLargeTickPaint;
        Paint mFadePaint;
        Paint mDayPaint;
        boolean mMute;
        Calendar mCalendar;

        float startAngle;
        int mHourDigitsColor = MinimaWatchFaceUtil.COLOR_VALUE_DEFAULT_AND_AMBIENT_HOUR_DIGITS;
        int mDayDigitsColor = MinimaWatchFaceUtil.COLOR_VALUE_DEFAULT_AND_AMBIENT_HOUR_DIGITS;
        boolean isRound;
        int lastHour;
        String[] monthArray = {"jan","feb","mar","apr","may","jun","jul","aug","sep","oct","nov","dec"};

        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            }
        };
        boolean mRegisteredTimeZoneReceiver = false;

        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        boolean mLowBitAmbient;

        Bitmap mBackgroundBitmap;
        Bitmap mBackgroundScaledBitmap;

        @Override
        public void onCreate(SurfaceHolder holder) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "onCreate");
            }
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(MinimaWatchFaceService.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .build());

            Resources resources = MinimaWatchFaceService.this.getResources();
            Drawable backgroundDrawable = resources.getDrawable(R.drawable.bg, null /* theme */);
            mBackgroundBitmap = ((BitmapDrawable) backgroundDrawable).getBitmap();

            startAngle = 270;

            mHourPaint = createTextPaint(mHourDigitsColor, NORMAL_TYPEFACE);
            mHourPaint.setTextAlign(Paint.Align.CENTER);
            mHourPaint.setLetterSpacing(-0.15f);

            mDayPaint = createTextPaint(mDayDigitsColor, NORMAL_TYPEFACE);

            mMinutePaint = new Paint();
            mMinutePaint.setARGB(255, 200, 200, 200);
            mMinutePaint.setStrokeWidth(6f);
            mMinutePaint.setAntiAlias(true);
            mMinutePaint.setStrokeCap(Paint.Cap.ROUND);

            mSecondPaint = new Paint();
            mSecondPaint.setARGB(255, 255, 0, 0);
            mSecondPaint.setStrokeWidth(5.f);
            mSecondPaint.setAntiAlias(true);
            mSecondPaint.setStrokeCap(Paint.Cap.ROUND);
            mSecondPaint.setStyle(Paint.Style.STROKE);

            mTickPaint = new Paint();
            mTickPaint.setARGB(100, 255, 255, 255);
            mTickPaint.setStrokeWidth(2.f);
            mTickPaint.setAntiAlias(true);

            mLargeTickPaint = new Paint();
            mLargeTickPaint.setARGB(170, 255, 255, 255);
            mLargeTickPaint.setStrokeWidth(3f);
            mLargeTickPaint.setAntiAlias(true);

            mFadePaint = new Paint();
            mFadePaint.setARGB(180, 0, 0, 0);
            mFadePaint.setStrokeWidth(4f);
            mFadePaint.setAntiAlias(true);
            mFadePaint.setStyle(Paint.Style.FILL);

            mCalendar = Calendar.getInstance();
        }

        private Paint createTextPaint(int color, Typeface typeface) {
            Paint paint = new Paint();
            paint.setColor(color);
            paint.setTypeface(typeface);
            paint.setAntiAlias(true);
            return paint;
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "onApplyWindowInsets: " + (insets.isRound() ? "round" : "square"));
            }
            super.onApplyWindowInsets(insets);

            // Load resources that have alternate values for round watches.
            Resources resources = MinimaWatchFaceService.this.getResources();
            isRound = insets.isRound();

            float hourTextSize = resources.getDimension(isRound ? R.dimen.digital_text_size_round : R.dimen.digital_text_size);
            float dayTextSize = resources.getDimension(R.dimen.digital_date_text_size);

            mHourPaint.setTextSize(hourTextSize);
            mDayPaint.setTextSize(dayTextSize);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "onPropertiesChanged: low-bit ambient = " + mLowBitAmbient);
            }
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "onTimeTick: ambient = " + isInAmbientMode());
            }
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "onAmbientModeChanged: " + inAmbientMode);
            }

            mHourPaint.setAlpha(inAmbientMode ? 185 : 255);

            if (mLowBitAmbient) {
                boolean antiAlias = !inAmbientMode;
                mHourPaint.setAntiAlias(antiAlias);
                mMinutePaint.setAntiAlias(antiAlias);
                mSecondPaint.setAntiAlias(antiAlias);
                mTickPaint.setAntiAlias(antiAlias);
            }
            invalidate();
        }

        @Override
        public void onInterruptionFilterChanged(int interruptionFilter) {
            super.onInterruptionFilterChanged(interruptionFilter);
            boolean inMuteMode = (interruptionFilter == WatchFaceService.INTERRUPTION_FILTER_NONE);
            if (mMute != inMuteMode) {
                mMute = inMuteMode;
                mHourPaint.setAlpha(inMuteMode ? 100 : 255);
                mMinutePaint.setAlpha(inMuteMode ? 100 : 255);
                mSecondPaint.setAlpha(inMuteMode ? 80 : 255);
                invalidate();
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            if (mBackgroundScaledBitmap == null
                    || mBackgroundScaledBitmap.getWidth() != width
                    || mBackgroundScaledBitmap.getHeight() != height) {
                mBackgroundScaledBitmap = Bitmap.createScaledBitmap(mBackgroundBitmap,
                        width, height, true /* filter */);
            }
            super.onSurfaceChanged(holder, format, width, height);
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            mCalendar.setTimeInMillis(System.currentTimeMillis());

            int width = bounds.width();
            int height = bounds.height();

            // Draw the background, scaled to fit.
            canvas.drawBitmap(mBackgroundScaledBitmap, 0, 0, null);

            // Find the center. Ignore the window insets so that, on round watches with a
            // "chin", the watch face is centered on the entire screen, not just the usable
            // portion.
            float centerX = width / 2f;
            float centerY = height / 2f;

            int hour = mCalendar.get(Calendar.HOUR);
            if (hour == 0) {
                hour = 12;
            }
            if (lastHour != hour) {
                Resources resources = MinimaWatchFaceService.this.getResources();
                if (hour >= 10) {
                    mHourPaint.setTextSize(resources.getDimension(isRound ? R.dimen.digital_double_text_size_round : R.dimen.digital_double_text_size));
                } else {
                    mHourPaint.setTextSize(resources.getDimension(isRound ? R.dimen.digital_text_size_round : R.dimen.digital_text_size));
                }
            }
            lastHour = hour;
            String hourString = String.valueOf(hour);

            // Draw hour text
            canvas.drawText(hourString, centerX, centerY - (mHourPaint.descent() + mHourPaint.ascent()) / 2, mHourPaint);

            // Draw overlay
            canvas.drawArc(0, 0, width, height, startAngle,
                    (mCalendar.get(Calendar.MINUTE) * 6), true, mFadePaint);
            // angles are in degrees: 6 comes from MIN * (2PI / 60) * (180 / PI)

            // Draw minute hand
            if (mCalendar.get(Calendar.MINUTE) != 0) {
                float minutes = mCalendar.get(Calendar.MINUTE);
                float minRot = minutes / 60f * TWO_PI;
                float minLength = centerX - 5; // full length
                float minX = (float) Math.sin(minRot) * minLength;
                float minY = (float) -Math.cos(minRot) * minLength;
                canvas.drawLine(centerX, centerY, centerX + minX, centerY + minY, mMinutePaint);
                canvas.drawCircle(centerX, centerY, 7, mMinutePaint);
            }

            // Draw date text
            canvas.drawText(monthArray[mCalendar.get(Calendar.MONTH)], 43, 220, mDayPaint);
            canvas.drawText(String.valueOf(mCalendar.get(Calendar.DAY_OF_MONTH)), 70, 250, mDayPaint);

            // Draw the ticks.
            float innerTickRadius = centerX - 15;
            float innerLargeTickRadius = innerTickRadius - 5;
            float outerTickRadius = centerX;
            for (int tickIndex = 1; tickIndex < mCalendar.get(Calendar.MINUTE); tickIndex++) {
                float tickRot = tickIndex * TWO_PI / 60;
                float tickRadius = innerTickRadius;
                Paint tickPaint = mTickPaint;
                if (tickIndex % 5 == 0) {
                    // Hour ticks
                    tickRadius = innerLargeTickRadius;
                    tickPaint = mLargeTickPaint;
                }
                float innerX = (float) Math.sin(tickRot) * tickRadius;
                float innerY = (float) -Math.cos(tickRot) * tickRadius;
                float outerX = (float) Math.sin(tickRot) * outerTickRadius;
                float outerY = (float) -Math.cos(tickRot) * outerTickRadius;
                canvas.drawLine(centerX + innerX, centerY + innerY,
                        centerX + outerX, centerY + outerY, tickPaint);
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "onVisibilityChanged: " + visible);
            }

            if (visible) {
                registerReceiver();

                // Update time zone in case it changed while we weren't visible.
                mCalendar.setTimeZone(TimeZone.getDefault());
            } else {
                unregisterReceiver();
            }

        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            MinimaWatchFaceService.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            MinimaWatchFaceService.this.unregisterReceiver(mTimeZoneReceiver);
        }

    }
}
