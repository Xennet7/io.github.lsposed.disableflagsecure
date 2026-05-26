package com.silentcaller;

import java.io.BufferedReader;
import java.io.DataOutputStream;
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

    private void root(String cmd) {

        try {

            Process p =
                    Runtime.getRuntime().exec("su");

            DataOutputStream os =
                    new DataOutputStream(
                            p.getOutputStream()
                    );

            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");

            os.flush();

            p.waitFor();

        } catch (Throwable t) {

            XposedBridge.log(t);
        }
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

            Class<?> cls =
                    XposedHelpers.findClass(
                            "com.android.server.TelephonyRegistry",
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
                        ) {

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

                                // CALL_STATE_RINGING
                                if (state == 1 &&
                                        isBlocked(number)) {

                                    XposedBridge.log(
                                            "BLOCKED CALL -> DND ON"
                                    );

                                    root(
                                            "cmd notification set_dnd none"
                                    );
                                }

                                // CALL_STATE_IDLE
                                if (state == 0) {

                                    XposedBridge.log(
                                            "CALL ENDED -> DND OFF"
                                    );

                                    root(
                                            "cmd notification set_dnd off"
                                    );
                                }

                            } catch (Throwable t) {

                                XposedBridge.log(t);
                            }
                        }
                    });

            XposedBridge.log(
                    "SilentCaller loaded"
            );

        } catch (Throwable t) {

            XposedBridge.log(t);
        }
    }
}
