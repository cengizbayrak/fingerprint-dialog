package com.cengizb.fingerprintdialog;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Looper;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

/**
 * Created by cengizb on 30.05.2019
 * <p>
 * Dialog that acts as backport of {@link android.hardware.fingerprint.FingerprintDialog} for
 * android version below P.
 *
 * @author <a href="https://github.com/cengizbayrak">cengizb</a>
 */
@SuppressWarnings("deprecation")
@TargetApi(Build.VERSION_CODES.M)
public class FingerprintDialogCompatV23 extends DialogFragment {
    private static final String KEY_NAME = UUID.randomUUID().toString();

    // keys of arguments
    private static final String ARG_TITLE = "arg_title";
    private static final String ARG_SUBTITLE = "arg_subtitle";
    private static final String ARG_NEGATIVE_BUTTON_TITLE = "arg_negative_button_title";
    private static final String ARG_DESCRIPTION = "arg_description";

    // activity context
    private Context context;

    // for fingerprint authentication key
    private KeyStore keyStore;
    //
    private Cipher cipher;

    // fingerprint scan is running
    private boolean isScanning = false;

    private AppCompatTextView statusText;

    // notify caller about authentication status
    private AuthenticationCallback callback;

    // cancellation signal for fingerprint authentication
    private CancellationSignal cancellationSignal;

    private Runnable statusTextRunnable;

    /**
     * Create new instance of {@link FingerprintDialogCompatV23}.
     *
     * @param title               dialog title
     * @param subtitle            dialog subtitle of which only two lines will be displayed
     * @param description         dialog description of which only four lines will be displayed
     * @param negativeButtonTitle dialog negative/cancel button title
     * @return {@link FingerprintDialogCompatV23}
     */
    static FingerprintDialogCompatV23 createDialog(@NonNull String title,
                                                   @NonNull String subtitle,
                                                   @NonNull String description,
                                                   @NonNull String negativeButtonTitle) {
        FingerprintDialogCompatV23 dialog = new FingerprintDialogCompatV23();
        // set arguments
        Bundle bundle = new Bundle();
        bundle.putString(ARG_TITLE, title);
        bundle.putString(ARG_SUBTITLE, subtitle);
        bundle.putString(ARG_DESCRIPTION, description);
        bundle.putString(ARG_NEGATIVE_BUTTON_TITLE, negativeButtonTitle);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        LayoutInflater li = LayoutInflater.from(getContext());
        return li.inflate(R.layout.fingerprint_compat_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments == null) throw new IllegalStateException(getString(R.string.argument_error));

        // set title
        if (arguments.containsKey(ARG_TITLE)) {
            final AppCompatTextView titleTv = view.findViewById(R.id.title_tv);
            titleTv.setText(arguments.getString(ARG_TITLE));
            titleTv.setSelected(true);
        } else {
            throw new IllegalStateException(getString(R.string.title_error));
        }

        // set subtitle
        if (arguments.containsKey(ARG_SUBTITLE)) {
            final AppCompatTextView subtitleTv = view.findViewById(R.id.subtitle_tv);
            subtitleTv.setText(arguments.getString(ARG_SUBTITLE));
        } else {
            throw new IllegalStateException(getString(R.string.subtitle_error));
        }

        // set description
        if (arguments.containsKey(ARG_DESCRIPTION)) {
            final AppCompatTextView descriptionTv = view.findViewById(R.id.description_tv);
            descriptionTv.setText(arguments.getString(ARG_DESCRIPTION));
        } else {
            throw new IllegalStateException(getString(R.string.description_error));
        }

        // set negative/cancel button text
        final AppCompatButton button = view.findViewById(R.id.negative_btn);
        if (arguments.containsKey(ARG_NEGATIVE_BUTTON_TITLE)) {
            String text = arguments.getString(ARG_NEGATIVE_BUTTON_TITLE);
            button.setText(text);
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                // close dialog
                closeDialog();
            }
        });

        // set application drawable
        try {
            AppCompatImageView appIcon = view.findViewById(R.id.app_icon_iv);
            appIcon.setImageDrawable(getApplicationIcon(context));
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage());
        }

        // status text
        statusText = view.findViewById(R.id.fingerprint_status_tv);
    }

    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        if (window == null) return;

        // display dialog full screen width
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setLayout(getResources().getDisplayMetrics().widthPixels,
                WindowManager.LayoutParams.WRAP_CONTENT);

        // display at the bottom of screen
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.BOTTOM;
        wlp.windowAnimations = R.style.DialogAnimation;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);
    }

    @Override
    public void onResume() {
        super.onResume();

        // check if device has fingerprint supported hardware
        if (FingerprintUtils.hardwareSupported(context)) {
            // start fingerprint authentication
            startAuth();
        } else {
            callback.fingerprintAuthenticationNotSupported();
            closeDialog();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopAuthIfRunning();
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        stopAuthIfRunning();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopAuthIfRunning();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        // No call for super(). Bug on API Level > 11.
        // https://stackoverflow.com/a/10261449
    }

    /**
     * Set {@link AuthenticationCallback} for notifying fingerprint authentication status.
     * <p>
     * Application must call {@link #createDialog(String, String, String, String)}.
     *
     * @param callback {@link AuthenticationCallback}
     */
    public void setAuthenticationCallback(@NonNull final AuthenticationCallback callback) {
        this.callback = callback;
    }

    /**
     * Generate authentication key.
     *
     * @return true if key generated successfully
     */
    @TargetApi(Build.VERSION_CODES.M)
    private boolean generateKey() {
        keyStore = null;
        KeyGenerator keyGenerator;

        // get instance of the key store
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        } catch (NoSuchAlgorithmException | NoSuchProviderException | KeyStoreException e) {
            return false;
        }

        // generate key
        try {
            keyStore.load(null);
            keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();

            return true;
        } catch (NoSuchAlgorithmException
                | InvalidAlgorithmParameterException
                | CertificateException
                | IOException e) {
            return false;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Nullable
    private FingerprintManager.CryptoObject getCryptoObject() {
        return cipherInit() ? new FingerprintManager.CryptoObject(cipher) : null;
    }

    /**
     * Initialize cipher.
     *
     * @return true if initialization is successful
     */
    @TargetApi(Build.VERSION_CODES.M)
    private boolean cipherInit() {
        boolean isKeyGenerated = generateKey();

        if (!isKeyGenerated) return false;

        try {
            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException |
                NoSuchPaddingException e) {
            return false;
        }

        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME, null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (KeyStoreException | CertificateException
                | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            return false;
        }
    }

    /**
     * Start fingerprint authentication by enabling the finger print sensor.
     * <p>
     * Note: Use this function in the onResume() of the activity/fragment. Never forget to call
     * {@link #stopAuthIfRunning()} in onPause() of the activity/fragment.
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void startAuth() {
        if (isScanning) stopAuthIfRunning();
        final FingerprintManager fpm;
        fpm = (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);

        //Cannot access the fingerprint manager.
        if (fpm == null) {
            callback.fingerprintAuthenticationNotSupported();
            return;
        }

        //No fingerprint enrolled.
        if (!fpm.hasEnrolledFingerprints()) {
            callback.noEnrolledFingerprints();
            return;
        }

        final FingerprintManager.CryptoObject co = getCryptoObject();
        if (co != null) {
            final FingerprintManager.AuthenticationCallback authCallback = new FingerprintManager.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errMsgId, CharSequence errString) {
                    displayStatusText(errString.toString(), true);

                    switch (errMsgId) {
                        case FingerprintManager.FINGERPRINT_ERROR_CANCELED:
                        case FingerprintManager.FINGERPRINT_ERROR_USER_CANCELED:
                            callback.authenticationCanceledByUser();
                            break;
                        case FingerprintManager.FINGERPRINT_ERROR_HW_NOT_PRESENT:
                        case FingerprintManager.FINGERPRINT_ERROR_HW_UNAVAILABLE:
                            callback.fingerprintAuthenticationNotSupported();
                            break;
                        default:
                            callback.onAuthenticationError(errMsgId, errString);
                    }
                }

                @Override
                public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                    displayStatusText(helpString.toString(), false);
                    callback.onAuthenticationHelp(helpMsgId, helpString);
                }

                @Override
                public void onAuthenticationFailed() {
                    displayStatusText(getString(R.string.fingerprint_not_recognised), false);
                    callback.onAuthenticationFailed();
                }

                @Override
                public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                    callback.onAuthenticationSucceeded();
                    closeDialog();
                }
            };

            cancellationSignal = new CancellationSignal();

            Looper mainLooper = Looper.getMainLooper();
            Handler handler = new Handler(mainLooper);
            fpm.authenticate(co, cancellationSignal, 0, authCallback, handler);
        } else {
            //Cannot access the secure keystore.
            callback.fingerprintAuthenticationNotSupported();
            closeDialog();
        }
    }

    /**
     * Stop fingerprint authentication if running
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void stopAuthIfRunning() {
        if (statusTextRunnable != null) {
            new Handler().removeCallbacks(statusTextRunnable);
            statusTextRunnable = null;
        }

        if (cancellationSignal != null) {
            isScanning = false;
            cancellationSignal.cancel();
            cancellationSignal = null;
        }
    }

    private void closeDialog() {
        stopAuthIfRunning();
        dismiss();
    }

    /**
     * Display text in the {@link #statusText} for 1 second.
     *
     * @param status  status text to display
     * @param dismiss true if dialog should dismiss after status text displayed
     */
    private void displayStatusText(@NonNull final String status, final boolean dismiss) {
        statusText.setText(status);
        statusTextRunnable = new Runnable() {
            @Override
            public void run() {
                Dialog dialog = getDialog();
                if (dialog != null && dialog.isShowing()) {
                    statusText.setText("");
                    if (dismiss) closeDialog();
                }
            }
        };
        new Handler().postDelayed(statusTextRunnable, 1000);
    }

    /**
     * Get application icon.
     *
     * @param context {@link Context} caller context
     * @return {@link Drawable} application icon
     * @throws PackageManager.NameNotFoundException if package npt found.
     */
    @NonNull
    private Drawable getApplicationIcon(@NonNull final Context context) throws PackageManager.NameNotFoundException {
        try {
            return context.getPackageManager().getApplicationIcon(context.getPackageName());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            throw e;
        }
    }
}
