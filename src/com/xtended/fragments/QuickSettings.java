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

import static android.os.UserHandle.USER_CURRENT;
import static android.os.UserHandle.USER_SYSTEM;

import com.android.internal.logging.nano.MetricsProto;

import android.app.Activity;
import android.content.Context;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.om.IOverlayManager;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
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

import com.android.internal.util.xtended.ThemesUtils;

import com.xtended.support.preferences.SystemSettingEditTextPreference;
import com.xtended.support.preferences.SystemSettingListPreference;
import com.xtended.support.preferences.SystemSettingSwitchPreference;
import com.xtended.support.preferences.SystemSettingSeekBarPreference;

import java.util.List;
import java.util.ArrayList;

public class QuickSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    public static final String[] QS_STYLES = {
        "com.android.system.qs.outline",
        "com.android.system.qs.twotoneaccent",
        "com.android.system.qs.shaded",
        "com.android.system.qs.cyberpunk",
        "com.android.system.qs.neumorph",
        "com.android.system.qs.reflected",
        "com.android.system.qs.surround",
        "com.android.system.qs.thin",
        "com.android.system.qs.twotoneaccenttrans"
    };

    private static final String X_FOOTER_TEXT_STRING = "x_footer_text_string";
    private static final String KEY_QS_PANEL_STYLE  = "qs_panel_style";
    private static final String PREF_QS_HEADER_NEW_IMAGE = "qs_header_type_background";
    private static final String FILE_HEADER_SELECT = "file_header_select";
    private static final String PREF_QS_HEADER_LIST = "qs_header_image";
    private static final int REQUEST_PICK_HEADER_IMAGE = 0;

    private Handler mHandler;
    private IOverlayManager mOverlayService;
    private ThemesUtils mThemeUtils;
    private SystemSettingEditTextPreference mFooterString;
    private SystemSettingListPreference mQsStyle;
    private Preference mQsHeaderCustomImage;
    private SystemSettingSwitchPreference mQsHeaderImage;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.x_settings_quicksettings);

        PreferenceScreen prefScreen = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mOverlayService = IOverlayManager.Stub
        .asInterface(ServiceManager.getService(Context.OVERLAY_SERVICE));

        mThemeUtils = new ThemesUtils(getActivity());

        mFooterString = (SystemSettingEditTextPreference) findPreference(X_FOOTER_TEXT_STRING);
        mFooterString.setOnPreferenceChangeListener(this);
        String footerString = Settings.System.getString(getContentResolver(),
                X_FOOTER_TEXT_STRING);
        if (footerString != null && footerString != "")
            mFooterString.setText(footerString);
        else {
            mFooterString.setText("Xtended");
            Settings.System.putString(getActivity().getContentResolver(),
                    Settings.System.X_FOOTER_TEXT_STRING, "Xtended");
        }

        mQsStyle = (SystemSettingListPreference) findPreference(KEY_QS_PANEL_STYLE);
        mCustomSettingsObserver.observe();

        mQsHeaderCustomImage = findPreference(FILE_HEADER_SELECT);

        mQsHeaderImage = (SystemSettingSwitchPreference) findPreference(PREF_QS_HEADER_NEW_IMAGE);

    }

    private CustomSettingsObserver mCustomSettingsObserver = new CustomSettingsObserver(mHandler);
    private class CustomSettingsObserver extends ContentObserver {

        CustomSettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            Context mContext = getContext();
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.QS_PANEL_STYLE),
                    false, this, UserHandle.USER_ALL);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (uri.equals(Settings.System.getUriFor(Settings.System.QS_PANEL_STYLE))) {
                updateQsStyle();
            }
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mFooterString) {
            String value = (String) newValue;
            if (value != "" && value != null)
                Settings.System.putString(getActivity().getContentResolver(),
                        Settings.System.X_FOOTER_TEXT_STRING, value);
            else {
                mFooterString.setText("Xtended");
                Settings.System.putString(getActivity().getContentResolver(),
                        Settings.System.X_FOOTER_TEXT_STRING, "Xtended");
            }
            return true;
        } else if (preference == mQsStyle) {
            mCustomSettingsObserver.observe();
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mQsHeaderCustomImage) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_PICK_HEADER_IMAGE);
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (requestCode == REQUEST_PICK_HEADER_IMAGE) {
            if (resultCode != Activity.RESULT_OK) {
                return;
            }
            final Uri headerImageUri = result.getData();
            Settings.System.putString(getContentResolver(), Settings.System.QS_HEADER_CUSTOM_IMAGE, headerImageUri.toString());
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.XTENSIONS;
    }

    private void updateQsStyle() {
        ContentResolver resolver = getActivity().getContentResolver();

        int qsPanelStyle = Settings.System.getIntForUser(getContext().getContentResolver(),
                Settings.System.QS_PANEL_STYLE , 0, UserHandle.USER_CURRENT);

        switch (qsPanelStyle) {
            case 0:
              setDefaultStyle(mOverlayService);
              break;
            case 1:
              setQsStyle("com.android.system.qs.outline");
              break;
            case 2:
              setQsStyle("com.android.system.qs.twotoneaccent");
              break;
            case 3:
              setDefaultStyle(mOverlayService);
              break;
            case 4:
              setQsStyle("com.android.system.qs.shaded");
              break;
            case 5:
              setQsStyle("com.android.system.qs.cyberpunk");
              break;
            case 6:
              setQsStyle("com.android.system.qs.neumorph");
              break;
            case 7:
              setQsStyle("com.android.system.qs.reflected");
              break;
            case 8:
              setQsStyle("com.android.system.qs.surround");
              break;
            case 9:
              setQsStyle("com.android.system.qs.thin");
              break;
            case 10:
              setQsStyle("com.android.system.qs.twotoneaccenttrans");
              break;
            default:
              break;
        }
    }

    public static void setDefaultStyle(IOverlayManager overlayManager) {
        for (int i = 0; i < QS_STYLES.length; i++) {
            String qsStyles = QS_STYLES[i];
            try {
                overlayManager.setEnabled(qsStyles, false, USER_SYSTEM);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void setQsStyle(String overlayName) {
        mThemeUtils.setOverlayEnabled("android.theme.customization.qs_panel", overlayName);
    }
}
