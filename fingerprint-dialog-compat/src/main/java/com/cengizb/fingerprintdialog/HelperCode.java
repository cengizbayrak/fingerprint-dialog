package com.cengizb.fingerprintdialog;

import android.annotation.SuppressLint;
import android.hardware.fingerprint.FingerprintManager;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by cengizb on 30.05.2019
 * <p>
 * Codes to detect recoverable error from fingerprint authentication.
 * <p>
 * Fingerprint authentication won't terminate once the any of these error code occurs.
 *
 * @author <a href="https://github.com/cengizbayrak">cengizb</a>
 */
@SuppressWarnings("deprecation")
@SuppressLint("InlinedApi")
@Retention(RetentionPolicy.SOURCE)
@IntDef({FingerprintManager.FINGERPRINT_ACQUIRED_GOOD,
        FingerprintManager.FINGERPRINT_ACQUIRED_IMAGER_DIRTY,
        FingerprintManager.FINGERPRINT_ACQUIRED_INSUFFICIENT,
        FingerprintManager.FINGERPRINT_ACQUIRED_PARTIAL,
        FingerprintManager.FINGERPRINT_ACQUIRED_TOO_FAST,
        FingerprintManager.FINGERPRINT_ACQUIRED_TOO_SLOW})
public @interface HelperCode {
}
