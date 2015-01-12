package se.johan.wendler.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.sql.SQLException;

import se.johan.wendler.R;
import se.johan.wendler.fragment.base.EditFragment;
import se.johan.wendler.sql.SqlHandler;
import se.johan.wendler.util.Constants;
import se.johan.wendler.util.PreferenceUtil;
import se.johan.wendler.util.Utils;
import se.johan.wendler.util.WendlerConstants;
import se.johan.wendler.util.WendlerizedLog;
import se.johan.wendler.ui.view.FilterEditText;

/**
 * Edit fragment where misc settings can be changed.
 */
public class EditOtherFragment extends EditFragment {

    private FilterEditText mOneRmPress;
    private FilterEditText mOneRmDeadlift;
    private FilterEditText mOneRmBench;
    private FilterEditText mOneRmSquat;

    private FilterEditText mIncrementPress;
    private FilterEditText mIncrementDeadlift;
    private FilterEditText mIncrementBench;
    private FilterEditText mIncrementSquat;

    private FilterEditText mDeloadSetOne;
    private FilterEditText mDeloadSetTwo;
    private FilterEditText mDeloadSetThree;

    public EditOtherFragment() {
    }

    /**
     * Static creation to avoid problems on rotation.
     */
    public static EditOtherFragment newInstance() {
        return new EditOtherFragment();
    }

    /**
     * Called when the view is created.
     */
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_edit_other, container, false);
        initViews(view);
        initValues();

        return view;
    }

    /**
     * Called to save the new data.
     */
    @Override
    public void saveData(SqlHandler handler) {

        if (isIncrementOk()) {
            handler.insertIncrements(
                    Utils.getDoubleFromEditText(mIncrementPress),
                    Utils.getDoubleFromEditText(mIncrementDeadlift),
                    Utils.getDoubleFromEditText(mIncrementBench),
                    Utils.getDoubleFromEditText(mIncrementSquat));
        }

        if (isOneRmOk()) {
            handler.updateOneRm(
                    Utils.getDoubleFromEditText(mOneRmPress),
                    Utils.getDoubleFromEditText(mOneRmDeadlift),
                    Utils.getDoubleFromEditText(mOneRmBench),
                    Utils.getDoubleFromEditText(mOneRmSquat));
        }


        if (isDeloadOk() && isAdded()) {
            PreferenceUtil.putString(
                    getActivity(),
                    PreferenceUtil.KEY_DELOAD_REPS,
                    mDeloadSetOne.getText() + ","
                            + mDeloadSetTwo.getText() + ","
                             + mDeloadSetThree.getText());
        }
    }

    /**
     * Return if all the deload fields has proper values.
     */
    private boolean isDeloadOk() {
        return mDeloadSetOne.getText().toString().trim().length() > 0
                && mDeloadSetTwo.getText().toString().trim().length() > 0
                && mDeloadSetThree.getText().toString().trim().length() > 0;
    }

    /**
     * Return if all the one rm fields has proper values.
     */
    private boolean isOneRmOk() {
        return mOneRmPress.getText().toString().trim().length() > 0
                && mOneRmDeadlift.getText().toString().trim().length() > 0
                && mOneRmBench.getText().toString().trim().length() > 0
                && mOneRmSquat.getText().toString().trim().length() > 0;
    }

    /**
     * Return if all the increment fields has proper values.
     */
    private boolean isIncrementOk() {
        return mIncrementPress.getText().toString().trim().length() > 0
                && mIncrementDeadlift.getText().toString().trim().length() > 0
                && mIncrementBench.getText().toString().trim().length() > 0
                && mIncrementSquat.getText().toString().trim().length() > 0;
    }

    /**
     * Initialize our views.
     */
    private void initViews(View view) {
        mOneRmPress = (FilterEditText) view.findViewById(R.id.et_one_rm_press);
        mOneRmDeadlift = (FilterEditText) view.findViewById(R.id.et_one_rm_deadlift);
        mOneRmBench = (FilterEditText) view.findViewById(R.id.et_one_rm_bench);
        mOneRmSquat = (FilterEditText) view.findViewById(R.id.et_one_rm_squat);

        mIncrementPress = (FilterEditText) view.findViewById(R.id.et_increment_press);
        mIncrementDeadlift = (FilterEditText) view.findViewById(R.id.et_increment_deadlift);
        mIncrementBench = (FilterEditText) view.findViewById(R.id.et_increment_bench);
        mIncrementSquat = (FilterEditText) view.findViewById(R.id.et_increment_squat);

        mDeloadSetOne = (FilterEditText) view.findViewById(R.id.et_deload_set_one);
        mDeloadSetTwo = (FilterEditText) view.findViewById(R.id.et_deload_set_two);
        mDeloadSetThree = (FilterEditText) view.findViewById(R.id.et_deload_set_three);
    }

    /**
     * Initialize our values.
     */
    private void initValues() {
        SqlHandler sqlHandler = new SqlHandler(getActivity());

        try {
            sqlHandler.open();

            mOneRmPress.setText("" +
                    sqlHandler.getOneRmForExercise(Constants.EXERCISES[0]));
            mOneRmDeadlift.setText("" +
                    sqlHandler.getOneRmForExercise(Constants.EXERCISES[1]));
            mOneRmBench.setText("" +
                    sqlHandler.getOneRmForExercise(Constants.EXERCISES[2]));
            mOneRmSquat.setText("" +
                    sqlHandler.getOneRmForExercise(Constants.EXERCISES[3]));

            mIncrementPress.setText("" +
                    sqlHandler.getIncrement(Constants.EXERCISES[0]));
            mIncrementDeadlift.setText("" +
                    sqlHandler.getIncrement(Constants.EXERCISES[1]));
            mIncrementBench.setText("" +
                    sqlHandler.getIncrement(Constants.EXERCISES[2]));
            mIncrementSquat.setText("" +
                    sqlHandler.getIncrement(Constants.EXERCISES[3]));

        } catch (SQLException e) {
            WendlerizedLog.e("Failed to initialize values", e);
        } finally {
            sqlHandler.close();
        }

        String repsAsString = PreferenceUtil.getString(
                getActivity(),
                PreferenceUtil.KEY_DELOAD_REPS,
                WendlerConstants.DEFAULT_DELOAD_REPS);
        String[] repsAsArray = TextUtils.split(repsAsString, ",");
        mDeloadSetOne.setText(repsAsArray[0]);
        mDeloadSetTwo.setText(repsAsArray[1]);
        mDeloadSetThree.setText(repsAsArray[2]);
    }
}
