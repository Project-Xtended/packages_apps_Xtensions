/*
 * Copyright (C) 2014 TeamEos
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

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.os.Vibrator;
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
import com.android.internal.util.hwkeys.ActionUtils;
import com.android.settings.R;

public class NavbarSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String ENABLE_NAV_BAR = "enable_nav_bar";

    private SwitchPreference mEnableNavigationBar;
    private boolean mIsNavSwitchingMode = false;
    private Handler mHandler;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.x_settings_navigation);
        final PreferenceScreen prefScreen = getPreferenceScreen();

        // Navigation bar related options
        mEnableNavigationBar = (SwitchPreference) findPreference(ENABLE_NAV_BAR);

        // Only visible on devices that have a navigation bar already
        if (ActionUtils.hasNavbarByDefault(getActivity())) {
            mEnableNavigationBar.setOnPreferenceChangeListener(this);
            mHandler = new Handler();
            updateNavBarOption();
        } else {
            prefScreen.removePreference(mEnableNavigationBar);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mEnableNavigationBar) {
            if (mIsNavSwitchingMode) {
                return false;
            }
            mIsNavSwitchingMode = true;
            boolean isNavBarChecked = ((Boolean) newValue);
            mEnableNavigationBar.setEnabled(false);
            writeNavBarOption(isNavBarChecked);
            updateNavBarOption();
            mEnableNavigationBar.setEnabled(true);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mIsNavSwitchingMode = false;
                }
            }, 1000);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.XTENSIONS;
    }

    private void writeNavBarOption(boolean enabled) {
        Settings.System.putIntForUser(getActivity().getContentResolver(),
                Settings.System.FORCE_SHOW_NAVBAR, enabled ? 1 : 0, UserHandle.USER_CURRENT);
    }

    private void updateNavBarOption() {
        boolean enabled = Settings.System.getIntForUser(getActivity().getContentResolver(),
                Settings.System.FORCE_SHOW_NAVBAR, 1, UserHandle.USER_CURRENT) != 0;
        mEnableNavigationBar.setChecked(enabled);
    }
}
