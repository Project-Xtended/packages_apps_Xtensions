/*
 * Copyright (C) 2020 The Syberia Project
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
import android.content.Intent;
import android.content.res.Resources;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.ParcelFileDescriptor;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;

import com.android.internal.logging.nano.MetricsProto;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import java.io.FileDescriptor;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.xtended.support.preferences.SystemSettingSwitchPreference;
import com.xtended.preferences.FODIconPicker;

public class FODIconPickerFragment extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String CUSTOM_FOD_ICON_KEY = "custom_fp_icon_enabled";
    private static final String FOD_ICON_PICKER = "fod_icon";
    private static final String CUSTOM_FP_FILE_SELECT = "custom_fp_file_select";
    private static final int REQUEST_PICK_IMAGE = 0;

    private Preference mCustomFPImage;
    private SystemSettingSwitchPreference mCustomFodIcon;
    private FODIconPicker mFodIconPicker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.x_settings_fod_picker);

        getActivity().getActionBar().setTitle(R.string.fod_icon_picker_title);

        mFodIconPicker = (FODIconPicker) findPreference(FOD_ICON_PICKER);

        mCustomFPImage = findPreference(CUSTOM_FP_FILE_SELECT);
        final String customIconURI = Settings.System.getString(getContext().getContentResolver(),
               Settings.System.OMNI_CUSTOM_FP_ICON);
        if (!TextUtils.isEmpty(customIconURI)) {
            setPickerIcon(customIconURI);
        }

        mCustomFodIcon = (SystemSettingSwitchPreference) findPreference(CUSTOM_FOD_ICON_KEY);
        boolean val = Settings.System.getIntForUser(getActivity().getContentResolver(),
                Settings.System.OMNI_CUSTOM_FP_ICON_ENABLED, 0, UserHandle.USER_CURRENT) == 1;
        mCustomFodIcon.setOnPreferenceChangeListener(this);
        if (val) {
            mFodIconPicker.setEnabled(false);
        } else {
            mFodIconPicker.setEnabled(true);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mCustomFPImage) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_PICK_IMAGE);
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mCustomFodIcon) {
            boolean val = (Boolean) newValue;
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                    Settings.System.OMNI_CUSTOM_FP_ICON_ENABLED, val ? 1 : 0,
                    UserHandle.USER_CURRENT);
            if (val) {
                mFodIconPicker.setEnabled(false);
            } else {
                mFodIconPicker.setEnabled(true);
            }
            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent result) {
       if (requestCode == REQUEST_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
           Uri uri = null;
           if (result != null) {
               uri = result.getData();
               setPickerIcon(uri.toString());
               Settings.System.putString(getContentResolver(), Settings.System.OMNI_CUSTOM_FP_ICON,
                   uri.toString());
            }
        } else if (requestCode == REQUEST_PICK_IMAGE && resultCode == Activity.RESULT_CANCELED) {
            mCustomFPImage.setIcon(new ColorDrawable(Color.TRANSPARENT));
            Settings.System.putString(getContentResolver(), Settings.System.OMNI_CUSTOM_FP_ICON, "");
        }
    }

    private void setPickerIcon(String uri) {
        try {
                ParcelFileDescriptor parcelFileDescriptor =
                    getContext().getContentResolver().openFileDescriptor(Uri.parse(uri), "r");
                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                parcelFileDescriptor.close();
                Drawable d = new BitmapDrawable(getResources(), image);
                mCustomFPImage.setIcon(d);
            }
            catch (Exception e) {}
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.XTENSIONS;
    }
}
