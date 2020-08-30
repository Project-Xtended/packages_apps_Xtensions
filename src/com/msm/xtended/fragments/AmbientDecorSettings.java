package com.msm.xtended.fragments;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;

import com.android.internal.logging.nano.MetricsProto;
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
import com.msm.xtended.preferences.CustomSeekBarPreference;
import com.msm.xtended.preferences.SystemSettingSeekBarPreference;

public class AmbientDecorSettings extends SettingsPreferenceFragment
                         implements OnPreferenceChangeListener {

    private static final String KEY_PULSE_BRIGHTNESS = "ambient_pulse_brightness";
    private static final String KEY_DOZE_BRIGHTNESS = "ambient_doze_brightness";
    private static final String PULSE_AMBIENT_LIGHT_COLOR = "pulse_ambient_light_color";
    private static final String PULSE_AMBIENT_TYPE_COLOR = "pulse_ambient_type_color";
    private static final String PULSE_AMBIENT_LIGHT_DURATION = "pulse_ambient_light_duration";
    private static final String PULSE_AMBIENT_LIGHT_REPEAT_COUNT = "pulse_ambient_light_repeat_count";

    private Preference mChargingLeds;
    private CustomSeekBarPreference mPulseBrightness;
    private CustomSeekBarPreference mDozeBrightness;
    private ListPreference mAmbientColorType;
    private ColorPickerPreference mEdgeLightColorPreference;
    private SystemSettingSeekBarPreference mEdgeLightDurationPreference;
    private SystemSettingSeekBarPreference mEdgeLightRepeatCountPreference;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        ContentResolver resolver = getActivity().getContentResolver();
        addPreferencesFromResource(R.xml.x_ambient_decor);

        int intColor = 0xffffffff;
        String hexColor = String.format("#%08x", (0xffffffff & 0xffffffff));

        PreferenceScreen prefScreen = getPreferenceScreen();
        int defaultDoze = getResources().getInteger(
                com.android.internal.R.integer.config_screenBrightnessDoze);
        int defaultPulse = getResources().getInteger(
                com.android.internal.R.integer.config_screenBrightnessPulse);
        if (defaultPulse == -1) {
            defaultPulse = defaultDoze;
        }

        mPulseBrightness = (CustomSeekBarPreference) findPreference(KEY_PULSE_BRIGHTNESS);
        int value = Settings.System.getInt(getContentResolver(),
                Settings.System.OMNI_PULSE_BRIGHTNESS, defaultPulse);
        mPulseBrightness.setValue(value);
        mPulseBrightness.setOnPreferenceChangeListener(this);

        mDozeBrightness = (CustomSeekBarPreference) findPreference(KEY_DOZE_BRIGHTNESS);
        value = Settings.System.getInt(getContentResolver(),
                Settings.System.OMNI_DOZE_BRIGHTNESS, defaultDoze);
        mDozeBrightness.setValue(value);
        mDozeBrightness.setOnPreferenceChangeListener(this);

        // ambient light color type
        mAmbientColorType = (ListPreference) findPreference(PULSE_AMBIENT_TYPE_COLOR);
        mAmbientColorType.setValue(String.valueOf(Settings.System.getInt(
                getContentResolver(), Settings.System.PULSE_AMBIENT_TYPE_COLOR, 0)));
        mAmbientColorType.setSummary(mAmbientColorType.getEntry());
        mAmbientColorType.setOnPreferenceChangeListener(this);

        mEdgeLightColorPreference = (ColorPickerPreference) findPreference(PULSE_AMBIENT_LIGHT_COLOR);
        int edgeLightColor = Settings.System.getInt(getContentResolver(),
                Settings.System.PULSE_AMBIENT_LIGHT_COLOR, 0xFF3980FF);
        mEdgeLightColorPreference.setNewPreviewColor(edgeLightColor);
        mEdgeLightColorPreference.setAlphaSliderEnabled(false);
        String edgeLightColorHex = String.format("#%08x", (0xFF3980FF & edgeLightColor));
        if (edgeLightColorHex.equals("#ff3980ff")) {
            mEdgeLightColorPreference.setSummary(R.string.default_string);
        } else {
            mEdgeLightColorPreference.setSummary(edgeLightColorHex);
        }
        mEdgeLightColorPreference.setOnPreferenceChangeListener(this);

        mEdgeLightRepeatCountPreference = (SystemSettingSeekBarPreference) findPreference(PULSE_AMBIENT_LIGHT_REPEAT_COUNT);
        mEdgeLightRepeatCountPreference.setOnPreferenceChangeListener(this);
        int rCount = Settings.System.getInt(getContentResolver(),
                Settings.System.PULSE_AMBIENT_LIGHT_REPEAT_COUNT, 0);
        mEdgeLightRepeatCountPreference.setValue(rCount);

        mEdgeLightDurationPreference = (SystemSettingSeekBarPreference) findPreference(PULSE_AMBIENT_LIGHT_DURATION);
        mEdgeLightDurationPreference.setOnPreferenceChangeListener(this);
        int duration = Settings.System.getInt(getContentResolver(),
                Settings.System.PULSE_AMBIENT_LIGHT_DURATION, 2);
        mEdgeLightDurationPreference.setValue(duration);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
         ContentResolver resolver = getActivity().getContentResolver();
         if (preference == mPulseBrightness) {
             int value = (Integer) newValue;
             Settings.System.putInt(getContentResolver(),
                   Settings.System.OMNI_PULSE_BRIGHTNESS, value);
             return true;
         } else if (preference == mDozeBrightness) {
             int value = (Integer) newValue;
             Settings.System.putInt(getContentResolver(),
                  Settings.System.OMNI_DOZE_BRIGHTNESS, value);
             return true;
         } else if (preference == mAmbientColorType) {
             int value = Integer.valueOf((String) newValue);
             int index = mAmbientColorType.findIndexOfValue((String) newValue);
             mAmbientColorType.setSummary(mAmbientColorType.getEntries()[index]);
             Settings.System.putInt(getContentResolver(),
                    Settings.System.PULSE_AMBIENT_TYPE_COLOR, value);
             if (value == 2) {
                 mEdgeLightColorPreference.setEnabled(true);
  	     } else {
	         mEdgeLightColorPreference.setEnabled(false);
	     }
             return true;
         } else if (preference == mEdgeLightColorPreference) {
             String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
             if (hex.equals("#ff3980ff")) {
                 preference.setSummary(R.string.default_string);
             } else {
                 preference.setSummary(hex);
             }
             int intHex = ColorPickerPreference.convertToColorInt(hex);
             Settings.System.putInt(getContentResolver(),
                     Settings.System.PULSE_AMBIENT_LIGHT_COLOR, intHex);
             return true;
         } else if (preference == mEdgeLightRepeatCountPreference) {
             int value = (Integer) newValue;
             Settings.System.putInt(getContentResolver(),
                     Settings.System.PULSE_AMBIENT_LIGHT_REPEAT_COUNT, value);
             return true;
         } else if (preference == mEdgeLightDurationPreference) {
             int value = (Integer) newValue;
             Settings.System.putInt(getContentResolver(),
                    Settings.System.PULSE_AMBIENT_LIGHT_DURATION, value);
             return true;
         }
         return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.XTENSIONS;
    }
}

