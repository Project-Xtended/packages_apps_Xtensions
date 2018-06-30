/*
 *  Copyright (C) 2015 The OmniROM Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.xtended.xtensions.fragments;

import com.android.internal.logging.nano.MetricsProto;

import android.app.Activity;
import android.content.Context;
import android.content.ContentResolver;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.ServiceManager;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import android.content.Context;

import android.provider.Settings;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.xtended.xtensions.preferences.SecureSettingSeekBarPreference;
import com.xtended.xtensions.preferences.AppMultiSelectListPreference;
import com.xtended.xtensions.preferences.ScrollAppsViewPreference;

public class MiscSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String SYSUI_ROUNDED_SIZE = "sysui_rounded_size";
    private static final String SYSUI_ROUNDED_CONTENT_PADDING = "sysui_rounded_content_padding";
    private static final String KEY_ASPECT_RATIO_APPS_ENABLED = "aspect_ratio_apps_enabled";
    private static final String KEY_ASPECT_RATIO_APPS_LIST = "aspect_ratio_apps_list";
    private static final String KEY_ASPECT_RATIO_CATEGORY = "aspect_ratio_category";
    private static final String KEY_ASPECT_RATIO_APPS_LIST_SCROLLER = "aspect_ratio_apps_list_scroller";

    private Context mContext;
    private SecureSettingSeekBarPreference mCornerRadius;
    private SecureSettingSeekBarPreference mContentPadding;
    private AppMultiSelectListPreference mAspectRatioAppsSelect;
    private ScrollAppsViewPreference mAspectRatioApps;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.settings_misc);

        // Rounded Corner Radius
        mCornerRadius = (SecureSettingSeekBarPreference) findPreference(SYSUI_ROUNDED_SIZE);
        int cornerRadius = Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.SYSUI_ROUNDED_SIZE, 0);
        mCornerRadius.setValue(cornerRadius / 1);
        mCornerRadius.setOnPreferenceChangeListener(this);

        // Rounded Content Padding
        mContentPadding = (SecureSettingSeekBarPreference) findPreference(SYSUI_ROUNDED_CONTENT_PADDING);
        int contentPadding = Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.SYSUI_ROUNDED_CONTENT_PADDING, 0);
        mContentPadding.setValue(contentPadding / 1);
        mContentPadding.setOnPreferenceChangeListener(this);

        //final PreferenceCategory aspectRatioCategory =
        //        (PreferenceCategory) getPreferenceScreen().findPreference(KEY_ASPECT_RATIO_CATEGORY);
        //final boolean supportMaxAspectRatio = getResources().getBoolean(com.android.internal.R.bool.config_haveHigherAspectRatioScreen);
        //if (!supportMaxAspectRatio) {
        //    getPreferenceScreen().removePreference(aspectRatioCategory);
        //} else {
        mAspectRatioAppsSelect = (AppMultiSelectListPreference) findPreference(KEY_ASPECT_RATIO_APPS_LIST);
        mAspectRatioApps = (ScrollAppsViewPreference) findPreference(KEY_ASPECT_RATIO_APPS_LIST_SCROLLER);
        final String valuesString = Settings.System.getString(getContentResolver(), Settings.System.ASPECT_RATIO_APPS_LIST);
        List<String> valuesList = new ArrayList<String>();
        if (!TextUtils.isEmpty(valuesString)) {
             valuesList.addAll(Arrays.asList(valuesString.split(":")));
             mAspectRatioApps.setVisible(true);
             mAspectRatioApps.setValues(valuesList);
        } else {
             mAspectRatioApps.setVisible(false);
        }
        mAspectRatioAppsSelect.setValues(valuesList);
        mAspectRatioAppsSelect.setOnPreferenceChangeListener(this);
        //}
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mCornerRadius) {
            int value = (Integer) newValue;
            Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.SYSUI_ROUNDED_SIZE, value * 1);
        } else if (preference == mContentPadding) {
            int value = (Integer) newValue;
            Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.SYSUI_ROUNDED_CONTENT_PADDING, value * 1);
        } else if (preference == mAspectRatioAppsSelect) {
            Collection<String> valueList = (Collection<String>) newValue;
            mAspectRatioApps.setVisible(false);
            if (valueList != null) {
                Settings.System.putString(getContentResolver(), Settings.System.ASPECT_RATIO_APPS_LIST,
                        TextUtils.join(":", valueList));
                mAspectRatioApps.setVisible(true);
                mAspectRatioApps.setValues(valueList);
            } else {
                Settings.System.putString(getContentResolver(), Settings.System.ASPECT_RATIO_APPS_LIST, "");
            }
            return true;
        }
        return true;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.XTENDED;
    }

}

