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

import static android.os.UserHandle.USER_SYSTEM;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;
import androidx.preference.PreferenceScreen;
import android.content.Context;
import android.content.ContentResolver;
import android.content.om.IOverlayManager;
import android.content.om.OverlayInfo;
import androidx.preference.Preference.OnPreferenceChangeListener;

import com.android.internal.logging.nano.MetricsProto;
import android.graphics.Color;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.Settings;
import com.android.settings.R;
import android.net.ConnectivityManager;
import com.android.settings.SettingsPreferenceFragment;

import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;
import android.provider.SearchIndexableResource;
import java.util.ArrayList;
import java.util.List;

import com.android.internal.util.xtended.ThemesUtils;
import com.android.internal.util.xtended.XtendedUtils;
import com.android.internal.util.hwkeys.ActionUtils;
import com.xtended.support.preferences.SystemSettingSwitchPreference;
import com.xtended.support.preferences.SystemSettingListPreference;
import com.xtended.support.colorpicker.SystemSettingColorPickerPreference;
import com.xtended.fragments.XThemeRoom;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class NotificationSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String INCALL_VIB_OPTIONS = "incall_vib_options";
    private static final String FLASHLIGHT_ON_CALL = "flashlight_on_call";
    private static final String SMS_BREATH = "sms_breath";
    private static final String MISSED_CALL_BREATH = "missed_call_breath";
    private static final String VOICEMAIL_BREATH = "voicemail_breath";
    private static final String PREF_TICKER_FONT_STYLE = "status_bar_ticker_font_style";
    private static final String CATEGORY_LED = "light_cat";
    private static final String PREF_NOTIF_CAT_STYLE = "notification_header_cat_style";
    private static final String BG_COLOR = "notif_bg_color";
    private static final String ICON_COLOR = "notif_icon_color";
    private static final String BG_MODE = "notif_bg_color_mode";
    private static final String ICON_MODE = "notif_icon_color_mode";

    protected Context mContext;
    private IOverlayManager mOverlayService;
    private Preference mChargingLeds;
    private Preference mNotifLeds;
    private ListPreference mFlashlightOnCall;
    private SwitchPreference mSmsBreath;
    private SwitchPreference mMissedCallBreath;
    private SwitchPreference mVoicemailBreath;
    private ListPreference mTickerFontStyle;
    private ListPreference mNotifCatStyle;
    private SystemSettingListPreference mBgMode;
    private SystemSettingListPreference mIconMode;
    private SystemSettingColorPickerPreference mBgColor;
    private SystemSettingColorPickerPreference mIconColor;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.x_settings_notifications);

        mOverlayService = IOverlayManager.Stub
                .asInterface(ServiceManager.getService(Context.OVERLAY_SERVICE));

        mContext = getActivity().getApplicationContext();
        ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        final PreferenceCategory lightCat = (PreferenceCategory) prefScreen
                .findPreference(CATEGORY_LED);
        final boolean variableIntrusiveLed = getResources().getBoolean(
                    com.android.internal.R.bool.config_intrusiveBatteryLed);

        if (variableIntrusiveLed) {
            mChargingLeds = (Preference) findPreference("charging_light");
            mNotifLeds = (Preference) findPreference("notification_light");
        } else {
            prefScreen.removePreference(lightCat);
        }

        PreferenceCategory incallVibCategory = (PreferenceCategory) findPreference(INCALL_VIB_OPTIONS);

        mFlashlightOnCall = (ListPreference) findPreference(FLASHLIGHT_ON_CALL);
        Preference FlashOnCall = findPreference("flashlight_on_call");
        int flashlightValue = Settings.System.getInt(getContentResolver(),
                Settings.System.FLASHLIGHT_ON_CALL, 0);
        mFlashlightOnCall.setValue(String.valueOf(flashlightValue));
        mFlashlightOnCall.setOnPreferenceChangeListener(this);

        if (!ActionUtils.deviceSupportsFlashLight(getActivity())
                   || !XtendedUtils.isVoiceCapable(getActivity())) {
            prefScreen.removePreference(incallVibCategory);
        }

        // Breathing Notifications
        mSmsBreath = (SwitchPreference) findPreference(SMS_BREATH);
        mMissedCallBreath = (SwitchPreference) findPreference(MISSED_CALL_BREATH);
        mVoicemailBreath = (SwitchPreference) findPreference(VOICEMAIL_BREATH);

        ConnectivityManager cm = (ConnectivityManager)
               getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm.isNetworkSupported(ConnectivityManager.TYPE_MOBILE)) {
            mSmsBreath.setChecked(Settings.Global.getInt(resolver,
                    Settings.Global.KEY_SMS_BREATH, 0) == 1);
            mSmsBreath.setOnPreferenceChangeListener(this);

            mMissedCallBreath.setChecked(Settings.Global.getInt(resolver,
                    Settings.Global.KEY_MISSED_CALL_BREATH, 0) == 1);
            mMissedCallBreath.setOnPreferenceChangeListener(this);

            mVoicemailBreath.setChecked(Settings.System.getInt(resolver,
                    Settings.System.KEY_VOICEMAIL_BREATH, 0) == 1);
            mVoicemailBreath.setOnPreferenceChangeListener(this);
        } else {
            prefScreen.removePreference(mSmsBreath);
            prefScreen.removePreference(mMissedCallBreath);
            prefScreen.removePreference(mVoicemailBreath);
        }

        mTickerFontStyle = (ListPreference) findPreference(PREF_TICKER_FONT_STYLE);
        mTickerFontStyle.setValue(String.valueOf(Settings.System.getInt(
                getContentResolver(), Settings.System.STATUS_BAR_TICKER_FONT_STYLE, 0)));
        mTickerFontStyle.setSummary(mTickerFontStyle.getEntry());
        mTickerFontStyle.setOnPreferenceChangeListener(this);

        mNotifCatStyle = (ListPreference) findPreference(PREF_NOTIF_CAT_STYLE);
        int notifCatValue = getOverlayPosition(ThemesUtils.NOTIF_CAT_STYLE);
        if (notifCatValue != -1) {
            mNotifCatStyle.setValue(String.valueOf(notifCatValue + 2));
        } else {
            mNotifCatStyle.setValue("1");
        }
        mNotifCatStyle.setSummary(mNotifCatStyle.getEntry());
        mNotifCatStyle.setOnPreferenceChangeListener(this);

        mBgColor = (SystemSettingColorPickerPreference) findPreference(BG_COLOR);
        int color = Settings.System.getInt(getContentResolver(),
                Settings.System.NOTIF_CLEAR_ALL_BG_COLOR, 0xFF3980FF) ;
        mBgColor.setNewPreviewColor(color);
        mBgColor.setAlphaSliderEnabled(false);
        String Hex = convertToRGB(color);
        mBgColor.setSummary(Hex);
        mBgColor.setOnPreferenceChangeListener(this);

        mIconColor = (SystemSettingColorPickerPreference) findPreference(ICON_COLOR);
        int iconColor = Settings.System.getInt(getContentResolver(),
                Settings.System.NOTIF_CLEAR_ALL_ICON_COLOR, 0xFF3980FF);
        mIconColor.setNewPreviewColor(iconColor);
        mIconColor.setAlphaSliderEnabled(false);
        String Hex2 = convertToRGB(iconColor);
        mIconColor.setSummary(Hex2);
        mIconColor.setOnPreferenceChangeListener(this);

        mBgMode = (SystemSettingListPreference) findPreference(BG_MODE);
        int mode = Settings.System.getInt(getContentResolver(),
                Settings.System.NOTIF_DISMISALL_COLOR_MODE, 0);
        mBgMode.setOnPreferenceChangeListener(this);
	if (mode == 2) {
	    mBgColor.setEnabled(true);
	} else {
	    mBgColor.setEnabled(false);
        }

        mIconMode = (SystemSettingListPreference) findPreference(ICON_MODE);
        int iconmode = Settings.System.getInt(getContentResolver(),
                Settings.System.NOTIF_DISMISALL_ICON_COLOR_MODE, 0);
        mIconMode.setOnPreferenceChangeListener(this);
	if (iconmode == 2) {
	    mIconColor.setEnabled(true);
	} else {
	    mIconColor.setEnabled(false);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();

        if (preference == mFlashlightOnCall) {
            int flashlightValue = Integer.parseInt(((String) newValue).toString());
            Settings.System.putInt(resolver,
                  Settings.System.FLASHLIGHT_ON_CALL, flashlightValue);
            mFlashlightOnCall.setValue(String.valueOf(flashlightValue));
            return true;
        } else if (preference == mSmsBreath) {
            boolean value = (Boolean) newValue;
            Settings.Global.putInt(getContentResolver(), SMS_BREATH, value ? 1 : 0);
            return true;
        } else if (preference == mMissedCallBreath) {
            boolean value = (Boolean) newValue;
            Settings.Global.putInt(getContentResolver(), MISSED_CALL_BREATH, value ? 1 : 0);
            return true;
        } else if (preference == mVoicemailBreath) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getContentResolver(), VOICEMAIL_BREATH, value ? 1 : 0);
            return true;
        }  else if (preference == mTickerFontStyle) {
            int showTickerFont = Integer.valueOf((String) newValue);
            int index = mTickerFontStyle.findIndexOfValue((String) newValue);
            Settings.System.putInt(getContentResolver(), Settings.System.
                STATUS_BAR_TICKER_FONT_STYLE, showTickerFont);
            mTickerFontStyle.setSummary(mTickerFontStyle.getEntries()[index]);
            return true;
        } else if (preference == mNotifCatStyle) {
            String notifStyle = (String) newValue;
            int notifCatValue = Integer.parseInt(notifStyle);
            mNotifCatStyle.setValue(String.valueOf(notifCatValue));
            String overlayName = getOverlayName(ThemesUtils.NOTIF_CAT_STYLE);
            if (overlayName != null) {
                handleOverlays(overlayName, false, mOverlayService);
            }
            if (notifCatValue > 1) {
                try {
                    mOverlayService.reloadAndroidAssets(UserHandle.USER_CURRENT);
                    mOverlayService.reloadAssets("com.android.settings", UserHandle.USER_CURRENT);
                    mOverlayService.reloadAssets("com.android.systemui", UserHandle.USER_CURRENT);
                } catch (RemoteException ignored) {
                }
                handleOverlays(ThemesUtils.NOTIF_CAT_STYLE[notifCatValue -2],
                        true, mOverlayService);
            }
            mNotifCatStyle.setSummary(mNotifCatStyle.getEntry());
        } else if (preference == mBgMode) {
            int value = Integer.parseInt((String) newValue);
            if (value == 2) {
	        mBgColor.setEnabled(true);
            } else {
	        mBgColor.setEnabled(false);
            }
            XtendedUtils.showSystemUiRestartDialog(getContext());
            return true;
        } else if (preference == mIconMode) {
            int iconValue = Integer.parseInt((String) newValue);
            if (iconValue == 2) {
	        mIconColor.setEnabled(true);
            } else {
	        mIconColor.setEnabled(false);
            }
            XtendedUtils.showSystemUiRestartDialog(getContext());
            return true;
        } else if (preference == mBgColor) {
             String hex = convertToRGB(
                    Integer.valueOf(String.valueOf(newValue)));
             preference.setSummary(hex);
             XtendedUtils.showSystemUiRestartDialog(getContext());
             return true;
        } else if (preference == mIconColor) {
             String hex = convertToRGB(
                    Integer.valueOf(String.valueOf(newValue)));
             preference.setSummary(hex);
             XtendedUtils.showSystemUiRestartDialog(getContext());
             return true;
        }
        return false;
    }

    private int getOverlayPosition(String[] overlays) {
        int position = -1;
        for (int i = 0; i < overlays.length; i++) {
            String overlay = overlays[i];
            if (XtendedUtils.isThemeEnabled(overlay)) {
                position = i;
            }
        }
        return position;
    }

    private String getOverlayName(String[] overlays) {
        String overlayName = null;
        for (int i = 0; i < overlays.length; i++) {
            String overlay = overlays[i];
            if (XtendedUtils.isThemeEnabled(overlay)) {
                overlayName = overlay;
            }
        }
        return overlayName;
    }

    public void handleOverlays(String packagename, Boolean state, IOverlayManager mOverlayManager) {
        try {
            mOverlayService.setEnabled(packagename, state, USER_SYSTEM);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static String convertToRGB(int color) {
        String red = Integer.toHexString(Color.red(color));
        String green = Integer.toHexString(Color.green(color));
        String blue = Integer.toHexString(Color.blue(color));

        if (red.length() == 1) {
            red = "0" + red;
        }

        if (green.length() == 1) {
            green = "0" + green;
        }

        if (blue.length() == 1) {
            blue = "0" + blue;
        }

        return "#" + red + green + blue;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.XTENSIONS;
    }

    /**
     * For Search
     */

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {

                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();
                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.x_settings_notifications;
                    result.add(sir);
                    return result;
                }

           @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    return keys;
                }
    };
}
