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

import static android.os.UserHandle.USER_CURRENT;
import static android.os.UserHandle.USER_SYSTEM;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.content.res.Resources;
import android.net.Uri;
import android.content.om.IOverlayManager;
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
import com.android.settings.SettingsPreferenceFragment;
import java.util.Locale;
import android.text.TextUtils;
import android.view.View;

import com.xtended.support.preferences.SystemSettingEditTextPreference;
import com.xtended.support.preferences.SystemSettingListPreference;

import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;
import android.provider.SearchIndexableResource;

import java.util.List;
import java.util.ArrayList;

public class QuickSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String X_FOOTER_TEXT_STRING = "x_footer_text_string";
    private static final String QS_CLOCK_PICKER = "qs_clock_picker";

    private SystemSettingEditTextPreference mFooterString;
    private SystemSettingListPreference mQsClockPicker;

    private IOverlayManager mOverlayManager;
    private IOverlayManager mOverlayService;
    private Handler mHandler;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.x_settings_quicksettings);

        PreferenceScreen prefScreen = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mFooterString = (SystemSettingEditTextPreference) findPreference(X_FOOTER_TEXT_STRING);
        mFooterString.setOnPreferenceChangeListener(this);
        String footerString = Settings.System.getString(getContentResolver(),
                X_FOOTER_TEXT_STRING);
        if (footerString != null && footerString != "")
            mFooterString.setText(footerString);
        else {
            mFooterString.setText("Xtended");
            Settings.System.putString(getActivity().getContentResolver(),
                    Settings.System.X_FOOTER_TEXT_STRING, "Xtended");
        }
        
        mOverlayManager = IOverlayManager.Stub.asInterface(
                ServiceManager.getService(Context.OVERLAY_SERVICE));
        mOverlayService = IOverlayManager.Stub
                .asInterface(ServiceManager.getService(Context.OVERLAY_SERVICE));

        mQsClockPicker = (SystemSettingListPreference) findPreference(QS_CLOCK_PICKER);
        boolean isAospClock = Settings.System.getIntForUser(resolver,
                QS_CLOCK_PICKER, 0, UserHandle.USER_CURRENT) == 4;
        mQsClockPicker.setOnPreferenceChangeListener(this);
        mCustomSettingsObserver.observe();
    }

    private CustomSettingsObserver mCustomSettingsObserver = new CustomSettingsObserver(mHandler);
    private class CustomSettingsObserver extends ContentObserver {

        CustomSettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            Context mContext = getContext();
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.QS_CLOCK_PICKER ),
                    false, this, UserHandle.USER_ALL);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (uri.equals(Settings.System.getUriFor(Settings.System.QS_CLOCK_PICKER ))) {
                updateQsClock();
            }
        }
    }

    private void updateQsClock() {
        ContentResolver resolver = getActivity().getContentResolver();

        boolean AospClock = Settings.System.getIntForUser(getContext().getContentResolver(),
                Settings.System.QS_CLOCK_PICKER , 0, UserHandle.USER_CURRENT) == 4;
        boolean ColorOsClock = Settings.System.getIntForUser(getContext().getContentResolver(),
                Settings.System.QS_CLOCK_PICKER , 0, UserHandle.USER_CURRENT) == 5;

        if (AospClock) {
            updateQsClockPicker(mOverlayManager, "com.spark.qsclockoverlays.aosp");
        } else if (ColorOsClock) {
            updateQsClockPicker(mOverlayManager, "com.spark.qsclockoverlays.coloros");
        } else {
            setDefaultClock(mOverlayManager);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mFooterString) {
            String value = (String) newValue;
            if (value != "" && value != null)
                Settings.System.putString(getActivity().getContentResolver(),
                        Settings.System.X_FOOTER_TEXT_STRING, value);
            else {
                mFooterString.setText("Xtended");
                Settings.System.putString(getActivity().getContentResolver(),
                        Settings.System.X_FOOTER_TEXT_STRING, "Xtended");
            }
            return true;
        } else if (preference == mQsClockPicker) {
            int SelectedClock = Integer.valueOf((String) newValue);
            Settings.System.putInt(resolver, Settings.System.QS_CLOCK_PICKER, SelectedClock);
            mCustomSettingsObserver.observe();
            return true;
        }
        return false;
    }

    public static void setDefaultClock(IOverlayManager overlayManager) {
        for (int i = 0; i < CLOCKS.length; i++) {
            String clocks = CLOCKS[i];
            try {
                overlayManager.setEnabled(clocks, false, USER_SYSTEM);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public static void updateQsClockPicker(IOverlayManager overlayManager, String overlayName) {
        try {
            for (int i = 0; i < CLOCKS.length; i++) {
                String clocks = CLOCKS[i];
                try {
                    overlayManager.setEnabled(clocks, false, USER_SYSTEM);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            overlayManager.setEnabled(overlayName, true, USER_SYSTEM);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void handleOverlays(String packagename, Boolean state, IOverlayManager mOverlayManager) {
        try {
            mOverlayManager.setEnabled(packagename,
                    state, USER_SYSTEM);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static final String[] CLOCKS = {
        "com.spark.qsclockoverlays.aosp",
        "com.spark.qsclockoverlays.coloros",
    };
    
    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.XTENSIONS;
    }
}
