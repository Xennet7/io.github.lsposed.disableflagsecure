package com.silentcaller;

import android.app.AndroidAppHelper;
import android.app.NotificationManager;
import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {

    private static final String CONFIG =
            "/data/adb/silentcaller.txt";

    private boolean isBlocked(String number) {

        try {

            if (number == null)
                return false;

            File file = new File(CONFIG);

            if (!file.exists())
                return false;

            BufferedReader br =
                    new BufferedReader(
                            new FileReader(file)
                    );

            String line;

            while ((line = br.readLine()) != null) {

                line = line.trim();

                if (line.isEmpty())
                    continue;

                if (number.contains(line)) {

                    br.close();

                    return true;
                }
            }

            br.close();

        } catch (Throwable t) {

            XposedBridge.log(t);
        }

        return false;
    }

    private void setDnd(int mode) {

        try {

            Context context =
                    AndroidAppHelper.currentApplication();

            if (context == null)
                return;

            NotificationManager nm =
                    (NotificationManager)
                            context.getSystemService(
                                    Context.NOTIFICATION_SERVICE
                            );

            if (nm == null)
                return;

            nm.setInterruptionFilter(mode);

        } catch (Throwable t) {

            XposedBridge.log(t);
        }
    }

    @Override
    public void handleLoadPackage(
            XC_LoadPackage.LoadPackageParam lpparam
    ) {

        if (!lpparam.packageName.equals(
                "android"
        )) {
            return;
        }

        try {

            XposedBridge.log(
                    "SilentCaller loaded"
            );

            Class<?> cls =
                    XposedHelpers.findClass(
                            "com.android.server.TelephonyRegistry",
                            lpparam.classLoader
                    );

            XposedHelpers.findAndHookMethod(
                    cls,
                    "notifyCallState",
                    int.class,
                    int.class,
                    int.class,
                    String.class,

                    new XC_MethodHook() {

                        @Override
                        protected void beforeHookedMethod(
                                MethodHookParam param
                        ) {

                            try {

                                int state =
                                        (int) param.args[2];

                                String number =
                                        (String) param.args[3];

                                XposedBridge.log(
                                        "CALL STATE: "
                                                + state
                                                + " NUMBER: "
                                                + number
                                );

                                // CALL_STATE_RINGING
                                if (state == 1 &&
                                        isBlocked(number)) {

                                    XposedBridge.log(
                                            "BLOCKED CALL -> DND ON"
                                    );

                                    setDnd(
                                            NotificationManager.INTERRUPTION_FILTER_NONE
                                    );
                                }

                                // CALL_STATE_IDLE
                                if (state == 0) {

                                    XposedBridge.log(
                                            "CALL ENDED -> DND OFF"
                                    );

                                    setDnd(
                                            NotificationManager.INTERRUPTION_FILTER_ALL
                                    );
                                }

                            } catch (Throwable t) {

                                XposedBridge.log(t);
                            }
                        }
                    });

        } catch (Throwable t) {

            XposedBridge.log(t);
        }
    }
}
