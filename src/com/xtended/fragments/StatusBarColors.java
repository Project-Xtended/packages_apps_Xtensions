/*
 * Copyright (C) 2017-2021 The Project-Xtended
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

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Bundle;
import androidx.preference.ListPreference;
import androidx.preference.SwitchPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceScreen;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;

import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;
import android.provider.SearchIndexableResource;
import java.util.ArrayList;
import java.util.List;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto;

import com.xtended.support.colorpicker.ColorPickerPreference;

import com.android.internal.util.xtended.ColorConstants;
import com.android.internal.util.xtended.StatusBarColorHelper;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class StatusBarColors extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "StatusBarColors";

    private static final String PREF_TEXT_COLOR = "colors_status_bar_text_color";
    private static final String PREF_ICON_COLOR = "colors_status_bar_icon_color";
    private static final String PREF_TEXT_COLOR_DARK_MODE = "colors_status_bar_text_color_dark_mode";
    private static final String PREF_ICON_COLOR_DARK_MODE = "colors_status_bar_icon_color_dark_mode";
    private static final String PREF_BATTERY_TEXT_COLOR = "colors_status_bar_battery_text_color";
    private static final String PREF_BATTERY_TEXT_COLOR_DARK_MODE = "colors_status_bar_battery_text_color_dark_mode";

    static final int DEFAULT = 0xffffffff;
    static final int TRANSPARENT = 0x99FFFFFF;

    private static final int MENU_RESET = Menu.FIRST;

    private ColorPickerPreference mTextColor;
    private ColorPickerPreference mIconColor;
    private ColorPickerPreference mTextColorDarkMode;
    private ColorPickerPreference mIconColorDarkMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.status_bar_colors);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        int intColor;

        mTextColor =
                (ColorPickerPreference) findPreference(PREF_TEXT_COLOR);
        intColor = StatusBarColorHelper.getTextColor(getActivity());
        mTextColor.setNewPreviewColor(intColor);
        mTextColor.setOnPreferenceChangeListener(this);

        mIconColor =
                (ColorPickerPreference) findPreference(PREF_ICON_COLOR);
        intColor = StatusBarColorHelper.getIconColor(getActivity());
        mIconColor.setNewPreviewColor(intColor);
        mIconColor.setOnPreferenceChangeListener(this);

        mTextColorDarkMode =
                (ColorPickerPreference) findPreference(PREF_TEXT_COLOR_DARK_MODE);
        intColor = StatusBarColorHelper.getTextColorDarkMode(getActivity());
        mTextColorDarkMode.setNewPreviewColor(intColor);
        mTextColorDarkMode.setOnPreferenceChangeListener(this);

        mIconColorDarkMode =
                (ColorPickerPreference) findPreference(PREF_ICON_COLOR_DARK_MODE);
        intColor = StatusBarColorHelper.getIconColorDarkMode(getActivity());
        mIconColorDarkMode.setNewPreviewColor(intColor);
        mIconColorDarkMode.setOnPreferenceChangeListener(this);

       setHasOptionsMenu(true);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        String hex;
        int intHex;

        if (preference == mTextColor) {
            hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(resolver,
                    Settings.System.STATUS_BAR_TEXT_COLOR, intHex);
            return true;
        } else if (preference == mIconColor) {
            hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(resolver,
                    Settings.System.STATUS_BAR_ICON_COLOR, intHex);
            return true;
        } else if (preference == mTextColorDarkMode) {
            hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(resolver,
                    Settings.System.STATUS_BAR_TEXT_COLOR_DARK_MODE, intHex);
            return true;
        } else if (preference == mIconColorDarkMode) {
            hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(resolver,
                    Settings.System.STATUS_BAR_ICON_COLOR_DARK_MODE, intHex);
            return true;
        }
        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET, 0, R.string.reset)
                .setIcon(R.drawable.ic_action_reset_alpha)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESET:
                resetToDefault();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void resetToDefault() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(R.string.lockscreen_colors_reset_title);
        alertDialog.setMessage(R.string.lockscreen_colors_reset_message);
        alertDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                resetValues();
            }
        });
        alertDialog.setNegativeButton(R.string.cancel, null);
        alertDialog.create().show();
    }

    private void resetValues() {
        ContentResolver resolver = getActivity().getContentResolver();
        Settings.System.putInt(resolver,
        Settings.System.STATUS_BAR_TEXT_COLOR, ColorConstants.LIGHT_MODE_COLOR_SINGLE_TONE);
        mTextColor.setNewPreviewColor(ColorConstants.LIGHT_MODE_COLOR_SINGLE_TONE);
        Settings.System.putInt(resolver,
        Settings.System.STATUS_BAR_ICON_COLOR, ColorConstants.LIGHT_MODE_COLOR_SINGLE_TONE);
        mIconColor.setNewPreviewColor(ColorConstants.LIGHT_MODE_COLOR_SINGLE_TONE);
        Settings.System.putInt(resolver,
        Settings.System.STATUS_BAR_TEXT_COLOR_DARK_MODE, ColorConstants.DARK_MODE_COLOR_SINGLE_TONE);
        mTextColorDarkMode.setNewPreviewColor(ColorConstants.DARK_MODE_COLOR_SINGLE_TONE);
        Settings.System.putInt(resolver,
        Settings.System.STATUS_BAR_ICON_COLOR_DARK_MODE, ColorConstants.DARK_MODE_COLOR_SINGLE_TONE);
        mIconColorDarkMode.setNewPreviewColor(ColorConstants.DARK_MODE_COLOR_SINGLE_TONE);
    }

    @Override
    public void onResume() {
        super.onResume();
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
                    sir.xmlResId = R.xml.status_bar_colors;
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

