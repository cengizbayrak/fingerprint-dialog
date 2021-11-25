package com.cengizb.fingerprintdialog;

import android.annotation.TargetApi;
import android.hardware.biometrics.BiometricPrompt;
import android.os.Build;
import android.support.annotation.NonNull;

/**
 * Created by cengizb on 30.05.2019
 * <p>
 * Converts {@link BiometricPrompt.AuthenticationCallback} into {@link AuthenticationCallback}
 * for Android version P and above.
 *
 * @author <a href="https://github.com/cengizbayrak">cengizb</a>
 */
@TargetApi(Build.VERSION_CODES.P)
class AuthenticationCallbackV28 extends BiometricPrompt.AuthenticationCallback {

    /**
     * {@link AuthenticationCallback} implemented by caller.
     */
    @NonNull
    private final AuthenticationCallback callback;

    /**
     * Public constructor.
     *
     * @param callback {@link AuthenticationCallback} to fire events for fingerprint authentication.
     */
    AuthenticationCallbackV28(@NonNull final AuthenticationCallback callback) {
        this.callback = callback;
    }

    /**
     * @see BiometricPrompt.AuthenticationCallback#onAuthenticationError(int, CharSequence)
     */
    @Override
    public void onAuthenticationError(final int code, final CharSequence error) {
        super.onAuthenticationError(code, error);

        switch (code) {
            // fingerprint scan is canceled by negative/cancel button
            case BiometricPrompt.BIOMETRIC_ERROR_USER_CANCELED:
                callback.authenticationCanceledByUser();
                break;
            // no fingerprint hardware of device
            case BiometricPrompt.BIOMETRIC_ERROR_HW_NOT_PRESENT:
            case BiometricPrompt.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                callback.fingerprintAuthenticationNotSupported();
                break;
            // no enrolled fingerprints
            case BiometricPrompt.BIOMETRIC_ERROR_NO_BIOMETRICS:
                callback.noEnrolledFingerprints();
                break;
            // any other unrecoverable error
            default:
                callback.onAuthenticationError(code, error);
        }
    }


    /**
     * @see BiometricPrompt.AuthenticationCallback#onAuthenticationFailed()
     */
    @Override
    public void onAuthenticationFailed() {
        super.onAuthenticationFailed();

        callback.onAuthenticationFailed();
    }

    /**
     * @see BiometricPrompt.AuthenticationCallback#onAuthenticationHelp(int, CharSequence)
     */
    @Override
    public void onAuthenticationHelp(final int code, final CharSequence help) {
        super.onAuthenticationHelp(code, help);

        callback.onAuthenticationHelp(code, help);
    }

    /**
     * @see BiometricPrompt.AuthenticationCallback#onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult)
     */
    @Override
    public void onAuthenticationSucceeded(final BiometricPrompt.AuthenticationResult result) {
        super.onAuthenticationSucceeded(result);

        callback.onAuthenticationSucceeded();
    }
}
