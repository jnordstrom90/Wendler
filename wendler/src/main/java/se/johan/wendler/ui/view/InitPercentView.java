package se.johan.wendler.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import se.johan.wendler.R;
import se.johan.wendler.util.Util;

/**
 * View for setting up percentages.
 */
@SuppressWarnings("UnusedDeclaration")
public class InitPercentView extends RelativeLayout implements TextWatcher {
    private FilterEditText mEtSetOne;
    private FilterEditText mEtSetTwo;
    private FilterEditText mEtSetThree;

    private onTextChangedListener mListener;

    /**
     * Constructor.
     */
    public InitPercentView(Context context) {
        super(context);
    }

    /**
     * Constructor.
     */
    public InitPercentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initLayout(context, attrs);
    }

    /**
     * Constructor.
     */
    public InitPercentView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initLayout(context, attrs);
    }

    /**
     * Initialize the layout.
     */
    private void initLayout(Context context, AttributeSet attrs) {
        LayoutInflater inflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_init_percentage, this, true);

        TypedArray attr = context.obtainStyledAttributes(attrs, R.styleable.InitWeightViewTitle);
        String title = attr.getString(R.styleable.InitWeightViewTitle_weightTitle);
        attr.recycle();

        ((TextView) findViewById(R.id.tv_title)).setText(title);

        mEtSetOne = (FilterEditText) findViewById(R.id.et_set_one);
        mEtSetTwo = (FilterEditText) findViewById(R.id.et_set_two);
        mEtSetThree = (FilterEditText) findViewById(R.id.et_set_three);
    }

    /**
     * Set the listener for the text changes.
     */
    public void setTextChangedListener(onTextChangedListener listener) {
        mListener = listener;
    }

    /**
     * Called before the text is changed.
     */
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    /**
     * Called when the text is changed.
     */
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (mListener != null) {
            mListener.onTextChanged();
        }
    }

    /**
     * called after the text is changed.
     */
    @Override
    public void afterTextChanged(Editable s) {

    }

    /**
     * Insert values to the text fields and add a TextWatcher if needed.
     */
    public void insertValues(int[] values, boolean addTextWatcher) {
        mEtSetOne.setText(String.valueOf(values[0]));
        mEtSetTwo.setText(String.valueOf(values[1]));
        mEtSetThree.setText(String.valueOf(values[2]));

        if (addTextWatcher) {
            mEtSetOne.addTextChangedListener(this);
            mEtSetTwo.addTextChangedListener(this);
            mEtSetThree.addTextChangedListener(this);
        }
    }

    /**
     * Return if all data is ok to be saved.
     */
    public boolean isDataOk() {
        return mEtSetOne.getText().toString().trim().length() > 0
                && mEtSetTwo.getText().toString().trim().length() > 0
                && mEtSetThree.getText().toString().trim().length() > 0;
    }

    /**
     * Return the percentages entered in the views.
     */
    public int[] getPercentages() {
        return new int[]{
                Util.getIntFromEditText(mEtSetOne),
                Util.getIntFromEditText(mEtSetTwo),
                Util.getIntFromEditText(mEtSetThree)};
    }


    /**
     * Interface for listening for text input.
     */
    public interface onTextChangedListener {

        /**
         * Called when text has been altered.
         */
        public void onTextChanged();
    }
}
