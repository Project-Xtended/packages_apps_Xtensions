/*
 * Copyright (C) 2020 The Dirty Unicorns Project
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

package com.msm.xtended.fragments;

import java.util.ArrayList;
import java.util.List;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settingslib.search.SearchIndexable;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.UserHandle;

import androidx.preference.PreferenceCategory;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;

import com.msm.xtended.Xtensions;
import com.msm.xtended.preferences.SecureSettingSeekBarPreference;
import com.msm.xtended.preferences.SecureSettingSwitchPreference;
import com.msm.xtended.preferences.SecureSettingListPreference;

import android.provider.SearchIndexableResource;
import android.provider.Settings;

@SearchIndexable
public class PulseSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {
    private static final String TAG = PulseSettings.class.getSimpleName();
    private static final String PULSE_COLOR_MODE_KEY = "pulse_color_mode";
    private static final String PULSE_COLOR_MODE_CHOOSER_KEY = "pulse_color_user";
    private static final String PULSE_COLOR_MODE_LAVA_SPEED_KEY = "pulse_lavalamp_speed";
    private static final String PULSE_RENDER_CATEGORY_SOLID = "pulse_2";
    private static final String PULSE_RENDER_CATEGORY_FADING = "pulse_fading_bars_category";
    private static final String PULSE_RENDER_MODE_KEY = "pulse_render_style";
    private static final int RENDER_STYLE_FADING_BARS = 0;
    private static final int RENDER_STYLE_SOLID_LINES = 1;
    private static final int COLOR_TYPE_ACCENT = 0;
    private static final int COLOR_TYPE_USER = 1;
    private static final int COLOR_TYPE_LAVALAMP = 2;
    private static final int COLOR_TYPE_AUTO = 3;

    static final int DEFAULT = 0xffffffff;

    private Preference mRenderMode;
    private SecureSettingListPreference mColorModePref;
    private ColorPickerPreference mColorPickerPref;
    private Preference mLavaSpeedPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pulse_settings);

        mFooterPreferenceMixin.createFooterPreference()
                .setTitle(R.string.pulse_help_policy_notice_summary);
        int intColor;
        String hexColor;

        mColorModePref = (SecureSettingListPreference) findPreference(PULSE_COLOR_MODE_KEY);
        mLavaSpeedPref = findPreference(PULSE_COLOR_MODE_LAVA_SPEED_KEY);
        mColorModePref.setOnPreferenceChangeListener(this);

        mColorPickerPref = (ColorPickerPreference) findPreference(PULSE_COLOR_MODE_CHOOSER_KEY);
        mColorPickerPref.setOnPreferenceChangeListener(this);
        intColor = Settings.Secure.getInt(getContentResolver(),
                   Settings.Secure.PULSE_COLOR_USER, DEFAULT);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mColorPickerPref.setSummary(hexColor);
        mColorPickerPref.setNewPreviewColor(intColor);

        int colorMode = Settings.Secure.getIntForUser(getContentResolver(),
                Settings.Secure.PULSE_COLOR_MODE, COLOR_TYPE_ACCENT, UserHandle.USER_CURRENT);
        updateColorPrefs(colorMode);

        mRenderMode = findPreference(PULSE_RENDER_MODE_KEY);
        mRenderMode.setOnPreferenceChangeListener(this);
        int renderMode = Settings.Secure.getIntForUser(getContentResolver(),
                Settings.Secure.PULSE_RENDER_STYLE, 0, UserHandle.USER_CURRENT);
        updateRenderCategories(renderMode);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.equals(mColorModePref)) {
            updateColorPrefs(Integer.valueOf(String.valueOf(newValue)));
            return true;
        } else if (preference.equals(mColorPickerPref)) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.Secure.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.Secure.PULSE_COLOR_USER, intHex);
            return true;
        } else if (preference.equals(mRenderMode)) {
            updateRenderCategories(Integer.valueOf(String.valueOf(newValue)));
            return true;
        }
        return false;
    }

    private void updateColorPrefs(int val) {
        switch (val) {
            case COLOR_TYPE_ACCENT:
                mColorPickerPref.setEnabled(false);
                mLavaSpeedPref.setEnabled(false);
                break;
            case COLOR_TYPE_USER:
                mColorPickerPref.setEnabled(true);
                mLavaSpeedPref.setEnabled(false);
                break;
            case COLOR_TYPE_LAVALAMP:
                mColorPickerPref.setEnabled(false);
                mLavaSpeedPref.setEnabled(true);
                break;
            case COLOR_TYPE_AUTO:
                mColorPickerPref.setEnabled(false);
                mLavaSpeedPref.setEnabled(false);
                break;
        }
    }

    private void updateRenderCategories(int mode) {
        PreferenceCategory fadingBarsCat = (PreferenceCategory) findPreference(
                PULSE_RENDER_CATEGORY_FADING);
        fadingBarsCat.setEnabled(mode == RENDER_STYLE_FADING_BARS);
        PreferenceCategory solidBarsCat = (PreferenceCategory) findPreference(
                PULSE_RENDER_CATEGORY_SOLID);
        solidBarsCat.setEnabled(mode == RENDER_STYLE_SOLID_LINES);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.XTENDED;
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        @Override
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                boolean enabled) {
            final ArrayList<SearchIndexableResource> result = new ArrayList<>();
            final SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = R.xml.pulse_settings;
            result.add(sir);
            return result;
        }

        @Override
        public List<String> getNonIndexableKeys(Context context) {
            final List<String> keys = super.getNonIndexableKeys(context);
            return keys;
        }
    };
}


