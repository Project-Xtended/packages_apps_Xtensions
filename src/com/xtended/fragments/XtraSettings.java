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
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.SharedPreferences;
import android.provider.Settings;
import com.android.settings.R;

import java.util.Arrays;
import java.util.HashSet;

import com.android.settings.SettingsPreferenceFragment;
import com.xtended.support.preferences.CustomSeekBarPreference;
import com.xtended.support.preferences.SecureSettingSwitchPreference;
import com.xtended.support.preferences.SystemSettingSeekBarPreference;

import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;
import android.provider.SearchIndexableResource;
import java.util.ArrayList;
import java.util.List;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class XtraSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "XtraSettings";

    private static final String STATUSBAR_LEFT_PADDING = "statusbar_left_padding";
    private static final String STATUSBAR_RIGHT_PADDING = "statusbar_right_padding";
    private static final String SYSUI_ROUNDED_SIZE = "sysui_rounded_size";
    private static final String SYSUI_ROUNDED_CONTENT_PADDING = "sysui_rounded_content_padding";
    private static final String SYSUI_ROUNDED_FWVALS = "sysui_rounded_fwvals";

    private SystemSettingSeekBarPreference mSbLeftPadding;
    private SystemSettingSeekBarPreference mSbRightPadding;
    private CustomSeekBarPreference mCornerRadius;
    private CustomSeekBarPreference mContentPadding;
    private SecureSettingSwitchPreference mRoundedFwvals;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.x_settings_xtras);

        final PreferenceScreen prefScreen = getPreferenceScreen();

        final ContentResolver resolver = getActivity().getContentResolver();

        Resources res = null;
        Context ctx = getContext();
        float density = Resources.getSystem().getDisplayMetrics().density;

        try {
            res = ctx.getPackageManager().getResourcesForApplication("com.android.systemui");
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        mSbLeftPadding = (SystemSettingSeekBarPreference) findPreference(STATUSBAR_LEFT_PADDING);
        int sbLeftPadding = Settings.System.getIntForUser(ctx.getContentResolver(),
                Settings.System.LEFT_PADDING, ((int) (res.getIdentifier("com.android.systemui:dimen/status_bar_padding_start", null, null) / density)), UserHandle.USER_CURRENT);
        mSbLeftPadding.setValue(sbLeftPadding);
        mSbLeftPadding.setOnPreferenceChangeListener(this);

        mSbRightPadding = (SystemSettingSeekBarPreference) findPreference(STATUSBAR_RIGHT_PADDING);
        int sbRightPadding = Settings.System.getIntForUser(ctx.getContentResolver(),
                Settings.System.RIGHT_PADDING, ((int) (res.getIdentifier("com.android.systemui:dimen/status_bar_padding_end", null, null) / density)), UserHandle.USER_CURRENT);
        mSbRightPadding.setValue(sbRightPadding);
        mSbRightPadding.setOnPreferenceChangeListener(this);

        // Rounded Corner Radius
        mCornerRadius = (CustomSeekBarPreference) findPreference(SYSUI_ROUNDED_SIZE);
        int resourceIdRadius = (int) ctx.getResources().getDimension(com.android.internal.R.dimen.rounded_corner_radius);
        int cornerRadius = Settings.Secure.getIntForUser(ctx.getContentResolver(), Settings.Secure.SYSUI_ROUNDED_SIZE,
                ((int) (resourceIdRadius / density)), UserHandle.USER_CURRENT);
        mCornerRadius.setValue(cornerRadius);
        mCornerRadius.setOnPreferenceChangeListener(this);

        // Rounded use Framework Values
        mRoundedFwvals = (SecureSettingSwitchPreference) findPreference(SYSUI_ROUNDED_FWVALS);
        mRoundedFwvals.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        Resources res = null;
        Context ctx = getContext();
        float density = Resources.getSystem().getDisplayMetrics().density;

        try {
            res = ctx.getPackageManager().getResourcesForApplication("com.android.systemui");
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        if (preference == mSbLeftPadding) {
            int leftValue = (Integer) newValue;
            int sbLeft = ((int) (leftValue / density));
            Settings.System.putIntForUser(getContext().getContentResolver(),
                    Settings.System.LEFT_PADDING, sbLeft, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mSbRightPadding) {
            int rightValue = (Integer) newValue;
            int sbRight = ((int) (rightValue / density));
            Settings.System.putIntForUser(getContext().getContentResolver(),
                    Settings.System.RIGHT_PADDING, sbRight, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mCornerRadius) {
            Settings.Secure.putIntForUser(getContext().getContentResolver(), Settings.Secure.SYSUI_ROUNDED_SIZE,
                    (int) newValue, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mRoundedFwvals) {
            restoreCorners();
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.XTENSIONS;
    }

    private void restoreCorners() {
        Resources res = null;
        float density = Resources.getSystem().getDisplayMetrics().density;
        Context ctx = getContext();

        try {
            res = ctx.getPackageManager().getResourcesForApplication("com.android.systemui");
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        int resourceIdRadius = (int) ctx.getResources().getDimension(com.android.internal.R.dimen.rounded_corner_radius);
        mCornerRadius.setValue((int) (resourceIdRadius / density));
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {

                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();
                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.x_settings_xtras;
                    result.add(sir);
                    return result;
                }

          @Override
               public List<String> getNonIndexableKeys(Context  context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    return keys;
                }
    };
}
