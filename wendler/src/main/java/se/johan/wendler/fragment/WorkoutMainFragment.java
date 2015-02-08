package se.johan.wendler.fragment;

import android.os.Bundle;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.doomonafireball.betterpickers.numberpicker.NumberPickerBuilder;
import com.doomonafireball.betterpickers.numberpicker.NumberPickerDialogFragment;

import se.johan.wendler.R;
import se.johan.wendler.fragment.base.WorkoutFragment;
import se.johan.wendler.model.Action;
import se.johan.wendler.model.MainExercise;
import se.johan.wendler.model.Workout;
import se.johan.wendler.sql.SqlHandler;
import se.johan.wendler.ui.adapter.MainExerciseAdapter;
import se.johan.wendler.ui.view.MainExerciseFooterView;
import se.johan.wendler.util.Utils;
import se.johan.wendler.util.WendlerMath;

/**
 * WorkoutFragment for the main exercise.
 */
public class WorkoutMainFragment extends WorkoutFragment implements
        NumberPickerDialogFragment.NumberPickerDialogHandler,
        Action.ActionListener {

    public static final String TAG = WorkoutMainFragment.class.getName();

    private static final String EXTRA_EXERCISE_ITEM = "exerciseItem";
    private static final String EXTRA_WEEK = "week";
    private static final String EXTRA_IS_COMPLETE = "isComplete";

    private MainExerciseFooterView mFooter;
    private MainExercise mMainExercise;
    private ListView mListView;
    private MainExerciseAdapter mAdapter;

    public WorkoutMainFragment() {
    }

    /**
     * Static creation to avoid problems on rotation.
     */
    public static WorkoutMainFragment newInstance(
            MainExercise mainExercise, int week, boolean isComplete) {
        Bundle arguments = new Bundle();
        arguments.putParcelable(EXTRA_EXERCISE_ITEM, mainExercise);
        arguments.putInt(EXTRA_WEEK, week);
        arguments.putBoolean(EXTRA_IS_COMPLETE, isComplete);
        WorkoutMainFragment fragment = new WorkoutMainFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    /**
     * Called when the view is created.
     */
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.list_empty, container, false);
        mListView = (ListView) view.findViewById(R.id.listView);
        mFooter = (MainExerciseFooterView) inflater.inflate(R.layout.footer_main_exercise, null);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mMainExercise = getArguments().getParcelable(EXTRA_EXERCISE_ITEM);

        mAdapter = new MainExerciseAdapter(
                getActivity(), mMainExercise, getArguments().getInt(EXTRA_WEEK), this);
        updateFooter();
    }

    /**
     * Called when a workout should be stored.
     */
    @Override
    public boolean storeWorkout(
            boolean complete,
            Workout workout,
            SqlHandler handler,
            boolean delayedDeload) {

        boolean newWorkout = false;
        if (complete && !workout.isComplete()) {
            Time now = new Time();
            now.setToNow();
            workout.updateWorkoutTime(now.year, now.month, now.monthDay);
            workout.updateInsertTime(now.normalize(false));
            workout.setComplete();
            newWorkout = true;
        }

        workout.setMainExercise(mMainExercise);

        workout.setIsWon(WendlerMath.isWorkoutWon(
                getArguments().getInt(EXTRA_WEEK), mMainExercise) && !delayedDeload);
        handler.updateWorkoutStats(workout, newWorkout);
        return handler.storeMainExercise(workout, complete);
    }

    /**
     * Called when a number is set in the number picker.
     */
    @Override
    public void onDialogNumberSet(
            int reference, int number, double decimal, boolean isNegative, double fullNumber) {
        updateProgress(number);
    }

    /**
     * Update the progress of the main exercise.
     */
    public void updateProgress(int number) {
        mMainExercise.setLastSetProgress(number);
        updateFooter();
        getActivity().invalidateOptionsMenu();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDialogDismissed() {
        if (mMainExercise.getLastSetProgress() == -1) {
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Called when an action in the floating action button is performed.
     */
    @Override
    public void onActionTaken(Action action) {
        switch (action) {
            case SET_REPS:
                displayNumberPicker();
                break;
        }
    }

    /**
     * Display the number picker which allows the user to enter reps.
     */
    private void displayNumberPicker() {
        new NumberPickerBuilder()
                .setFragmentManager(getActivity().getSupportFragmentManager())
                .setMaxNumber(50)
                .setMinNumber(0)
                .setTargetFragment(this)
                .setStyleResId(R.style.BetterPickersDialogFragment_Light)
                .setPlusMinusVisibility(View.GONE)
                .setDecimalVisibility(View.GONE)
                .show();
    }

    /**
     * Update the footer.
     */
    private void updateFooter() {
        if (mListView.getFooterViewsCount() == 0 && mMainExercise.getLastSetProgress() != -1) {
            mListView.addFooterView(mFooter, null, false);
            View footer = new View(getActivity());
            footer.setLayoutParams(new AbsListView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    getResources().getDimensionPixelSize(R.dimen.list_double_line_height)));
            mListView.addFooterView(footer, null, false);
            if (!Utils.hasKitKat()) {
                mListView.setAdapter(mAdapter);
            }
        }

        if (mListView.getAdapter() == null) {
            mListView.setAdapter(mAdapter);
        }

        if (mFooter != null
                && mFooter.updateInfo(
                getArguments().getInt(EXTRA_WEEK),
                mMainExercise,
                getArguments().getBoolean(EXTRA_IS_COMPLETE),
                this)) {
            mListView.smoothScrollToPosition(mListView.getChildCount());
        }
    }
}
