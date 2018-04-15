/**
 * Copyright (C) 2017 Project-Xtended
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.xtended.xtensions.preferences;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog; 
import android.app.IActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.UserManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.DisplayInfo;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.settings.R;

public final class XUtils {
    private static final String TAG = "XUtils";

    /**
     * Determine whether a package is a "system package", in which case certain things (like
     * disabling notifications or disabling the package altogether) should be disallowed.
     */
    public static boolean isSystemPackage(PackageManager pm, PackageInfo pkg) {
        if (sSystemSignature == null) {
            sSystemSignature = new Signature[]{ getSystemSignature(pm) };
        }
        return sSystemSignature[0] != null && sSystemSignature[0].equals(getFirstSignature(pkg));
    }

    private static Signature[] sSystemSignature;

    private static Signature getFirstSignature(PackageInfo pkg) {
        if (pkg != null && pkg.signatures != null && pkg.signatures.length > 0) {
            return pkg.signatures[0];
        }
        return null;
    }

    private static Signature getSystemSignature(PackageManager pm) {
        try {
            final PackageInfo sys = pm.getPackageInfo("android", PackageManager.GET_SIGNATURES);
            return getFirstSignature(sys);
        } catch (NameNotFoundException e) {
        }
        return null;
    }

    public static boolean isPackageInstalled(Context context, String pkg, boolean ignoreState) {
        if (pkg != null) {
            try {
                PackageInfo pi = context.getPackageManager().getPackageInfo(pkg, 0);
                if (!pi.applicationInfo.enabled && !ignoreState) {
                    return false;
                }
            } catch (NameNotFoundException e) {
                return false;
            }
        }

        return true;
    }

    public static boolean isPackageInstalled(Context context, String pkg) {
        return isPackageInstalled(context, pkg, true);
    }

    public static boolean isPackageInstalled(String packageName, PackageManager pm) {
        try {
            String mVersion = pm.getPackageInfo(packageName, 0).versionName;
            return mVersion != null;
        } catch (PackageManager.NameNotFoundException notFound) {
            return false;
        }
    }

    public static void restartSystemUi(Context context) {
        Toast.makeText(context, R.string.systemui_restart_toast, Toast.LENGTH_LONG).show();
        new RestartSystemUiTask(context).execute();
    }

    public static void restartSystemUiSilent(Context context) {
        new RestartSystemUiTask(context).execute();
    }

    public static void showSystemUiRestartDialog(Context context) {
        restartSystemUi(context);
    }

    private static class RestartSystemUiTask extends AsyncTask<Void, Void, Void> {
        private Context mContext;

        public RestartSystemUiTask(Context context) {
            super();
            mContext = context;
        }

        @Override
        protected Void doInBackground(Void... params) {
        	try {
        	     Thread.sleep(3000); //3s
        	} catch (InterruptedException ie) {}

            try {
                ActivityManager am =
                        (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
                IActivityManager ams = ActivityManager.getService();
                for (ActivityManager.RunningAppProcessInfo app: am.getRunningAppProcesses()) {
                    if ("com.android.systemui".equals(app.processName)) {
                        ams.killApplicationProcess(app.processName, app.uid);
                        break;
                    }
                }
                //Class ActivityManagerNative = Class.forName("android.app.ActivityManagerNative");
                //Method getDefault = ActivityManagerNative.getDeclaredMethod("getDefault", null);
                //Object amn = getDefault.invoke(null, null);
                //Method killApplicationProcess = amn.getClass().getDeclaredMethod("killApplicationProcess", String.class, int.class);
                //mContext.stopService(new Intent().setComponent(new ComponentName("com.android.systemui", "com.android.systemui.SystemUIService")));
                //am.killBackgroundProcesses("com.android.systemui");
                //for (ActivityManager.RunningAppProcessInfo app : am.getRunningAppProcesses()) {
                //    if ("com.android.systemui".equals(app.processName)) {
                //        killApplicationProcess.invoke(amn, app.processName, app.uid);
                //        break;
                //    }
                //}
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}

