package se.johan.wendler.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.sql.SQLException;
import java.util.ArrayList;

import se.johan.wendler.R;
import se.johan.wendler.activity.base.BaseActivity;
import se.johan.wendler.adapter.DrawerAdapter;
import se.johan.wendler.dialog.ChangelogDialog;
import se.johan.wendler.fragment.DrawerAdditionalWorkoutsFragment;
import se.johan.wendler.fragment.DrawerBackupManagerFragment;
import se.johan.wendler.fragment.DrawerEditFragment;
import se.johan.wendler.fragment.DrawerOldWorkoutsFragment;
import se.johan.wendler.fragment.DrawerWorkoutNavigationFragment;
import se.johan.wendler.fragment.base.DrawerFragment;
import se.johan.wendler.model.ListItemType;
import se.johan.wendler.model.WendlerListItem;
import se.johan.wendler.sql.SqlHandler;
import se.johan.wendler.util.Constants;
import se.johan.wendler.util.MathHelper;
import se.johan.wendler.util.PreferenceUtil;
import se.johan.wendler.util.Util;
import se.johan.wendler.util.WendlerConstants;
import se.johan.wendler.util.WendlerizedLog;
import se.johan.wendler.view.MyDrawerLayout;
import se.johan.wendler.view.MyDrawerLayout.OnHideListener;

/**
 * Activity handling the main view in the app, including the drawer
 */
public class MainActivity extends BaseActivity {

    public static final int REQUEST_WORKOUT_RESULT = 1337;
    public static final String ACTION_UPDATE = "action_update";

    private static final String EXTRA_SAVED_SELECTION = "savedSelection";
    private static final String EXTRA_SAVED_EDIT_PAGE = "savedEditPage";
    private static final String EXTRA_IS_DRAWER_OPEN = "isDrawerOpen";

    private ArrayList<WendlerListItem> mListItems;
    private MyDrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private boolean mDrawerIsLocked = false;
    private boolean mIsDrawerOpen = false;
    private DrawerAdapter mAdapter;
    private int mSavedSelection = 0;
    private ActionBarDrawerToggle mDrawerToggle;

    /**
     * Called when the activity is created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_view);

        mListItems = generateListItems();

        mDrawerLayout = (MyDrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerToggle = getActionBarDrawerToggle();

        mAdapter = new DrawerAdapter(this, mListItems);
        mDrawerList.setAdapter(mAdapter);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        if (getResources().getBoolean(R.bool.drawer_locked_open)) {
            mDrawerLayout.setScrimColor(getResources().getColor(R.color.drawer_no_shadow));
            mDrawerIsLocked = true;
            mDrawerLayout.setOnHideListener(mOnHideListener);
        }

        if (!mDrawerIsLocked && getActionBar() != null) {
            mDrawerLayout.setDrawerListener(mDrawerToggle);
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
        }

        int savedPosition = 0;
        if (savedInstanceState != null) {
            mSavedSelection = savedInstanceState.getInt(EXTRA_SAVED_SELECTION, 0);
            savedPosition = savedInstanceState.getInt(EXTRA_SAVED_EDIT_PAGE, 0);
            mIsDrawerOpen = savedInstanceState.getBoolean(EXTRA_IS_DRAWER_OPEN, false);
        }
        selectItem(mSavedSelection, savedPosition);

        showChangelogIfNeeded();
        purgeExtraExercisesIfNeeded();
        updateCycleNameIfNeeded();
        migrateFromOldRoundToValues();
    }

    /**
     * Called when the configuration has changed.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Called when an option item is selected.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);

    }

    /**
     * Called to set the title of the ActionBar.
     */
    @Override
    public void setTitle(CharSequence title) {
        if (getActionBar() != null) {
            getActionBar().setTitle(title);
        }
    }

    /**
     * Called when the Activity is resumed.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (mIsDrawerOpen && !mDrawerIsLocked) {
            WendlerizedLog.d("Drawer was previously open, open it again!");
            mDrawerLayout.openDrawer(GravityCompat.START);
        } else if (!mDrawerIsLocked) {
            WendlerizedLog.d("Drawer was previously closed, make sure it's closed!");
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    /**
     * Called when the Activity needs to save it's current instance.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save our currently selected page
        outState.putInt(EXTRA_SAVED_SELECTION, mSavedSelection);

        // If we're currently editing something in the edit section save that selection
        DrawerEditFragment fragment = (DrawerEditFragment)
                getSupportFragmentManager().findFragmentByTag(DrawerEditFragment.TAG);
        int page = fragment != null ? fragment.getCurrentPage() : 0;
        outState.putInt(EXTRA_SAVED_EDIT_PAGE, page);

        // Save the state of our drawer
        outState.putBoolean(EXTRA_IS_DRAWER_OPEN, mIsDrawerOpen);
    }

    /**
     * Called when we receive a result code from the started activity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        WendlerizedLog.d("Received result with code " + resultCode);
        Intent intent = new Intent();
        intent.setAction(ACTION_UPDATE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /**
     * Called when activity start-up is complete.
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
        if (mDrawerIsLocked) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
            mDrawerLayout.openDrawer(Gravity.START);
        } else {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }
    }

    /**
     * Create a ActionbarDrawerToggle to use.
     */
    private ActionBarDrawerToggle getActionBarDrawerToggle() {
        return new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.drawable.ic_drawer,
                R.string.title_open,
                R.string.title_close) {

            public void onDrawerClosed(View view) {
                invalidateOptionsMenu();
                mIsDrawerOpen = false;
            }

            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu();
                mIsDrawerOpen = true;
            }
        };
    }

    /**
     * Swaps fragments in the main content view if needed.
     */
    private void selectItem(int position, int editPos) {
        if (!mAdapter.isPositionSelected(position)) {
            DrawerFragment fragment = getFragmentToDisplay(position, editPos);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_frame, fragment, fragment.getFragmentTag())
                    .commit();

            setTitle(mListItems.get(position).getTitle());
        }
    }

    /**
     * Switch over the position and get the fragment to display.
     */
    private DrawerFragment getFragmentToDisplay(int position, int editPos) {
        switch (position) {
            case 0:
                return DrawerWorkoutNavigationFragment.newInstance();
            case 1:
                return DrawerAdditionalWorkoutsFragment.newInstance();
            case 2:
                return DrawerEditFragment.newInstance(editPos);
            case 3:
                return DrawerOldWorkoutsFragment.newInstance();
            default:
                return DrawerBackupManagerFragment.newInstance();
        }
    }

    /**
     * Listener for pressing back in our drawer.
     */
    private final OnHideListener mOnHideListener = new OnHideListener() {
        @Override
        public void onBackPressed() {
            MainActivity.this.finish();
        }
    };

    /**
     * When updating the extra exercises in a release the database needed to be cleared,
     * do this if needed.
     */
    private void purgeExtraExercisesIfNeeded() {
        if (!PreferenceUtil.getBoolean(this, PreferenceUtil.KEY_HAS_PURGED)) {
            WendlerizedLog.d("Purge the extra exercises!");
            SqlHandler handler = new SqlHandler(this);
            try {
                handler.open();
                handler.purgeExtraExercises();
            } catch (SQLException e) {
                WendlerizedLog.e("Error purging extra exercises", e);
            } finally {
                handler.close();
            }

            PreferenceUtil.putBoolean(this, PreferenceUtil.KEY_HAS_PURGED, true);
        }
    }


    /**
     * Migrate from the old round to values.
     */
    private void migrateFromOldRoundToValues() {
        String roundToValue = PreferenceUtil.getString(
                this,
                PreferenceUtil.KEY_ROUND_TO,
                String.valueOf(WendlerConstants.DEFAULT_ROUND_TO));

        if (roundToValue.equals("0")) {
            PreferenceUtil.putFloat(
                    this,
                    PreferenceUtil.KEY_ROUND_TO_VALUE,
                    2.5f);
            MathHelper.getInstance().resetRoundToValue();
        } else if (roundToValue.equals("1")) {
            PreferenceUtil.putFloat(
                    this,
                    PreferenceUtil.KEY_ROUND_TO_VALUE,
                    5f);
            MathHelper.getInstance().resetRoundToValue();
        }
    }

    /**
     * Called ot update the mCycle mName if needed
     */
    private void updateCycleNameIfNeeded() {
        if (!PreferenceUtil.getBoolean(this, PreferenceUtil.KEY_HAS_CYCLE_NAME)) {
            WendlerizedLog.d("Update of the mCycle mName is needed");
            SqlHandler handler = new SqlHandler(this);
            try {
                handler.open();
                handler.updateCycleName();
            } catch (SQLException e) {
                WendlerizedLog.e("Error updating mCycle mName", e);
            } finally {
                handler.close();
            }

            PreferenceUtil.putBoolean(this, PreferenceUtil.KEY_HAS_CYCLE_NAME, true);
        }
    }

    /**
     * Show the changelog at startup if the version has changed.
     */
    private void showChangelogIfNeeded() {

        String currentVersion = Util.getCurrentAppVersion(this);

        String savedVersion = PreferenceUtil.getString(
                this,
                PreferenceUtil.KEY_VERSION,
                Constants.NO_VERSION);

        if (!savedVersion.equals(currentVersion)) {
            WendlerizedLog.d("New version detected! Display changelog for " + currentVersion);
            ChangelogDialog.newInstance().show(getSupportFragmentManager(), ChangelogDialog.TAG);
            PreferenceUtil.putString(this, PreferenceUtil.KEY_VERSION, currentVersion);
        }
    }

    /**
     * Generate items for the drawer.
     */
    private ArrayList<WendlerListItem> generateListItems() {

        ArrayList<WendlerListItem> items = new ArrayList<WendlerListItem>();

        String[] titles = getResources().getStringArray(R.array.drawer);
        for (String title : titles) {
            items.add(new WendlerListItem(ListItemType.REGULAR, title, null, -1));
        }

        items.add(new WendlerListItem(
                ListItemType.SMALL,
                getString(R.string.action_item_settings),
                null,
                R.drawable.ic_action_settings));

        items.add(new WendlerListItem(
                ListItemType.SMALL,
                getString(R.string.action_item_about),
                null,
                R.drawable.ic_action_about_drawer));

        return items;
    }

    /**
     * ClickListener for the Drawer.
     */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView parent, View view, final int position, long id) {
            if (!mDrawerIsLocked) {
                mDrawerLayout.closeDrawer(mDrawerList);
            }

            // Don't update the selected item until the FragmentTransaction is completed
            switch (mListItems.get(position).getType()) {
                case REGULAR:
                    mDrawerLayout.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            selectItem(position, 0);
                        }
                    }, 200);

                    mSavedSelection = position;
                    break;

                case SMALL:
                    if (mListItems.get(position).getTitle()
                            .equals(getString(R.string.action_item_settings))) {
                        launchActivity(SettingsActivity.class);
                    } else if (mListItems.get(position).getTitle()
                            .equals(getString(R.string.action_item_about))) {
                        launchActivity(AboutActivity.class);
                    }
                    break;
            }
        }

        /**
         * Launch provided class with a delay to avoid lag when the drawer closes.
         */
        private void launchActivity(final Class<?> target) {
            mDrawerLayout.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(MainActivity.this, target));
                }
            }, 200);
        }
    }
}