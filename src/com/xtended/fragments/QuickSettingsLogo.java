/*
 * Copyright (C) 2021 Project-Xtended
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

import android.app.Activity;
import android.content.Context;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.UserHandle;
import android.provider.Settings;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;

import com.xtended.support.colorpicker.ColorPickerPreference;
import com.xtended.support.preferences.CustomSeekBarPreference;
import com.xtended.support.preferences.SystemSettingSwitchPreference;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.Utils;

import java.io.FileDescriptor;

public class QuickSettingsLogo extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String CUSTOM_QS_LOGO_ENABLED = "custom_qs_logo_enabled";
    private static final String CUSTOM_QS_LOGO_IMAGE = "custom_qs_logo_image";
    private static final String CUSTOM_QS_LOGO_SIZE = "custom_qs_logo_size";
    private static final int REQUEST_PICK_QS_IMAGE = 0;

    private ListPreference mShowQsLogo;
    private ListPreference mQsLogoStyle;
    private ColorPickerPreference mQsLogoColor;
    private Preference mCustomQsLogoImage;
    private SystemSettingSwitchPreference mCustomQsLogoEnabled;
    private CustomSeekBarPreference mQsLogoSize;
    static final int DEFAULT_LOGO_COLOR = 0xffff8800;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.x_quicksettings_logo);

        Resources res = null;
        Context ctx = getContext();

        try {
            res = ctx.getPackageManager().getResourcesForApplication("com.android.systemui");
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        String hexColor;
        int intColor;

        mShowQsLogo = (ListPreference) findPreference("qs_panel_logo");
        mShowQsLogo.setOnPreferenceChangeListener(this);
        int showQsLogo = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.QS_PANEL_LOGO,
                0, UserHandle.USER_CURRENT);
        mShowQsLogo.setValue(String.valueOf(showQsLogo));
        mShowQsLogo.setSummary(mShowQsLogo.getEntry());

        mQsLogoStyle = (ListPreference) findPreference("qs_panel_logo_style");
        mQsLogoStyle.setOnPreferenceChangeListener(this);
        int logoQsStyle = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.QS_PANEL_LOGO_STYLE,
                0, UserHandle.USER_CURRENT);
        mQsLogoStyle.setValue(String.valueOf(logoQsStyle));
        mQsLogoStyle.setSummary(mQsLogoStyle.getEntry());

        mQsLogoColor = (ColorPickerPreference) findPreference("qs_panel_logo_color");
        mQsLogoColor.setOnPreferenceChangeListener(this);
        intColor = Settings.System.getInt(getContentResolver(),
                Settings.System.QS_PANEL_LOGO_COLOR, DEFAULT_LOGO_COLOR);
        hexColor = String.format("#%08x", (DEFAULT_LOGO_COLOR & intColor));
        mQsLogoColor.setSummary(hexColor);
        mQsLogoColor.setNewPreviewColor(intColor);

        mCustomQsLogoImage = findPreference(CUSTOM_QS_LOGO_IMAGE);

        mCustomQsLogoEnabled = (SystemSettingSwitchPreference) findPreference(CUSTOM_QS_LOGO_ENABLED);
        boolean valQsLogo = Settings.System.getIntForUser(getActivity().getContentResolver(),
                Settings.System.CUSTOM_QS_LOGO_ENABLED, 0, UserHandle.USER_CURRENT) == 1;
        mCustomQsLogoEnabled.setOnPreferenceChangeListener(this);
        if (valQsLogo) {
            mQsLogoStyle.setEnabled(false);
            mQsLogoColor.setEnabled(false);
        } else {
            mQsLogoStyle.setEnabled(true);
            mQsLogoColor.setEnabled(true);
        }

        mQsLogoSize = (CustomSeekBarPreference) findPreference(CUSTOM_QS_LOGO_SIZE);
        int logoSize = Settings.System.getIntForUser(ctx.getContentResolver(),
                Settings.System.CUSTOM_QS_LOGO_SIZE, res.getIdentifier("com.android.systemui:dimen/qs_logo_size", null, null), UserHandle.USER_CURRENT);
        mQsLogoSize.setValue(logoSize);
        mQsLogoSize.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mCustomQsLogoImage) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_PICK_QS_IMAGE);
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.XTENSIONS;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference.equals(mShowQsLogo)) {
            int showQsLogo = Integer.parseInt(((String) newValue).toString());
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.QS_PANEL_LOGO, showQsLogo, UserHandle.USER_CURRENT);
            int index = mShowQsLogo.findIndexOfValue((String) newValue);
            mShowQsLogo.setSummary(mShowQsLogo.getEntries()[index]);
            return true;
        } else if (preference.equals(mQsLogoStyle)) {
            int logoQsStyle = Integer.parseInt(((String) newValue).toString());
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.QS_PANEL_LOGO_STYLE, logoQsStyle, UserHandle.USER_CURRENT);
            int index = mQsLogoStyle.findIndexOfValue((String) newValue);
            mQsLogoStyle.setSummary(
                    mQsLogoStyle.getEntries()[index]);
            return true;
        } else if (preference.equals(mQsLogoColor)) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(resolver,
                    Settings.System.QS_PANEL_LOGO_COLOR, intHex);
            return true;
        } else if (preference.equals(mCustomQsLogoEnabled)) {
            boolean valQsLogo = (Boolean) newValue;
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                    Settings.System.CUSTOM_SB_LOGO_ENABLED, valQsLogo ? 1 : 0,
                    UserHandle.USER_CURRENT);
            if (valQsLogo) {
                mQsLogoStyle.setEnabled(false);
                mQsLogoColor.setEnabled(false);
            } else {
                mQsLogoStyle.setEnabled(true);
                mQsLogoColor.setEnabled(true);
            }
            return true;
        } else if (preference.equals(mQsLogoSize)) {
            int logoSize = (Integer) newValue;
            Settings.System.putIntForUser(getContext().getContentResolver(),
                    Settings.System.CUSTOM_QS_LOGO_SIZE, logoSize, UserHandle.USER_CURRENT);
            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (requestCode == REQUEST_PICK_QS_IMAGE) {
            if (resultCode != Activity.RESULT_OK) {
                return;
            }
            final Uri uriQs = result.getData();
            setQsPickerIcon(uriQs.toString());
            Settings.System.putString(getContentResolver(), Settings.System.CUSTOM_QS_LOGO_IMAGE, uriQs.toString());
        }
    }

    private void setQsPickerIcon(String uri) {
        try {
                ParcelFileDescriptor parcelFileDescriptor =
                    getContext().getContentResolver().openFileDescriptor(Uri.parse(uri), "r");
                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                Bitmap imageQsLogo = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                parcelFileDescriptor.close();
                Drawable d = new BitmapDrawable(getResources(), imageQsLogo);
                mCustomQsLogoImage.setIcon(d);
            }
            catch (Exception e) {}
    }
}

