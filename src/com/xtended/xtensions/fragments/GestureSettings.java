package com.xtended.xtensions.fragments;

import com.android.internal.logging.nano.MetricsProto;

import android.content.Context;
import android.content.ContentResolver;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.R;

import com.android.settings.SettingsPreferenceFragment;

import com.xtended.xtensions.preferences.Utils;
import android.provider.SearchIndexableResource;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

import java.util.ArrayList;
import java.util.List;

public class GestureSettings extends SettingsPreferenceFragment implements
          Preference.OnPreferenceChangeListener, Indexable {

    private static final String TORCH_POWER_BUTTON_GESTURE = "torch_power_button_gesture";
    private static final String KEY_TORCH_LONG_PRESS_POWER_TIMEOUT = "torch_long_press_power_timeout";

    private ListPreference mTorchPowerButton;
    private ListPreference mTorchLongPressPowerTimeout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings_gestures);

        ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        mTorchPowerButton = (ListPreference) findPreference(TORCH_POWER_BUTTON_GESTURE);
        int mTorchPowerButtonValue = Settings.Secure.getInt(resolver,
               Settings.Secure.TORCH_POWER_BUTTON_GESTURE, 0);
        mTorchPowerButton.setValue(Integer.toString(mTorchPowerButtonValue));
        mTorchPowerButton.setSummary(mTorchPowerButton.getEntry());
        mTorchPowerButton.setOnPreferenceChangeListener(this);

        mTorchLongPressPowerTimeout = (ListPreference) findPreference(KEY_TORCH_LONG_PRESS_POWER_TIMEOUT);
        int mTorchLongPressPowerTimeoutValue = Settings.System.getInt(resolver,
             Settings.System.TORCH_LONG_PRESS_POWER_TIMEOUT, 0);
        mTorchLongPressPowerTimeout.setValue(Integer.toString(mTorchLongPressPowerTimeoutValue));
        mTorchLongPressPowerTimeout.setSummary(mTorchLongPressPowerTimeout.getEntry());
        mTorchLongPressPowerTimeout.setOnPreferenceChangeListener(this);

        if (!Utils.deviceSupportsFlashLight(getActivity())) {
	     prefScreen.removePreference(mTorchPowerButton);
	     prefScreen.removePreference(mTorchLongPressPowerTimeout);
	}
    }

     public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();

        if (preference == mTorchPowerButton) {
            int mTorchPowerButtonValue = Integer.valueOf((String) newValue);
            int index = mTorchPowerButton.findIndexOfValue((String) newValue);
            mTorchPowerButton.setSummary(
                    mTorchPowerButton.getEntries()[index]);
            Settings.Secure.putInt(resolver, Settings.Secure.TORCH_POWER_BUTTON_GESTURE,
                    mTorchPowerButtonValue);
            if (mTorchPowerButtonValue == 1) {
                //if doubletap for torch is enabled, switch off double tap for camera
                Settings.Secure.putInt(resolver, Settings.Secure.CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED,
                        1);
           }
           return true;
        } else if (preference == mTorchLongPressPowerTimeout) {
            int mTorchLongPressPowerTimeoutValue = Integer.valueOf((String) newValue);
            int index = mTorchLongPressPowerTimeout.findIndexOfValue((String) newValue);
            mTorchLongPressPowerTimeout.setSummary(
                    mTorchLongPressPowerTimeout.getEntries()[index]);
            Settings.System.putInt(resolver, Settings.System.TORCH_LONG_PRESS_POWER_TIMEOUT,
            mTorchLongPressPowerTimeoutValue);
            return true;
        }
        return false;
      }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.XTENDED;
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.settings_gestures;
                    result.add(sir);
                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    ArrayList<String> result = new ArrayList<String>();
                    return result;
                }
     };
}
