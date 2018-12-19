package com.msm.xtended.fragments;

import com.android.internal.logging.nano.MetricsProto;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.ListPreference;

import com.android.settings.R;

import com.android.settings.SettingsPreferenceFragment;

import com.msm.xtended.preferences.SystemSettingSeekBarPreference;

public class GestureSettings extends SettingsPreferenceFragment implements
                                Preference.OnPreferenceChangeListener {

    private static final String KEY_TORCH_LONG_PRESS_POWER_TIMEOUT =
            "torch_long_press_power_timeout";
    private static final String KEY_SWIPE_LENGTH = "gesture_swipe_length";
    private static final String KEY_SWIPE_TIMEOUT = "gesture_swipe_timeout";

    private ListPreference mTorchLongPressPowerTimeout;
    private SystemSettingSeekBarPreference mSwipeTriggerLength;
    private SystemSettingSeekBarPreference mSwipeTriggerTimeout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.x_settings_gestures);
        ContentResolver resolver = getActivity().getContentResolver();
        mFooterPreferenceMixin.createFooterPreference().setTitle(R.string.gesture_settings_info);

        mTorchLongPressPowerTimeout =
                    (ListPreference) findPreference(KEY_TORCH_LONG_PRESS_POWER_TIMEOUT);
        mTorchLongPressPowerTimeout.setOnPreferenceChangeListener(this);
        int TorchTimeout = Settings.System.getInt(getContentResolver(),
                        Settings.System.TORCH_LONG_PRESS_POWER_TIMEOUT, 0);
        mTorchLongPressPowerTimeout.setValue(Integer.toString(TorchTimeout));
        mTorchLongPressPowerTimeout.setSummary(mTorchLongPressPowerTimeout.getEntry());

        mSwipeTriggerLength = (SystemSettingSeekBarPreference) findPreference(KEY_SWIPE_LENGTH);
        int triggerLength = Settings.System.getInt(resolver, Settings.System.BOTTOM_GESTURE_SWIPE_LIMIT,
                getSwipeLengthInPixel(getResources().getInteger(com.android.internal.R.integer.nav_gesture_swipe_min_length)));
        mSwipeTriggerLength.setValue(triggerLength);
        mSwipeTriggerLength.setOnPreferenceChangeListener(this);

        mSwipeTriggerTimeout = (SystemSettingSeekBarPreference) findPreference(KEY_SWIPE_TIMEOUT);
        int triggerTimeout = Settings.System.getInt(resolver, Settings.System.BOTTOM_GESTURE_TRIGGER_TIMEOUT,
                getResources().getInteger(com.android.internal.R.integer.nav_gesture_swipe_timout));
        mSwipeTriggerTimeout.setValue(triggerTimeout);
        mSwipeTriggerTimeout.setOnPreferenceChangeListener(this);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
         ContentResolver resolver = getActivity().getContentResolver();
         if (preference == mTorchLongPressPowerTimeout) {
            String TorchTimeout = (String) newValue;
            int TorchTimeoutValue = Integer.parseInt(TorchTimeout);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.TORCH_LONG_PRESS_POWER_TIMEOUT, TorchTimeoutValue);
            int TorchTimeoutIndex = mTorchLongPressPowerTimeout
                    .findIndexOfValue(TorchTimeout);
            mTorchLongPressPowerTimeout
                    .setSummary(mTorchLongPressPowerTimeout.getEntries()[TorchTimeoutIndex]);
            return true;
         } else if (preference == mSwipeTriggerLength) {
            int value = (Integer) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.BOTTOM_GESTURE_SWIPE_LIMIT, value);
            return true;
         } else if (preference == mSwipeTriggerTimeout) {
            int value = (Integer) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.BOTTOM_GESTURE_TRIGGER_TIMEOUT, value);
            return true;
        }
        return false;
    }

    private int getSwipeLengthInPixel(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.XTENSIONS;
    }
}
