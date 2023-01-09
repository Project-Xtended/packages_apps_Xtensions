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

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;
import androidx.preference.PreferenceScreen;
import android.content.Context;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import androidx.preference.Preference.OnPreferenceChangeListener;

import com.android.internal.logging.nano.MetricsProto;
import android.os.Bundle;
import android.provider.Settings;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class NotificationSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String SMS_BREATH = "sms_breath";
    private static final String MISSED_CALL_BREATH = "missed_call_breath";
    private static final String VOICEMAIL_BREATH = "voicemail_breath";

    protected Context mContext;
    private SwitchPreference mSmsBreath;
    private SwitchPreference mMissedCallBreath;
    private SwitchPreference mVoicemailBreath;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.x_settings_notifications);

        mContext = getActivity().getApplicationContext();
        ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();
    
        // Breathing Notifications
        mSmsBreath = (SwitchPreference) findPreference(SMS_BREATH);
        mMissedCallBreath = (SwitchPreference) findPreference(MISSED_CALL_BREATH);
        mVoicemailBreath = (SwitchPreference) findPreference(VOICEMAIL_BREATH);

        if (mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
            mSmsBreath.setChecked(Settings.Global.getInt(resolver,
                    Settings.Global.KEY_SMS_BREATH, 1) == 1);
            mSmsBreath.setOnPreferenceChangeListener(this);

            mMissedCallBreath.setChecked(Settings.Global.getInt(resolver,
                    Settings.Global.KEY_MISSED_CALL_BREATH, 1) == 1);
            mMissedCallBreath.setOnPreferenceChangeListener(this);

            mVoicemailBreath.setChecked(Settings.Global.getInt(resolver,
                    Settings.Global.KEY_VOICEMAIL_BREATH, 1) == 1);
            mVoicemailBreath.setOnPreferenceChangeListener(this);
        } else {
            prefScreen.removePreference(mSmsBreath);
            prefScreen.removePreference(mMissedCallBreath);
            prefScreen.removePreference(mVoicemailBreath);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mSmsBreath) {
            boolean value = (Boolean) newValue;
            Settings.Global.putInt(getContentResolver(), SMS_BREATH, value ? 1 : 0);
            return true;
        } else if (preference == mMissedCallBreath) {
            boolean value = (Boolean) newValue;
            Settings.Global.putInt(getContentResolver(), MISSED_CALL_BREATH, value ? 1 : 0);
            return true;
        } else if (preference == mVoicemailBreath) {
            boolean value = (Boolean) newValue;
            Settings.Global.putInt(getContentResolver(), VOICEMAIL_BREATH, value ? 1 : 0);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.XTENSIONS;
    }
}
