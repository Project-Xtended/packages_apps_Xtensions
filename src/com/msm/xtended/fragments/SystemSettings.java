package com.msm.xtended.fragments;

import com.android.internal.logging.nano.MetricsProto;

import android.content.Context;
import android.content.SharedPreferences;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SELinux;
import android.os.SystemProperties;
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
import android.util.Log;

import com.msm.xtended.utils.SuTask;
import com.msm.xtended.utils.SuShell;

import java.util.Arrays;
import java.util.HashSet;

import com.android.settings.SettingsPreferenceFragment;

public class SystemSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String TAG = "SystemSettings";

    private static final String SELINUX_CATEGORY = "selinux";
    private static final String PREF_SELINUX_MODE = "selinux_mode";
    private static final String PREF_SELINUX_PERSISTENCE = "selinux_persistence";

    private SwitchPreference mSelinuxMode;
    private SwitchPreference mSelinuxPersistence;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.x_settings_system);

        boolean enableSmartPixels = getContext().getResources().
                getBoolean(com.android.internal.R.bool.config_enableSmartPixels);
        Preference SmartPixels = findPreference("smart_pixels");
        if (!enableSmartPixels){
            getPreferenceScreen().removePreference(SmartPixels);
        }
      // SELinux
      Preference selinuxCategory = findPreference(SELINUX_CATEGORY);
      mSelinuxMode = (SwitchPreference) findPreference(PREF_SELINUX_MODE);
      mSelinuxMode.setChecked(SELinux.isSELinuxEnforced());
      mSelinuxMode.setOnPreferenceChangeListener(this);

      mSelinuxPersistence =
          (SwitchPreference) findPreference(PREF_SELINUX_PERSISTENCE);
      mSelinuxPersistence.setOnPreferenceChangeListener(this);
      mSelinuxPersistence.setChecked(getContext()
          .getSharedPreferences("selinux_pref", Context.MODE_PRIVATE)
          .contains(PREF_SELINUX_MODE));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
      if (preference == mSelinuxMode) {
        boolean enabled = (Boolean) objValue;
        new SwitchSelinuxTask(getActivity()).execute(enabled);
        setSelinuxEnabled(enabled, mSelinuxPersistence.isChecked());
        return true;
      } else if (preference == mSelinuxPersistence) {
        setSelinuxEnabled(mSelinuxMode.isChecked(), (Boolean) objValue);
        return true;
      }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.XTENSIONS;
    }

        private void setSelinuxEnabled(boolean status, boolean persistent) {
      SharedPreferences.Editor editor = getContext()
          .getSharedPreferences("selinux_pref", Context.MODE_PRIVATE).edit();
      if (persistent) {
        editor.putBoolean(PREF_SELINUX_MODE, status);
      } else {
        editor.remove(PREF_SELINUX_MODE);
      }
      editor.apply();
      mSelinuxMode.setChecked(status);
    }

    private class SwitchSelinuxTask extends SuTask<Boolean> {
      public SwitchSelinuxTask(Context context) {
        super(context);
      }
      @Override
      protected void sudoInBackground(Boolean... params) throws SuShell.SuDeniedException {
        if (params.length != 1) {
          Log.e(TAG, "SwitchSelinuxTask: invalid params count");
          return;
        }
        if (params[0]) {
          SuShell.runWithSuCheck("setenforce 1");
        } else {
          SuShell.runWithSuCheck("setenforce 0");
        }
      }

      @Override
      protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        if (!result) {
          // Did not work, so restore actual value
          setSelinuxEnabled(SELinux.isSELinuxEnforced(), mSelinuxPersistence.isChecked());
        }
      }
    }
}
