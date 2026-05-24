package com.silentcaller;

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

        try {

            Class<?> clazz =
                    XposedHelpers.findClass(
                            "com.android.incallui.incomingshow.view.IncomingShowView",
                            lpparam.classLoader
                    );

            XposedHelpers.findAndHookMethod(
                    clazz,
                    "play",

                    new XC_MethodHook() {

                        @Override
                        protected void beforeHookedMethod(
                                MethodHookParam param
                        ) {

                            XposedBridge.log(
                                    "IncomingShowView.play BLOCKED"
                            );

                            param.setResult(null);
                        }
                    });

            XposedBridge.log(
                    "IncomingShowView hook installed"
            );

        } catch (Throwable t) {

            XposedBridge.log(t);
        }
    }
}
