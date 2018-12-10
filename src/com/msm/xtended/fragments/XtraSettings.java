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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v14.preference.SwitchPreference;
import android.text.TextUtils;
import android.provider.Settings;
import com.android.settings.R;
import com.android.settings.SettingsActivity;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.HashSet;

import com.android.settings.SettingsPreferenceFragment;

import com.msm.xtended.preferences.AppMultiSelectListPreference;
import com.msm.xtended.preferences.ScrollAppsViewPreference;
import com.msm.xtended.preferences.SystemSettingSwitchPreference;
import com.msm.xtended.preferences.SystemSettingSeekBarPreference;

public class XtraSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String MEDIA_SCANNER_ON_BOOT = "media_scanner_on_boot";
    private static final String KEY_ASPECT_RATIO_APPS_ENABLED = "aspect_ratio_apps_enabled";
    private static final String KEY_ASPECT_RATIO_APPS_LIST = "aspect_ratio_apps_list";
    private static final String KEY_ASPECT_RATIO_CATEGORY = "aspect_ratio_category";
    private static final String KEY_ASPECT_RATIO_APPS_LIST_SCROLLER = "aspect_ratio_apps_list_scroller";
    private static final String HEADSET_CONNECT_PLAYER = "headset_connect_player";
    private static final String SYSUI_ROUNDED_CONTENT_PADDING = "sysui_rounded_content_padding";
    private static final String SYSUI_ROUNDED_SIZE = "sysui_rounded_size";
    private static final String KEY_SHOW_DASHBOARD_COLUMNS = "show_dashboard_columns";
    private static final String KEY_HIDE_DASHBOARD_SUMMARY = "hide_dashboard_summary";

    private SharedPreferences mAppPreferences;

    private SystemSettingSeekBarPreference mContentPadding;
    private SystemSettingSeekBarPreference mCornerRadius;
    private ListPreference mMSOB;
    private ListPreference mLaunchPlayerHeadsetConnection;
    private AppMultiSelectListPreference mAspectRatioAppsSelect;
    private ScrollAppsViewPreference mAspectRatioApps;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.x_settings_xtra);
        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        // MediaScanner behavior on boot
        mMSOB = (ListPreference) findPreference(MEDIA_SCANNER_ON_BOOT);
        int mMSOBValue = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.MEDIA_SCANNER_ON_BOOT, 0);
        mMSOB.setValue(String.valueOf(mMSOBValue));
        mMSOB.setSummary(mMSOB.getEntry());
        mMSOB.setOnPreferenceChangeListener(this);

        mLaunchPlayerHeadsetConnection = (ListPreference) findPreference(HEADSET_CONNECT_PLAYER);
        int mLaunchPlayerHeadsetConnectionValue = Settings.System.getIntForUser(resolver,
                Settings.System.HEADSET_CONNECT_PLAYER, 4, UserHandle.USER_CURRENT);
        mLaunchPlayerHeadsetConnection.setValue(Integer.toString(mLaunchPlayerHeadsetConnectionValue));
        mLaunchPlayerHeadsetConnection.setSummary(mLaunchPlayerHeadsetConnection.getEntry());
        mLaunchPlayerHeadsetConnection.setOnPreferenceChangeListener(this);

        final PreferenceCategory aspectRatioCategory =
                (PreferenceCategory) getPreferenceScreen().findPreference(KEY_ASPECT_RATIO_CATEGORY);
        final boolean supportMaxAspectRatio = getResources().getBoolean(com.android.internal.R.bool.config_haveHigherAspectRatioScreen);
        if (!supportMaxAspectRatio) {
            getPreferenceScreen().removePreference(aspectRatioCategory);
        } else {
            mAspectRatioAppsSelect = (AppMultiSelectListPreference) findPreference(KEY_ASPECT_RATIO_APPS_LIST);
            mAspectRatioApps = (ScrollAppsViewPreference) findPreference(KEY_ASPECT_RATIO_APPS_LIST_SCROLLER);
            final String valuesString = Settings.System.getString(resolver, Settings.System.OMNI_ASPECT_RATIO_APPS_LIST);
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
        }

        mContentPadding = (SystemSettingSeekBarPreference) findPreference(SYSUI_ROUNDED_CONTENT_PADDING);
        int contentPadding = Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.SYSUI_ROUNDED_CONTENT_PADDING, 1);
                mContentPadding.setValue(contentPadding / 1);
                mContentPadding.setOnPreferenceChangeListener(this);

        mCornerRadius = (SystemSettingSeekBarPreference) findPreference(SYSUI_ROUNDED_SIZE);
        int cornerRadius = Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.SYSUI_ROUNDED_SIZE, 1);
                mCornerRadius.setValue(cornerRadius / 1);
                mCornerRadius.setOnPreferenceChangeListener(this);

        mAppPreferences = getActivity().getSharedPreferences(SettingsActivity.APP_PREFERENCES_NAME,
                Context.MODE_PRIVATE);

        SwitchPreference showColumnsLayout = (SwitchPreference) findPreference(KEY_SHOW_DASHBOARD_COLUMNS);
        showColumnsLayout.setChecked(mAppPreferences.getInt(SettingsActivity.KEY_COLUMNS_COUNT, 1) == 2);
        showColumnsLayout.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if ((Boolean) newValue ) {
                    mAppPreferences.edit().putInt(SettingsActivity.KEY_COLUMNS_COUNT, 2).commit();
                } else {
                    mAppPreferences.edit().putInt(SettingsActivity.KEY_COLUMNS_COUNT, 1).commit();
                }
                return true;
            }
        });

        SwitchPreference hideColumnSummary = (SwitchPreference) findPreference(KEY_HIDE_DASHBOARD_SUMMARY);
        hideColumnSummary.setChecked(mAppPreferences.getBoolean(SettingsActivity.KEY_HIDE_SUMMARY, false));
        hideColumnSummary.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                mAppPreferences.edit().putBoolean(SettingsActivity.KEY_HIDE_SUMMARY, ((Boolean) newValue)).commit();
                return true;
            }
        });
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();

        if (preference == mMSOB) {
            int value = Integer.parseInt(((String) newValue).toString());
            Settings.System.putInt(getContentResolver(),
                    Settings.System.MEDIA_SCANNER_ON_BOOT, value);
            mMSOB.setValue(String.valueOf(value));
            mMSOB.setSummary(mMSOB.getEntries()[value]);
            return true;
        } else if (preference == mLaunchPlayerHeadsetConnection) {
            int mLaunchPlayerHeadsetConnectionValue = Integer.valueOf((String) newValue);
            int index = mLaunchPlayerHeadsetConnection.findIndexOfValue((String) newValue);
            mLaunchPlayerHeadsetConnection.setSummary(
                    mLaunchPlayerHeadsetConnection.getEntries()[index]);
            Settings.System.putIntForUser(resolver, Settings.System.HEADSET_CONNECT_PLAYER,
                    mLaunchPlayerHeadsetConnectionValue, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mAspectRatioAppsSelect) {
            Collection<String> valueList = (Collection<String>) newValue;
            mAspectRatioApps.setVisible(false);
            if (valueList != null) {
                Settings.System.putString(resolver, Settings.System.OMNI_ASPECT_RATIO_APPS_LIST,
                        TextUtils.join(":", valueList));
                mAspectRatioApps.setVisible(true);
                mAspectRatioApps.setValues(valueList);
            } else {
                Settings.System.putString(resolver, Settings.System.OMNI_ASPECT_RATIO_APPS_LIST, "");
            }
            return true;
        } else if (preference == mContentPadding) {
            int value = (Integer) newValue;
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.SYSUI_ROUNDED_CONTENT_PADDING, value * 1);
            return true;
        } else if (preference == mCornerRadius) {
            int value = (Integer) newValue;
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.SYSUI_ROUNDED_SIZE, value * 1);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.XTENSIONS;
    }
}
