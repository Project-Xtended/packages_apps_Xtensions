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

import java.util.List;
import java.util.ArrayList;

import com.msm.xtended.preferences.CustomSeekBarPreference;

public class QuickSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String QUICK_PULLDOWN = "quick_pulldown";
    private static final String PREF_COLUMNS_PORTRAIT = "qs_columns_portrait";
    private static final String PREF_COLUMNS_LANDSCAPE = "qs_columns_landscape";
    private static final String PREF_COLUMNS_QUICKBAR = "qs_columns_quickbar";
    private static final String PREF_ROWS_PORTRAIT = "qs_rows_portrait";
    private static final String PREF_ROWS_LANDSCAPE = "qs_rows_landscape";

    private CustomSeekBarPreference mQsColumnsPortrait;
    private CustomSeekBarPreference mQsColumnsLandscape;
    private CustomSeekBarPreference mQsColumnsQuickbar;
    private CustomSeekBarPreference mQsRowsPortrait;
    private CustomSeekBarPreference mQsRowsLandscape;
    private ListPreference mQuickPulldown;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.x_settings_quicksettings);

        PreferenceScreen prefScreen = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mQuickPulldown = (ListPreference) findPreference(QUICK_PULLDOWN);
        mQuickPulldown.setOnPreferenceChangeListener(this);
        int quickPulldownValue = Settings.System.getIntForUser(resolver,
                Settings.System.STATUS_BAR_QUICK_QS_PULLDOWN, 0, UserHandle.USER_CURRENT);
        mQuickPulldown.setValue(String.valueOf(quickPulldownValue));
        updatePulldownSummary(quickPulldownValue);

        mQsColumnsPortrait = (CustomSeekBarPreference) findPreference(PREF_COLUMNS_PORTRAIT);
        int columnsPortrait = Settings.System.getIntForUser(resolver,
                Settings.System.QS_LAYOUT_COLUMNS, 4, UserHandle.USER_CURRENT);
        mQsColumnsPortrait.setValue(columnsPortrait);
        mQsColumnsPortrait.setOnPreferenceChangeListener(this);

        mQsColumnsLandscape = (CustomSeekBarPreference) findPreference(PREF_COLUMNS_LANDSCAPE);
        int columnsLandscape = Settings.System.getIntForUser(resolver,
                Settings.System.QS_LAYOUT_COLUMNS_LANDSCAPE, 4, UserHandle.USER_CURRENT);
        mQsColumnsLandscape.setValue(columnsLandscape);
        mQsColumnsLandscape.setOnPreferenceChangeListener(this);

        mQsColumnsQuickbar = (CustomSeekBarPreference) findPreference(PREF_COLUMNS_QUICKBAR);
        int columnsQuickbar = Settings.System.getInt(resolver,
                Settings.System.QS_QUICKBAR_COLUMNS, 6);
        mQsColumnsQuickbar.setValue(columnsQuickbar);
        mQsColumnsQuickbar.setOnPreferenceChangeListener(this);

        mQsRowsPortrait = (CustomSeekBarPreference) findPreference(PREF_ROWS_PORTRAIT);
        int rowsPortrait = Settings.System.getIntForUser(resolver,
                Settings.System.QS_LAYOUT_ROWS, 3, UserHandle.USER_CURRENT);
        mQsRowsPortrait.setValue(rowsPortrait);
        mQsRowsPortrait.setOnPreferenceChangeListener(this);

        mQsRowsLandscape = (CustomSeekBarPreference) findPreference(PREF_ROWS_LANDSCAPE);
        int rowsLandscape = Settings.System.getIntForUser(resolver,
                Settings.System.QS_LAYOUT_ROWS_LANDSCAPE, 2, UserHandle.USER_CURRENT);
        mQsRowsLandscape.setValue(rowsLandscape);
        mQsRowsLandscape.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
    	ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mQuickPulldown) {
            int quickPulldownValue = Integer.valueOf((String) newValue);
            Settings.System.putIntForUser(resolver, Settings.System.STATUS_BAR_QUICK_QS_PULLDOWN,
                    quickPulldownValue, UserHandle.USER_CURRENT);
            updatePulldownSummary(quickPulldownValue);
            return true;
        } else if (preference == mQsColumnsQuickbar) {
            int value = (Integer) newValue;
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.QS_QUICKBAR_COLUMNS, value, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mQsColumnsPortrait) {
            int value = (Integer) newValue;
            Settings.System.putIntForUser(resolver,
                    Settings.System.QS_LAYOUT_COLUMNS, value, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mQsColumnsLandscape) {
            int value = (Integer) newValue;
            Settings.System.putIntForUser(resolver,
                    Settings.System.QS_LAYOUT_COLUMNS_LANDSCAPE, value, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mQsRowsPortrait) {
            int value = (Integer) newValue;
            Settings.System.putIntForUser(resolver,
                    Settings.System.QS_LAYOUT_ROWS, value, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mQsRowsLandscape) {
            int value = (Integer) newValue;
            Settings.System.putIntForUser(resolver,
                    Settings.System.QS_LAYOUT_ROWS_LANDSCAPE, value, UserHandle.USER_CURRENT);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.XTENSIONS;
    }

    private void updatePulldownSummary(int value) {
        Resources res = getResources();
         if (value == 0) {
            // quick pulldown deactivated
            mQuickPulldown.setSummary(res.getString(R.string.quick_pulldown_off));
        } else if (value == 3) {
            // quick pulldown always
            mQuickPulldown.setSummary(res.getString(R.string.quick_pulldown_summary_always));
        } else {
            String direction = res.getString(value == 2
                    ? R.string.quick_pulldown_left
                    : R.string.quick_pulldown_right);
            mQuickPulldown.setSummary(res.getString(R.string.quick_pulldown_summary, direction));
        }
    }
}
