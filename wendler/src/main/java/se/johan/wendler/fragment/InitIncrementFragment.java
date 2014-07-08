package se.johan.wendler.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import se.johan.wendler.R.id;
import se.johan.wendler.R.layout;
import se.johan.wendler.R.string;
import se.johan.wendler.animation.CustomObjectAnimator;
import se.johan.wendler.fragment.base.InitFragment;
import se.johan.wendler.sql.SqlHandler;
import se.johan.wendler.util.Util;
import se.johan.wendler.util.WendlerConstants;
import se.johan.wendler.view.FilterEditText;

/**
 * Initialize the increment for each exercise.
 */
public class InitIncrementFragment extends InitFragment {

    public static final String TAG = InitIncrementFragment.class.getName();

    private FilterEditText mPressIncrement;
    private FilterEditText mDeadliftIncrement;
    private FilterEditText mBenchIncrement;
    private FilterEditText mSquatIncrement;

    public InitIncrementFragment() {
    }

    /**
     * Static creation to avoid problems on rotation.
     */
    public static InitIncrementFragment newInstance() {
        return new InitIncrementFragment();
    }

    /**
     * Called when the view is created.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(layout.init_increment_layout, container, false);

        mPressIncrement = (FilterEditText) view.findViewById(id.et_increment_press);
        mDeadliftIncrement = (FilterEditText) view.findViewById(id.et_increment_deadlift);
        mBenchIncrement = (FilterEditText) view.findViewById(id.et_increment_bench);
        mSquatIncrement = (FilterEditText) view.findViewById(id.et_increment_squat);

        mPressIncrement.setText(String.valueOf(WendlerConstants.DEFAULT_PRESS_INCREMENT));
        mBenchIncrement.setText(String.valueOf(WendlerConstants.DEFAULT_BENCH_INCREMENT));

        mDeadliftIncrement.setText(String.valueOf(WendlerConstants.DEFAULT_DEADLIFT_INCREMENT));
        mSquatIncrement.setText(String.valueOf(WendlerConstants.DEFAULT_SQUAT_INCREMENT));

        return view;
    }

    /**
     * Save needed data to the database.
     */
    @Override
    public void saveData(SqlHandler handler) {
        double pressIncrementVal = Util.getDoubleFromEditText(mPressIncrement);
        double deadliftIncrementVal = Util.getDoubleFromEditText(mDeadliftIncrement);
        double benchIncrementVal = Util.getDoubleFromEditText(mBenchIncrement);
        double squatIncrementVal = Util.getDoubleFromEditText(mSquatIncrement);
        handler.insertIncrements(
                pressIncrementVal,
                deadliftIncrementVal,
                benchIncrementVal,
                squatIncrementVal);
    }

    /**
     * Return so that all data has valid values.
     */
    @Override
    public boolean allDataIsOk() {
        return isDataOk(mPressIncrement)
                && isDataOk(mDeadliftIncrement)
                && isDataOk(mBenchIncrement)
                && isDataOk(mSquatIncrement);
    }

    /**
     * Return the helping message for this fragment.
     */
    @Override
    public String getHelpingMessage() {
        return getString(string.help_increment_dialog);
    }

    /**
     * Notify the user of an error in one of the input fields.
     */
    @Override
    public void notifyError() {
        if (!isDataOk(mPressIncrement)) {
            CustomObjectAnimator.nope(mPressIncrement).start();
        } else if (!isDataOk(mDeadliftIncrement)) {
            CustomObjectAnimator.nope(mDeadliftIncrement).start();
        } else if (!isDataOk(mBenchIncrement)) {
            CustomObjectAnimator.nope(mBenchIncrement).start();
        } else {
            CustomObjectAnimator.nope(mSquatIncrement).start();
        }
    }

    /**
     * Return if data entered into an EditText is valid.
     */
    private boolean isDataOk(EditText editText) {
        return editText.getText() != null
                && editText.getText().toString().trim().length() > 0;
    }
}
