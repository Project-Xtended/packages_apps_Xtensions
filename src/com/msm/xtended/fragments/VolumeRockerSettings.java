package com.msm.xtended.fragments;

import com.android.internal.logging.nano.MetricsProto;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v14.preference.SwitchPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.WindowManagerGlobal;
import android.view.IWindowManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.Locale;
import android.text.TextUtils;
import android.view.View;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class VolumeRockerSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String VOLUME_KEY_CURSOR_CONTROL = "volume_key_cursor_control";
    private static final int DLG_SAFE_HEADSET_VOLUME = 0;
    private static final String KEY_SAFE_HEADSET_VOLUME = "safe_headset_volume";

    private ListPreference mVolumeKeyCursorControl;
    private SwitchPreference mSafeHeadsetVolume;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        ContentResolver resolver = getActivity().getContentResolver();
        PreferenceScreen prefScreen = getPreferenceScreen();

        addPreferencesFromResource(R.xml.x_settings_volume);

        // volume key cursor control
        mVolumeKeyCursorControl = (ListPreference) findPreference(VOLUME_KEY_CURSOR_CONTROL);
        if (mVolumeKeyCursorControl != null) {
            mVolumeKeyCursorControl.setOnPreferenceChangeListener(this);
            int volumeRockerCursorControl = Settings.System.getInt(getContentResolver(),
                    Settings.System.VOLUME_KEY_CURSOR_CONTROL, 0);
            mVolumeKeyCursorControl.setValue(Integer.toString(volumeRockerCursorControl));
           mVolumeKeyCursorControl.setSummary(mVolumeKeyCursorControl.getEntry());
	}

        mSafeHeadsetVolume = (SwitchPreference) findPreference(KEY_SAFE_HEADSET_VOLUME);
        mSafeHeadsetVolume.setChecked(Settings.System.getInt(resolver,
				Settings.System.SAFE_HEADSET_VOLUME, 1) == 1);
        mSafeHeadsetVolume.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mVolumeKeyCursorControl) {
            String volumeKeyCursorControl = (String) objValue;
            int volumeKeyCursorControlValue = Integer.parseInt(volumeKeyCursorControl);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.VOLUME_KEY_CURSOR_CONTROL, volumeKeyCursorControlValue);
            int volumeKeyCursorControlIndex = mVolumeKeyCursorControl
                    .findIndexOfValue(volumeKeyCursorControl);
            mVolumeKeyCursorControl
                    .setSummary(mVolumeKeyCursorControl.getEntries()[volumeKeyCursorControlIndex]);
            return true;
        } else if (preference == mSafeHeadsetVolume) {
           boolean value = (Boolean) objValue;
           Settings.System.putInt(resolver,
                    Settings.System.SAFE_HEADSET_VOLUME, value ? 1 : 0);
                    showDialogInner(DLG_SAFE_HEADSET_VOLUME);
           return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.XTENSIONS;
    }

    private void showDialogInner(int id) {
        DialogFragment newFragment = MyAlertDialogFragment.newInstance(id);
        newFragment.setTargetFragment(this, 0);
        newFragment.show(getFragmentManager(), "dialog " + id);
    }

    public static class MyAlertDialogFragment extends DialogFragment {
        public static MyAlertDialogFragment newInstance(int id) {
            MyAlertDialogFragment frag = new MyAlertDialogFragment();
            Bundle args = new Bundle();
            args.putInt("id", id);
            frag.setArguments(args);
            return frag;
        }

        VolumeRockerSettings getOwner() {
            return (VolumeRockerSettings) getTargetFragment();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int id = getArguments().getInt("id");
                switch (id) {
                   case DLG_SAFE_HEADSET_VOLUME:
                       return new AlertDialog.Builder(getActivity())
                           .setTitle(R.string.attention)
                           .setMessage(R.string.safe_headset_volume_warning_dialog_text)
                           .setPositiveButton(R.string.ok,
                               new DialogInterface.OnClickListener() {
                                   public void onClick(DialogInterface dialog, int which) {
                                       Settings.System.putInt(getOwner().getActivity().getContentResolver(),
                                       Settings.System.SAFE_HEADSET_VOLUME, 0);
                                   }
                               })
                           .create();
                }
                   throw new IllegalArgumentException("unknown id " + id);
        }
    }
}
