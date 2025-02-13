package com.ulfben.PlatformerMK3.utilities;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;
// Created by Ulf Benjaminsson (ulfben) on 2017-04-01.

public class SysUtils {
    private static final String TAG = "SysUtils";
    private SysUtils() {
        super();
    }

    public static int pxToDp(final int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static int dpToPx(final int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static boolean isProbablyEmulator() {
        int rating = 0;
        final int threshold = 4;
        if ((Build.PRODUCT.contains("sdk_google_phone_x86")) || (Build.PRODUCT.equals("sdk")) || (Build.PRODUCT.contains("google_sdk"))
                || (Build.PRODUCT.contains("sdk_x86")) || (Build.PRODUCT.contains("vbox86p"))) {
            rating++;
        }
        if ((Build.MANUFACTURER.contains("unknown")) || (Build.MANUFACTURER.contains("Genymotion"))) {
            rating++;
        }
        if ((Build.BRAND.contains("generic")) || (Build.BRAND.contains("generic_x86")) || (Build.BRAND.equals("Android"))) {
            rating++;
        }
        if ((Build.DEVICE.contains("generic")) || (Build.DEVICE.contains("generic_x86")) || (Build.DEVICE.contains("vbox86p"))) {
            rating++;
        }
        if ((Build.MODEL.contains("Android SDK built for x86")) || (Build.MODEL.contains("sdk")) || (Build.MODEL.contains("google_sdk")) || (Build.MODEL.contains("Emulator"))) {
            rating++;
        }
        if ((Build.HARDWARE.contains("ranchu")) || (Build.HARDWARE.contains("goldfish")) || (Build.HARDWARE.contains("vbox86"))) {
            rating++;
        }
        if ((Build.FINGERPRINT.contains("sdk_google_phone_x86"))
                || (Build.FINGERPRINT.contains("generic_x86"))
                || (Build.FINGERPRINT.contains("userdebug/test-keys"))
                || (Build.FINGERPRINT.contains("generic/google_sdk/generic"))
                || (Build.FINGERPRINT.contains("generic/vbox86p/vbox86p"))) {
            rating++;
        }
        logDeviceInfo();
        Log.i(TAG, "isProbablyEmulator() rating: " + rating);
        return rating > threshold;
    }

    private static void logDeviceInfo(){
        final String info = "Build.PRODUCT " + Build.PRODUCT + "\n" +
                "Build.DEVICE " + Build.DEVICE + "\n" +
                "Build.MODEL " + Build.MODEL + "\n" +
                "Build.BRAND " + Build.BRAND + "\n" +
                "Build.MANUFACTURER " + Build.MANUFACTURER + "\n" +
                "Build.FINGERPRINT " + Build.FINGERPRINT + "\n" +
                "Build.HARDWARE " + Build.HARDWARE + "\n";
        Log.i(TAG, "Device Info: " + info);
    }
}
