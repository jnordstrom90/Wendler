package se.johan.wendler.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import java.sql.SQLException;

import se.johan.wendler.R;
import se.johan.wendler.fragment.base.EditFragment;
import se.johan.wendler.sql.SqlHandler;
import se.johan.wendler.util.WendlerConstants;
import se.johan.wendler.util.WendlerMath;
import se.johan.wendler.util.WendlerizedLog;
import se.johan.wendler.view.InitPercentView;

/**
 * Edit fragment where the percentages can be changed.
 */
public class EditPercentageFragment extends EditFragment implements
        InitPercentView.onTextChangedListener, CompoundButton.OnCheckedChangeListener {

    private static final String EXTRA_WEEK_ONE = "weekOne";
    private static final String EXTRA_WEEK_TWO = "weekTwo";
    private static final String EXTRA_WEEK_THREE = "weekThree";
    private static final String EXTRA_WEEK_FOUR = "weekFour";

    private InitPercentView mWeekOneView;
    private InitPercentView mWeekTwoView;
    private InitPercentView mWeekThreeView;
    private InitPercentView mWeekFourView;

    private RadioButton mButtonCustom;

    private Bundle mSavedInstanceState;

    public EditPercentageFragment() {
    }

    /**
     * Static creation to avoid problems on rotation.
     */
    public static EditPercentageFragment newInstance() {
        return new EditPercentageFragment();
    }

    /**
     * Called when the view is created.
     */
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.edit_percentage_layout, container, false);

        mWeekOneView = (InitPercentView) view.findViewById(R.id.init_one);
        mWeekTwoView = (InitPercentView) view.findViewById(R.id.init_two);
        mWeekThreeView = (InitPercentView) view.findViewById(R.id.init_three);
        mWeekFourView = (InitPercentView) view.findViewById(R.id.init_four);

        mWeekOneView.setTextChangedListener(this);
        mWeekTwoView.setTextChangedListener(this);
        mWeekThreeView.setTextChangedListener(this);
        mWeekFourView.setTextChangedListener(this);

        RadioButton buttonFresh = (RadioButton) view.findViewById(R.id.rbFresh);
        RadioButton buttonHeavy = (RadioButton) view.findViewById(R.id.rbHeavy);
        mButtonCustom = (RadioButton) view.findViewById(R.id.rbCustom);

        buttonFresh.setOnCheckedChangeListener(this);
        buttonHeavy.setOnCheckedChangeListener(this);
        mButtonCustom.setOnCheckedChangeListener(this);

        if (savedInstanceState == null) {
            insertCurrentValues(buttonHeavy);
        } else {
            mSavedInstanceState = savedInstanceState;
        }

        return view;
    }

    /**
     * Called when a RadioButton has been checked.
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            switch (buttonView.getId()) {
                case R.id.rbFresh:
                    insertFreshValues();
                    break;
                case R.id.rbHeavy:
                    insertHeavyValues();
                    break;
            }
        }
    }

    /**
     * Called when a EditText has been edited.
     */
    @Override
    public void onTextChanged() {
        mButtonCustom.setChecked(true);
    }

    /**
     * Called when the fragment is started.
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
     * Called when the fragment needs to save it's instance state.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putIntArray(EXTRA_WEEK_ONE, mWeekOneView.getPercentages());
        outState.putIntArray(EXTRA_WEEK_TWO, mWeekTwoView.getPercentages());
        outState.putIntArray(EXTRA_WEEK_THREE, mWeekThreeView.getPercentages());
        outState.putIntArray(EXTRA_WEEK_FOUR, mWeekFourView.getPercentages());
    }

    /**
     * Called when we need to save data.
     */
    @Override
    public void saveData(SqlHandler handler) {
        if (allDataIsOk()) {
            handler.updatePercentages(
                    mWeekOneView.getPercentages(),
                    mWeekTwoView.getPercentages() ,
                    mWeekThreeView.getPercentages() ,
                    mWeekFourView.getPercentages());
        }
    }

    /*'
    Return if all data is ok.
     */
    private boolean allDataIsOk() {
        return mWeekOneView.isDataOk()
                && mWeekTwoView.isDataOk()
                && mWeekThreeView.isDataOk()
                && mWeekFourView.isDataOk();
    }

    /**
     * Insert values into the EditTexts.
     */
    private void insertCurrentValues(RadioButton buttonHeavy) {
        SqlHandler sql = new SqlHandler(getActivity());
        try {
            sql.open();
            int[] weekOneP = sql.getSetPercentages(1);
            int[] weekTwoP = sql.getSetPercentages(2);
            int[] weekThreeP = sql.getSetPercentages(3);
            int[] weekFourP = sql.getSetPercentages(4);

            if (WendlerMath.arePercentagesHeavy(weekOneP, weekTwoP, weekThreeP, weekFourP)) {
                buttonHeavy.setChecked(true);
            } else if (!WendlerMath.arePercentagesFresh(
                    weekOneP,
                    weekTwoP,
                    weekThreeP,
                    weekFourP)) {
                mButtonCustom.setChecked(true);
            }

            mWeekOneView.insertValues(weekOneP, true);
            mWeekTwoView.insertValues(weekTwoP, true);
            mWeekThreeView.insertValues(weekThreeP, true);
            mWeekFourView.insertValues(weekFourP, true);

        } catch (SQLException e) {
            WendlerizedLog.e("Failed to insert current values", e);
        } finally {
            sql.close();
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
     * Insert the fresh values into the EditTexts.
     */
    private void insertFreshValues() {
        mWeekOneView.insertValues(WendlerConstants.FRESH_PERCENTAGES_W_1, false);
        mWeekTwoView.insertValues(WendlerConstants.FRESH_PERCENTAGES_W_2, false);
        mWeekThreeView.insertValues(WendlerConstants.FRESH_PERCENTAGES_W_3, false);
        mWeekFourView.insertValues(WendlerConstants.FRESH_PERCENTAGES_W_4, false);
    }
}
