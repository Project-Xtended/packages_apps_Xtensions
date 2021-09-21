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

import com.android.internal.logging.nano.MetricsProto;

import android.app.Activity;
import android.content.Context;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemProperties;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import java.util.Locale;
import android.text.TextUtils;
import android.view.View;

import com.xtended.support.colorpicker.ColorPickerPreference;
import com.xtended.support.preferences.CustomSeekBarPreference;
import com.xtended.support.preferences.SystemSettingIntListPreference;
import com.xtended.support.preferences.SystemSettingSwitchPreference;

import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;
import android.provider.SearchIndexableResource;
import java.util.List;
import java.util.ArrayList;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class QsThemeSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String FILE_QSPANEL_SELECT = "file_qspanel_select";
    private static final String PREF_QS_NEW_BG = "qs_new_bg_enabled";
    private static final String PREF_QS_NEW_IMAGE = "qs_panel_type_background";
    private static final int REQUEST_PICK_IMAGE = 0;
    private static final String QS_PANEL_IMAGE_SHADOW = "qs_panel_image_shadow";

    private Preference mQsPanelImage;
    private SystemSettingIntListPreference mQsNewBgEnabled;
    private SystemSettingSwitchPreference mQsNewImage;
    private CustomSeekBarPreference mQsPanelImageShadow;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.x_qs_theme_settings);

        PreferenceScreen prefScreen = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mQsPanelImage = findPreference(FILE_QSPANEL_SELECT);

        mQsNewImage = (SystemSettingSwitchPreference) findPreference(PREF_QS_NEW_IMAGE);

        mQsPanelImageShadow = (CustomSeekBarPreference) findPreference(QS_PANEL_IMAGE_SHADOW);
        final int qsPanelShadow = Settings.System.getInt(getContentResolver(),
                Settings.System.QS_PANEL_IMAGE_SHADOW, 0);
        mQsPanelImageShadow.setValue((int)(((double) qsPanelShadow / 255) * 100));
        mQsPanelImageShadow.setOnPreferenceChangeListener(this);

        mQsNewBgEnabled = (SystemSettingIntListPreference) findPreference(PREF_QS_NEW_BG);
        int val = Settings.System.getIntForUser(getActivity().getContentResolver(),
                Settings.System.QS_NEW_BG_ENABLED, 0, UserHandle.USER_CURRENT);
        mQsNewBgEnabled.setOnPreferenceChangeListener(this);
        if (val > 0) {
            mQsNewImage.setEnabled(false);
        } else {
            mQsNewImage.setEnabled(true);
        }
        if (val > 0) {
            mQsNewImage.setEnabled(false);
            mQsPanelImage.setEnabled(false);
            mQsPanelImageShadow.setEnabled(false);
        } else {
            mQsNewImage.setEnabled(true);
            mQsPanelImage.setEnabled(true);
            mQsPanelImageShadow.setEnabled(true);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mQsPanelImageShadow) {
            Integer qsPanelShadow = (Integer) newValue;
            int realShadowValue = (int) (((double) qsPanelShadow / 100) * 255);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.QS_PANEL_IMAGE_SHADOW, realShadowValue);
            return true;
        } else if (preference == mQsNewBgEnabled) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.QS_NEW_BG_ENABLED, val);
            if (val > 0) {
                mQsNewImage.setEnabled(false);
                mQsPanelImage.setEnabled(false);
                mQsPanelImageShadow.setEnabled(false);
            } else {
                mQsNewImage.setEnabled(true);
                mQsPanelImage.setEnabled(true);
                mQsPanelImageShadow.setEnabled(true);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mQsPanelImage) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_PICK_IMAGE);
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (requestCode == REQUEST_PICK_IMAGE) {
            if (resultCode != Activity.RESULT_OK) {
                return;
            }
            final Uri imageUri = result.getData();
            Settings.System.putString(getContentResolver(), Settings.System.QS_PANEL_CUSTOM_IMAGE, imageUri.toString());
		}
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.XTENSIONS;
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {

                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();
                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.x_qs_theme_settings;
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


