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

            if (number == null)
                return false;

            number = number.trim();

            File file = new File(CONFIG);

            if (!file.exists()) {

                XposedBridge.log(
                        "CONFIG NOT FOUND"
                );

                return false;
            }

            BufferedReader br =
                    new BufferedReader(
                            new FileReader(file)
                    );

            String line;

            while ((line = br.readLine()) != null) {

                line = line.trim();

                if (line.isEmpty())
                    continue;

                XposedBridge.log(
                        "COMPARE: ["
                                + number
                                + "] vs ["
                                + line
                                + "]"
                );

                if (number.equals(line)) {

                    XposedBridge.log(
                            "MATCH FOUND"
                    );

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

            XposedBridge.log(
                    "TelephonyRegistry hooked"
            );

            XposedHelpers.findAndHookMethod(
                    cls,
                    "notifyCallState",
                    int.class,
                    int.class,
                    int.class,
                    String.class,

                    new XC_MethodHook() {

                        @Override
                        protected void beforeHookedMethod(
                                MethodHookParam param
                        ) {

                            try {

                                int state =
                                        (int) param.args[2];

                                String number =
                                        (String) param.args[3];

                                XposedBridge.log(
                                        "CALL STATE: "
                                                + state
                                                + " NUMBER: "
                                                + number
                                );

                                if (state != 1)
                                    return;

                                if (!isBlocked(number))
                                    return;

                                XposedBridge.log(
                                        "BLOCKED CALL"
                                );

                            } catch (Throwable t) {

                                XposedBridge.log(t);
                            }
                        }
                    });

        } catch (Throwable t) {

            XposedBridge.log(t);
        }
    }
}
