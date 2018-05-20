/*
 * Copyright (C) 2017 MSM-Xtended Project
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

package com.xtended.xtensions.fragments;

import android.content.Context;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.Utils;

import android.provider.SearchIndexableResource;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

import java.util.ArrayList;
import java.util.List;

public class DecorRoomSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {

    private static final String PREF_STATUS_BAR_WEATHER = "status_bar_show_weather_temp";

    private ListPreference mStatusBarWeather;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.decor_room);

        ContentResolver resolver = getActivity().getContentResolver();
        PreferenceScreen prefSet = getPreferenceScreen();
        Resources res = getResources();

       // Status bar weather
       mStatusBarWeather = (ListPreference) findPreference(PREF_STATUS_BAR_WEATHER);
       int temperatureShow = Settings.System.getIntForUser(resolver,
               Settings.System.STATUS_BAR_SHOW_WEATHER_TEMP, 0,
               UserHandle.USER_CURRENT);
       mStatusBarWeather.setValue(String.valueOf(temperatureShow));
       if (temperatureShow == 0) {
           mStatusBarWeather.setSummary(R.string.statusbar_weather_summary);
       } else {
           mStatusBarWeather.setSummary(mStatusBarWeather.getEntry());
       }
          mStatusBarWeather.setOnPreferenceChangeListener(this);
    }


    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mStatusBarWeather) {
            int temperatureShow = Integer.valueOf((String) objValue);
            int index = mStatusBarWeather.findIndexOfValue((String) objValue);
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                   Settings.System.STATUS_BAR_SHOW_WEATHER_TEMP,
                   temperatureShow, UserHandle.USER_CURRENT);
            if (temperatureShow == 0) {
                mStatusBarWeather.setSummary(R.string.statusbar_weather_summary);
            } else {
                mStatusBarWeather.setSummary(
                mStatusBarWeather.getEntries()[index]);
            }
            return true;
        }
         return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.XTENDED;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.decor_room;
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

