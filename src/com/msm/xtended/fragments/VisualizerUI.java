/*
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

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.SettingsPreferenceFragment;
import com.msm.xtended.preferences.SystemSettingSeekBarPreference;
import com.msm.xtended.preferences.SecureSettingListPreference;

import com.android.settings.R;

public class VisualizerUI extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String KEY_AUTOCOLOR = "lockscreen_visualizer_autocolor";
    private static final String KEY_LAVALAMP = "lockscreen_lavalamp_enabled";
    private static final String LOCKSCREEN_MEDIA_FILTER = "lockscreen_albumart_filter";
    private static final String LOCKSCREEN_MEDIA_BLUR = "lockscreen_media_blur";

    private SwitchPreference mAutoColor;
    private SwitchPreference mLavaLamp;
    private SecureSettingListPreference mLockscreenMediaFilter;
    private SystemSettingSeekBarPreference mLockscreenMediaBlur;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.x_visualizer_ui);

        ContentResolver resolver = getActivity().getContentResolver();

        boolean mLavaLampEnabled = Settings.Secure.getIntForUser(resolver,
                Settings.Secure.LOCKSCREEN_LAVALAMP_ENABLED, 1,
                UserHandle.USER_CURRENT) != 0;

        mAutoColor = (SwitchPreference) findPreference(KEY_AUTOCOLOR);
        mAutoColor.setEnabled(!mLavaLampEnabled);

        if (mLavaLampEnabled) {
            mAutoColor.setSummary(getActivity().getString(
                    R.string.lockscreen_autocolor_lavalamp));
        } else {
            mAutoColor.setSummary(getActivity().getString(
                    R.string.lockscreen_autocolor_summary));
        }

        mLavaLamp = (SwitchPreference) findPreference(KEY_LAVALAMP);
        mLavaLamp.setOnPreferenceChangeListener(this);

        mLockscreenMediaBlur = (SystemSettingSeekBarPreference) findPreference(LOCKSCREEN_MEDIA_BLUR);
        mLockscreenMediaBlur.setOnPreferenceChangeListener(this);
        int lsBlurValue = Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKSCREEN_MEDIA_BLUR, 100);
        mLockscreenMediaBlur.setValue(lsBlurValue);

        mLockscreenMediaFilter = (SecureSettingListPreference) findPreference(LOCKSCREEN_MEDIA_FILTER);
        mLockscreenMediaFilter.setOnPreferenceChangeListener(this);
        int lsFilter = Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.LOCKSCREEN_ALBUMART_FILTER, 0);
        mLockscreenMediaFilter.setValue(String.valueOf(lsFilter));
        mLockscreenMediaFilter.setOnPreferenceChangeListener(this);
        if (lsFilter == 3 || lsFilter == 4) {
            mLockscreenMediaBlur.setEnabled(true);
        } else {
            mLockscreenMediaBlur.setEnabled(false);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mLavaLamp) {
            boolean mLavaLampEnabled = (Boolean) newValue;
            if (mLavaLampEnabled) {
                mAutoColor.setSummary(getActivity().getString(
                        R.string.lockscreen_autocolor_lavalamp));
            } else {
                mAutoColor.setSummary(getActivity().getString(
                        R.string.lockscreen_autocolor_summary));
            }
            return true;
        } else if (preference == mLockscreenMediaBlur) {
            int lsBlurValue = (Integer) newValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_MEDIA_BLUR, lsBlurValue);
            return true;
        } else if (preference == mLockscreenMediaFilter) {
            int lsFilterValue = Integer.parseInt(((String) newValue).toString());
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.LOCKSCREEN_ALBUMART_FILTER, lsFilterValue);
            mLockscreenMediaFilter.setValue(String.valueOf(lsFilterValue));
            if (lsFilterValue == 0 ||
                      lsFilterValue == 1 || lsFilterValue == 2) {
                mLockscreenMediaBlur.setEnabled(false);
            } else {
                mLockscreenMediaBlur.setEnabled(true);
            }
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.XTENSIONS;
    }
}

