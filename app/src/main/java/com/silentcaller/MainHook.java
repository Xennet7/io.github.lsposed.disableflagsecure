package com.silentcaller;

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

    @Override
    public void handleLoadPackage(
            XC_LoadPackage.LoadPackageParam lpparam
    ) {

        if (!lpparam.packageName.equals(
                "com.android.server.telecom"
        )) {
            return;
        }

        try {

            XposedBridge.log(
                    "SilentCaller loaded"
            );

            Class<?> clazz =
                    XposedHelpers.findClass(
                            "com.android.server.telecom.AsyncRingtonePlayer",
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

                            try {

                                String data =
                                        String.valueOf(
                                                param.thisObject
                                        );

                                if (isBlocked(data)) {

                                    XposedBridge.log(
                                            "SilentCaller blocked"
                                    );

                                    param.setResult(null);
                                }

                            } catch (Throwable t) {

                                XposedBridge.log(t);
                            }
                        }
                    });

            XposedBridge.log(
                    "SilentCaller hook attached"
            );

        } catch (Throwable t) {

            XposedBridge.log(t);
        }
    }
}
