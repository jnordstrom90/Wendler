package se.johan.wendler.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import se.johan.wendler.R;
import se.johan.wendler.util.GenericTextWatcher;
import se.johan.wendler.util.Util;

/**
 * View for entering and calculating weights.
 */
@SuppressWarnings("ALL")
public class InitWeightView extends RelativeLayout {

    private static final String EXTRA_WEIGHT_VALUE = "weight";
    private static final String EXTRA_REPS_VALUE = "reps";
    private static final String EXTRA_ONE_RM_VALUE = "oneRm";

    private FilterEditText mEditTextWeight;
    private FilterEditText mEditTextReps;
    private FilterEditText mEditTextOneRm;
    private Bundle mSavedInstanceState;

    public InitWeightView(Context context, String title) {
        super(context);
    }

    public InitWeightView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public InitWeightView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    /**
     * Initialize the view.
     */
    private void init(Context context, AttributeSet attrs) {

        LayoutInflater inflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_init_weight, this, true);

        TypedArray attr = context.obtainStyledAttributes(attrs, R.styleable.InitWeightViewTitle);
        String title = attr.getString(R.styleable.InitWeightViewTitle_weightTitle);
        attr.recycle();

        ((TextView) findViewById(R.id.tv_title)).setText(title);

        mEditTextWeight = (FilterEditText) findViewById(R.id.edit_text_weight);
        mEditTextReps = (FilterEditText) findViewById(R.id.edit_text_reps);
        mEditTextOneRm = (FilterEditText) findViewById(R.id.edit_text_one_rm);

        setGenericTextWatcher(mEditTextWeight, mEditTextReps, mEditTextOneRm);
    }

    /**
     * Return the one rm.
     */
    public double getOneRm() {
        return Double.parseDouble(mEditTextOneRm.getText().toString());
    }

    /**
     * Return if the data is ok.
     */
    public boolean isDataOk() {
        return mEditTextOneRm.getText().toString().trim().length() > 0
                && Util.getDoubleFromEditText(mEditTextOneRm) > 0;
    }

    /**
     * Workaround for nasty bug. When restoring instance of several custom views they'd all copy
     * the instance of the last one. Instead update each individually
     */
    public void restoreInstance() {
        if (mSavedInstanceState != null) {
            if (mSavedInstanceState.containsKey(EXTRA_WEIGHT_VALUE)) {
                mEditTextWeight.setText(
                        String.valueOf(mSavedInstanceState.getDouble(EXTRA_WEIGHT_VALUE)));
            }

            if (mSavedInstanceState.containsKey(EXTRA_REPS_VALUE)) {
                mEditTextReps.setText(
                        String.valueOf(mSavedInstanceState.getInt(EXTRA_REPS_VALUE)));
            }

            if (mSavedInstanceState.containsKey(EXTRA_ONE_RM_VALUE)) {
                mEditTextOneRm.setText(
                        String.valueOf(mSavedInstanceState.getDouble(EXTRA_ONE_RM_VALUE)));
            }
        }
    }

    /**
     * Set the saved instance state.
     */
    public void setSavedInstance(Bundle savedInstance) {
        mSavedInstanceState = savedInstance;
    }

    /**
     * Return the instance state to save.
     */
    public Bundle getSavedInstance() {
        Bundle bundle = new Bundle();
        double val = Util.getDoubleFromEditText(mEditTextOneRm);

        if (val > 0) {
            bundle.putDouble(EXTRA_ONE_RM_VALUE, val);
        }

        int intVal = Util.getIntFromEditText(mEditTextReps);

        if (intVal > 0) {
            bundle.putInt(EXTRA_REPS_VALUE, intVal);
        }

        val = Util.getDoubleFromEditText(mEditTextOneRm);

        if (val > 0) {
            bundle.putDouble(EXTRA_WEIGHT_VALUE, val);
        }
        return bundle;
    }

    /**
     * Set the text watcher for the needed views.
     */
    private void setGenericTextWatcher(FilterEditText weight,
                                       FilterEditText reps,
                                       FilterEditText oneRm) {
        weight.addTextChangedListener(new GenericTextWatcher(weight, reps, oneRm));
        reps.addTextChangedListener(new GenericTextWatcher(weight, reps, oneRm));
    }
}
