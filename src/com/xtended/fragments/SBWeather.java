package com.xtended.fragments;

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
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;
import android.provider.Settings;
import com.android.settings.R;

import java.util.Arrays;
import java.util.HashSet;

import com.android.settings.SettingsPreferenceFragment;
import com.xtended.support.preferences.CustomSeekBarPreference;
import com.xtended.support.colorpicker.ColorPickerPreference;

public class SBWeather extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String STATUS_BAR_WEATHER_ICON_COLOR = "status_bar_weather_icon_color";
    private static final String STATUS_BAR_WEATHER_FONT_COLOR = "status_bar_weather_font_color";
    private static final String STATUS_BAR_WEATHER_FONT_SIZE  = "status_bar_weather_font_size";
    private static final String STATUS_BAR_WEATHER_FONT_STYLE  = "status_bar_weather_font_style";
    private static final String WEATHER_FOOTER = "weather_footer";

    static final int DEFAULT_WEATHER_COLOR = 0xffffffff;

    private ColorPickerPreference mWeatherIconColor;
    private ColorPickerPreference mWeatherFontColor;
    private CustomSeekBarPreference mWeatherFontSize;
    private ListPreference mWeatherFontStyle;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.x_sb_weather);
        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        int intColor;
        String hexColor;

        findPreference(WEATHER_FOOTER).setTitle(R.string.weather_footer);

        mWeatherIconColor = (ColorPickerPreference) findPreference(STATUS_BAR_WEATHER_ICON_COLOR);
        mWeatherIconColor.setOnPreferenceChangeListener(this);
            intColor = Settings.System.getInt(resolver,
                    Settings.System.STATUS_BAR_WEATHER_ICON_COLOR, DEFAULT_WEATHER_COLOR);
            hexColor = String.format("#%08x", (0xffffffff & intColor));
        mWeatherIconColor.setSummary(hexColor);
        mWeatherIconColor.setNewPreviewColor(intColor);

        mWeatherFontColor = (ColorPickerPreference) findPreference(STATUS_BAR_WEATHER_FONT_COLOR);
        mWeatherFontColor.setOnPreferenceChangeListener(this);
            intColor = Settings.System.getInt(resolver,
                    Settings.System.STATUS_BAR_WEATHER_FONT_COLOR, DEFAULT_WEATHER_COLOR);
            hexColor = String.format("#%08x", (0xffffffff & intColor));
        mWeatherFontColor.setSummary(hexColor);
        mWeatherFontColor.setNewPreviewColor(intColor);

        mWeatherFontSize = (CustomSeekBarPreference) findPreference(STATUS_BAR_WEATHER_FONT_SIZE);
        int fontSize = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_WEATHER_FONT_SIZE, 14);
        mWeatherFontSize.setValue(fontSize / 1);
        mWeatherFontSize.setOnPreferenceChangeListener(this);

        mWeatherFontStyle = (ListPreference) findPreference(STATUS_BAR_WEATHER_FONT_STYLE);
        int weatherFont = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_WEATHER_FONT_STYLE, 0);
        mWeatherFontStyle.setValue(String.valueOf(weatherFont));
        mWeatherFontStyle.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
 	ContentResolver resolver = getActivity().getContentResolver();

        if (preference == mWeatherIconColor) {
             String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(objValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(resolver,
                    Settings.System.STATUS_BAR_WEATHER_ICON_COLOR, intHex);
            return true;
        } else if (preference == mWeatherFontColor) {
             String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(objValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(resolver,
                    Settings.System.STATUS_BAR_WEATHER_FONT_COLOR, intHex);
            return true;
        }  else if (preference == mWeatherFontSize) {
            int fontSize = ((Integer)objValue).intValue();
            Settings.System.putInt(resolver,
                    Settings.System.STATUS_BAR_WEATHER_FONT_SIZE, fontSize);
            return true;
        }  else if (preference == mWeatherFontStyle) {
            int weatherFont = Integer.valueOf((String) objValue);
            int index = mWeatherFontStyle.findIndexOfValue((String) objValue);
            Settings.System.putInt(resolver, Settings.System.
                STATUS_BAR_WEATHER_FONT_STYLE, weatherFont);
            mWeatherFontStyle.setSummary(mWeatherFontStyle.getEntries()[index]);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.XTENSIONS;
    }
}



