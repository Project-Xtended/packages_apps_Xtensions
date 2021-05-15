/**
 * Copyright (C) 2017 - 2021 The Project-Xtended
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
package com.xtended.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;
import android.provider.SearchIndexableResource;

import com.xtended.preferences.DozeUtils;
import com.android.settings.SettingsPreferenceFragment;
import com.xtended.support.preferences.CustomSeekBarPreference;
import com.xtended.support.preferences.SystemSettingSeekBarPreference;
import com.xtended.support.preferences.SystemSettingSwitchPreference;
import com.xtended.support.preferences.SecureSettingSwitchPreference;

import java.util.List;
import java.util.ArrayList;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class XAmbientDecorSettings extends SettingsPreferenceFragment
                         implements OnPreferenceChangeListener {

    public static final String TAG = "XAmbientDecorSettings";

    private static final String KEY_DOZE_ALWAYS_ON = "doze_always_on";

    private static final String CATEG_DOZE_SENSOR = "doze_sensor";

    private static final String KEY_DOZE_TILT_GESTURE = "doze_tilt_gesture";
    private static final String KEY_DOZE_PICK_UP_GESTURE = "doze_pick_up_gesture";
    private static final String KEY_DOZE_HANDWAVE_GESTURE = "doze_handwave_gesture";
    private static final String KEY_DOZE_POCKET_GESTURE = "doze_pocket_gesture";
    private static final String KEY_DOZE_GESTURE_VIBRATE = "doze_gesture_vibrate";

    private SwitchPreference mDozeAlwaysOnPreference;
    private SwitchPreference mTiltPreference;
    private SwitchPreference mPickUpPreference;
    private SwitchPreference mHandwavePreference;
    private SwitchPreference mPocketPreference;

    private SharedPreferences mPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ContentResolver resolver = getActivity().getContentResolver();
        PreferenceScreen prefScreen = getPreferenceScreen();
        addPreferencesFromResource(R.xml.x_ambient_decor);

        Context context = getContext();

        PreferenceCategory dozeSensorCategory =
                (PreferenceCategory) getPreferenceScreen().findPreference(CATEG_DOZE_SENSOR);

        mDozeAlwaysOnPreference = (SwitchPreference) findPreference(KEY_DOZE_ALWAYS_ON);

        mTiltPreference = (SwitchPreference) findPreference(KEY_DOZE_TILT_GESTURE);
        mTiltPreference.setOnPreferenceChangeListener(this);

        mPickUpPreference = (SwitchPreference) findPreference(KEY_DOZE_PICK_UP_GESTURE);
        mPickUpPreference.setOnPreferenceChangeListener(this);

        mHandwavePreference = (SwitchPreference) findPreference(KEY_DOZE_HANDWAVE_GESTURE);
        mHandwavePreference.setOnPreferenceChangeListener(this);

        mPocketPreference = (SwitchPreference) findPreference(KEY_DOZE_POCKET_GESTURE);
        mPocketPreference.setOnPreferenceChangeListener(this);

        // Hide sensor related features if the device doesn't support them
        if (!DozeUtils.getTiltSensor(context) && !DozeUtils.getPickupSensor(context) && !DozeUtils.getProximitySensor(context)) {
            getPreferenceScreen().removePreference(dozeSensorCategory);
        } else {
            if (!DozeUtils.getTiltSensor(context)) {
                getPreferenceScreen().removePreference(mTiltPreference);
            } else if (!DozeUtils.getPickupSensor(context)) {
                getPreferenceScreen().removePreference(mPickUpPreference);
            } else if (!DozeUtils.getProximitySensor(context)) {
                getPreferenceScreen().removePreference(mHandwavePreference);
                getPreferenceScreen().removePreference(mPocketPreference);
            }
        }

        // Hides always on toggle if device doesn't support it (based on config_dozeAlwaysOnDisplayAvailable overlay)
        boolean mAlwaysOnAvailable = getResources().getBoolean(com.android.internal.R.bool.config_dozeAlwaysOnDisplayAvailable);
        if (!mAlwaysOnAvailable) {
            getPreferenceScreen().removePreference(mDozeAlwaysOnPreference);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
         Context context = getContext();
         ContentResolver resolver = getActivity().getContentResolver();

         if (preference == mTiltPreference) {
             boolean value = (Boolean) newValue;
             Settings.Secure.putIntForUser(resolver, Settings.Secure.DOZE_TILT_GESTURE,
                  value ? 1 : 0, UserHandle.USER_CURRENT);
             DozeUtils.enableService(context);
             if (newValue != null)
                 sensorWarning(context);
             return true;
         } else if (preference == mPickUpPreference) {
             boolean value = (Boolean) newValue;
             Settings.Secure.putIntForUser(resolver, Settings.Secure.DOZE_PICK_UP_GESTURE,
                  value ? 1 : 0, UserHandle.USER_CURRENT);
             DozeUtils.enableService(context);
             if (newValue != null)
                 sensorWarning(context);
             return true;
         } else if (preference == mHandwavePreference) {
             boolean value = (Boolean) newValue;
             Settings.Secure.putIntForUser(resolver, Settings.Secure.DOZE_HANDWAVE_GESTURE,
                  value ? 1 : 0, UserHandle.USER_CURRENT);
             DozeUtils.enableService(context);
             if (newValue != null)
                 sensorWarning(context);
             return true;
         } else if (preference == mPocketPreference) {
             boolean value = (Boolean) newValue;
             Settings.Secure.putIntForUser(resolver, Settings.Secure.DOZE_POCKET_GESTURE,
                  value ? 1 : 0, UserHandle.USER_CURRENT);
             DozeUtils.enableService(context);
             if (newValue != null)
                 sensorWarning(context);
             return true;
         }
         return false;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        return super.onPreferenceTreeClick(preference);
    }

    private void sensorWarning(Context context) {
        mPreferences = context.getSharedPreferences("dozesettingsfragment", Activity.MODE_PRIVATE);
        if (mPreferences.getBoolean("sensor_warning_shown", false)) {
            return;
        }
        context.getSharedPreferences("dozesettingsfragment", Activity.MODE_PRIVATE)
                .edit()
                .putBoolean("sensor_warning_shown", true)
                .commit();

        new AlertDialog.Builder(context)
                .setTitle(getResources().getString(R.string.sensor_warning_title))
                .setMessage(getResources().getString(R.string.sensor_warning_message))
                .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                }).show();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.XTENSIONS;
    }

    /**
     * For Search
     */
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {

                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();
                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.x_ambient_decor;
                    result.add(sir);
                    return result;
                }

          @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    return keys;
                }
    };
}
