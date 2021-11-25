package com.cengizb.fingerprintdialog;

import android.annotation.SuppressLint;
import android.hardware.fingerprint.FingerprintManager;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by cengizb on 30.05.2019
 * <p>
 * Codes to detect unrecoverable error from fingerprint authentication.
 * <p>
 * Fingerprint authentication will terminate once the any of these error code occurs.
 *
 * @author <a href="https://github.com/kevalpatel2106">kevalpatel2106</a>
 */
@SuppressWarnings("deprecation")
@SuppressLint("InlinedApi")
@Retention(RetentionPolicy.SOURCE)
@IntDef({FingerprintManager.FINGERPRINT_ERROR_CANCELED,
        FingerprintManager.FINGERPRINT_ERROR_LOCKOUT,
        FingerprintManager.FINGERPRINT_ERROR_LOCKOUT_PERMANENT,
        FingerprintManager.FINGERPRINT_ERROR_NO_FINGERPRINTS,
        FingerprintManager.FINGERPRINT_ERROR_NO_SPACE,
        FingerprintManager.FINGERPRINT_ERROR_TIMEOUT,
        FingerprintManager.FINGERPRINT_ERROR_UNABLE_TO_PROCESS,
        FingerprintManager.FINGERPRINT_ERROR_VENDOR})
public @interface ErrorCode {
}
