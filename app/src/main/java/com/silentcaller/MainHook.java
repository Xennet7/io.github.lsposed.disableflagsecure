package com.silentcaller;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {

    // HARD CODED NUMBER

    private static final String BLOCKED =
            "+918086298339";

    @Override
    public void handleLoadPackage(
            XC_LoadPackage.LoadPackageParam lpparam
    ) throws Throwable {

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

                                XposedBridge.log(
                                        "CALL STATE="
                                                + state
                                                + " NUMBER="
                                                + number
                                );

                                if (number != null
                                        && (number.contains(BLOCKED)
                                        || BLOCKED.contains(number))) {

                                    XposedBridge.log(
                                            "MATCH FOUND"
                                    );
                                }

                                // STATES:
                                //
                                // 0 = IDLE
                                // 1 = RINGING
                                // 2 = OFFHOOK

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
