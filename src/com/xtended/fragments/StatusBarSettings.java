/*
 * Copyright (C) 2020-21 The Project-Xtended
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.xtended.fragments;

import com.android.internal.logging.nano.MetricsProto;

import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import android.content.ContentResolver;
import android.content.res.Resources;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import com.android.settings.R;

import java.util.Locale;
import android.text.TextUtils;
import android.view.View;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import android.util.Log;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import com.android.internal.util.xtended.XtendedUtils;
import com.xtended.support.preferences.SystemSettingListPreference;

public class StatusBarSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String VOLTE_ICON_STYLE = "volte_icon_style";
    private static final String VOWIFI_ICON_STYLE = "vowifi_icon_style";

    private SystemSettingListPreference mVolteIconStyle;
    private SystemSettingListPreference mVowifiIconStyle;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.x_settings_statusbar);

        final PreferenceScreen prefScreen = getPreferenceScreen();

        mVolteIconStyle = (SystemSettingListPreference) findPreference(VOLTE_ICON_STYLE);
        int volteIconStyle = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.VOLTE_ICON_STYLE, 1);
        mVolteIconStyle.setValue(String.valueOf(volteIconStyle));
        mVolteIconStyle.setOnPreferenceChangeListener(this);

        mVowifiIconStyle = (SystemSettingListPreference) findPreference(VOWIFI_ICON_STYLE);
        int vowifiIconStyle = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.VOWIFI_ICON_STYLE, 1);
        mVowifiIconStyle.setValue(String.valueOf(vowifiIconStyle));
        mVowifiIconStyle.setOnPreferenceChangeListener(this);
        if (vowifiIconStyle == 0) {
            mVolteIconStyle.setEnabled(true);
        } else {
            mVolteIconStyle.setEnabled(false);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mVolteIconStyle) {
            int volteIconStyle = Integer.parseInt(((String) newValue).toString());
            Settings.System.putInt(resolver,
                  Settings.System.VOWIFI_ICON_STYLE, volteIconStyle);
            mVolteIconStyle.setValue(String.valueOf(volteIconStyle));
            XtendedUtils.restartSystemUi(getContext());
            return true;
        } else if (preference == mVowifiIconStyle) {
            int vowifiIconStyle = Integer.parseInt(((String) newValue).toString());
            Settings.System.putInt(resolver,
                  Settings.System.VOWIFI_ICON_STYLE, vowifiIconStyle);
            mVowifiIconStyle.setValue(String.valueOf(vowifiIconStyle));
            if (vowifiIconStyle == 0) {
                mVolteIconStyle.setEnabled(true);
            } else {
                mVolteIconStyle.setEnabled(false);
            }
            XtendedUtils.restartSystemUi(getContext());
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.XTENSIONS;
    }
}
