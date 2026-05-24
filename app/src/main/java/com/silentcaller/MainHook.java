package com.silentcaller;

import android.net.Uri;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(
            XC_LoadPackage.LoadPackageParam lpparam
    ) {

        if (!lpparam.packageName.equals("android")) {
            return;
        }

        try {

            Class<?> clazz =
                    XposedHelpers.findClass(
                            "com.android.server.notification.NotificationAttentionHelper",
                            lpparam.classLoader
                    );

            for (var method : clazz.getDeclaredMethods()) {

                XposedBridge.log(
                        "METHOD: " + method.toString()
                );
            }

        } catch (Throwable t) {

            XposedBridge.log(t);
        }
    }
}
