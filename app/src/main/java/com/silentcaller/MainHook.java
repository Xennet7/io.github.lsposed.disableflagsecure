package com.silentcaller;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {

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

            for (var method : cls.getDeclaredMethods()) {

                if (method.getName().contains(
                        "notifyCallState"
                )) {

                    XposedBridge.log(
                            "FOUND METHOD: "
                                    + method.toString()
                    );
                }
            }

        } catch (Throwable t) {

            XposedBridge.log(t);
        }
    }
}
