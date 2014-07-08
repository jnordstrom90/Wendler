package se.johan.wendler.fragment;

import android.graphics.Point;
import android.os.Bundle;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ListView;

import com.doomonafireball.betterpickers.numberpicker.NumberPickerBuilder;
import com.doomonafireball.betterpickers.numberpicker.NumberPickerDialogFragment;
import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;

import se.johan.wendler.R;
import se.johan.wendler.R.layout;
import se.johan.wendler.adapter.MainExerciseAdapter;
import se.johan.wendler.fragment.base.WorkoutFragment;
import se.johan.wendler.model.MainExercise;
import se.johan.wendler.model.Workout;
import se.johan.wendler.sql.SqlHandler;
import se.johan.wendler.util.PreferenceUtil;
import se.johan.wendler.util.Util;
import se.johan.wendler.util.WendlerMath;
import se.johan.wendler.view.MainExerciseFooterView;

/**
 * WorkoutFragment for the main exercise.
 */
public class WorkoutMainFragment extends WorkoutFragment implements
        AdapterView.OnItemClickListener, NumberPickerDialogFragment.NumberPickerDialogHandler {

    public static final String TAG = WorkoutMainFragment.class.getName();

    private static final String EXTRA_EXERCISE_ITEM = "exerciseItem";
    private static final String EXTRA_WEEK = "week";

    private MainExerciseFooterView mFooter;
    private MainExercise mMainExercise;
    private ListView mListView;
    private ShowcaseView mShowcaseView;
    private MainExerciseAdapter mAdapter;

    public WorkoutMainFragment() {
    }

    /**
     * Static creation to avoid problems on rotation.
     */
    public static WorkoutMainFragment newInstance(MainExercise mainExercise, int week) {
        Bundle arguments = new Bundle();
        arguments.putParcelable(EXTRA_EXERCISE_ITEM, mainExercise);
        arguments.putInt(EXTRA_WEEK, week);
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

        View view = inflater.inflate(R.layout.listview_empty, container, false);
        mListView = (ListView) view.findViewById(R.id.listView);
        mListView.setOnItemClickListener(this);
        mFooter = (MainExerciseFooterView) inflater.inflate(layout.main_exercise_footer, null);

        setObserver();

        return view;
    }

    /**
     * Called when the state needs to be saved.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EXTRA_EXERCISE_ITEM, mMainExercise);
    }

    /**
     * Called when the activity is created.
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            mMainExercise = savedInstanceState.getParcelable(EXTRA_EXERCISE_ITEM);
        } else {
            mMainExercise = getArguments().getParcelable(EXTRA_EXERCISE_ITEM);
        }

        mAdapter = new MainExerciseAdapter(getActivity(), mMainExercise);
        updateFooter();
    }

    /**
     * Called when a list item is clicked.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position == (mListView.getCount() - 1 - mListView.getFooterViewsCount())) {
            if (mShowcaseView != null) {
                mShowcaseView.hide();
            }
            NumberPickerBuilder npb = new NumberPickerBuilder()
                    .setFragmentManager(getActivity().getSupportFragmentManager())
                    .setMaxNumber(50)
                    .setMinNumber(0)
                    .setTargetFragment(this)
                    .setStyleResId(R.style.BetterPickersDialogFragment_Light)
                    .setPlusMinusVisibility(View.GONE)
                    .setDecimalVisibility(View.GONE);
            npb.show();
        } else {
            mMainExercise.getExerciseSet(position).toggleCompletion();
            mAdapter.notifyDataSetChanged();
        }
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
        mMainExercise.setLastSetProgress(number);
        updateFooter();
    }

    /**
     * Return if the user has input any repetitions for the main exercise.
     */
    public boolean hasSetReps() {
        return mMainExercise.getLastSetProgress() > -1;
    }

    /**
     * Show the ShowcaseView.
     */
    private void showShowcaseView() {

        if (!PreferenceUtil.getBoolean(getActivity(),
                PreferenceUtil.KEY_HAS_SEEN_SHOWCASE_WORKOUTS)) {
            final View child = mListView.getChildAt(mListView.getLastVisiblePosition());

            final int[] pos = new int[2];
            child.getLocationOnScreen(pos);
            Target target = new Target() {
                @Override
                public Point getPoint() {
                    return new Point(mListView.getWidth() / 2, pos[1] + child.getHeight() / 2);
                }
            };

            mShowcaseView = new ShowcaseView.Builder(getActivity(), true)
                    .setTarget(target)
                    .setContentTitle(R.string.showcase_workout_title)
                    .setContentText(R.string.showcase_workout_detail)
                    .setShowcaseEventListener(mShowcaseListener)
                    .hideOnTouchOutside()
                    .setStyle(R.style.CustomShowcaseTheme)
                    .build();
            mShowcaseView.show();
        }
    }

    /**
     * Create an observer to notify the ShowcaseView it can be shown.
     */
    private void setObserver() {
        mListView.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @SuppressWarnings("deprecation")
                    @Override
                    public void onGlobalLayout() {
                        mListView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        showShowcaseView();
                    }
                });
    }

    /**
     * Update the footer.
     */
    private void updateFooter() {
        if (mListView.getFooterViewsCount() == 0 && mMainExercise.getLastSetProgress() != -1) {
            mListView.addFooterView(mFooter, null, false);
            if (!Util.hasKitKat()) {
                mListView.setAdapter(mAdapter);
            }
        }

        if (mListView.getAdapter() == null) {
            mListView.setAdapter(mAdapter);
        }

        if (mFooter != null && mFooter.updateInfo(
                getArguments().getInt(EXTRA_WEEK), mMainExercise)) {
            mListView.smoothScrollToPosition(mListView.getChildCount());
        }
    }

    /**
     * Listener for events related to the ShowcaseView.
     */
    private final OnShowcaseEventListener mShowcaseListener = new OnShowcaseEventListener() {
        @Override
        public void onShowcaseViewHide(ShowcaseView showcaseView) {
            PreferenceUtil.putBoolean(
                    getActivity(),
                    PreferenceUtil.KEY_HAS_SEEN_SHOWCASE_WORKOUTS,
                    true);
        }

        @Override
        public void onShowcaseViewDidHide(ShowcaseView showcaseView) {

        }

        @Override
        public void onShowcaseViewShow(ShowcaseView showcaseView) {

        }
    };
}
