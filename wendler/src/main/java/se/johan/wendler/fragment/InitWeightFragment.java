package se.johan.wendler.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.doomonafireball.betterpickers.numberpicker.NumberPickerBuilder;
import com.doomonafireball.betterpickers.numberpicker.NumberPickerDialogFragment;

import se.johan.wendler.R;
import se.johan.wendler.R.id;
import se.johan.wendler.R.string;
import se.johan.wendler.animation.CustomObjectAnimator;
import se.johan.wendler.fragment.base.InitFragment;
import se.johan.wendler.sql.SqlHandler;
import se.johan.wendler.util.WendlerConstants;
import se.johan.wendler.util.WendlerMath;
import se.johan.wendler.ui.view.InitWeightView;

/**
 * This Fragment will let the user set the starting weights for the workouts.
 */
public class InitWeightFragment extends InitFragment implements
        OnClickListener, NumberPickerDialogFragment.NumberPickerDialogHandler {

    public static final String TAG = InitWeightFragment.class.getName();

    private static final String EXTRA_PERCENTAGE = "percentage";
    private static final String EXTRA_PRESS = "press";
    private static final String EXTRA_DEADLIFT = "deadlift";
    private static final String EXTRA_BENCH = "bench";
    private static final String EXTRA_SQUAT = "squat";

    private InitWeightView mInitPress;
    private InitWeightView mInitDeadlift;
    private InitWeightView mInitBench;
    private InitWeightView mInitSquat;

    private Button mPercentageButton;

    private int mWorkoutPercentage = WendlerConstants.DEFAULT_WORKOUT_PERCENTAGE;

    public InitWeightFragment() {
    }

    /**
     * Static creation to avoid problems on rotation.
     */
    public static InitWeightFragment newInstance() {
        return new InitWeightFragment();
    }

    /**
     * Called when the view is created.
     */
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_init_weight, container, false);

        mInitPress = (InitWeightView) view.findViewById(id.init_press);
        mInitDeadlift = (InitWeightView) view.findViewById(id.init_deadlift);
        mInitBench = (InitWeightView) view.findViewById(id.init_bench);
        mInitSquat = (InitWeightView) view.findViewById(id.init_squat);

        if (savedInstanceState != null) {
            mWorkoutPercentage = savedInstanceState.getInt(
                    EXTRA_PERCENTAGE, WendlerConstants.DEFAULT_WORKOUT_PERCENTAGE);
            mInitPress.setSavedInstance(savedInstanceState.getBundle(EXTRA_PRESS));
            mInitDeadlift.setSavedInstance(savedInstanceState.getBundle(EXTRA_DEADLIFT));
            mInitBench.setSavedInstance(savedInstanceState.getBundle(EXTRA_BENCH));
            mInitSquat.setSavedInstance(savedInstanceState.getBundle(EXTRA_SQUAT));
        }

        mPercentageButton = (Button) view.findViewById(id.btn_percentage);
        mPercentageButton.setOnClickListener(this);

        mPercentageButton.setText(
                String.format(getString(string.btn_percentage), "" + mWorkoutPercentage));

        return view;
    }

    /**
     * Called when the fragment is started.
     */
    @Override
    public void onStart() {
        super.onStart();
        mInitPress.restoreInstance();
        mInitDeadlift.restoreInstance();
        mInitBench.restoreInstance();
        mInitSquat.restoreInstance();
    }

    /**
     * Save the data entered in the view.
     */
    @Override
    public void saveData(SqlHandler handler) {
        handler.insertOneRmAndWorkoutPercentage(
                getTrainingMax(mInitPress.getOneRm()),
                getTrainingMax(mInitDeadlift.getOneRm()),
                getTrainingMax(mInitBench.getOneRm()),
                getTrainingMax(mInitSquat.getOneRm()),
                mWorkoutPercentage);
    }

    /**
     * Return if all entered data is ok.
     */
    @Override
    public boolean allDataIsOk() {
        return mInitPress.isDataOk()
                && mInitDeadlift.isDataOk()
                && mInitBench.isDataOk()
                && mInitSquat.isDataOk();
    }

    /**
     * Return the helping message of the view.
     */
    @Override
    public String getHelpingMessage() {
        return getString(R.string.help_weight_dialog);

    }

    /**
     * Return the helping message of the view.
     */
    @Override
    public int getHelpingMessageRes() {
        return R.string.help_weight_dialog;

    }

    /**
     * Notify the user of an input error.
     */
    @Override
    public void notifyError() {
        if (!mInitPress.isDataOk()) {
            CustomObjectAnimator.nope(mInitPress).start();
        } else if (!mInitDeadlift.isDataOk()) {
            CustomObjectAnimator.nope(mInitDeadlift).start();
        } else if (!mInitBench.isDataOk()) {
            CustomObjectAnimator.nope(mInitBench).start();
        } else {
            CustomObjectAnimator.nope(mInitSquat).start();
        }
    }

    /**
     * Called when a button is clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case id.btn_percentage:
                NumberPickerBuilder npb = new NumberPickerBuilder()
                        .setFragmentManager(getActivity().getSupportFragmentManager())
                        .setMaxNumber(100)
                        .setMinNumber(1)
                        .setTargetFragment(this)
                        .setStyleResId(R.style.BetterPickersDialogFragment_Light)
                        .setPlusMinusVisibility(View.GONE)
                        .setDecimalVisibility(View.GONE);
                npb.show();
                break;
        }
    }

    /**
     * Called when the fragment needs to save its' state.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_PERCENTAGE, mWorkoutPercentage);
        outState.putBundle(EXTRA_PRESS, mInitPress.getSavedInstance());
        outState.putBundle(EXTRA_DEADLIFT, mInitDeadlift.getSavedInstance());
        outState.putBundle(EXTRA_BENCH, mInitBench.getSavedInstance());
        outState.putBundle(EXTRA_SQUAT, mInitSquat.getSavedInstance());
    }

    /**
     * Called when we set a value in the number picker.
     */
    @Override
    public void onDialogNumberSet(
            int reference, int number, double decimal, boolean isNegative, double fullNumber) {
        mWorkoutPercentage = number;
        mPercentageButton.setText(
                String.format(getString(string.btn_percentage),
                        String.valueOf(mWorkoutPercentage))
        );
    }

    @Override
    public void onDialogDismissed() {

    }

    /**
     * Return the training max.
     */
    private double getTrainingMax(double oneRm) {
        return WendlerMath.calculateWeight(getActivity(), oneRm, mWorkoutPercentage);
    }
}
