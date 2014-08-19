package se.johan.wendler.activity;

import android.R.anim;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;

import java.sql.SQLException;

import se.johan.wendler.R;
import se.johan.wendler.activity.base.BaseActivity;
import se.johan.wendler.animation.ZoomOutPageTransformer;
import se.johan.wendler.dialog.ConfirmationDialog;
import se.johan.wendler.fragment.InitIncrementFragment;
import se.johan.wendler.fragment.InitOrderFragment;
import se.johan.wendler.fragment.InitPercentageFragment;
import se.johan.wendler.fragment.InitWeightFragment;
import se.johan.wendler.fragment.base.InitFragment;
import se.johan.wendler.sql.SqlHandler;
import se.johan.wendler.util.Constants;
import se.johan.wendler.util.PreferenceUtil;
import se.johan.wendler.util.Util;
import se.johan.wendler.util.WendlerMath;
import se.johan.wendler.util.WendlerizedLog;
import se.johan.wendler.view.SlidingTabLayout;

/**
 * Activity handling the initial setup of the application
 */
public class StartupActivity extends BaseActivity
        implements ConfirmationDialog.ConfirmationDialogListener, ViewPager.OnPageChangeListener {

    private ViewPager mViewPager;
    private static final SparseArray<InitFragment> mFragmentList = new SparseArray<InitFragment>();

    /**
     * Called when the Activity is created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isInitialized()) {
            WendlerizedLog.d("Already initialized! Start the MainActivity");

            recalculateOneRmIfNeeded();

            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        WendlerizedLog.d("First run, let the user initialize");

        setContentView(R.layout.startup_activity_view);

        InitWeightFragment initWeightFragment;
        InitIncrementFragment initIncrementFragment;
        InitOrderFragment initOrderFragment;
        InitPercentageFragment initPercentageFragment;

        if (savedInstanceState == null) {
            initWeightFragment = InitWeightFragment.newInstance();
            initIncrementFragment = InitIncrementFragment.newInstance();
            initOrderFragment = InitOrderFragment.newInstance();
            initPercentageFragment = InitPercentageFragment.newInstance();
        } else {
            initWeightFragment = (InitWeightFragment) getSupportFragmentManager()
                    .getFragment(savedInstanceState, InitWeightFragment.TAG);
            initIncrementFragment = (InitIncrementFragment) getSupportFragmentManager()
                    .getFragment(savedInstanceState, InitIncrementFragment.TAG);
            initOrderFragment = (InitOrderFragment) getSupportFragmentManager()
                    .getFragment(savedInstanceState, InitOrderFragment.TAG);
            initPercentageFragment = (InitPercentageFragment) getSupportFragmentManager()
                    .getFragment(savedInstanceState, InitPercentageFragment.TAG);
        }

        mFragmentList.put(0, initWeightFragment);
        mFragmentList.put(1, initOrderFragment);
        mFragmentList.put(2, initPercentageFragment);
        mFragmentList.put(3, initIncrementFragment);

        SectionsPagerAdapter mSectionsPagerAdapter =
                new SectionsPagerAdapter(getSupportFragmentManager(), mFragmentList);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setPageTransformer(true, new ZoomOutPageTransformer());
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(4);

        SlidingTabLayout mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setViewPager(mViewPager);
        mSlidingTabLayout.setOnPageChangeListener(this);
    }

    /**
     * Called when our instances need to be saved.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        try {
            getSupportFragmentManager().putFragment(
                    outState,
                    InitWeightFragment.TAG,
                    mFragmentList.get(0));

            getSupportFragmentManager().putFragment(
                    outState,
                    InitOrderFragment.TAG,
                    mFragmentList.get(1));

            getSupportFragmentManager().putFragment(
                    outState,
                    InitPercentageFragment.TAG,
                    mFragmentList.get(2));

            getSupportFragmentManager().putFragment(
                    outState,
                    InitIncrementFragment.TAG,
                    mFragmentList.get(3));

        } catch (IllegalStateException ise) {
            WendlerizedLog.e("Error saving instance", ise);
        }
    }

    /**
     * Called when the Activity is started.
     */
    @Override
    public void onStart() {
        super.onStart();

        if (!PreferenceUtil.getBoolean(this, PreferenceUtil.KEY_HAS_SEEN_FIRST_TIME_DIALOG)) {
            ConfirmationDialog.newInstance(
                    getString(R.string.first_time_dialog),
                    getString(R.string.title_welcome),
                    getString(R.string.btn_ok),
                    null,
                    null).show(getSupportFragmentManager(), ConfirmationDialog.TAG);
            PreferenceUtil.putBoolean(this, PreferenceUtil.KEY_HAS_SEEN_FIRST_TIME_DIALOG, true);
        }
    }

    /**
     * Create our options menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_startup, menu);
        return true;
    }


    /**
     * Called when an option item is selected.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_help:
                ConfirmationDialog.newInstance(
                        mFragmentList.get(mViewPager.getCurrentItem()).getHelpingMessage(),
                        getString(R.string.action_item_about),
                        getString(R.string.btn_ok),
                        null,
                        null).show(getSupportFragmentManager(), ConfirmationDialog.TAG);
                break;
            case R.id.action_done:
                saveInput();
                break;
        }
        return true;
    }

    /**
     * Called when the dialog is confirmed.
     */
    @Override
    public void onDialogConfirmed(boolean confirmed) {
        // Not used
    }

    /**
     * Called when a page is scrolled in the viewpager.
     */
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        // Not used
    }

    /**
     * Called when a page is selected.
     */
    @Override
    public void onPageSelected(int position) {
        Util.hideKeyboard(this);
    }

    /**
     * Called when a page state is changed.
     */
    @Override
    public void onPageScrollStateChanged(int state) {
        switch (state) {
            case ViewPager.SCROLL_STATE_IDLE:
                Util.hideKeyboard(this);
                break;
        }
    }

    /**
     * Save our entered information.
     */
    private void saveInput() {
        boolean allIsGood = true;
        int position = 0;

        for (int i = 0; i < mFragmentList.size(); i++) {
            if (!mFragmentList.get(i).allDataIsOk()) {
                WendlerizedLog.d("Data incorrectly enter on position " + i);
                allIsGood = false;
                position = i;
                break;
            }
        }

        if (allIsGood) {
            SqlHandler handler = new SqlHandler(this);
            try {
                handler.open();
                for (int i = 0; i < mFragmentList.size(); i++) {
                    mFragmentList.get(i).saveData(handler);
                }
            } catch (SQLException e) {
                WendlerizedLog.e("Failed to save starting information", e);
            } finally {
                handler.close();
            }
            PreferenceUtil.putBoolean(this, PreferenceUtil.KEY_UPDATE_TO_TM, true);
            startActivity(new Intent(this, MainActivity.class));
            overridePendingTransition(anim.fade_in, anim.fade_out);
            finish();
        } else {
            displayErrorAndScroll(position);
        }
    }

    /**
     * Something wasn't entered correctly, scroll to that position and notify the user.
     */
    private void displayErrorAndScroll(int position) {
        mViewPager.setCurrentItem(position);
        mFragmentList.get(position).notifyError();
    }

    /**
     * Due to the change with using TM instead of real max we need to calculate the tm and update
     * it properly.
     */
    private void recalculateOneRmIfNeeded() {

        if (!PreferenceUtil.getBoolean(this, PreferenceUtil.KEY_UPDATE_TO_TM)) {
            SqlHandler handler = new SqlHandler(this);
            try {
                handler.open();
                double newPressWeight = WendlerMath.calculateWeight(
                        this,
                        handler.getOneRmForExercise(Constants.EXERCISES[0]),
                        handler.getWorkoutPercentage(Constants.EXERCISES[0]));

                double newDeadliftWeight = WendlerMath.calculateWeight(
                        this,
                        handler.getOneRmForExercise(Constants.EXERCISES[1]),
                        handler.getWorkoutPercentage(Constants.EXERCISES[1]));

                double newBenchWeight = WendlerMath.calculateWeight(
                        this,
                        handler.getOneRmForExercise(Constants.EXERCISES[2]),
                        handler.getWorkoutPercentage(Constants.EXERCISES[2]));

                double newSquatWeight = WendlerMath.calculateWeight(
                        this,
                        handler.getOneRmForExercise(Constants.EXERCISES[3]),
                        handler.getWorkoutPercentage(Constants.EXERCISES[3]));

                handler.updateOneRm(
                        newPressWeight,
                        newDeadliftWeight,
                        newBenchWeight,
                        newSquatWeight);

                handler.updateOldWorkouts();
                PreferenceUtil.putBoolean(this, PreferenceUtil.KEY_UPDATE_TO_TM, true);
            } catch (SQLException e) {
                WendlerizedLog.e("Failed to update to TM", e);
            } finally {
                handler.close();
            }
        }
    }

    /**
     * Check if our database is up and running which means our application is initialized.
     */
    private boolean isInitialized() {
        SqlHandler handler = new SqlHandler(this);
        try {
            handler.open();
            return handler.isInitialized();
        } catch (SQLException e) {
            WendlerizedLog.e("Failed to check if database is initialized", e);
        } finally {
            handler.close();
        }
        return false;
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        private final SparseArray<InitFragment> mList;

        public SectionsPagerAdapter(FragmentManager fm, SparseArray<InitFragment> list) {
            super(fm);
            mList = list;
        }

        @Override
        public Fragment getItem(int position) {
            return mList.get(position);
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                default:
                case 0:
                    return getString(R.string.title_starting_weights).toUpperCase();
                case 1:
                    return getString(R.string.title_order_of_workouts).toUpperCase();
                case 2:
                    return getString(R.string.title_workout_percentages).toUpperCase();
                case 3:
                    return getString(R.string.title_increments).toUpperCase();
            }
        }
    }
}
