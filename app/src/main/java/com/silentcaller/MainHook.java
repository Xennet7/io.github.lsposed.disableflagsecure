package com.silentcaller;

import android.app.NotificationManager;
import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;

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

        // ONLY ANDROID PROCESS

        if (!lpparam.packageName.equals("android")) {
            return;
        }

        // FIND TELEPHONY REGISTRY

        Class<?> telephonyRegistryClass =
                XposedHelpers.findClass(
                        "com.android.server.TelephonyRegistry",
                        lpparam.classLoader
                );

        // HOOK CALL STATE

        XposedHelpers.findAndHookMethod(

                telephonyRegistryClass,

                "notifyCallState",

                int.class,
                int.class,
                int.class,
                String.class,

                new XC_MethodHook() {

                    @Override
                    protected void beforeHookedMethod(
                            MethodHookParam param
                    ) throws Throwable {

                        try {

                            // GET SYSTEM CONTEXT

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

                            // FIRST THING
                            // MODULE ENABLE CHECK

                            if (!isModuleEnabled(context)) {
                                return;
                            }

                            // NOW PROCESS

                            int state =
                                    (int) param.args[2];

                            String incomingNumber =
                                    (String) param.args[3];

                            XposedBridge.log(
                                    "CALL STATE=" +
                                            state +
                                            " NUMBER=" +
                                            incomingNumber
                            );

                            // INCOMING CALL

                            if (state ==
                                    TelephonyManager.CALL_STATE_RINGING) {

                                if (incomingNumber != null &&
                                        isBlockedNumber(
                                                context,
                                                incomingNumber
                                        )) {

                                    XposedBridge.log(
                                            "MATCH FOUND"
                                    );

                                    NotificationManager nm =
                                            (NotificationManager)
                                                    context.getSystemService(
                                                            Context.NOTIFICATION_SERVICE
                                                    );

                                    // ENABLE TOTAL SILENT

                                    nm.setInterruptionFilter(
                                            NotificationManager.INTERRUPTION_FILTER_NONE
                                    );

                                    XposedBridge.log(
                                            "DND NONE ENABLED"
                                    );
                                }
                            }

                            // CALL ENDED

                            if (state ==
                                    TelephonyManager.CALL_STATE_IDLE) {

                                NotificationManager nm =
                                        (NotificationManager)
                                                context.getSystemService(
                                                        Context.NOTIFICATION_SERVICE
                                                );

                                // RESTORE NORMAL

                                nm.setInterruptionFilter(
                                        NotificationManager.INTERRUPTION_FILTER_ALL
                                );

                                XposedBridge.log(
                                        "DND RESTORED"
                                );
                            }

                        } catch (Throwable e) {

                            XposedBridge.log(e);
                        }
                    }
                }
        );
    }

    // ENABLE / DISABLE MODULE

    private static boolean isModuleEnabled(
            Context context
    ) {

        try {

            int disabled =
                    Settings.Global.getInt(

                            context.getContentResolver(),

                            "silentcaller_disabled",

                            0
                    );

            return disabled == 0;

        } catch (Throwable e) {

            return true;
        }
    }

    // BLOCKED NUMBER CHECK

    private static boolean isBlockedNumber(
            Context context,
            String incoming
    ) {

        try {

            String numbers =
                    Settings.Global.getString(

                            context.getContentResolver(),

                            "silentcaller_numbers"
                    );

            if (numbers == null ||
                    numbers.isEmpty()) {

                return false;
            }

            for (String n : numbers.split(",")) {

                n = n.trim();

                if (n.isEmpty()) {
                    continue;
                }

                if (incoming.contains(n)) {

                    XposedBridge.log(
                            "BLOCKED NUMBER MATCH: " + n
                    );

                    return true;
                }
            }

        } catch (Throwable e) {

            XposedBridge.log(e);
        }

        return false;
    }
}
