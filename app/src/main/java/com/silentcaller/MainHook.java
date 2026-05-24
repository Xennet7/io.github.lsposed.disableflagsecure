package com.silentcaller;

import android.media.MediaPlayer;

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
                            "com.android.incallui.incomingshow.view.IncomingShowView$3",
                            lpparam.classLoader
                    );

            XposedHelpers.findAndHookMethod(
                    clazz,
                    "onPrepared",
                    MediaPlayer.class,

                    new XC_MethodHook() {

                        @Override
                        protected void beforeHookedMethod(
                                MethodHookParam param
                        ) {

                            XposedBridge.log(
                                    "BLOCKED onPrepared"
                            );

                            MediaPlayer mp =
                                    (MediaPlayer) param.args[0];

                            mp.stop();

                            param.setResult(null);
                        }
                    });

            XposedBridge.log(
                    "onPrepared hook installed"
            );

        } catch (Throwable t) {

            XposedBridge.log(t);
        }
    }
}
