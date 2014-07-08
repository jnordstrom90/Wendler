package se.johan.wendler.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import se.johan.wendler.R;
import se.johan.wendler.util.GenericTextWatcher;

/**
 * InitWeightView
 */
public class InitWeightView extends RelativeLayout {

    private static final String WEIGHT = "mWeight";
    private static final String REPS = "reps";
    private static final String ONE_RM = "oneRm";

    private FilterEditText mEditTextWeight;
    private FilterEditText mEditTextReps;
    private FilterEditText mEditTextOneRm;
    private Bundle savedInstance;

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

    private void init(Context context, AttributeSet attrs) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.init_weight_view, this, true);

        TypedArray attr = context.obtainStyledAttributes(attrs, R.styleable.InitWeightViewTitle);
        String title = attr.getString(R.styleable.InitWeightViewTitle_title);
        attr.recycle();

        ((TextView) findViewById(R.id.tv_title)).setText(title);

        mEditTextWeight = (FilterEditText) findViewById(R.id.edit_text_weight);
        mEditTextReps = (FilterEditText) findViewById(R.id.edit_text_reps);
        mEditTextOneRm = (FilterEditText) findViewById(R.id.edit_text_one_rm);

        setGenericTextWatcher(mEditTextWeight, mEditTextReps, mEditTextOneRm);


    }

    private void setGenericTextWatcher(FilterEditText weight,
                                       FilterEditText reps,
                                       FilterEditText oneRm) {
        weight.addTextChangedListener(new GenericTextWatcher(weight, reps, oneRm));
        reps.addTextChangedListener(new GenericTextWatcher(weight, reps, oneRm));
    }

    private int getText(FilterEditText editText) {
        if (editText.getText().toString().trim().length() > 0) {
            Double d = Double.parseDouble(editText.getText().toString());
            return d.intValue();
        }
        return -1;
    }

    public int getOneRm() {
        Double d = Double.parseDouble(mEditTextOneRm.getText().toString());
        return d.intValue();
    }

    public boolean isDataOk() {
        return mEditTextOneRm.getText().toString().trim().length() > 0
                && getText(mEditTextOneRm) > 0;
    }

    /**
     * Workaround for nasty bug. When restoring instance of several custom views they'd all copy
     * the instance of the last one. Instead update each individually
     */
    public void restoreInstance() {
        if (savedInstance != null) {
            if (savedInstance.containsKey(WEIGHT)) {
                mEditTextWeight.setText("" + savedInstance.getInt(WEIGHT));
            }

            if (savedInstance.containsKey(REPS)) {
                mEditTextReps.setText("" + savedInstance.getInt(REPS));
            }

            if (savedInstance.containsKey(ONE_RM)) {
                mEditTextOneRm.setText("" + savedInstance.getInt(ONE_RM));
            }
        }
    }

    public void setSavedInstance(Bundle savedInstance) {
        this.savedInstance = savedInstance;
    }

    public Bundle getSavedInstance() {
        Bundle bundle = new Bundle();
        int val = getText(mEditTextOneRm);

        if (val > 0) {
            bundle.putInt(ONE_RM, val);
        }

        val = getText(mEditTextReps);

        if (val > 0) {
            bundle.putInt(REPS, val);
        }

        val = getText(mEditTextWeight);

        if (val > 0) {
            bundle.putInt(WEIGHT, val);
        }
        return bundle;
    }
}
