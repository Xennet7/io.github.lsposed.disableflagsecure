package com.silentcaller;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {

    // =========================
    // HARD CODED NUMBER
    // =========================

    private static final String BLOCKED_NUMBER =
            "+918086298339";

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
                            "com.android.phone.PhoneInterfaceManager",
                            lpparam.classLoader
                    );

            XposedHelpers.findAndHookMethod(

                    cls,

                    "notifyCallState",

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

                                XposedBridge.log(
                                        "CALL STATE: "
                                                + state
                                                + " NUMBER: "
                                                + number
                                );

                                // =====================
                                // MATCH CHECK
                                // =====================

                                if (number != null
                                        && (number.contains(BLOCKED_NUMBER)
                                        || BLOCKED_NUMBER.contains(number))) {

                                    XposedBridge.log(
                                            "MATCH FOUND: "
                                                    + number
                                    );

                                    // STATES:
                                    //
                                    // 0 = IDLE
                                    // 1 = RINGING
                                    // 2 = OFFHOOK
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
                    "notifyCallState hooked"
            );

        } catch (Throwable e) {

            XposedBridge.log(
                    "hook failed"
            );

            XposedBridge.log(e);
        }
    }
}
