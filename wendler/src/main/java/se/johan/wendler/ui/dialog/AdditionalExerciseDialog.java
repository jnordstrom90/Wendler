package se.johan.wendler.ui.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import se.johan.wendler.R;
import se.johan.wendler.animation.CustomObjectAnimator;
import se.johan.wendler.model.AdditionalExercise;
import se.johan.wendler.model.ExerciseSet;
import se.johan.wendler.model.SetType;
import se.johan.wendler.sql.SqlHandler;
import se.johan.wendler.ui.dialog.base.AnimationDialog;
import se.johan.wendler.ui.view.FilterEditText;
import se.johan.wendler.util.Util;
import se.johan.wendler.util.WendlerMath;
import se.johan.wendler.util.WendlerizedLog;

/**
 * Dialog for adding additional exercises.
 */
public class AdditionalExerciseDialog extends AnimationDialog implements
        CompoundButton.OnCheckedChangeListener,
        View.OnClickListener,
        AdapterView.OnItemSelectedListener {

    public static final String TAG = AdditionalExerciseDialog.class.getName();

    private static final String EXTRA_EXERCISE_ID = "exerciseId";
    private static final String EXTRA_ADDITIONAL_EXERCISE = "additionalExercise";
    private static final String EXTRA_TITLE = "title";

    private AutoCompleteTextView mAutoCompleteNameTextView;
    private EditText mGoalEditText, mWeightEditText, mPercentageEditText;
    private SwitchCompat mMainExerciseSwitch;
    private Spinner mMainExerciseSpinner;
    private boolean forced = false;

    public AdditionalExerciseDialog() {
    }

    /**
     * Static creation to avoid problems on rotation.
     */
    public static AdditionalExerciseDialog newInstance(String title,
                                                       AdditionalExercise exercise,
                                                       int id,
                                                       Fragment targetFragment) {
        AdditionalExerciseDialog dialog = new AdditionalExerciseDialog();
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_ADDITIONAL_EXERCISE, exercise);
        bundle.putString(EXTRA_TITLE, title);
        bundle.putInt(EXTRA_EXERCISE_ID, id);
        dialog.setArguments(bundle);

        if (targetFragment != null) {
            dialog.setTargetFragment(targetFragment, 1234);
        }

        return dialog;
    }

    /**
     * Called when the dialog is started.
     */
    @Override
    public void onStart() {
        super.onStart();
        AlertDialog d = (AlertDialog) getDialog();
        if (d != null) {
            Button positive = d.getButton(Dialog.BUTTON_POSITIVE);
            if (positive != null) {
                positive.setOnClickListener(this);
            }
        }
    }

    /**
     * Called when the dialog is supposed to be created.
     */
    @NonNull
    @Override
    @SuppressLint("InflateParams")
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString(EXTRA_TITLE);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_exercise, null, false);
        ((TextView) view.findViewById(R.id.title)).setText(title);
        AdditionalExercise exercise = getArguments().getParcelable(EXTRA_ADDITIONAL_EXERCISE);
        setupViews(view, exercise);
        builder.setView(view);
        initializeButtons(view, exercise);
        return builder.create();
    }

    /**
     * Initialize the buttons.
     */
    private void initializeButtons(View view, AdditionalExercise exercise) {
        String positiveText = getString(R.string.add);

        if (exercise != null) {
            positiveText = getString(R.string.save);
        }
        Button cancel = (Button) view.findViewById(R.id.button_cancel);
        cancel.setText(R.string.btn_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        Button save = (Button) view.findViewById(R.id.button_save);
        save.setText(positiveText);
        save.setOnClickListener(this);
    }

    /**
     * Init our views
     */
    private void setupViews(View view, AdditionalExercise exercise) {
        mPercentageEditText = (FilterEditText) view.findViewById(R.id.et_percentage);
        mPercentageEditText.addTextChangedListener(percentageWatcher);

        mMainExerciseSpinner = (Spinner) view.findViewById(R.id.spinner);
        mMainExerciseSpinner.setOnItemSelectedListener(this);

        mMainExerciseSwitch = (SwitchCompat) view.findViewById(R.id.switch1);
        mMainExerciseSwitch.setOnCheckedChangeListener(this);

        mAutoCompleteNameTextView =
                (AutoCompleteTextView) view.findViewById(R.id.tv_name_auto_complete);
        mAutoCompleteNameTextView.setAdapter(
                new ArrayAdapter<String>(getActivity(),
                        android.R.layout.simple_dropdown_item_1line,
                        getStoredAdditionalExerciseNames())
        );

        mWeightEditText = (FilterEditText) view.findViewById(R.id.et_weight_amount);
        mWeightEditText.addTextChangedListener(weightWatcher);

        mGoalEditText = (FilterEditText) view.findViewById(R.id.et_set_rep_amount);

        if (exercise != null) {
            setupFromExercise(exercise);
        }
    }

    /**
     * Called when the view is attached to the activity.
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (getListener() == null) {
            throw new ClassCastException("Class doesn't implement onConfirmClickedListener");
        }
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        updateVisibility(isChecked ? View.VISIBLE : View.GONE);
        if (!isChecked) {
            mPercentageEditText.setText("");
            mMainExerciseSpinner.setSelection(0);
            mWeightEditText.setSelection(mWeightEditText.getText().length());
        }
    }

    /**
     * Called when the positive button is clicked.
     */
    @Override
    public void onClick(View v) {
        if (allDataIsOk()) {
            getListener().onConfirmClicked(generateAdditionalExercise());
            Util.hideKeyboard(getActivity());
            getDialog().dismiss();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if ((mPercentageEditText.getText().length() > 0)) {
            forced = true;
            setWeightFromPercentage(getNameOfPosition(position),
                    Util.getIntFromEditText(mPercentageEditText));
        }
    }

    /**
     * Get an ArrayList with names of all stored additional exercises.
     */
    private ArrayList<String> getStoredAdditionalExerciseNames() {
        ArrayList<String> items = new ArrayList<String>();

        String[] staticList = getResources().getStringArray(R.array.additional_exercises);
        items.addAll(Arrays.asList(staticList));

        SqlHandler handler = new SqlHandler(getActivity());
        try {
            handler.open();
            ArrayList<String> storedExercises = handler.getAdditionalExerciseNames();
            for (String name : storedExercises) {
                if (!items.contains(name)) {
                    items.add(name);
                }
            }
        } catch (SQLException e) {
            WendlerizedLog.e("Failed to get additional exercise names", e);
        } finally {
            handler.close();
        }

        return items;
    }

    /**
     * Init the views from an already existing exercise
     */
    private void setupFromExercise(final AdditionalExercise exercise) {
        mAutoCompleteNameTextView.setText(exercise.getName());

        // Set the cursor at the end
        mAutoCompleteNameTextView.post(new Runnable() {
            @Override
            public void run() {
                mAutoCompleteNameTextView.setSelection(
                        mAutoCompleteNameTextView.getText().length());
            }
        });

        mWeightEditText.setText(String.valueOf(exercise.getExerciseSet(0).getWeight()));
        mGoalEditText.setText(String.valueOf(exercise.getExerciseSet(0).getGoal()));

        boolean checked = !TextUtils.isEmpty(exercise.getMainExerciseName());

        if (checked) {
            mMainExerciseSpinner.setSelection(getPositionOfName(exercise.getMainExerciseName()));
            mPercentageEditText.setText(String.valueOf(exercise.getMainExercisePercentage()));
        }

        mMainExerciseSwitch.setChecked(checked);
    }

    /**
     * Update the visibility dependant on if you're using a weight based of a main exercise.
     */
    private void updateVisibility(int visibility) {
        mMainExerciseSpinner.setVisibility(visibility);
        mPercentageEditText.setVisibility(visibility);
    }

    /**
     * Check the required fields so they have data.
     */
    private boolean allDataIsOk() {
        if (mAutoCompleteNameTextView.getText() != null
                && mAutoCompleteNameTextView.getText().toString().trim().isEmpty()) {
            CustomObjectAnimator.nope(mAutoCompleteNameTextView).start();
            return false;
        } else if (mGoalEditText.getText() != null
                && mGoalEditText.getText().toString().trim().isEmpty()) {
            CustomObjectAnimator.nope(mGoalEditText).start();
            return false;
        }
        return true;
    }

    /**
     * Set the weight based of an entered percentage.
     */
    private void setWeightFromPercentage(String name, int percentage) {

        SqlHandler handler = new SqlHandler(getActivity());
        try {
            handler.open();
            double weight = handler.getOneRmForExercise(name);
            weight = WendlerMath.calculateWeight(getActivity(), weight, percentage);
            mWeightEditText.setText(String.valueOf(weight));
        } catch (SQLException e) {
            WendlerizedLog.e("Failed to get one RM for " + name, e);
        } finally {
            handler.close();
        }
    }

    /**
     * Get the position of the exercise corresponding to a name.
     */
    private int getPositionOfName(String name) {
        if (name.equals(getString(R.string.press))) {
            return 0;
        } else if (name.equals(getString(R.string.deadlift))) {
            return 1;
        } else if (name.equals(getString(R.string.bench))) {
            return 2;
        } else {
            return 3;
        }
    }

    /**
     * Get the name of an exercise corresponding to a position
     */
    private String getNameOfPosition(int position) {
        switch (position) {
            case 0:
                return getString(R.string.press);
            case 1:
                return getString(R.string.deadlift);
            case 2:
                return getString(R.string.bench);
            default:
                return getString(R.string.squat);
        }
    }

    /**
     * Create an additional exercise.
     */
    private AdditionalExercise generateAdditionalExercise() {

        int id = getArguments().getInt(EXTRA_EXERCISE_ID);

        AdditionalExercise exercise = getArguments().getParcelable(EXTRA_ADDITIONAL_EXERCISE);
        int progress = exercise != null ? exercise.getProgress(0) : 0;

        String name = mAutoCompleteNameTextView.getText().toString();
        double weight = mWeightEditText.getText().length() < 1 ?
                0 : Util.getDoubleFromEditText(mWeightEditText);
        int goal = Util.getIntFromEditText(mGoalEditText);

        String mainName = "";
        int mainPercentage = 0;
        if (mMainExerciseSwitch.isChecked()
                && mPercentageEditText.getText().toString().trim().length() > 0) {
            mainName = getNameOfPosition(mMainExerciseSpinner.getSelectedItemPosition());
            mainPercentage = Util.getIntFromEditText(mPercentageEditText);
        }

        progress = progress < 0 ? 0 : progress;

        ExerciseSet set = new ExerciseSet(SetType.REGULAR, weight, goal, progress);
        ArrayList<ExerciseSet> sets = new ArrayList<ExerciseSet>();
        sets.add(set);

        return new AdditionalExercise(name, sets, mainName, mainPercentage, id);
    }

    /**
     * TextWatcher for tracking changes in the mPercentageEditText field
     */
    private final TextWatcher percentageWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (mPercentageEditText.getText().length() > 0) {
                forced = true;
                setWeightFromPercentage(
                        getNameOfPosition(mMainExerciseSpinner.getSelectedItemPosition()),
                        Util.getIntFromEditText(mPercentageEditText));
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    /**
     * TextWatcher for keeping track of changes in the mWeight field
     */
    private final TextWatcher weightWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!forced) {
                mMainExerciseSwitch.setChecked(false);
            }
            forced = false;
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    /**
     * Make sure our target is listening for our confirmation
     */
    private onConfirmClickedListener getListener() {
        if (getActivity() instanceof onConfirmClickedListener) {
            return (onConfirmClickedListener) getActivity();
        } else if (getTargetFragment() instanceof onConfirmClickedListener) {
            return (onConfirmClickedListener) getTargetFragment();
        }
        return null;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    /**
     * Interface for confirming the adding of an additional exercise.
     */
    public interface onConfirmClickedListener {
        /**
         * Called when the positive button has been clicked.
         */
        public void onConfirmClicked(AdditionalExercise exercise);
    }
}
