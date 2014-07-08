package se.johan.wendler.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import se.johan.wendler.dialog.base.AnimationDialog;

/**
 * Confirmation dialog used for various places.
 */
public class ConfirmationDialog extends AnimationDialog implements OnClickListener {

    public static final String TAG = ConfirmationDialog.class.getName();

    private static final String EXTRA_BTN_POSITIVE = "buttonPositive";
    private static final String EXTRA_BTN_NEGATIVE = "buttonNegative";
    private static final String EXTRA_TITLE = "title";
    private static final String EXTRA_MESSAGE = "message";

    public ConfirmationDialog() {
    }

    /**
     * Static creation to avoid problems on rotation.
     */
    public static ConfirmationDialog newInstance(String message,
                                                 String title,
                                                 String positiveText,
                                                 String negativeText,
                                                 Fragment targetFragment) {
        ConfirmationDialog dialog = new ConfirmationDialog();

        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_TITLE, title);
        bundle.putString(EXTRA_MESSAGE, message);
        bundle.putString(EXTRA_BTN_POSITIVE, positiveText);
        bundle.putString(EXTRA_BTN_NEGATIVE, negativeText);
        dialog.setArguments(bundle);

        if (targetFragment != null) {
            dialog.setTargetFragment(targetFragment, 1234);
        }

        return dialog;
    }

    /**
     * Called when the dialog is about to be created.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
        Bundle bundle = getArguments();
        mBuilder.setTitle(bundle.getString(EXTRA_TITLE))
                .setMessage(bundle.getString(EXTRA_MESSAGE)).
                setPositiveButton(bundle.getString(EXTRA_BTN_POSITIVE), this);

        String negativeText = bundle.getString(EXTRA_BTN_NEGATIVE);
        if (!TextUtils.isEmpty(negativeText)) {
            mBuilder.setNegativeButton(bundle.getString(EXTRA_BTN_NEGATIVE), this);
        }

        return mBuilder.create();
    }

    /**
     * Called when the dialog is attached to an activity.
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (getListener() == null) {
            throw new ClassCastException("Class doesn't implement onConfirmClickedListener");
        }
    }

    /**
     * Called when one of the buttons in the dialog is pressed.
     */
    @Override
    public void onClick(DialogInterface dialog, int which) {
        getListener().onDialogConfirmed(which == android.content.DialogInterface.BUTTON_POSITIVE);
    }

    /**
     * Return the listener attached to the dialog.
     */
    private ConfirmationDialogListener getListener() {
        if (getActivity() instanceof ConfirmationDialogListener) {
            return (ConfirmationDialogListener) getActivity();
        } else if (getTargetFragment() instanceof ConfirmationDialogListener) {
            return (ConfirmationDialogListener) getTargetFragment();
        }

        return null;
    }

    /**
     * Interface for listening for confirmations from the dialog.
     */
    public interface ConfirmationDialogListener {

        /**
         * Called when a button is pressed.
         */
        public void onDialogConfirmed(boolean confirmed);
    }
}
