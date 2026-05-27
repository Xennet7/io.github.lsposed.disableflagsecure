package com.silentcaller;

import android.content.Context;
import android.telephony.TelephonyManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {

    private static final String TAG = "SilentCaller";

    private static String CONFIG = null;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam)
            throws Throwable {

        if (!lpparam.packageName.equals("android"))
            return;

        XposedBridge.log(TAG + ": loaded");

        try {

            Context context =
                    (Context) XposedHelpers.callStaticMethod(
                            XposedHelpers.findClass(
                                    "android.app.ActivityThread",
                                    null
                            ),
                            "currentApplication"
                    );

            if (context != null) {

                Context de =
                        context.createDeviceProtectedStorageContext();

                File dir = de.getFilesDir();

                if (!dir.exists()) {
                    dir.mkdirs();
                }

                File configFile =
                        new File(dir, "silentcaller.txt");

                CONFIG = configFile.getAbsolutePath();

                if (!configFile.exists()) {

                    FileWriter fw =
                            new FileWriter(configFile);

                    fw.write("+918086298339\n");

                    fw.flush();
                    fw.close();

                    XposedBridge.log(
                            "Default config created"
                    );
                }

                XposedBridge.log(
                        "CONFIG PATH=" + CONFIG
                );
            }

        } catch (Throwable t) {

            XposedBridge.log(
                    "Config init failed"
            );

            XposedBridge.log(t);
        }

        XposedHelpers.findAndHookMethod(
                "com.android.server.telecom.calls.BlockChecker",
                lpparam.classLoader,
                "isBlocked",
                android.content.Context.class,
                String.class,
                new XC_MethodHook() {

                    @Override
                    protected void beforeHookedMethod(
                            MethodHookParam param
                    ) throws Throwable {

                        String number =
                                (String) param.args[1];

                        XposedBridge.log(
                                "Incoming number=" + number
                        );

                        if (isBlocked(number)) {

                            XposedBridge.log(
                                    "MATCH FOUND"
                            );

                            param.setResult(
                                    TelephonyManager.CALL_STATE_RINGING
                            );
                        }
                    }
                }
        );
    }

    private static boolean isBlocked(String number) {

        try {

            XposedBridge.log(
                    "isBlocked entered"
            );

            if (number == null)
                return false;

            if (CONFIG == null) {

                XposedBridge.log(
                        "CONFIG NULL"
                );

                return false;
            }

            number = number.trim()
                    .replaceAll("\\s+", "");

            XposedBridge.log(
                    "number=" + number
            );

            File file = new File(CONFIG);

            XposedBridge.log(
                    "path=" + file.getAbsolutePath()
            );

            XposedBridge.log(
                    "exists=" + file.exists()
            );

            XposedBridge.log(
                    "canRead=" + file.canRead()
            );

            XposedBridge.log(
                    "length=" + file.length()
            );

            if (!file.exists())
                return false;

            BufferedReader br =
                    new BufferedReader(
                            new FileReader(file)
                    );

            String line;

            while ((line = br.readLine()) != null) {

                line = line.trim()
                        .replaceAll("\\s+", "");

                XposedBridge.log(
                        "COMPARE " +
                                number +
                                " vs " +
                                line
                );

                if (number.equals(line)) {

                    br.close();

                    return true;
                }
            }

            br.close();

        } catch (Throwable t) {

            XposedBridge.log(
                    "isBlocked crash"
            );

            XposedBridge.log(t);
        }

        return false;
    }
}
