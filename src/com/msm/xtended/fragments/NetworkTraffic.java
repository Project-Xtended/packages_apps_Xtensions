package com.msm.xtended.fragments;

import com.android.internal.logging.nano.MetricsProto;

import android.os.Bundle;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import android.content.ContentResolver;
import android.content.res.Resources;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceCategory;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceFragment;
import android.provider.Settings;
import com.android.settings.R;

import java.util.Locale;
import android.text.TextUtils;
import android.view.View;

import com.android.settings.SettingsPreferenceFragment;
import com.msm.xtended.preferences.CustomSeekBarPreference;
import com.msm.xtended.preferences.SystemSettingSwitchPreference;
import com.msm.xtended.preferences.SystemSettingListPreference;
import com.android.settings.Utils;
import android.util.Log;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

public class NetworkTraffic extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String NETWORK_TRAFFIC_FONT_SIZE  = "network_traffic_font_size";
    private static final String NETWORK_TRAFFIC_FONT_STYLE = "network_traffic_font_style";

    private CustomSeekBarPreference mThreshold;
    private SystemSettingSwitchPreference mNetMonitor;
    private ListPreference mNetTrafficType;
    private CustomSeekBarPreference mNetTrafficSize;
    private SystemSettingListPreference mNetTrafficFont;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.x_network_traffic);

        PreferenceScreen prefSet = getPreferenceScreen();
        final ContentResolver resolver = getActivity().getContentResolver();

        boolean isNetMonitorEnabled = Settings.System.getIntForUser(resolver,
                Settings.System.NETWORK_TRAFFIC_STATE, 1, UserHandle.USER_CURRENT) == 1;
        mNetMonitor = (SystemSettingSwitchPreference) findPreference("network_traffic_state");
        mNetMonitor.setChecked(isNetMonitorEnabled);
        mNetMonitor.setOnPreferenceChangeListener(this);

        int value = Settings.System.getIntForUser(resolver,
                Settings.System.NETWORK_TRAFFIC_TYPE, 0, UserHandle.USER_CURRENT);
        mNetTrafficType = (ListPreference) findPreference("network_traffic_type");
        mNetTrafficType.setValue(String.valueOf(value));
        mNetTrafficType.setSummary(mNetTrafficType.getEntry());
        mNetTrafficType.setOnPreferenceChangeListener(this);

        mNetTrafficSize = (CustomSeekBarPreference) findPreference(NETWORK_TRAFFIC_FONT_SIZE);
        int NetTrafficSize = Settings.System.getInt(resolver,
                Settings.System.NETWORK_TRAFFIC_FONT_SIZE, 21);
        mNetTrafficSize.setValue(NetTrafficSize / 1);
        mNetTrafficSize.setOnPreferenceChangeListener(this);
	mNetTrafficFont = (SystemSettingListPreference) findPreference(NETWORK_TRAFFIC_FONT_STYLE);
        int netTrafFont = Settings.System.getInt(resolver,
                Settings.System.NETWORK_TRAFFIC_FONT_STYLE, 0);
        mNetTrafficFont.setValue(String.valueOf(netTrafFont));
        mNetTrafficFont.setOnPreferenceChangeListener(this);

        value = Settings.System.getIntForUser(resolver,
                Settings.System.NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD, 1, UserHandle.USER_CURRENT);
        mThreshold = (CustomSeekBarPreference) findPreference("network_traffic_autohide_threshold");
        mThreshold.setValue(value);
        mThreshold.setOnPreferenceChangeListener(this);
        mThreshold.setEnabled(isNetMonitorEnabled);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {

        if (preference == mNetMonitor) {
            boolean value = (Boolean) objValue;
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_STATE, value ? 1 : 0,
                    UserHandle.USER_CURRENT);
            mNetMonitor.setChecked(value);
            mThreshold.setEnabled(value);
            return true;
        } else if (preference == mThreshold) {
            int val = (Integer) objValue;
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD, val,
                    UserHandle.USER_CURRENT);
            return true;
        }  else if (preference == mNetTrafficSize) {
            int width = ((Integer)objValue).intValue();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_FONT_SIZE, width);
            return true;
	}  else if (preference == mNetTrafficFont) {
            int netTrafFont = Integer.valueOf((String) objValue);
            int index = mNetTrafficFont.findIndexOfValue((String) objValue);
            Settings.System.putInt(getActivity().getContentResolver(), Settings.System.
                NETWORK_TRAFFIC_FONT_STYLE, netTrafFont);
            mNetTrafficFont.setSummary(mNetTrafficFont.getEntries()[index]);
            return true;
        } else if (preference == mNetTrafficType) {
            int val = Integer.valueOf((String) objValue);
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_TYPE, val,
                    UserHandle.USER_CURRENT);
            int index = mNetTrafficType.findIndexOfValue((String) objValue);
            mNetTrafficType.setSummary(mNetTrafficType.getEntries()[index]);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.XTENSIONS;
    }

}

