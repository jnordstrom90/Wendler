package se.johan.wendler.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * TextWatcher which handles the 3 EditTexts which will calculate the One rep max based
 * on Wendler's formula.
 */
public class GenericTextWatcher implements TextWatcher {

    private final EditText mWeight;
    private final EditText mReps;
    private final EditText mOneRm;

    /**
     * Constructor.
     */
    public GenericTextWatcher(EditText weight, EditText reps, EditText oneRm) {
        mWeight = weight;
        mReps = reps;
        mOneRm = oneRm;
    }

    /**
     * Called before the text is changed.
     */
    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

    }

    /**
     * Called when the text is changed.
     */
    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

    }

    /**
     * Called after the text is changed.
     */
    @Override
    public void afterTextChanged(Editable editable) {
        try {
            if (mWeight.getText().toString().trim().length() > 0
                    && mReps.getText().toString().trim().length() > 0) {
                int oneRm = WendlerMath.calculateOneRm(
                        Double.parseDouble(mWeight.getText().toString()),
                        Integer.parseInt(mReps.getText().toString()));

                mOneRm.setText(String.valueOf(oneRm));
            }
        } catch (NumberFormatException e) {
            // Sometimes they still are empty
        }
    }
}
