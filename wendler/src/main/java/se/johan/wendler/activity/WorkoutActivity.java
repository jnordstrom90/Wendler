package se.johan.wendler.activity;

import android.annotation.TargetApi;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.res.Resources;
import android.os.Build;
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
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.astuetz.PagerSlidingTabStrip;
import com.doomonafireball.betterpickers.calendardatepicker.CalendarDatePickerDialog;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.listeners.EventListener;

import java.sql.SQLException;

import se.johan.wendler.R;
import se.johan.wendler.activity.base.BaseActivity;
import se.johan.wendler.animation.ZoomOutPageTransformer;
import se.johan.wendler.fragment.WorkoutAdditionalFragment;
import se.johan.wendler.fragment.WorkoutMainFragment;
import se.johan.wendler.fragment.base.WorkoutFragment;
import se.johan.wendler.model.Action;
import se.johan.wendler.model.Workout;
import se.johan.wendler.sql.SqlHandler;
import se.johan.wendler.ui.dialog.ConfirmationDialog;
import se.johan.wendler.ui.dialog.EditTextDialog;
import se.johan.wendler.ui.dialog.StopwatchDialog;
import se.johan.wendler.util.Constants;
import se.johan.wendler.util.Utils;
import se.johan.wendler.util.WendlerizedLog;
import se.johan.wendler.util.WorkoutHolder;

/**
 * Activity for handling the workout fragments
 */
public class WorkoutActivity extends BaseActivity implements
        TabListener,
        OnPageChangeListener,
        EditTextDialog.EditTextListener,
        StopwatchDialog.StopWatchListener,
        ConfirmationDialog.ConfirmationDialogListener,
        EventListener {

    private static final String EXTRA_ELAPSED_TIME = "elapsedTime";
    private static final String EXTRA_TIMER_IS_RUNNING = "timerIsRunning";
    private static final String EXTRA_CURRENT_PAGE = "mCurrentPage";

    private ViewPager mViewPager;
    private Workout mWorkout;
    private int mCurrentPage;
    private long mTimeElapsed = -1;
    private boolean mTimerIsRunning;

    /**
     * Called when our activity is created.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_workout);

        overrideElevation(getResources().getDimension(R.dimen.toolbar_elevation));
        if (Utils.hasLollipop()) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        if (savedInstanceState != null) {
            mWorkout = savedInstanceState.getParcelable(Constants.BUNDLE_EXERCISE_ITEM);
            mTimerIsRunning = savedInstanceState.getBoolean(EXTRA_TIMER_IS_RUNNING);
            mCurrentPage = savedInstanceState.getInt(EXTRA_CURRENT_PAGE, 0);
            mTimeElapsed = savedInstanceState.getLong(EXTRA_ELAPSED_TIME, -1);
        } else {
            WorkoutHolder.WorkoutItem item = WorkoutHolder.getInstance().getWorkout();
            mWorkout = item.getWorkout();
            mTimerIsRunning = item.isTimerRunning();
            mCurrentPage = item.getCurrentPage();
            mTimeElapsed = item.getElapsedTime();
        }

        FragmentManager fragmentManager = initFragmentManagement();

        CalendarDatePickerDialog dialog = (CalendarDatePickerDialog)
                fragmentManager.findFragmentByTag(CalendarDatePickerDialog.class.getName());
        if (dialog != null) {
            dialog.setOnDateSetListener(mDateSetListener);
        }

        String text = String.format(
                getString(R.string.actionbar_title_workout), mWorkout.getDisplayName());
        updateTitle(text);

        String subtitle = getString(
                R.string.actionbar_subtitle_workout,
                mWorkout.getWeek(),
                mWorkout.getCycle(),
                mWorkout.getMainExercise().getWeight());
        updateHelpMessage(subtitle);

        initActionButton();
    }

    /**
     * Called when the application is paused.
     */
    @Override
    protected void onPause() {
        super.onPause();
        int pos = mViewPager != null ? mViewPager.getCurrentItem() : mCurrentPage;
        WorkoutHolder.getInstance().putWorkout(
                new WorkoutHolder.WorkoutItem(
                        mWorkout,
                        pos,
                        mTimerIsRunning,
                        mTimeElapsed));
    }

    @Override
    protected int getNavigationResource() {
        return R.drawable.ic_arrow_back_black_24dp;
    }

    @Override
    protected String getToolbarTitle() {
        return "";
    }

    @Override
    protected int getToolbarHelpMessage() {
        return 0;
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
            case R.id.action_done:
                checkWorkoutForDeload(true);
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
     * Override on back press to store the workout.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        checkWorkoutForDeload(false);
    }

    /**
     * Save our instance when needed.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        int pos = mViewPager != null ? mViewPager.getCurrentItem() : mCurrentPage;
        outState.putParcelable(Constants.BUNDLE_EXERCISE_ITEM, mWorkout);
        outState.putBoolean(EXTRA_TIMER_IS_RUNNING, mTimerIsRunning);
        outState.putInt(EXTRA_CURRENT_PAGE, pos);
        outState.putLong(EXTRA_ELAPSED_TIME, mTimeElapsed);
    }

    /**
     * Called before our option menu is created.
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_calendar).setVisible(
                mWorkout != null && mWorkout.isComplete());
        menu.findItem(R.id.action_done).setVisible(
                mWorkout != null && mWorkout.getMainExercise().getLastSetProgress() > -1);
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
        // Not used
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
     * Called when snack bar is shown.
     */
    @Override
    public void onShow(Snackbar snackbar) {
        final View view = findViewById(R.id.multiple_actions);
        view.animate()
                .translationY(-snackbar.getHeight())
                .setInterpolator(new DecelerateInterpolator(1.5f))
                .setDuration(300)
                .start();
    }

    /**
     * Called when snack bar is in place.
     */
    @Override
    public void onShown(Snackbar snackbar) {

    }

    /**
     * Called when the snack bar is dismissed.
     */
    @Override
    public void onDismiss(Snackbar snackbar) {
        final View view = findViewById(R.id.multiple_actions);
        view.animate()
                .setInterpolator(new AccelerateInterpolator(1.5f))
                .translationY(view.getTranslationY() + snackbar.getHeight())
                .setDuration(300)
                .start();
    }

    /**
     * Called when snack bar is gone.
     */
    @Override
    public void onDismissed(Snackbar snackbar) {

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
        WorkoutHolder.getInstance().destroy();
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
     * Initialize the Floating Action Button.
     */
    private void initActionButton() {
        final FloatingActionsMenu menu = (FloatingActionsMenu) findViewById(R.id.multiple_actions);

        findViewById(R.id.action_notes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditTextDialog.newInstance(
                        getString(R.string.my_notes),
                        mWorkout.getNotes())
                        .show(getSupportFragmentManager(), EditTextDialog.TAG);
                menu.collapse();
            }
        });

        findViewById(R.id.action_add_additional).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = getSupportFragmentManager().
                        findFragmentByTag(WorkoutAdditionalFragment.TAG);
                if (fragment instanceof WorkoutAdditionalFragment) {
                    ((WorkoutAdditionalFragment) fragment).onActionTaken(Action.ADD_EXERCISE);
                }
                menu.collapse();
            }
        });

        findViewById(R.id.action_enter_reps).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = getSupportFragmentManager().
                        findFragmentByTag(WorkoutMainFragment.TAG);
                if (fragment instanceof WorkoutMainFragment) {
                    ((WorkoutMainFragment) fragment).onActionTaken(Action.SET_REPS);
                }
                menu.collapse();
            }
        });
    }

    /**
     * Initialize the fragment handling.
     */
    private FragmentManager initFragmentManagement() {
        FragmentManager fragmentManager = getSupportFragmentManager();

        WorkoutMainFragment main =
                (WorkoutMainFragment) fragmentManager.findFragmentByTag(WorkoutMainFragment.TAG);
        WorkoutAdditionalFragment extra = (WorkoutAdditionalFragment)
                fragmentManager.findFragmentByTag(WorkoutAdditionalFragment.TAG);

        android.support.v4.app.FragmentTransaction remove = fragmentManager.beginTransaction();

        if (main == null) {
            main = WorkoutMainFragment.newInstance(
                    mWorkout.getMainExercise(), mWorkout.getWeek(), mWorkout.isComplete());
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
                new TwoFragmentAdapter(fragmentManager, main, extra, getResources());

        if (mViewPager != null) {
            mViewPager.setAdapter(adapter);
            mViewPager.setCurrentItem(mCurrentPage);
            mViewPager.setPageTransformer(true, new ZoomOutPageTransformer());
            mViewPager.setOnPageChangeListener(this);

            PagerSlidingTabStrip mSlidingTabLayout = (PagerSlidingTabStrip) findViewById(R.id.sliding_tabs);
            mSlidingTabLayout.setViewPager(mViewPager);
        } else {
            fragmentManager.beginTransaction()
                    .add(R.id.left_pane, main, WorkoutMainFragment.TAG)
                    .add(R.id.right_pane, extra, WorkoutAdditionalFragment.TAG)
                    .commit();
        }
        return fragmentManager;
    }

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

        public TwoFragmentAdapter(
                FragmentManager fragmentManager, Fragment one, Fragment two, Resources res) {
            this.fragmentManager = fragmentManager;
            this.one = one;
            this.two = two;
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
                    return res.getString(R.string.additional_exercise).toUpperCase();
            }
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return ((Fragment) object).getView() == view;
        }
    }
}