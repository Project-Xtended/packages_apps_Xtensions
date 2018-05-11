package com.xtended.xtensions.fragments;

import com.android.internal.logging.nano.MetricsProto;

import android.content.ContentResolver;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.widget.Toast;

import com.android.settings.R;

import com.android.settings.SettingsPreferenceFragment;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

import com.xtended.xtensions.preferences.Utils;

public class NotificationSettings extends SettingsPreferenceFragment
                        implements OnPreferenceChangeListener {

    private static final String INCALL_VIB_OPTIONS = "incall_vib_options";
    private static final String PREF_LESS_NOTIFICATION_SOUNDS = "less_notification_sounds";
    private static final String TOAST_ICON_COLOR = "toast_icon_color";
    private static final String TOAST_TEXT_COLOR = "toast_text_color";

    private ColorPickerPreference mIconColor;
    private ColorPickerPreference mTextColor;
    private ListPreference mAnnoyingNotifications;
    private Preference mChargingLeds;
    private ListPreference mTickerMode;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        ContentResolver resolver = getActivity().getContentResolver();

        addPreferencesFromResource(R.xml.settings_notifications);

        PreferenceScreen prefScreen = getPreferenceScreen();

        PreferenceCategory incallVibCategory = (PreferenceCategory) findPreference(INCALL_VIB_OPTIONS);
        if (!Utils.isVoiceCapable(getActivity())) {
            prefScreen.removePreference(incallVibCategory);
        }

        mChargingLeds = (Preference) findPreference("charging_light");
        if (mChargingLeds != null
                && !getResources().getBoolean(
                        com.android.internal.R.bool.config_intrusiveBatteryLed)) {
            prefScreen.removePreference(mChargingLeds);
        }

        mAnnoyingNotifications = (ListPreference) findPreference(PREF_LESS_NOTIFICATION_SOUNDS);
        int notificationThreshold = Settings.System.getInt(getContentResolver(),
                Settings.System.MUTE_ANNOYING_NOTIFICATIONS_THRESHOLD, 0);
        mAnnoyingNotifications.setValue(Integer.toString(notificationThreshold));
        int valueIndex = mAnnoyingNotifications.findIndexOfValue(String.valueOf(notificationThreshold));
        if (valueIndex > 0) {
            mAnnoyingNotifications.setSummary(mAnnoyingNotifications.getEntries()[valueIndex]);
        }
        mAnnoyingNotifications.setOnPreferenceChangeListener(this);

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
                Settings.System.TOAST_TEXT_COLOR, 0xde000000);
        hexColor = String.format("#%08x", intColor);
        mTextColor.setNewPreviewColor(intColor);
        mTextColor.setSummary(hexColor);
        mTextColor.setOnPreferenceChangeListener(this);

        mTickerMode = (ListPreference) findPreference("ticker_mode");
        mTickerMode.setOnPreferenceChangeListener(this);
        int tickerMode = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.STATUS_BAR_SHOW_TICKER,
                0, UserHandle.USER_CURRENT);
        updatePrefs();
        mTickerMode.setValue(String.valueOf(tickerMode));
        mTickerMode.setSummary(mTickerMode.getEntry());
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();

        if (preference == mAnnoyingNotifications) {
            String notificationThreshold = (String) newValue;
            int notificationThresholdValue = Integer.parseInt(notificationThreshold);
            Settings.System.putInt(resolver,
                    Settings.System.MUTE_ANNOYING_NOTIFICATIONS_THRESHOLD, notificationThresholdValue);
            int notificationThresholdIndex = mAnnoyingNotifications
                    .findIndexOfValue(notificationThreshold);
            mAnnoyingNotifications
                    .setSummary(mAnnoyingNotifications.getEntries()[notificationThresholdIndex]);
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
            } else if (preference.equals(mTickerMode)) {
                int tickerMode = Integer.parseInt(((String) newValue).toString());
                Settings.System.putIntForUser(getContentResolver(),
                      Settings.System.STATUS_BAR_SHOW_TICKER, tickerMode, UserHandle.USER_CURRENT);
                updatePrefs();
  		 	   int index = mTickerMode.findIndexOfValue((String) newValue);
                mTickerMode.setSummary(
                      mTickerMode.getEntries()[index]);
                return true;
            }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.XTENDED;
    }

    private void updatePrefs() {
          ContentResolver resolver = getActivity().getContentResolver();
          boolean enabled = (Settings.Global.getInt(resolver,
                  Settings.Global.HEADS_UP_NOTIFICATIONS_ENABLED, 0) == 1);
        if (enabled) {
            Settings.System.putInt(resolver,
                Settings.System.STATUS_BAR_SHOW_TICKER, 0);
            mTickerMode.setEnabled(false);
        }
    }
}
