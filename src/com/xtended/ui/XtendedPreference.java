/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.xtended.ui;

import android.annotation.ColorInt;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.content.om.IOverlayManager;
import android.content.om.OverlayInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.Settings;
import androidx.annotation.VisibleForTesting;
import androidx.core.content.res.TypedArrayUtils;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.widget.ImageViewCompat;

import com.android.settings.R;
import com.android.settings.Utils;

import java.util.List;
import java.util.Random;

public class XtendedPreference extends Preference {

    private final View.OnClickListener mClickListener = v -> performClick(v);

    private boolean mAllowDividerAbove;
    private boolean mAllowDividerBelow;
    private ImageView mBackground;
    private int mColorRandom;
    private int mColorAccent;
    private int mColorGradient;
    private int mColorAlpha;
    private int xtensionStyle;

    public XtendedPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Preference);

        mAllowDividerAbove = TypedArrayUtils.getBoolean(a, R.styleable.Preference_allowDividerAbove,
                R.styleable.Preference_allowDividerAbove, false);
        mAllowDividerBelow = TypedArrayUtils.getBoolean(a, R.styleable.Preference_allowDividerBelow,
                R.styleable.Preference_allowDividerBelow, false);

        a.recycle();

        setLayoutResource(R.layout.xtended_preference);
    }

    public XtendedPreference(Context context, View view) {
        super(context);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        final Context context = getContext();
        holder.itemView.setOnClickListener(mClickListener);

        final boolean selectable = isSelectable();
        holder.itemView.setFocusable(selectable);
        holder.itemView.setClickable(selectable);
        holder.setDividerAllowedAbove(mAllowDividerAbove);
        holder.setDividerAllowedBelow(mAllowDividerBelow);

        mBackground = (ImageView) holder.findViewById(R.id.buttonshape);
        setStyleColor(context);
        mBackground.setColorFilter(mColorAlpha);
    }

    private void setStyleColor(Context context) {
        xtensionStyle = Settings.System.getIntForUser(context.getContentResolver(),
                    Settings.System.XTENSIONS_STYLE, 0, UserHandle.USER_CURRENT);

        mColorRandom = getRandomColor();
        mColorAccent = context.getResources().getColor(com.android.internal.R.color.gradient_start);
	mColorGradient = context.getResources().getColor(com.android.internal.R.color.gradient_end);
        if (xtensionStyle == 0) {
            mColorAlpha = adjustAlpha(mColorAccent, 0.9f);
        } else if (xtensionStyle == 1) {
            mColorAlpha = adjustAlpha(mColorGradient, 0.9f);
        } else {
            mColorAlpha = adjustAlpha(mColorRandom, 0.9f);
        }
    }

    public int getRandomColor(){
    Random rnd = new Random();
       return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
    }

    @ColorInt
    private static int adjustAlpha(@ColorInt int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }
}
