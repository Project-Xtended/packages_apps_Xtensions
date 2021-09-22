/**
 * Copyright (C) 2020-21 The Project-Xtended
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 b* the Free Software Foundation, either version 2 of the License, or
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

import static android.os.UserHandle.USER_SYSTEM;
import static android.os.UserHandle.USER_CURRENT;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.content.om.IOverlayManager;
import android.content.om.OverlayInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.ServiceManager;
import android.os.UserHandle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;
import android.provider.Settings;
import com.android.settings.R;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.development.OverlayCategoryPreferenceController;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;

import com.xtended.support.colorpicker.ColorPickerPreference;
import com.xtended.display.QsTileStylePreferenceController;
import com.xtended.support.preferences.CustomSeekBarPreference;
import com.xtended.support.preferences.SystemSettingIntListPreference;
import com.xtended.support.preferences.SystemSettingSwitchPreference;
import com.xtended.support.preferences.SystemSettingListPreference;

import com.android.internal.util.xtended.ThemesUtils;
import com.android.internal.util.xtended.XtendedUtils;

import com.android.settings.display.FontPickerPreferenceController;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;
import android.provider.SearchIndexableResource;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class XThemeRoom extends DashboardFragment implements
        OnPreferenceChangeListener {

    private static final String TAG = "XThemeRoom";

    private static final String PREF_NAVBAR_STYLE = "theme_navbar_style";
    private static final String BRIGHTNESS_SLIDER_STYLE = "brightness_slider_style";
    private static final String SYSTEM_SLIDER_STYLE = "system_slider_style";
    private static final String ACCENT_COLOR = "accent_color";
    private static final String ACCENT_COLOR_PROP = "persist.sys.theme.accentcolor";
    private static final String GRADIENT_COLOR = "gradient_color";
    private static final String GRADIENT_COLOR_PROP = "persist.sys.theme.gradientcolor";
    private static final String KEY_QS_PANEL_ALPHA = "qs_panel_alpha";
    private static final String PREF_PANEL_BG = "panel_bg";
    private static final String ONE_UI = "settings_spacer";
    private static final String A12_SEARCH = "use_new_searchbar";
    private static final String STYLE = "settings_spacer_style";
    private static final String FONT = "settings_spacer_font_style";
    private static final String IMAGE = "settings_spacer_image_style";
    private static final String SEARCHBAR = "settings_spacer_image_searchbar";
    private static final String FILE_SPACER_SELECT = "file_spacer_select";
    private static final String CROP = "settings_spacer_image_crop";
    private static final int REQUEST_PICK_IMAGE = 0;
    private static final int MENU_RESET = Menu.FIRST;

    static final int DEFAULT = 0xff1a73e8;

    private IOverlayManager mOverlayManager;
    private IOverlayManager mOverlayService;
    private ListPreference mBrightnessSliderStyle;
    private ListPreference mNavbarPicker;
    private ListPreference mSystemSliderStyle;
    private ColorPickerPreference mThemeColor;
    private ColorPickerPreference mGradientColor;
    private CustomSeekBarPreference mQsPanelAlpha;
    private ListPreference mPanelBg;
    private SystemSettingSwitchPreference mOneUI;
    private SystemSettingSwitchPreference mA12SearchBar;
    private SystemSettingListPreference mHomeStyle;
    private SystemSettingListPreference mHomeFont;
    private SystemSettingListPreference mImage;
    private SystemSettingListPreference mImageSize;
    private SystemSettingSwitchPreference mSearchbarImage;
    private Preference mSpacerImage;

    private IntentFilter mIntentFilter;
    private static FontPickerPreferenceController mFontPickerPreference;

    private Context mContext;

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("com.android.server.ACTION_FONT_CHANGED")) {
                mFontPickerPreference.stopProgress();
            }
        }
    };

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.x_theme_room;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mOverlayService = IOverlayManager.Stub
                .asInterface(ServiceManager.getService(Context.OVERLAY_SERVICE));

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("com.android.server.ACTION_FONT_CHANGED");

        mNavbarPicker = (ListPreference) findPreference(PREF_NAVBAR_STYLE);
        int navbarStyleValues = getOverlayPosition(ThemesUtils.NAVBAR_STYLES);
        if (navbarStyleValues != -1) {
            mNavbarPicker.setValue(String.valueOf(navbarStyleValues + 2));
        } else {
            mNavbarPicker.setValue("1");
        }
        mNavbarPicker.setSummary(mNavbarPicker.getEntry());
        mNavbarPicker.setOnPreferenceChangeListener(this);

        mOneUI = (SystemSettingSwitchPreference) findPreference(ONE_UI);
        mOneUI.setOnPreferenceChangeListener(this);

        mA12SearchBar = (SystemSettingSwitchPreference) findPreference(A12_SEARCH);
        mA12SearchBar.setOnPreferenceChangeListener(this);

        mImageSize = (SystemSettingListPreference) findPreference(CROP);
        mImageSize.setOnPreferenceChangeListener(this);

        mSpacerImage = findPreference(FILE_SPACER_SELECT);

        mImage = (SystemSettingListPreference) findPreference(IMAGE);
        int imagetype = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.SETTINGS_SPACER_IMAGE_STYLE, 0);
        mImage.setOnPreferenceChangeListener(this);
        if (imagetype == 3) {
            mSpacerImage.setEnabled(true);
            mImageSize.setEnabled(true);
        } else {
            mSpacerImage.setEnabled(false);
            mImageSize.setEnabled(false);
        }

        mSearchbarImage = (SystemSettingSwitchPreference) findPreference(SEARCHBAR);
        mSearchbarImage.setOnPreferenceChangeListener(this);

        mHomeFont = (SystemSettingListPreference) findPreference(FONT);
        mHomeFont.setOnPreferenceChangeListener(this);

        mHomeStyle = (SystemSettingListPreference) findPreference(STYLE);
        int style = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.SETTINGS_SPACER_STYLE, 0);
        mHomeStyle.setOnPreferenceChangeListener(this);
        if (style == 1) {
            mHomeFont.setEnabled(true);
            mImage.setEnabled(false);
            mSpacerImage.setEnabled(false);
            mImageSize.setEnabled(false);
        } else {
            mImage.setEnabled(true);
            mSpacerImage.setEnabled(true);
            mImageSize.setEnabled(true);
            mHomeFont.setEnabled(false);
        }

        mPanelBg = (ListPreference) findPreference(PREF_PANEL_BG);
        int panelValue = getOverlayPosition(ThemesUtils.PANEL_BG_STYLE);
        if (panelValue != -1) {
            mPanelBg.setValue(String.valueOf(panelValue + 2));
        } else {
            mPanelBg.setValue("1");
        }
        mPanelBg.setSummary(mPanelBg.getEntry());
        mPanelBg.setOnPreferenceChangeListener(this);

        setupAccentPref();
        setupGradientPref();
        getQsPanelAlphaPref();
        getBrightnessSliderPref();
        setSystemSliderPref();
        setHasOptionsMenu(true);
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

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getSettingsLifecycle(), this);
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(
            Context context, Lifecycle lifecycle, Fragment fragment) {

        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(mFontPickerPreference = new FontPickerPreferenceController(context, lifecycle));
        controllers.add(new OverlayCategoryPreferenceController(context,
                "android.theme.customization.adaptive_icon_shape"));
        controllers.add(new OverlayCategoryPreferenceController(context,
                "android.theme.customization.icon_pack.android"));
        controllers.add(new QsTileStylePreferenceController(context));
        controllers.add(new OverlayCategoryPreferenceController(context,
                "android.theme.customization.signal_icon"));
        controllers.add(new OverlayCategoryPreferenceController(context,
                "android.theme.customization.wifi_icon"));
        return controllers;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mThemeColor) {
            int color = (Integer) newValue;
            String hexColor = String.format("%08X", (0xFFFFFFFF & color));
            SystemProperties.set(ACCENT_COLOR_PROP, hexColor);
            try {
                 mOverlayService.reloadAndroidAssets(UserHandle.USER_CURRENT);
                 mOverlayService.reloadAssets("com.android.settings", UserHandle.USER_CURRENT);
                 mOverlayService.reloadAssets("com.android.systemui", UserHandle.USER_CURRENT);
             } catch (RemoteException ignored) {
             }
        } else if (preference == mGradientColor) {
            int color = (Integer) newValue;
            String hexColor = String.format("%08X", (0xFFFFFFFF & color));
            SystemProperties.set(GRADIENT_COLOR_PROP, hexColor);
            try {
                 mOverlayService.reloadAndroidAssets(UserHandle.USER_CURRENT);
                 mOverlayService.reloadAssets("com.android.settings", UserHandle.USER_CURRENT);
                 mOverlayService.reloadAssets("com.android.systemui", UserHandle.USER_CURRENT);
             } catch (RemoteException ignored) {
             }
        } else if (preference == mQsPanelAlpha) {
            int bgAlpha = (Integer) newValue;
            int trueValue = (int) (((double) bgAlpha / 100) * 255);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.QS_PANEL_BG_ALPHA, trueValue);
            return true;
        } else if (preference == mPanelBg) {
            String panelbg = (String) newValue;
            int panelBgValue = Integer.parseInt(panelbg);
            mPanelBg.setValue(String.valueOf(panelBgValue));
            String overlayName = getOverlayName(ThemesUtils.PANEL_BG_STYLE);
            if (overlayName != null) {
                handleOverlays(overlayName, false, mOverlayService);
            }
            if (panelBgValue > 1) {
                try {
                    mOverlayService.reloadAndroidAssets(UserHandle.USER_CURRENT);
                    mOverlayService.reloadAssets("com.android.settings", UserHandle.USER_CURRENT);
                    mOverlayService.reloadAssets("com.android.systemui", UserHandle.USER_CURRENT);
                } catch (RemoteException ignored) {
                }
                handleOverlays(ThemesUtils.PANEL_BG_STYLE[panelBgValue -2],
                        true, mOverlayService);
            }
            mPanelBg.setSummary(mPanelBg.getEntry());
        } else if (preference == mNavbarPicker) {
            String navbarStyle = (String) newValue;
            int navbarStyleValue = Integer.parseInt(navbarStyle);
            mNavbarPicker.setValue(String.valueOf(navbarStyleValue));
            String overlayName = getOverlayName(ThemesUtils.NAVBAR_STYLES);
                if (overlayName != null) {
                    handleOverlays(overlayName, false, mOverlayManager);
                }
                if (navbarStyleValue > 1) {
                    handleOverlays(ThemesUtils.NAVBAR_STYLES[navbarStyleValue - 2],
                            true, mOverlayManager);
            }
            mNavbarPicker.setSummary(mNavbarPicker.getEntry());
            return true;
        } else if (preference == mBrightnessSliderStyle) {
            String brightness_style = (String) newValue;
            final Context context = getContext();
            switch (brightness_style) {
                case "1":
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_DANIEL);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMEMINII);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMEROUND);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMEROUNDSTROKE);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMESTROKE);
                   break;
                case "2":
                    handleOverlays(true, context, ThemesUtils.BRIGHTNESS_SLIDER_DANIEL);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMEMINII);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMEROUND);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMEROUNDSTROKE);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMESTROKE);
                   break;
                case "3":
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_DANIEL);
                    handleOverlays(true, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMEMINII);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMEROUND);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMEROUNDSTROKE);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMESTROKE);
                   break;
                case "4":
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_DANIEL);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMEMINII);
                    handleOverlays(true, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMEROUND);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMEROUNDSTROKE);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMESTROKE);
                   break;
                case "5":
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_DANIEL);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMEMINII);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMEROUND);
                    handleOverlays(true, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMEROUNDSTROKE);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMESTROKE);
                   break;
                case "6":
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_DANIEL);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMEMINII);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMEROUND);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMEROUNDSTROKE);
                    handleOverlays(true, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMESTROKE);
                   break;
            }
            return true;
        } else if (preference == mSystemSliderStyle) {
            String slider_style = (String) newValue;
            final Context context = getContext();
            switch (slider_style) {
                case "1":
                    handleOverlays(false, context, ThemesUtils.SYSTEM_SLIDER_DANIEL);
                    handleOverlays(false, context, ThemesUtils.SYSTEM_SLIDER_MEMEMINII);
                    handleOverlays(false, context, ThemesUtils.SYSTEM_SLIDER_MEMEROUND);
                    handleOverlays(false, context, ThemesUtils.SYSTEM_SLIDER_MEMEROUNDSTROKE);
                    handleOverlays(false, context, ThemesUtils.SYSTEM_SLIDER_MEMESTROKE);
                   break;
                case "2":
                    handleOverlays(true, context, ThemesUtils.SYSTEM_SLIDER_DANIEL);
                    handleOverlays(false, context, ThemesUtils.SYSTEM_SLIDER_MEMEMINII);
                    handleOverlays(false, context, ThemesUtils.SYSTEM_SLIDER_MEMEROUND);
                    handleOverlays(false, context, ThemesUtils.SYSTEM_SLIDER_MEMEROUNDSTROKE);
                    handleOverlays(false, context, ThemesUtils.SYSTEM_SLIDER_MEMESTROKE);
                   break;
                case "3":
                    handleOverlays(false, context, ThemesUtils.SYSTEM_SLIDER_DANIEL);
                    handleOverlays(true, context, ThemesUtils.SYSTEM_SLIDER_MEMEMINII);
                    handleOverlays(false, context, ThemesUtils.SYSTEM_SLIDER_MEMEROUND);
                    handleOverlays(false, context, ThemesUtils.SYSTEM_SLIDER_MEMEROUNDSTROKE);
                    handleOverlays(false, context, ThemesUtils.SYSTEM_SLIDER_MEMESTROKE);
                   break;
                case "4":
                    handleOverlays(false, context, ThemesUtils.SYSTEM_SLIDER_DANIEL);
                    handleOverlays(false, context, ThemesUtils.SYSTEM_SLIDER_MEMEMINII);
                    handleOverlays(true, context, ThemesUtils.SYSTEM_SLIDER_MEMEROUND);
                    handleOverlays(false, context, ThemesUtils.SYSTEM_SLIDER_MEMEROUNDSTROKE);
                    handleOverlays(false, context, ThemesUtils.SYSTEM_SLIDER_MEMESTROKE);
                   break;
                case "5":
                    handleOverlays(false, context, ThemesUtils.SYSTEM_SLIDER_DANIEL);
                    handleOverlays(false, context, ThemesUtils.SYSTEM_SLIDER_MEMEMINII);
                    handleOverlays(false, context, ThemesUtils.SYSTEM_SLIDER_MEMEROUND);
                    handleOverlays(true, context, ThemesUtils.SYSTEM_SLIDER_MEMEROUNDSTROKE);
                    handleOverlays(false, context, ThemesUtils.SYSTEM_SLIDER_MEMESTROKE);
                   break;
                case "6":
                    handleOverlays(false, context, ThemesUtils.SYSTEM_SLIDER_DANIEL);
                    handleOverlays(false, context, ThemesUtils.SYSTEM_SLIDER_MEMEMINII);
                    handleOverlays(false, context, ThemesUtils.SYSTEM_SLIDER_MEMEROUND);
                    handleOverlays(false, context, ThemesUtils.SYSTEM_SLIDER_MEMEROUNDSTROKE);
                    handleOverlays(true, context, ThemesUtils.SYSTEM_SLIDER_MEMESTROKE);
                   break;
            }
            return true;
       } else if (preference == mOneUI) {
             AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
             alertDialog.setTitle(getString(R.string.dashboard_ui));
             alertDialog.setMessage(getString(R.string.dashboard_message));
             alertDialog.setButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                         Process.killProcess(Process.myPid());
                       }
                    });
              alertDialog.setButton(Dialog.BUTTON_NEGATIVE ,getString(R.string.cancel), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int which) {
                            return;
                         }
                  });
             alertDialog.show();
            return true;
         } else if (preference == mHomeFont) {
             int val = Integer.parseInt((String) newValue);
             AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
             alertDialog.setTitle(getString(R.string.dashboard_ui));
             alertDialog.setMessage(getString(R.string.dashboard_message));
             alertDialog.setButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                             Process.killProcess(Process.myPid());
                     }
               });
              alertDialog.setButton(Dialog.BUTTON_NEGATIVE ,getString(R.string.cancel), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int which) {
                            return;
                         }
                  });
             alertDialog.show();
             return true;
         }  else if (preference == mImageSize) {
             int value = Integer.parseInt((String) newValue);
             AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
             alertDialog.setTitle(getString(R.string.dashboard_ui));
             alertDialog.setMessage(getString(R.string.dashboard_message));
             alertDialog.setButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                         Process.killProcess(Process.myPid());
                       }
                    });
              alertDialog.setButton(Dialog.BUTTON_NEGATIVE ,getString(R.string.cancel), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int which) {
                            return;
                         }
                  });
             alertDialog.show();
             return true;
         } else if (preference == mImage) {
             int value = Integer.parseInt((String) newValue);
             if (value == 3) {
                 mSpacerImage.setEnabled(true);
                 mImageSize.setEnabled(true);
             } else {
                 mSpacerImage.setEnabled(false);
                 mImageSize.setEnabled(false);
             }
             AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
             alertDialog.setTitle(getString(R.string.dashboard_ui));
             alertDialog.setMessage(getString(R.string.dashboard_message));
             alertDialog.setButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                         Process.killProcess(Process.myPid());
                       }
                    });
              alertDialog.setButton(Dialog.BUTTON_NEGATIVE ,getString(R.string.cancel), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int which) {
                            return;
                         }
                  });
             alertDialog.show();
             return true;
         } else if (preference == mHomeStyle) {
             int val = Integer.parseInt((String) newValue);
             if (val == 1) {
                 mHomeFont.setEnabled(true);
                 mImage.setEnabled(false);
                 mSpacerImage.setEnabled(false);
                 mImageSize.setEnabled(false);
             } else {
                 mImage.setEnabled(true);
                 mSpacerImage.setEnabled(true);
                 mImageSize.setEnabled(true);
                 mHomeFont.setEnabled(false);
             }
             AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
             alertDialog.setTitle(getString(R.string.dashboard_ui));
             alertDialog.setMessage(getString(R.string.dashboard_message));
             alertDialog.setButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                             Process.killProcess(Process.myPid());
                     }
               });
              alertDialog.setButton(Dialog.BUTTON_NEGATIVE ,getString(R.string.cancel), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int which) {
                            return;
                         }
                  });
             alertDialog.show();
             return true;
         } else if (preference == mSearchbarImage) {
             AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
             alertDialog.setTitle(getString(R.string.dashboard_ui));
             alertDialog.setMessage(getString(R.string.dashboard_message));
             alertDialog.setButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                         Process.killProcess(Process.myPid());
                       }
                    });
              alertDialog.setButton(Dialog.BUTTON_NEGATIVE ,getString(R.string.cancel), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int which) {
                            return;
                         }
                  });
             alertDialog.show();
             return true;
         } else if (preference == mA12SearchBar) {
             AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
             alertDialog.setTitle(getString(R.string.dashboard_ui));
             alertDialog.setMessage(getString(R.string.dashboard_message));
             alertDialog.setButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                         Process.killProcess(Process.myPid());
                       }
                    });
              alertDialog.setButton(Dialog.BUTTON_NEGATIVE ,getString(R.string.cancel), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int which) {
                            return;
                         }
                  });
             alertDialog.show();
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mSpacerImage) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_PICK_IMAGE);
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    private void setupAccentPref() {
        mThemeColor = (ColorPickerPreference) findPreference(ACCENT_COLOR);
        String colorVal = SystemProperties.get(ACCENT_COLOR_PROP, "-1");
        int color = "-1".equals(colorVal)
                ? DEFAULT
                : Color.parseColor("#" + colorVal);
        mThemeColor.setNewPreviewColor(color);
        mThemeColor.setOnPreferenceChangeListener(this);
    }

    private void setupGradientPref() {
        mGradientColor = (ColorPickerPreference) findPreference(GRADIENT_COLOR);
        String colorVal = SystemProperties.get(GRADIENT_COLOR_PROP, "-1");
        int color = "-1".equals(colorVal)
                ? DEFAULT
                : Color.parseColor("#" + colorVal);
        mGradientColor.setNewPreviewColor(color);
        mGradientColor.setOnPreferenceChangeListener(this);
    }

    private void getQsPanelAlphaPref() {
        mQsPanelAlpha = (CustomSeekBarPreference) findPreference(KEY_QS_PANEL_ALPHA);
        int qsPanelAlpha = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.QS_PANEL_BG_ALPHA, 255);
        mQsPanelAlpha.setValue((int)(((double) qsPanelAlpha / 255) * 100));
        mQsPanelAlpha.setOnPreferenceChangeListener(this);
    }

    private void getBrightnessSliderPref() {
        mBrightnessSliderStyle = (ListPreference) findPreference(BRIGHTNESS_SLIDER_STYLE);
        mBrightnessSliderStyle.setOnPreferenceChangeListener(this);
        if (XtendedUtils.isThemeEnabled("com.android.systemui.brightness.slider.memestroke")) {
            mBrightnessSliderStyle.setValue("6");
        } else if (XtendedUtils.isThemeEnabled("com.android.systemui.brightness.slider.memeroundstroke")) {
            mBrightnessSliderStyle.setValue("5");
        } else if (XtendedUtils.isThemeEnabled("com.android.systemui.brightness.slider.memeround")) {
            mBrightnessSliderStyle.setValue("4");
        } else if (XtendedUtils.isThemeEnabled("com.android.systemui.brightness.slider.mememini")) {
            mBrightnessSliderStyle.setValue("3");
        } else if (XtendedUtils.isThemeEnabled("com.android.systemui.brightness.slider.daniel")) {
            mBrightnessSliderStyle.setValue("2");
        } else {
            mBrightnessSliderStyle.setValue("1");
        }
    }

    private void setSystemSliderPref() {
        mSystemSliderStyle = (ListPreference) findPreference(SYSTEM_SLIDER_STYLE);
        mSystemSliderStyle.setOnPreferenceChangeListener(this);
        if (XtendedUtils.isThemeEnabled("com.android.system.slider.memestroke")) {
            mSystemSliderStyle.setValue("6");
        } else if (XtendedUtils.isThemeEnabled("com.android.system.slider.memeroundstroke")) {
            mSystemSliderStyle.setValue("5");
        } else if (XtendedUtils.isThemeEnabled("com.android.system.slider.memeround")) {
            mSystemSliderStyle.setValue("4");
        } else if (XtendedUtils.isThemeEnabled("com.android.system.slider.mememini")) {
            mSystemSliderStyle.setValue("3");
        } else if (XtendedUtils.isThemeEnabled("com.android.system.slider.daniel")) {
            mSystemSliderStyle.setValue("2");
        } else {
            mSystemSliderStyle.setValue("1");
        }
    }

    private void handleOverlays(Boolean state, Context context, String[] overlays) {
        if (context == null) {
            return;
        }
        for (int i = 0; i < overlays.length; i++) {
            String xui = overlays[i];
            try {
                mOverlayService.setEnabled(xui, state, USER_SYSTEM);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (requestCode == REQUEST_PICK_IMAGE) {
            if (resultCode != Activity.RESULT_OK) {
                return;
            }
            final Uri spacerImageUri = result.getData();
            Settings.System.putString(getContentResolver(), Settings.System.SETTINGS_SPACER_CUSTOM, spacerImageUri.toString());
            AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
            alertDialog.setTitle(getString(R.string.dashboard_ui));
            alertDialog.setMessage(getString(R.string.dashboard_message));
            alertDialog.setButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                         Process.killProcess(Process.myPid());
                       }
              });
              alertDialog.setButton(Dialog.BUTTON_NEGATIVE ,getString(R.string.cancel), new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                           return;
                       }
             });
             alertDialog.show();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET, 0, R.string.reset)
                .setIcon(R.drawable.ic_reset)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESET:
                resetToDefault();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void resetToDefault() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(R.string.theme_option_reset_title);
        alertDialog.setMessage(R.string.theme_option_reset_message);
        alertDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                resetValues();
            }
        });
        alertDialog.setNegativeButton(R.string.cancel, null);
        alertDialog.create().show();
    }

    private void resetValues() {
        final Context context = getContext();
        mGradientColor = (ColorPickerPreference) findPreference(GRADIENT_COLOR);
        SystemProperties.set(GRADIENT_COLOR_PROP, "-1");
        mGradientColor.setNewPreviewColor(DEFAULT);
        mThemeColor = (ColorPickerPreference) findPreference(ACCENT_COLOR);
        SystemProperties.set(ACCENT_COLOR_PROP, "-1");
        mThemeColor.setNewPreviewColor(DEFAULT);
    }

    @Override
    public void onResume() {
        super.onResume();
        final Context context = getActivity();
        context.registerReceiver(mIntentReceiver, mIntentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        final Context context = getActivity();
        context.unregisterReceiver(mIntentReceiver);
        mFontPickerPreference.stopProgress();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.XTENSIONS;
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {

                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();
                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.x_theme_room;
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
