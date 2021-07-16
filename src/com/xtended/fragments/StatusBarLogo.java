/*
 * Copyright (C) 2021 Project-Xtended
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

package com.xtended.fragments;

import android.content.Context;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;
import com.xtended.support.colorpicker.ColorPickerPreference;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.Utils;

public class StatusBarLogo extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private ListPreference mShowLogo;
    private ListPreference mLogoStyle;
    private ColorPickerPreference mStatusBarLogoColor;
    private ListPreference mShowQsLogo;
    private ListPreference mQsLogoStyle;
    private ColorPickerPreference mQsLogoColor;
    static final int DEFAULT_LOGO_COLOR = 0xffff8800;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.x_statusbar_logo);

        String hexColor;
        int intColor;

        mShowLogo = (ListPreference) findPreference("status_bar_logo");
        mShowLogo.setOnPreferenceChangeListener(this);
        int showLogo = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.STATUS_BAR_LOGO,
                0, UserHandle.USER_CURRENT);
        mShowLogo.setValue(String.valueOf(showLogo));
        mShowLogo.setSummary(mShowLogo.getEntry());

        mLogoStyle = (ListPreference) findPreference("status_bar_logo_style");
        mLogoStyle.setOnPreferenceChangeListener(this);
        int logoStyle = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.STATUS_BAR_LOGO_STYLE,
                0, UserHandle.USER_CURRENT);
        mLogoStyle.setValue(String.valueOf(logoStyle));
        mLogoStyle.setSummary(mLogoStyle.getEntry());

        mStatusBarLogoColor = (ColorPickerPreference) findPreference("status_bar_logo_color");
        mStatusBarLogoColor.setOnPreferenceChangeListener(this);
        intColor = Settings.System.getInt(getContentResolver(),
                Settings.System.STATUS_BAR_LOGO_COLOR, DEFAULT_LOGO_COLOR);
        hexColor = String.format("#%08x", (DEFAULT_LOGO_COLOR & intColor));
        mStatusBarLogoColor.setSummary(hexColor);
        mStatusBarLogoColor.setNewPreviewColor(intColor);

        mShowQsLogo = (ListPreference) findPreference("qs_panel_logo");
        mShowQsLogo.setOnPreferenceChangeListener(this);
        int showQsLogo = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.QS_PANEL_LOGO,
                0, UserHandle.USER_CURRENT);
        mShowQsLogo.setValue(String.valueOf(showQsLogo));
        mShowQsLogo.setSummary(mShowQsLogo.getEntry());

        mQsLogoStyle = (ListPreference) findPreference("qs_panel_logo_style");
        mQsLogoStyle.setOnPreferenceChangeListener(this);
        int logoQsStyle = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.QS_PANEL_LOGO_STYLE,
                0, UserHandle.USER_CURRENT);
        mQsLogoStyle.setValue(String.valueOf(logoQsStyle));
        mQsLogoStyle.setSummary(mQsLogoStyle.getEntry());

        mQsLogoColor = (ColorPickerPreference) findPreference("qs_panel_logo_color");
        mQsLogoColor.setOnPreferenceChangeListener(this);
        intColor = Settings.System.getInt(getContentResolver(),
                Settings.System.QS_PANEL_LOGO_COLOR, DEFAULT_LOGO_COLOR);
        hexColor = String.format("#%08x", (DEFAULT_LOGO_COLOR & intColor));
        mQsLogoColor.setSummary(hexColor);
        mQsLogoColor.setNewPreviewColor(intColor);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.XTENSIONS;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference.equals(mShowLogo)) {
            int showLogo = Integer.parseInt(((String) newValue).toString());
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.STATUS_BAR_LOGO, showLogo, UserHandle.USER_CURRENT);
            int index = mShowLogo.findIndexOfValue((String) newValue);
            mShowLogo.setSummary(
                    mShowLogo.getEntries()[index]);
            return true;
        } else if (preference.equals(mLogoStyle)) {
            int logoStyle = Integer.parseInt(((String) newValue).toString());
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.STATUS_BAR_LOGO_STYLE, logoStyle, UserHandle.USER_CURRENT);
            int index = mLogoStyle.findIndexOfValue((String) newValue);
            mLogoStyle.setSummary(
                    mLogoStyle.getEntries()[index]);
            return true;
        } else if (preference.equals(mStatusBarLogoColor)) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(resolver,
                    Settings.System.STATUS_BAR_LOGO_COLOR, intHex);
            return true;
        } else if (preference.equals(mShowQsLogo)) {
            int showQsLogo = Integer.parseInt(((String) newValue).toString());
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.QS_PANEL_LOGO, showQsLogo, UserHandle.USER_CURRENT);
            int index = mShowQsLogo.findIndexOfValue((String) newValue);
            mShowQsLogo.setSummary(
                    mShowQsLogo.getEntries()[index]);
            return true;
        } else if (preference.equals(mQsLogoStyle)) {
            int logoQsStyle = Integer.parseInt(((String) newValue).toString());
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.QS_PANEL_LOGO_STYLE, logoQsStyle, UserHandle.USER_CURRENT);
            int index = mQsLogoStyle.findIndexOfValue((String) newValue);
            mQsLogoStyle.setSummary(
                    mQsLogoStyle.getEntries()[index]);
            return true;
        } else if (preference.equals(mQsLogoColor)) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(resolver,
                    Settings.System.QS_PANEL_LOGO_COLOR, intHex);
            return true;
        }
        return false;
    }
}

