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

import com.msm.xtended.preferences.XUtils;

import com.android.settings.SettingsPreferenceFragment;

public class XDecorRoom extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String PREF_STATUS_BAR_WEATHER = "status_bar_show_weather_temp";
    private static final String QS_TILE_STYLE = "qs_tile_style";
    private static final String QS_HEADER_STYLE = "qs_header_style";

    private ListPreference mStatusBarWeather;
    private ListPreference mQsTileStyle;
    private ListPreference mQsHeaderStyle;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.x_decor_room);

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

       mQsTileStyle = (ListPreference) findPreference(QS_TILE_STYLE);
       int qsTileStyle = Settings.System.getIntForUser(resolver,
               Settings.System.QS_TILE_STYLE, 0,
	       UserHandle.USER_CURRENT);
       int valueIndex = mQsTileStyle.findIndexOfValue(String.valueOf(qsTileStyle));
       mQsTileStyle.setValueIndex(valueIndex >= 0 ? valueIndex : 0);
       mQsTileStyle.setSummary(mQsTileStyle.getEntry());
       mQsTileStyle.setOnPreferenceChangeListener(this);

       mQsHeaderStyle = (ListPreference) findPreference(QS_HEADER_STYLE);
       int qsHeaderStyle = Settings.System.getInt(resolver,
               Settings.System.QS_HEADER_STYLE, 0);
       int newIndex = mQsHeaderStyle.findIndexOfValue(String.valueOf(qsHeaderStyle));
       mQsHeaderStyle.setValueIndex(newIndex >= 0 ? newIndex : 0);
       mQsHeaderStyle.setSummary(mQsHeaderStyle.getEntry());
       mQsHeaderStyle.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        ContentResolver resolver = getActivity().getContentResolver();

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
        } else if (preference == mQsTileStyle) {
            int qsTileStyleValue = Integer.valueOf((String) objValue);
            Settings.System.putIntForUser(resolver, Settings.System.QS_TILE_STYLE,
                    qsTileStyleValue, UserHandle.USER_CURRENT);
            mQsTileStyle.setSummary(mQsTileStyle.getEntries()[qsTileStyleValue]);
        } else if (preference == mQsHeaderStyle) {
            String value = (String) objValue;
            Settings.System.putInt(resolver, Settings.System.QS_HEADER_STYLE,
                   Integer.valueOf(value));
            int newIndex = mQsHeaderStyle.findIndexOfValue(value);
            mQsHeaderStyle.setSummary(mQsHeaderStyle.getEntries()[newIndex]);
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.XTENSIONS;
    }
}

