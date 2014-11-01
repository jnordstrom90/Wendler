package se.johan.wendler.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;

import se.johan.wendler.R;
import se.johan.wendler.animation.CustomObjectAnimator;
import se.johan.wendler.sql.SqlHandler;
import se.johan.wendler.fragment.base.InitFragment;
import se.johan.wendler.ui.view.InitPercentView;
import se.johan.wendler.util.WendlerConstants;

/**
 * Initialize the percentages to be used for workouts.
 * TODO Consolidate with EditPercentageFragment
 */
public class InitPercentageFragment extends InitFragment implements
        OnCheckedChangeListener, InitPercentView.onTextChangedListener {

    public static final String TAG = InitPercentageFragment.class.getName();

    private static final String EXTRA_WEEK_ONE = "weekOne";
    private static final String EXTRA_WEEK_TWO = "weekTwo";
    private static final String EXTRA_WEEK_THREE = "weekThree";
    private static final String EXTRA_WEEK_FOUR = "weekFour";

    private RadioButton mButtonCustom;

    private InitPercentView mWeekOneView;
    private InitPercentView mWeekTwoView;
    private InitPercentView mWeekThreeView;
    private InitPercentView mWeekFourView;

    private Bundle mSavedInstanceState;

    public InitPercentageFragment() {
    }

    /**
     * Static creation to avoid problems on rotation.
     */
    public static InitPercentageFragment newInstance() {
        return new InitPercentageFragment();
    }

    /**
     * Called when the view is created.
     */
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_init_percentage, container, false);

        mWeekOneView = (InitPercentView) view.findViewById(R.id.init_one);
        mWeekTwoView = (InitPercentView) view.findViewById(R.id.init_two);
        mWeekThreeView = (InitPercentView) view.findViewById(R.id.init_three);
        mWeekFourView = (InitPercentView) view.findViewById(R.id.init_four);

        mWeekOneView.setTextChangedListener(this);
        mWeekTwoView.setTextChangedListener(this);
        mWeekThreeView.setTextChangedListener(this);
        mWeekFourView.setTextChangedListener(this);

        if (savedInstanceState == null) {
            insertFreshValues(true);
        } else {
            mSavedInstanceState = savedInstanceState;
        }

        RadioButton mButtonFresh = (RadioButton) view.findViewById(R.id.rbFresh);
        RadioButton mButtonHeavy = (RadioButton) view.findViewById(R.id.rbHeavy);
        mButtonCustom = (RadioButton) view.findViewById(R.id.rbCustom);

        mButtonFresh.setOnCheckedChangeListener(this);
        mButtonHeavy.setOnCheckedChangeListener(this);
        mButtonCustom.setOnCheckedChangeListener(this);

        return view;
    }

    /**
     * Called when the view is started.
     */
    @Override
    public void onStart() {
        super.onStart();
        if (mSavedInstanceState != null) {
            int[] weekOneV = mSavedInstanceState.getIntArray(EXTRA_WEEK_ONE);
            int[] weekTwoV = mSavedInstanceState.getIntArray(EXTRA_WEEK_TWO);
            int[] weekThreeV = mSavedInstanceState.getIntArray(EXTRA_WEEK_THREE);
            int[] weekFourV = mSavedInstanceState.getIntArray(EXTRA_WEEK_FOUR);
            mWeekOneView.insertValues(weekOneV, true);
            mWeekTwoView.insertValues(weekTwoV, true);
            mWeekThreeView.insertValues(weekThreeV, true);
            mWeekFourView.insertValues(weekFourV, true);
            mSavedInstanceState = null;
        }
    }

    /**
     * Check and insert data if it's valid
     */
    @Override
    public void saveData(SqlHandler handler) {
        handler.insertWeekPercentages(
                mWeekOneView.getPercentages(),
                mWeekTwoView.getPercentages(),
                mWeekThreeView.getPercentages(),
                mWeekFourView.getPercentages());
    }

    /**
     * Return if all data is ok to save.
     */
    @Override
    public boolean allDataIsOk() {
        return mWeekOneView.isDataOk()
                && mWeekTwoView.isDataOk()
                && mWeekThreeView.isDataOk()
                && mWeekFourView.isDataOk();
    }

    /**
     * Return the helping message for this view.
     */
    @Override
    public String getHelpingMessage() {
        return getString(R.string.help_percentage_dialog);
    }

    /**
     * Return the helping message of the view.
     */
    @Override
    public int getHelpingMessageRes() {
        return R.string.help_percentage_dialog;
    }

    /**
     * Notify the user of an invalid input.
     */
    @Override
    public void notifyError() {
        if (!mWeekOneView.isDataOk()) {
            CustomObjectAnimator.nope(mWeekOneView).start();
        } else if (!mWeekTwoView.isDataOk()) {
            CustomObjectAnimator.nope(mWeekTwoView).start();
        } else if (!mWeekThreeView.isDataOk()) {
            CustomObjectAnimator.nope(mWeekThreeView).start();
        } else {
            CustomObjectAnimator.nope(mWeekFourView).start();
        }
    }

    /**
     * Called when a RadioButton is checked.
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            switch (buttonView.getId()) {
                case R.id.rbFresh:
                    insertFreshValues(false);
                    break;
                case R.id.rbHeavy:
                    insertHeavyValues();
                    break;
            }
        }
    }

    /**
     * Called when an EditText is edited.
     */
    @Override
    public void onTextChanged() {
        mButtonCustom.setChecked(true);
    }

    /**
     * Called when the state needs to be saved.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (allDataIsOk()) {
            outState.putIntArray(EXTRA_WEEK_ONE, mWeekOneView.getPercentages());
            outState.putIntArray(EXTRA_WEEK_TWO, mWeekTwoView.getPercentages());
            outState.putIntArray(EXTRA_WEEK_THREE, mWeekThreeView.getPercentages());
            outState.putIntArray(EXTRA_WEEK_FOUR, mWeekFourView.getPercentages());
        }
    }

    /**
     * Insert the heavy values into the EditTexts
     */
    private void insertHeavyValues() {
        mWeekOneView.insertValues(WendlerConstants.HEAVY_PERCENTAGES_W_1, false);
        mWeekTwoView.insertValues(WendlerConstants.HEAVY_PERCENTAGES_W_2, false);
        mWeekThreeView.insertValues(WendlerConstants.HEAVY_PERCENTAGES_W_3, false);
        mWeekFourView.insertValues(WendlerConstants.HEAVY_PERCENTAGES_W_4, false);
    }

    /**
     * Insert the fresh values into the EditTexts
     */
    private void insertFreshValues(boolean insertTextWatcher) {
        mWeekOneView.insertValues(WendlerConstants.FRESH_PERCENTAGES_W_1, insertTextWatcher);
        mWeekTwoView.insertValues(WendlerConstants.FRESH_PERCENTAGES_W_2, insertTextWatcher);
        mWeekThreeView.insertValues(WendlerConstants.FRESH_PERCENTAGES_W_3, insertTextWatcher);
        mWeekFourView.insertValues(WendlerConstants.FRESH_PERCENTAGES_W_4, insertTextWatcher);
    }
}
