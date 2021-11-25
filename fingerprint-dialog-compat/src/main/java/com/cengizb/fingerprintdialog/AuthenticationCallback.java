package com.cengizb.fingerprintdialog;

import android.content.Context;
import android.support.annotation.Nullable;

/**
 * Created by cengizb on 30.05.2019
 * <p>
 * Callback contract to get results of fingerprint authentication process.
 * This defines homogeneous way of getting callbacks for the process across Android versions.
 *
 * @author <a href="https://github.com/cengizbayrak">cengizb</a>
 */
public interface AuthenticationCallback {

    /**
     * This will notify, whenever,
     * <ul>
     * <li>There is no fingerprint hardware on device</li>
     * <li>Android version of device is below {@link android.os.Build.VERSION_CODES#M}</li>
     * </ul>
     * Fingerprint dialog will not be displayed, and authentication will not be performed.
     * <p>
     * Other authentication ways such as pin, password should be used.
     */
    void fingerprintAuthenticationNotSupported();

    /**
     * This will notify, no fingerprint enrolled into phone settings.
     * <p>
     * Fingerprint dialog will not be displayed, and authentication will not be performed.
     * <p>
     * User should be redirected to "Settings".
     * <p>
     * Use {@link FingerprintUtils#openSecuritySettings(Context)}, and prompt user to enroll
     * at least one fingerprint.
     *
     * @see FingerprintUtils#openSecuritySettings(Context)
     */
    void noEnrolledFingerprints();

    /**
     * This will notify, user cancels fingerprint authentication by clicking negative/cancel button.
     */
    void authenticationCanceledByUser();

    /**
     * This will notify, an unrecoverable error occurs during authentication.
     * <p>
     * Fingerprint scan will stop after this.
     *
     * @param code  code of error ({@link ErrorCode})
     * @param error message of error
     * @see ErrorCode
     */
    void onAuthenticationError(@ErrorCode final int code, @Nullable final CharSequence error);

    /**
     * This will notify, a recoverable error occurs during authentication.
     * <p>
     * Help string will be provided to give guidance to user about what goes wrong.
     * e.g. "Dirty sensor, clean it."
     * <p>
     * Fingerprint scan will continue after this.
     *
     * @param code code of error {@link HelperCode}
     * @param help message of error
     * @see HelperCode
     */
    void onAuthenticationHelp(@HelperCode final int code, @Nullable final CharSequence help);

    /**
     * This will notify, scanned finger does not match with any enrolled finger.
     * <p>
     * Fingerprint scan will continue after this.
     */
    void onAuthenticationFailed();

    /**
     * This will notify, fingerprint auth is successful.
     * <p>
     * Fingerprint scan will stop after this and dialog will be dismissed.
     */
    void onAuthenticationSucceeded();
}
