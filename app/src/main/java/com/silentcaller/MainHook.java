package com.silentcaller;

import android.app.NotificationManager;
import android.content.Context;
import android.provider.Settings;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(
            XC_LoadPackage.LoadPackageParam lpparam
    ) throws Throwable {

        // ONLY SYSTEM SERVER

        if (!"android".equals(
                lpparam.packageName))
            return;

        XposedBridge.log(
                "SilentCaller loaded"
        );

        try {

            Class<?> cls =
                    XposedHelpers.findClass(
                            "com.android.server.TelephonyRegistry",
                            lpparam.classLoader
                    );

            XposedHelpers.findAndHookMethod(

                    cls,

                    "notifyCallStateForAllSubs",

                    int.class,
                    String.class,

                    new XC_MethodHook() {

                        @Override
                        protected void beforeHookedMethod(
                                MethodHookParam param
                        ) throws Throwable {

                            try {

                                int state =
                                        (int) param.args[0];

                                String number =
                                        (String) param.args[1];

                                // =====================
                                // GET CONTEXT
                                // =====================

                                Context context =
                                        (Context)
                                                XposedHelpers.callMethod(

                                                        XposedHelpers.callStaticMethod(

                                                                XposedHelpers.findClass(
                                                                        "android.app.ActivityThread",
                                                                        null
                                                                ),

                                                                "currentActivityThread"
                                                        ),

                                                        "getSystemContext"
                                                );

                                // =====================
                                // ENABLE / DISABLE
                                // FIRST CHECK
                                // =====================

                                int disabled =
                                        Settings.Global.getInt(
                                                context.getContentResolver(),
                                                "silentcaller_disabled",
                                                0
                                        );

                                if (disabled == 1) {
                                    return;
                                }

                                XposedBridge.log(
                                        "CALL STATE="
                                                + state
                                                + " NUMBER="
                                                + number
                                );

                                // =====================
                                // GET BLOCKED NUMBERS
                                // =====================

                                String blockedList =
                                        Settings.Global.getString(
                                                context.getContentResolver(),
                                                "silentcaller_numbers"
                                        );

                                if (blockedList == null
                                        || blockedList.isEmpty()) {
                                    return;
                                }

                                boolean matched = false;

                                for (String n :
                                        blockedList.split(",")) {

                                    n = n.trim();

                                    if (!n.isEmpty()
                                            && number != null
                                            && (number.contains(n)
                                            || n.contains(number))) {

                                        matched = true;
                                        break;
                                    }
                                }

                                // =====================
                                // MATCH CHECK
                                // =====================

                                if (matched) {

                                    XposedBridge.log(
                                            "MATCH FOUND"
                                    );

                                    // =====================
                                    // ONLY WHEN RINGING
                                    // =====================

                                    if (state == 1) {

                                        try {

                                            NotificationManager nm =
                                                    (NotificationManager)
                                                            context.getSystemService(
                                                                    Context.NOTIFICATION_SERVICE
                                                            );

                                            nm.setInterruptionFilter(
                                                    NotificationManager.INTERRUPTION_FILTER_NONE
                                            );

                                            XposedBridge.log(
                                                    "DND NONE ENABLED"
                                            );

                                        } catch (Throwable e) {

                                            XposedBridge.log(
                                                    "DND FAILED"
                                            );

                                            XposedBridge.log(e);
                                        }
                                    }
                                }

                                // =====================
                                // OPTIONAL:
                                // RESTORE NORMAL DND
                                // WHEN CALL ENDS
                                // =====================

                                if (state == 0) {

                                    try {

                                        NotificationManager nm =
                                                (NotificationManager)
                                                        context.getSystemService(
                                                                Context.NOTIFICATION_SERVICE
                                                        );

                                        nm.setInterruptionFilter(
                                                NotificationManager.INTERRUPTION_FILTER_ALL
                                        );

                                        XposedBridge.log(
                                                "DND RESTORED"
                                        );

                                    } catch (Throwable e) {

                                        XposedBridge.log(
                                                "RESTORE FAILED"
                                        );
                                    }
                                }

                            } catch (Throwable e) {

                                XposedBridge.log(
                                        "hook crash"
                                );

                                XposedBridge.log(e);
                            }
                        }
                    }
            );

            XposedBridge.log(
                    "notifyCallStateForAllSubs hooked"
            );

        } catch (Throwable e) {

            XposedBridge.log(
                    "hook failed"
            );

            XposedBridge.log(e);
        }
    }
}
