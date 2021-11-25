package com.cengizb.fingerprintdialog;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;

/**
 * Created by cengizb on 30.05.2019
 * <p>
 * Util class.
 *
 * @author <a href="https://github.com/cengizbayrak">cengizb</a>
 */
@SuppressWarnings("WeakerAccess")
public class FingerprintUtils {
    /**
     * Open "Security" settings screen of device
     *
     * @param context caller context
     */
    public static void openSecuritySettings(@NonNull final Context context) {
        Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
        context.startActivity(intent);
    }

    /**
     * Check if device has hardware support for fingerprint scanning
     *
     * @param context caller context
     * @return true if device has hardware support
     */
    public static boolean hardwareSupported(@NonNull final Context context) {
        return FingerprintManagerCompat.from(context).isHardwareDetected();
    }

    /**
     * Check if any enrolled fingerprint exists in device
     *
     * @param context caller context
     * @return true if device has at least one enrolled fingerprint
     */
    public static boolean fingerprintEnrolled(@NonNull final Context context) {
        return FingerprintManagerCompat.from(context).hasEnrolledFingerprints();
    }
}
