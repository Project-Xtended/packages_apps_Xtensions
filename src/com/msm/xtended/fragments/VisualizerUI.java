/*
 *  Copyright (C) 2018 MSM-Xtended Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.msm.xtended.fragments;

import com.android.internal.logging.nano.MetricsProto;

import android.app.Activity;
import android.content.Context;
import android.content.ContentResolver;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;

import android.provider.Settings;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import com.msm.xtended.preferences.CustomSeekBarPreference;
import com.msm.xtended.preferences.SystemSettingSwitchPreference;

import com.msm.xtended.preferences.XUtils;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class VisualizerUI extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String LOCK_SCREEN_VISUALIZER_CUSTOM_COLOR = "lock_screen_visualizer_custom_color";
    private static final String KEY_LAVALAMP = "lockscreen_lavalamp_enabled";

    private ColorPickerPreference mVisualizerColor;
    private SwitchPreference mLavaLamp;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.x_visualizer_ui);

        ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();
        Resources resources = getResources();

        // Visualizer custom color
        mVisualizerColor = (ColorPickerPreference) findPreference(LOCK_SCREEN_VISUALIZER_CUSTOM_COLOR);
        int visColor = Settings.System.getInt(resolver,
                Settings.System.LOCK_SCREEN_VISUALIZER_CUSTOM_COLOR, 0xff1976D2);
        String visColorHex = String.format("#%08x", (0xff1976D2 & visColor));
        mVisualizerColor.setSummary(visColorHex);
        mVisualizerColor.setNewPreviewColor(visColor);
        mVisualizerColor.setAlphaSliderEnabled(true);
        mVisualizerColor.setOnPreferenceChangeListener(this);

        mLavaLamp = (SwitchPreference) findPreference(KEY_LAVALAMP);
        mLavaLamp.setOnPreferenceChangeListener(this);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();

        if (preference == mVisualizerColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(resolver,
                    Settings.System.LOCK_SCREEN_VISUALIZER_CUSTOM_COLOR, intHex);
            preference.setSummary(hex);
            return true;
        } else if (preference == mLavaLamp) {
            boolean value = (Boolean) newValue;
            Settings.Secure.putInt(resolver,
                Settings.Secure.LOCKSCREEN_LAVALAMP_ENABLED, value ? 1 : 0);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.XTENSIONS;
    }
}


