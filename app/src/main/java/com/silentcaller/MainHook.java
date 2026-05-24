package com.silentcaller;

import android.app.Activity;

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

        if (!lpparam.packageName.equals(
                "com.android.incallui"
        )) {
            return;
        }

        XposedBridge.log("INCALLUI LOADED");

        XposedHelpers.findAndHookMethod(
                Activity.class,
                "onResume",

                new XC_MethodHook() {

                    @Override
                    protected void beforeHookedMethod(
                            MethodHookParam param
                    ) {

                        XposedBridge.log(
                                "Activity resumed in InCallUI"
                        );
                    }
                });
    }
}
