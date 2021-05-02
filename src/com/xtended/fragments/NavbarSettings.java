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

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;

import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.xtended.XtendedUtils;
import com.android.settings.R;

public class NavbarSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String KEY_NAVIGATION_BAR_ENABLED = "force_show_navbar";
    private static final String LAYOUT_SETTINGS = "navbar_layout_views";
    private static final String NAVIGATION_BAR_INVERSE = "navbar_inverse_layout";

    private SwitchPreference mNavigationBar;
    private boolean mIsNavSwitchingMode = false;
    private Preference mLayoutSettings;
    private SwitchPreference mSwapNavButtons;
    private Handler mHandler;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.x_settings_navigation);

        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        mHandler = new Handler();

        final boolean defaultToNavigationBar = getResources().getBoolean(
                com.android.internal.R.bool.config_showNavigationBar);
        final boolean navigationBarEnabled = Settings.System.getIntForUser(
                resolver, Settings.System.FORCE_SHOW_NAVBAR,
                defaultToNavigationBar ? 1 : 0, UserHandle.USER_CURRENT) != 0;

        mNavigationBar = (SwitchPreference) findPreference(KEY_NAVIGATION_BAR_ENABLED);
        mNavigationBar.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.FORCE_SHOW_NAVBAR,
                defaultToNavigationBar ? 1 : 0) == 1));
        mNavigationBar.setOnPreferenceChangeListener(this);

        mLayoutSettings = findPreference(LAYOUT_SETTINGS);
        mSwapNavButtons = findPreference(NAVIGATION_BAR_INVERSE);

        if (!XtendedUtils.isThemeEnabled("com.android.internal.systemui.navbar.threebutton")) {
            mLayoutSettings.setEnabled(false);
            mSwapNavButtons.setEnabled(false);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mNavigationBar) {
            boolean value = (Boolean) newValue;
            if (mIsNavSwitchingMode) {
                return false;
            }
            mIsNavSwitchingMode = true;
            Settings.System.putInt(resolver, Settings.System
            .FORCE_SHOW_NAVBAR, value ? 1 : 0);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mIsNavSwitchingMode = false;
                }
            }, 1500);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.XTENSIONS;
    }
}
