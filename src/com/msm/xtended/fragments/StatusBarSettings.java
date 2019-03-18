package com.msm.xtended.fragments;

import com.android.internal.logging.nano.MetricsProto;

import android.os.Bundle;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v14.preference.PreferenceFragment;
import android.support.v14.preference.SwitchPreference;
import android.provider.Settings;
import com.android.settings.R;
import android.net.ConnectivityManager;

import java.util.Locale;
import android.text.TextUtils;
import android.view.View;

import com.android.settings.SettingsPreferenceFragment;
import com.msm.xtended.preferences.CustomSeekBarPreference;
import com.msm.xtended.preferences.SystemSettingSwitchPreference;
import com.android.settings.Utils;
import android.util.Log;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

public class StatusBarSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String SMS_BREATH = "sms_breath";
    private static final String MISSED_CALL_BREATH = "missed_call_breath";
    private static final String VOICEMAIL_BREATH = "voicemail_breath";
    private static final String PREF_KEY_CUTOUT = "cutout_settings";

    private SwitchPreference mSmsBreath;
    private SwitchPreference mMissedCallBreath;
    private SwitchPreference mVoicemailBreath;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.x_settings_statusbar);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        Preference mCutoutPref = (Preference) findPreference(PREF_KEY_CUTOUT);
        if (!hasPhysicalDisplayCutout(getContext()))
            getPreferenceScreen().removePreference(mCutoutPref);

            // Breathing Notifications
            mSmsBreath = (SwitchPreference) findPreference(SMS_BREATH);
            mMissedCallBreath = (SwitchPreference) findPreference(MISSED_CALL_BREATH);
            mVoicemailBreath = (SwitchPreference) findPreference(VOICEMAIL_BREATH);

            ConnectivityManager cm = (ConnectivityManager)
                    getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

            if (cm.isNetworkSupported(ConnectivityManager.TYPE_MOBILE)) {
                mSmsBreath.setChecked(Settings.Global.getInt(resolver,
                        Settings.Global.KEY_SMS_BREATH, 0) == 1);
                mSmsBreath.setOnPreferenceChangeListener(this);

                mMissedCallBreath.setChecked(Settings.Global.getInt(resolver,
                        Settings.Global.KEY_MISSED_CALL_BREATH, 0) == 1);
                mMissedCallBreath.setOnPreferenceChangeListener(this);

                mVoicemailBreath.setChecked(Settings.System.getInt(resolver,
                        Settings.System.KEY_VOICEMAIL_BREATH, 0) == 1);
                mVoicemailBreath.setOnPreferenceChangeListener(this);
            } else {
                prefSet.removePreference(mSmsBreath);
                prefSet.removePreference(mMissedCallBreath);
                prefSet.removePreference(mVoicemailBreath);
            }
    }

    private static boolean hasPhysicalDisplayCutout(Context context) {
        return context.getResources().getBoolean(
                com.android.internal.R.bool.config_physicalDisplayCutout);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
	    ContentResolver resolver = getActivity().getContentResolver();

            if (preference == mSmsBreath) {
                boolean value = (Boolean) objValue;
                Settings.Global.putInt(getContentResolver(), SMS_BREATH, value ? 1 : 0);
                return true;
            } else if (preference == mMissedCallBreath) {
                boolean value = (Boolean) objValue;
                Settings.Global.putInt(getContentResolver(), MISSED_CALL_BREATH, value ? 1 : 0);
                return true;
            } else if (preference == mVoicemailBreath) {
                boolean value = (Boolean) objValue;
                Settings.System.putInt(getContentResolver(), VOICEMAIL_BREATH, value ? 1 : 0);
                return true;
            }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.XTENSIONS;
    }

}
