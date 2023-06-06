/*
 * Copyright (C) 2017-22 The Project-Xtended
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
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.fingerprint.FingerprintManager;
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
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.text.TextUtils;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import com.android.internal.util.xtended.udfps.UdfpsUtils;
import com.android.internal.util.xtended.XtendedUtils;

import com.xtended.support.preferences.SystemSettingSwitchPreference;
import com.xtended.support.preferences.SecureSettingSwitchPreference;
import com.xtended.fragments.UdfpsIconPicker;

import java.io.FileDescriptor;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UdfpsSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String CUSTOM_FOD_ICON_KEY = "custom_fp_icon_enabled";
    private static final String CUSTOM_FP_FILE_SELECT = "custom_fp_file_select";
    private static final String SCREEN_OFF_UDFPS = "screen_off_udfps_enabled";

    private static final int REQUEST_PICK_IMAGE = 0;

    private Preference mCustomFPImage;
    private SystemSettingSwitchPreference mCustomFodIcon;
    private Preference mUdfpsIconPicker;
    private SecureSettingSwitchPreference mScreenOffUdfps;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.x_settings_udfps);

        final PreferenceScreen prefScreen = getPreferenceScreen();
        Resources resources = getContext().getResources();

        /**
        boolean udfpsResPkgInstalled = XtendedUtils.isPackageInstalled(getContext(),
                "com.xtended.udfps.resources");
	PreferenceCategory udfps_custom = (PreferenceCategory) prefScreen.findPreference("udfps_customization");*/

        mUdfpsIconPicker = (Preference) prefScreen.findPreference("udfps_icon_picker");

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
            mUdfpsIconPicker.setEnabled(false);
        } else {
            mUdfpsIconPicker.setEnabled(true);
        }

        mScreenOffUdfps = (SecureSettingSwitchPreference) prefScreen.findPreference(SCREEN_OFF_UDFPS);
        boolean mScreenOffUdfpsAvailable = resources.getBoolean(
                com.android.internal.R.bool.config_supportScreenOffUdfps);
        if (!mScreenOffUdfpsAvailable)
            prefScreen.removePreference(mScreenOffUdfps);
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
                mUdfpsIconPicker.setEnabled(false);
            } else {
                mUdfpsIconPicker.setEnabled(true);
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

