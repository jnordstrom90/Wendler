package se.johan.wendler.activity;

import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.doomonafireball.betterpickers.calendardatepicker.CalendarDatePickerDialog;
import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ActionItemTarget;

import java.sql.SQLException;

import se.johan.wendler.R;
import se.johan.wendler.R.layout;
import se.johan.wendler.R.string;
import se.johan.wendler.activity.base.BaseActivity;
import se.johan.wendler.animation.ZoomOutPageTransformer;
import se.johan.wendler.dialog.ConfirmationDialog;
import se.johan.wendler.dialog.EditTextDialog;
import se.johan.wendler.dialog.StopwatchDialog;
import se.johan.wendler.fragment.WorkoutAdditionalFragment;
import se.johan.wendler.fragment.WorkoutMainFragment;
import se.johan.wendler.fragment.base.WorkoutFragment;
import se.johan.wendler.model.Workout;
import se.johan.wendler.sql.SqlHandler;
import se.johan.wendler.util.Constants;
import se.johan.wendler.util.PreferenceUtil;
import se.johan.wendler.util.WendlerizedLog;
import se.johan.wendler.view.SlidingTabLayout;

/**
 * Activity for handling the workout fragments
 */
public class WorkoutActivity extends BaseActivity implements TabListener,
        OnPageChangeListener, EditTextDialog.EditTextListener,
        StopwatchDialog.StopWatchListener, ConfirmationDialog.ConfirmationDialogListener {

    private static final String EXTRA_ELAPSED_TIME = "elapsedTime";
    private static final String EXTRA_TIMER_IS_RUNNING = "timerIsRunning";
    private static final String EXTRA_KEY_NOTES = "keyNotes";
    private static final String EXTRA_CURRENT_PAGE = "mCurrentPage";

    private ViewPager mViewPager;
    private Workout mWorkout;
    private ShowcaseView mShowcaseView;
    private int mCurrentPage;
    private long mTimeElapsed = -1;
    private boolean mTimerIsRunning = false;

    /**
     * Called when our activity is created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.workout_activity_view);

        mWorkout = getIntent().getExtras().getParcelable(Constants.BUNDLE_EXERCISE_ITEM);

        if (savedInstanceState != null) {
            mCurrentPage = savedInstanceState.getInt(EXTRA_CURRENT_PAGE, 0);
            mTimeElapsed = savedInstanceState.getLong(EXTRA_ELAPSED_TIME, -1);
            mTimerIsRunning = savedInstanceState.getBoolean(EXTRA_TIMER_IS_RUNNING, false);
            mWorkout.updateNotes(savedInstanceState.getString(EXTRA_KEY_NOTES, ""));
        }

        FragmentManager fragmentManager = getSupportFragmentManager();

        WorkoutMainFragment main =
                (WorkoutMainFragment) fragmentManager.findFragmentByTag(WorkoutMainFragment.TAG);
        WorkoutAdditionalFragment extra = (WorkoutAdditionalFragment)
                fragmentManager.findFragmentByTag(WorkoutAdditionalFragment.TAG);

        android.support.v4.app.FragmentTransaction remove = fragmentManager.beginTransaction();

        if (main == null) {
            main = WorkoutMainFragment.newInstance(mWorkout.getMainExercise(), mWorkout.getWeek());
        } else {
            remove.remove(main);
        }
        if (extra == null) {
            extra = WorkoutAdditionalFragment.newInstance(
                    mWorkout.getAdditionalExercises(),
                    mWorkout.getName());
        } else {
            remove.remove(extra);
        }
        if (!remove.isEmpty()) {
            remove.commit();
            fragmentManager.executePendingTransactions();
        }

        mViewPager = (ViewPager) findViewById(R.id.pager);
        TwoFragmentAdapter adapter =
                new TwoFragmentAdapter(fragmentManager, main, extra, mWorkout, getResources());

        if (mViewPager != null) {
            mViewPager.setAdapter(adapter);
            mViewPager.setCurrentItem(mCurrentPage);
            mViewPager.setPageTransformer(true, new ZoomOutPageTransformer());
            mViewPager.setOnPageChangeListener(this);

            SlidingTabLayout mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
            mSlidingTabLayout.setViewPager(mViewPager);
        } else {
            fragmentManager.beginTransaction()
                    .add(R.id.left_pane, main, WorkoutMainFragment.TAG)
                    .add(R.id.right_pane, extra, WorkoutAdditionalFragment.TAG)
                    .commit();
        }

        String text = String.format(
                getString(string.actionbar_subtitle_workout),
                mWorkout.getDisplayName(),
                mWorkout.getWeek());

        CalendarDatePickerDialog dialog = (CalendarDatePickerDialog)
                fragmentManager.findFragmentByTag(CalendarDatePickerDialog.class.getName());
        if (dialog != null) {
            dialog.setOnDateSetListener(mDateSetListener);
        }

        if (getActionBar() != null) {
            getActionBar().setTitle(text);
        }
    }

    /**
     * Called when we need to save our instance
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        int position = mViewPager != null ? mViewPager.getCurrentItem() : mCurrentPage;
        outState.putInt(EXTRA_CURRENT_PAGE, position);
        outState.putLong(EXTRA_ELAPSED_TIME, mTimeElapsed);
        outState.putBoolean(EXTRA_TIMER_IS_RUNNING, mTimerIsRunning);
        outState.putString(EXTRA_KEY_NOTES, mWorkout.getNotes());
    }

    /**
     * Called when back is pressed.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        checkWorkoutForDeload(false);
    }

    /**
     * Called when an option item is selected.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                checkWorkoutForDeload(false);
                return true;
            case R.id.action_notes:
                EditTextDialog.newInstance(
                        getString(R.string.my_notes),
                        mWorkout.getNotes())
                        .show(getSupportFragmentManager(), EditTextDialog.TAG);
                return true;
            case R.id.action_done:
                if (!shouldShowShowcaseView()) {
                    checkWorkoutForDeload(true);
                } else {
                    showShowcaseView();
                }
                return true;
            case R.id.action_timer:
                StopwatchDialog.newInstance(
                        getString(R.string.stopwatch_title),
                        mTimeElapsed,
                        mTimerIsRunning)
                        .show(getSupportFragmentManager(), StopwatchDialog.TAG);
                return true;
            case R.id.action_calendar:
                Time time = mWorkout.getWorkoutTime();
                CalendarDatePickerDialog.newInstance(mDateSetListener,
                        time.year,
                        time.month,
                        time.monthDay)
                        .show(getSupportFragmentManager(),
                                CalendarDatePickerDialog.class.getName());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Show the ShowcaseView for the user informing him/her about not entering any repetitions.
     */
    private void showShowcaseView() {
        mShowcaseView = new ShowcaseView.Builder(this, true)
                .setTarget(new ActionItemTarget(this, R.id.action_done))
                .setContentTitle(R.string.showcase_workout_finish_title)
                .setContentText(R.string.showcase_workout_finish_detail)
                .setShowcaseEventListener(mShowcaseListener)
                .hideOnTouchOutside()
                .setStyle(R.style.CustomShowcaseTheme)
                .build();
        mShowcaseView.show();
    }

    /**
     * Called before our option menu is created.
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_calendar).setVisible(mWorkout.isComplete());
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * Called to create our options menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_workout, menu);
        return true;
    }

    /**
     * Called when a page in our ViewPager is scrolled
     */
    @Override
    public void onPageScrolled(int i, float v, int i2) {
        // Not used here
    }

    /**
     * Called when a page is selected in our ViewPager.
     */
    @Override
    public void onPageSelected(int i) {
        if (getActionBar() != null) {
            getActionBar().setSelectedNavigationItem(i);
        }
    }

    /**
     * Called when a page in our ViewPager has changed state.
     */
    @Override
    public void onPageScrollStateChanged(int i) {
        // Not used here
    }

    /**
     * Called when a tab is selected.
     */
    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        if (mViewPager != null) {
            mCurrentPage = tab.getPosition();
            mViewPager.setCurrentItem(tab.getPosition());
        }
    }

    /**
     * Called when a tab is unselected.
     */
    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        // Not used here
    }

    /**
     * Called when a tab is reselected.
     */
    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
        // Not used here
    }

    /**
     * Called when we confirm our text in the EditTextDialog.
     */
    @Override
    public void getTextFromDialog(String text) {
        mWorkout.updateNotes(text);
    }

    /**
     * Get callback information for time elapsed in the stopwatch
     */
    @Override
    public void onStopwatchDismissed(long timeElapsed, boolean isRunning) {
        mTimeElapsed = timeElapsed;
        mTimerIsRunning = isRunning;
    }

    /**
     * Called when our confirmation dialog is closed.
     */
    @Override
    public void onDialogConfirmed(boolean confirmed) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        WorkoutFragment main =
                (WorkoutMainFragment) fragmentManager.findFragmentByTag(WorkoutMainFragment.TAG);
        WorkoutFragment extra = (WorkoutAdditionalFragment)
                fragmentManager.findFragmentByTag(WorkoutAdditionalFragment.TAG);

        SqlHandler handler = new SqlHandler(this);
        try {
            handler.open();

            if (!handler.doDeload(mWorkout)) {
                int workoutId = mWorkout.getWorkoutId() == -1 ?
                        handler.getNextWorkoutId() : mWorkout.getWorkoutId();
                mWorkout.setWorkoutId(workoutId);
                int code = Activity.RESULT_CANCELED;
                if (main.storeWorkout(true, mWorkout, handler, !confirmed)
                        && extra.storeWorkout(true, mWorkout, handler, !confirmed)) {
                    code = Activity.RESULT_OK;
                }
                setResult(code);
                finish();
            }

        } catch (SQLException e) {
            WendlerizedLog.e("Failed to store mWorkout on deload", e);
        } finally {
            handler.close();
        }
    }

    /**
     * Return if we should display the ShowcaseView in case the user hasn't entered any repetitions.
     */
    private boolean shouldShowShowcaseView() {
        WorkoutMainFragment main =
                (WorkoutMainFragment) getSupportFragmentManager()
                        .findFragmentByTag(WorkoutMainFragment.TAG);
        return !main.hasSetReps()
                && !PreferenceUtil.getBoolean(this,
                PreferenceUtil.KEY_HAS_SEEN_SHOWCASE_FINISH_WORKOUTS, false)
                && mShowcaseView == null
                && mWorkout.getWeek() != 4;
    }

    /**
     * Check if we should deload the workout.
     */
    private void checkWorkoutForDeload(boolean complete) {
        SqlHandler handler = new SqlHandler(this);
        try {
            handler.open();

            if (!handler.doDeload(mWorkout)) {
                WendlerizedLog.d("No need to deload, store the workout");
                storeWorkout(handler, complete);
            } else if (complete) {
                WendlerizedLog.d("Workout is completed, but we should deload");
                ConfirmationDialog.newInstance(
                        getString(R.string.delayed_deload_message),
                        getString(R.string.delayed_deload_title),
                        getString(R.string.do_continue),
                        getString(R.string.do_deload),
                        null).show(getSupportFragmentManager(), ConfirmationDialog.TAG);
            }
        } catch (SQLException e) {
            WendlerizedLog.e("Failed to store mWorkout", e);
        } finally {
            handler.close();
        }
    }

    /**
     * Store our workout in the database.
     */
    private void storeWorkout(SqlHandler handler, boolean isComplete) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        WorkoutFragment main =
                (WorkoutMainFragment) fragmentManager.findFragmentByTag(WorkoutMainFragment.TAG);
        WorkoutFragment extra = (WorkoutAdditionalFragment)
                fragmentManager.findFragmentByTag(WorkoutAdditionalFragment.TAG);

        int workoutId = mWorkout.getWorkoutId() == -1 ?
                handler.getNextWorkoutId() : mWorkout.getWorkoutId();
        mWorkout.setWorkoutId(workoutId);
        int code = Activity.RESULT_CANCELED;
        if (main.storeWorkout(isComplete, mWorkout, handler, false)
                && extra.storeWorkout(isComplete, mWorkout, handler, false)) {
            code = Activity.RESULT_OK;
        }
        WendlerizedLog.d("Stored workout and we were successful: " + (code == Activity.RESULT_OK));
        setResult(code);
        finish();
    }

    /**
     * Listener for the CalenderPicker.
     */
    private final CalendarDatePickerDialog.OnDateSetListener
            mDateSetListener = new CalendarDatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(CalendarDatePickerDialog calendarDatePickerDialog,
                              int year,
                              int month,
                              int day) {
            mWorkout.updateWorkoutTime(year, month, day);
        }
    };

    /**
     * Listener for our ShowcaseView.
     */
    private final OnShowcaseEventListener mShowcaseListener = new OnShowcaseEventListener() {
        @Override
        public void onShowcaseViewHide(ShowcaseView showcaseView) {
            PreferenceUtil.putBoolean(
                    WorkoutActivity.this,
                    PreferenceUtil.KEY_HAS_SEEN_SHOWCASE_FINISH_WORKOUTS,
                    true);
        }

        @Override
        public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
            // Not used here
        }

        @Override
        public void onShowcaseViewShow(ShowcaseView showcaseView) {
            // Not used here
        }
    };

    /**
     * Custom TwoFragmentAdapter
     */
    private static class TwoFragmentAdapter extends PagerAdapter {
        private final FragmentManager fragmentManager;
        private final Fragment one;
        private final Fragment two;
        private android.support.v4.app.FragmentTransaction currentTransaction = null;
        private Fragment currentPrimaryItem = null;
        private final Resources res;
        private final Workout mWorkout;

        public TwoFragmentAdapter(FragmentManager fragmentManager, Fragment one,
                                  Fragment two, Workout workout, Resources res) {
            this.fragmentManager = fragmentManager;
            this.one = one;
            this.two = two;
            this.mWorkout = workout;
            this.res = res;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            if (currentTransaction == null) {
                currentTransaction = fragmentManager.beginTransaction();
            }

            String tag = (position == 0) ? WorkoutMainFragment.TAG : WorkoutAdditionalFragment.TAG;
            Fragment fragment = (position == 0) ? one : two;
            currentTransaction.add(container.getId(), fragment, tag);
            if (fragment != currentPrimaryItem) {
                fragment.setMenuVisibility(false);
                fragment.setUserVisibleHint(false);
            }

            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            // With two pages, fragments should never be destroyed.
            throw new AssertionError();
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            Fragment fragment = (Fragment) object;
            if (fragment != currentPrimaryItem) {
                if (currentPrimaryItem != null) {
                    currentPrimaryItem.setMenuVisibility(false);
                    currentPrimaryItem.setUserVisibleHint(false);
                }
                if (fragment != null) {
                    fragment.setMenuVisibility(true);
                    fragment.setUserVisibleHint(true);
                }
                currentPrimaryItem = fragment;
            }
        }

        @Override
        public void finishUpdate(ViewGroup container) {
            if (currentTransaction != null) {
                currentTransaction.commitAllowingStateLoss();
                currentTransaction = null;
                fragmentManager.executePendingTransactions();
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return res.getString(R.string.main_exercise).toUpperCase();
                default:
                    int count = mWorkout.getAdditionalExercises().size();
                    return res.getQuantityString(R.plurals.additional_exercise, count);
            }
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return ((Fragment) object).getView() == view;
        }
    }
}