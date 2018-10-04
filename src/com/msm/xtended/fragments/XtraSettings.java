package com.msm.xtended.fragments;

import com.android.internal.logging.nano.MetricsProto;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v14.preference.SwitchPreference;
import android.provider.Settings;
import com.android.settings.R;

import java.util.Arrays;
import java.util.HashSet;

import com.android.settings.SettingsPreferenceFragment;

public class XtraSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String MEDIA_SCANNER_ON_BOOT = "media_scanner_on_boot";

    private ListPreference mMSOB;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.x_settings_xtra);

        // MediaScanner behavior on boot
        mMSOB = (ListPreference) findPreference(MEDIA_SCANNER_ON_BOOT);
        int mMSOBValue = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.MEDIA_SCANNER_ON_BOOT, 0);
        mMSOB.setValue(String.valueOf(mMSOBValue));
        mMSOB.setSummary(mMSOB.getEntry());
        mMSOB.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        if (preference == mMSOB) {
            int value = Integer.parseInt(((String) newValue).toString());
            Settings.System.putInt(getContentResolver(),
                    Settings.System.MEDIA_SCANNER_ON_BOOT, value);
            mMSOB.setValue(String.valueOf(value));
            mMSOB.setSummary(mMSOB.getEntries()[value]);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.XTENSIONS;
    }
}
