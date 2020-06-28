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
import com.android.settings.gestures.SystemNavigationGestureSettings;
import android.provider.Settings;

import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.hwkeys.ActionUtils;
import com.android.internal.util.xtended.XtendedUtils;

import com.msm.xtended.preferences.SystemSettingSwitchPreference;

import com.android.settings.R;

public class NavbarSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String ENABLE_NAV_BAR = "enable_nav_bar";
    private static final String KEY_GESTURE_SYSTEM = "gesture_system_navigation";
    private static final String KEY_NAVIGATION_BAR_ARROWS = "navigation_bar_menu_arrow_keys";
    private static final String KEY_NAVIGATION_IME_SPACE = "navigation_bar_ime_space";
    private static final String KEY_PIXEL_NAV_ANIMATION = "pixel_nav_animation";

    private SwitchPreference mEnableNavigationBar;
    private Preference mGestureSystemNavigation;
    private SystemSettingSwitchPreference mNavigationArrows;
    private SystemSettingSwitchPreference mNavigationIMESpace;
    private SystemSettingSwitchPreference mPixelNavAnimation;

    private boolean defaultToNavigationBar;
    private boolean navigationBarEnabled;
    private boolean mIsNavSwitchingMode = false;
    private Handler mHandler;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.x_settings_navigation);
        final PreferenceScreen prefScreen = getPreferenceScreen();

        mGestureSystemNavigation = (Preference) findPreference(KEY_GESTURE_SYSTEM);

        // Navigation bar related options
        mEnableNavigationBar = (SwitchPreference) findPreference(ENABLE_NAV_BAR);
        mEnableNavigationBar.setChecked(isNavbarVisible());
        mEnableNavigationBar.setOnPreferenceChangeListener(this);

        mNavigationIMESpace = (SystemSettingSwitchPreference) findPreference(KEY_NAVIGATION_IME_SPACE);
        mNavigationIMESpace.setOnPreferenceChangeListener(this);

        mNavigationArrows = (SystemSettingSwitchPreference) findPreference(KEY_NAVIGATION_BAR_ARROWS);
        mPixelNavAnimation = (SystemSettingSwitchPreference) findPreference(KEY_PIXEL_NAV_ANIMATION);

        mHandler = new Handler();
        updateNavBarOption();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mEnableNavigationBar) {
            boolean value = (Boolean) newValue;
            if (mIsNavSwitchingMode) {
                return false;
            }
            mIsNavSwitchingMode = true;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.FORCE_SHOW_NAVBAR, value ? 1 : 0);
            updateNavBarOption();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mIsNavSwitchingMode = false;
                }
            }, 1000);
            return true;
        } else if (preference == mNavigationIMESpace) {
            updateNavBarOption();
            SystemNavigationGestureSettings.updateNavigationBarOverlays(getActivity());
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.XTENSIONS;
    }

    private boolean isNavbarVisible() {
        boolean defaultToNavigationBar = ActionUtils.hasNavbarByDefault(getActivity());
        return Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.FORCE_SHOW_NAVBAR, defaultToNavigationBar ? 1 : 0) == 1;
    }

    private void updateNavBarOption() {
        if (XtendedUtils.isThemeEnabled("com.android.internal.systemui.navbar.threebutton")) {
            mGestureSystemNavigation.setSummary(getString(R.string.legacy_navigation_title));
        } else if (XtendedUtils.isThemeEnabled("com.android.internal.systemui.navbar.twobutton")) {
            mGestureSystemNavigation.setSummary(getString(R.string.swipe_up_to_switch_apps_title));
        } else if (XtendedUtils.isThemeEnabled("com.android.internal.systemui.navbar.gestural")
                || XtendedUtils.isThemeEnabled("com.android.internal.systemui.navbar.gestural_wide_back")
                || XtendedUtils.isThemeEnabled("com.android.internal.systemui.navbar.gestural_extra_wide_back")
                || XtendedUtils.isThemeEnabled("com.android.internal.systemui.navbar.gestural_narrow_back")) {
            mGestureSystemNavigation.setSummary(getString(R.string.edge_to_edge_navigation_title));
        }

        int navbarWidth = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.NAVIGATION_HANDLE_WIDTH, 1, UserHandle.USER_CURRENT);
        boolean navbarSpaceEnabled = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.NAVIGATION_BAR_IME_SPACE, 1, UserHandle.USER_CURRENT) != 0;

        if (navbarWidth == 0) {
            mNavigationIMESpace.setVisible(true);
        } else {
            mNavigationIMESpace.setVisible(false);
        }

        if (navbarWidth == 0 && !navbarSpaceEnabled) {
            mNavigationArrows.setEnabled(false);
        } else {
            mNavigationArrows.setEnabled(true);
        }
    }
}
