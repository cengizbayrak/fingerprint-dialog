package com.cengizb.fingerprintdialog;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.biometrics.BiometricPrompt;
import android.os.Build;
import android.os.CancellationSignal;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;

import java.util.concurrent.Executor;

/**
 * Created by cengizb on 30.05.2019
 * <p>
 * Builder for fingerprint dialog. This will display dialog based on the android version.
 *
 * @author <a href="https://github.com/cengizbayrak">cengizb</a>
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class FingerprintDialogBuilder {

    // caller context
    @NonNull
    private final Context context;

    // dialog title
    private String title;

    // dialog subtitle
    private String subTitle;

    // dialog description
    private String description;

    // dialog negative/cancel button title
    private String buttonTitle;

    /**
     * Public constructor.
     *
     * @param context {@link Context} caller context
     */
    public FingerprintDialogBuilder(@NonNull final Context context) {
        this.context = context;
    }

    /**
     * Set dialog title. This field is required.
     *
     * @param title title string
     * @return {@link FingerprintDialogBuilder}
     * @see #title(int)
     */
    public FingerprintDialogBuilder title(@NonNull final String title) {
        this.title = title;
        return this;
    }

    /**
     * Set dialog title. This field is required.
     *
     * @param title title string resource
     * @return {@link FingerprintDialogBuilder}
     * @see #title(String)
     */
    public FingerprintDialogBuilder title(@StringRes final int title) {
        this.title = context.getString(title);
        return this;
    }

    /**
     * Set dialog subtitle. This field is required.
     *
     * @param subtitle subtitle string
     * @return {@link FingerprintDialogBuilder}
     * @see #subtitle(int)
     */
    public FingerprintDialogBuilder subtitle(@NonNull final String subtitle) {
        subTitle = subtitle;
        return this;
    }

    /**
     * Set dialog subtitle. This field is required.
     *
     * @param subtitle subtitle string resource
     * @return {@link FingerprintDialogBuilder}
     * @see #subtitle(String)
     */
    public FingerprintDialogBuilder subtitle(@StringRes final int subtitle) {
        subTitle = context.getString(subtitle);
        return this;
    }

    /**
     * Set dialog description. This field is required.
     *
     * @param description description string
     * @return {@link FingerprintDialogBuilder}
     * @see #description(int)
     */
    public FingerprintDialogBuilder description(@NonNull final String description) {
        this.description = description;
        return this;
    }

    /**
     * Set dialog description. This field is required.
     *
     * @param description description string resource
     * @return {@link FingerprintDialogBuilder}
     * @see #description(String)
     */
    public FingerprintDialogBuilder description(@StringRes final int description) {
        this.description = context.getString(description);
        return this;
    }

    /**
     * Set dialog negative/cancel button title. Default title is "Cancel".
     *
     * @param text title string
     * @return {@link FingerprintDialogBuilder}
     * @see #negativeButtonTitle(int)
     */
    public FingerprintDialogBuilder negativeButtonTitle(@Nullable final String text) {
        buttonTitle = text;
        return this;
    }

    /**
     * Set dialog negative/cancel button title. Default title is "Cancel".
     *
     * @param text title string resource
     * @return {@link FingerprintDialogBuilder}
     * @see #negativeButtonTitle(String)
     */
    public FingerprintDialogBuilder negativeButtonTitle(@StringRes final int text) {
        buttonTitle = context.getString(text);
        return this;
    }

    /**
     * Build {@link FingerprintDialogCompatV23}.
     * <p>
     * The dialog will be displayed for android version M and above.
     */
    public void show(@NonNull final FragmentManager manager,
                     @NonNull final AuthenticationCallback callback) {
        // validate title
        if (TextUtils.isEmpty(title)) {
            throw new IllegalArgumentException(context.getString(R.string.title_warning));
        }

        // validate subtitle
        if (TextUtils.isEmpty(subTitle)) {
            throw new IllegalArgumentException(context.getString(R.string.subtitle_warning));
        }

        // validate description
        if (TextUtils.isEmpty(description)) {
            throw new IllegalArgumentException(context.getString(R.string.description_warning));
        }

        // validate button title
        if (TextUtils.isEmpty(buttonTitle)) {
            // set default button title
            buttonTitle = context.getString(android.R.string.cancel);
        }

        // check if android version supports fingerprint authentication
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            callback.fingerprintAuthenticationNotSupported();
            return;
        }

        // check if device has fingerprint sensor
        if (!FingerprintUtils.hardwareSupported(context)) {
            callback.fingerprintAuthenticationNotSupported();
            return;
        }

        // check if there are any fingerprints enrolled
        if (!FingerprintUtils.fingerprintEnrolled(context)) {
            callback.noEnrolledFingerprints();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            showFingerprintDialog(callback);
        } else {
            final FingerprintDialogCompatV23 fpd = FingerprintDialogCompatV23.createDialog(
                    title,
                    subTitle,
                    description,
                    buttonTitle);
            fpd.setAuthenticationCallback(callback);
            fpd.show(manager, FingerprintDialogCompatV23.class.getName());
        }
    }

    @SuppressLint("MissingPermission")
    @TargetApi(Build.VERSION_CODES.P)
    private void showFingerprintDialog(@NonNull final AuthenticationCallback callback) {
        final Executor executor = context.getMainExecutor();
        final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callback.authenticationCanceledByUser();
            }
        };
        final AuthenticationCallbackV28 callbackV28 = new AuthenticationCallbackV28(callback);

        new BiometricPrompt.Builder(context)
                .setTitle(title)
                .setSubtitle(subTitle)
                .setDescription(description)
                .setNegativeButton(buttonTitle, executor, listener)
                .build()
                .authenticate(new CancellationSignal(), executor, callbackV28);
    }
}
