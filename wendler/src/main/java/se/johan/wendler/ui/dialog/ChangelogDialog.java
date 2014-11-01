package se.johan.wendler.ui.dialog;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import se.johan.wendler.R;
import se.johan.wendler.ui.dialog.base.AnimationDialog;

/**
 * Dialog for displaying the changelog.
 */
public class ChangelogDialog extends AnimationDialog {

    public static final String TAG = ChangelogDialog.class.getName();

    public ChangelogDialog() {
    }

    /**
     * Static creation to avoid problems on rotation.
     */
    public static ChangelogDialog newInstance() {
        return new ChangelogDialog();
    }

    /**
     * Called when the dialog is created.
     */
    @NonNull
    @Override
    @SuppressLint("InflateParams")
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_changelog, null, false);
        ((TextView) view.findViewById(R.id.title)).setText(getString(R.string.change_log));

        view.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .create();
    }
}
