/*
 *  Copyright (C) 2015 The OmniROM Project
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

public class LockScreenSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String KEY_FACE_AUTO_UNLOCK = "face_auto_unlock";
    private static final String KEY_FACE_UNLOCK_PACKAGE = "com.android.facelock";
    private static final String FINGERPRINT_VIB = "fingerprint_success_vib";
    private static final String LOCKSCREEN_MAX_NOTIF_CONFIG = "lockscreen_max_notif_cofig";
    private static final String FP_UNLOCK_KEYSTORE = "fp_unlock_keystore";

    private FingerprintManager mFingerprintManager;
    private SwitchPreference mFingerprintVib;
    private SwitchPreference mFaceUnlock;
    private CustomSeekBarPreference mMaxKeyguardNotifConfig;
    private SystemSettingSwitchPreference mFpKeystore;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.x_settings_lockscreen);

        ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();
        Resources resources = getResources();

        mFaceUnlock = (SwitchPreference) findPreference(KEY_FACE_AUTO_UNLOCK);
        if (!XUtils.isPackageInstalled(getActivity(), KEY_FACE_UNLOCK_PACKAGE)){
            prefScreen.removePreference(mFaceUnlock);
        } else {
            mFaceUnlock.setChecked((Settings.Secure.getInt(getContext().getContentResolver(),
                    Settings.Secure.FACE_AUTO_UNLOCK, 0) == 1));
            mFaceUnlock.setOnPreferenceChangeListener(this);
        }

        mFingerprintManager = (FingerprintManager) getActivity().getSystemService(Context.FINGERPRINT_SERVICE);
        mFingerprintVib = (SwitchPreference) findPreference(FINGERPRINT_VIB);
        mFpKeystore = (SystemSettingSwitchPreference) findPreference(FP_UNLOCK_KEYSTORE);
        if (mFingerprintManager == null){
            prefScreen.removePreference(mFingerprintVib);
            prefScreen.removePreference(mFpKeystore);
        } else {
            mFingerprintVib.setChecked((Settings.System.getInt(getContentResolver(),
                    Settings.System.FINGERPRINT_SUCCESS_VIB, 1) == 1));
            mFingerprintVib.setOnPreferenceChangeListener(this);
        }

        mMaxKeyguardNotifConfig = (CustomSeekBarPreference) findPreference(LOCKSCREEN_MAX_NOTIF_CONFIG);
        int kgconf = Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKSCREEN_MAX_NOTIF_CONFIG, 3);
        mMaxKeyguardNotifConfig.setValue(kgconf);
        mMaxKeyguardNotifConfig.setOnPreferenceChangeListener(this);

    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();

        if (preference == mFaceUnlock) {
            boolean value = (Boolean) newValue;
            Settings.Secure.putInt(getActivity().getContentResolver(),
                    Settings.Secure.FACE_AUTO_UNLOCK, value ? 1 : 0);
            return true;
        } else if (preference == mFingerprintVib) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.FINGERPRINT_SUCCESS_VIB, value ? 1 : 0);
            return true;
        } else if (preference == mMaxKeyguardNotifConfig) {
            int kgconf = (Integer) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_MAX_NOTIF_CONFIG, kgconf);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.XTENSIONS;
    }

}

