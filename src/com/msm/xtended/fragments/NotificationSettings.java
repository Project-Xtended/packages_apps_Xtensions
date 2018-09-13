package com.msm.xtended.fragments;

import com.android.internal.logging.nano.MetricsProto;
import android.support.v7.preference.Preference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.msm.xtended.preferences.XUtils;

import android.content.Context;
import android.content.ContentResolver;
import android.os.UserHandle;
import android.provider.Settings;
import android.os.Bundle;
import android.widget.Toast;
import com.android.settings.R;

import com.android.settings.SettingsPreferenceFragment;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class NotificationSettings extends SettingsPreferenceFragment 
                         implements OnPreferenceChangeListener {

    private static final String INCALL_VIB_OPTIONS = "incall_vib_options";
    private static final String FLASHLIGHT_ON_CALL = "flashlight_on_call";
    private static final String TOAST_ICON_COLOR = "toast_icon_color";
    private static final String TOAST_TEXT_COLOR = "toast_text_color";

    private Preference mChargingLeds;
    private ListPreference mFlashlightOnCall;
    private ColorPickerPreference mIconColor;
    private ColorPickerPreference mTextColor;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        ContentResolver resolver = getActivity().getContentResolver();
        addPreferencesFromResource(R.xml.x_settings_notifications);

        PreferenceScreen prefScreen = getPreferenceScreen();
        PreferenceCategory incallVibCategory = (PreferenceCategory) findPreference(INCALL_VIB_OPTIONS);
        if (!XUtils.isVoiceCapable(getActivity())) {
            prefScreen.removePreference(incallVibCategory);
        }

        mChargingLeds = (Preference) findPreference("charging_light");
        if (mChargingLeds != null
                && !getResources().getBoolean(
                        com.android.internal.R.bool.config_intrusiveBatteryLed)) {
            prefScreen.removePreference(mChargingLeds);
        }

        mFlashlightOnCall = (ListPreference) findPreference(FLASHLIGHT_ON_CALL);
        Preference FlashOnCall = findPreference("flashlight_on_call");
        int flashlightValue = Settings.System.getInt(getContentResolver(),
                Settings.System.FLASHLIGHT_ON_CALL, 1);
        mFlashlightOnCall.setValue(String.valueOf(flashlightValue));
        mFlashlightOnCall.setSummary(mFlashlightOnCall.getEntry());
        mFlashlightOnCall.setOnPreferenceChangeListener(this);

        if (!XUtils.deviceSupportsFlashLight(getActivity())) {
            prefScreen.removePreference(FlashOnCall);
        }

        int intColor = 0xffffffff;
        String hexColor = String.format("#%08x", (0xffffffff & 0xffffffff));

        // Toast icon color
        mIconColor = (ColorPickerPreference) findPreference(TOAST_ICON_COLOR);
        intColor = Settings.System.getInt(resolver,
                Settings.System.TOAST_ICON_COLOR, 0xffffffff);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mIconColor.setNewPreviewColor(intColor);
        mIconColor.setSummary(hexColor);
        mIconColor.setOnPreferenceChangeListener(this);

        // Toast text color
        mTextColor = (ColorPickerPreference) findPreference(TOAST_TEXT_COLOR);
        intColor = Settings.System.getInt(resolver,
                Settings.System.TOAST_TEXT_COLOR, 0xeeff0000);
        hexColor = String.format("#%08x", intColor);
        mTextColor.setNewPreviewColor(intColor);
        mTextColor.setSummary(hexColor);
        mTextColor.setOnPreferenceChangeListener(this);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
         ContentResolver resolver = getActivity().getContentResolver();

            if (preference == mFlashlightOnCall) {
               int flashlightValue = Integer.parseInt(((String) newValue).toString());
               Settings.System.putInt(resolver,
                     Settings.System.FLASHLIGHT_ON_CALL, flashlightValue);
               mFlashlightOnCall.setValue(String.valueOf(flashlightValue));
               mFlashlightOnCall.setSummary(mFlashlightOnCall.getEntry());
               return true;
            }  else if (preference == mIconColor) {
                String hex = ColorPickerPreference.convertToARGB(Integer
                       .valueOf(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(resolver,
                       Settings.System.TOAST_ICON_COLOR, intHex);
                return true;
            } else if (preference == mTextColor) {
                String hex = ColorPickerPreference.convertToARGB(Integer
                      .valueOf(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(resolver,
                      Settings.System.TOAST_TEXT_COLOR, intHex);
                return true;
            }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.XTENSIONS;
    }
}
