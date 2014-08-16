package se.johan.wendler.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import se.johan.wendler.R;
import se.johan.wendler.dialog.base.AnimationDialog;
import se.johan.wendler.util.Util;

/**
 * Dialog containing an EditText
 */
public class EditTextDialog extends AnimationDialog implements DialogInterface.OnClickListener {

    private static final String EXTRA_TITLE = "title";
    private static final String EXTRA_INPUT = "input";

    public static final String TAG = EditTextDialog.class.getName();
    private EditText mInput;

    public EditTextDialog() {
    }

    /**
     * Static creation to avoid problems on rotation.
     */
    public static EditTextDialog newInstance(String title, String notes) {
        EditTextDialog dialog = new EditTextDialog();
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_TITLE, title);
        bundle.putString(EXTRA_INPUT, notes);
        dialog.setArguments(bundle);
        return dialog;
    }

    @NonNull
    @Override
    @SuppressLint("InflateParams")
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.dialog_edit_text, null, false);
        mInput = (EditText) view.findViewById(R.id.editText);
        mInput.setText(getArguments().getString(EXTRA_INPUT, ""));
        mInput.setSelection(mInput.getText().length());

        return new AlertDialog.Builder(getActivity())
                .setPositiveButton(getString(R.string.btn_ok), this)
                .setNegativeButton(getString(R.string.btn_cancel), null)
                .setView(view)
                .setTitle(getArguments().getString(EXTRA_TITLE))
                .create();
    }

    /**
     * Called when the dialog is attached to the activity.
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (getListener() == null) {
            throw new ClassCastException("Class doesn't implement getTextFromDialog");
        }
    }

    /**
     * Called when one of the dialog buttons are clicked.
     */
    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case Dialog.BUTTON_POSITIVE:
                getListener().getTextFromDialog(mInput.getText().toString().trim());
                Util.hideKeyboard(getActivity());
                break;
            default:
                break;
        }
    }

    /**
     * Return the attached listener for this dialog.
     */
    private EditTextListener getListener() {
        if (getActivity() instanceof EditTextListener) {
            return (EditTextListener) getActivity();
        } else if (getTargetFragment() instanceof EditTextListener) {
            return (EditTextListener) getTargetFragment();
        }
        return null;
    }

    /**
     * Interface to listen for callbacks from the dialog.
     */
    public interface EditTextListener {

        /**
         * Called when the positive dialog button is pressed and the new text is returned.
         */
        public void getTextFromDialog(String text);
    }
}
