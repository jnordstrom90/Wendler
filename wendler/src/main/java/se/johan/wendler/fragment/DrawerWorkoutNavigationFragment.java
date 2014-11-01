package se.johan.wendler.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.sql.SQLException;

import se.johan.wendler.R;
import se.johan.wendler.activity.MainActivity;
import se.johan.wendler.ui.adapter.WorkoutNavigationAdapter;
import se.johan.wendler.animation.ZoomOutPageTransformer;
import se.johan.wendler.fragment.base.DrawerFragment;
import se.johan.wendler.sql.SqlHandler;
import se.johan.wendler.util.WendlerizedLog;
import se.johan.wendler.ui.view.SlidingTabLayout;

/**
 * Navigation fragment
 */
public class DrawerWorkoutNavigationFragment extends DrawerFragment {

    public static final String TAG = DrawerWorkoutNavigationFragment.class.getName();

    private WorkoutNavigationAdapter mAdapter;

    /**
     * Return the tag of the fragment.
     */
    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Override
    public int getMessageText() {
        return R.string.help_main_workouts;
    }

    public DrawerWorkoutNavigationFragment() {
    }

    /**
     * Static creation to avoid problems on rotation.
     */
    public static DrawerWorkoutNavigationFragment newInstance() {
        return new DrawerWorkoutNavigationFragment();
    }

    /**
     * Called when the view is created.
     */
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_pager, container, false);

        try {
            mAdapter = new WorkoutNavigationAdapter(getChildFragmentManager(), getActivity());
        } catch (SQLException e) {
            WendlerizedLog.e("Unable to load workouts", e);
            getActivity().finish();
        }

        ViewPager mViewPager = (ViewPager) view.findViewById(R.id.pager);
        mViewPager.setPageTransformer(true, new ZoomOutPageTransformer());
        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(getCurrentSelection());

        SlidingTabLayout mSlidingTabLayout =
                (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setViewPager(mViewPager);

        return view;
    }

    /**
     * Called when the fragment is destroyed.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mUpdateReceiver);
    }

    /**
     * Called when the fragment is created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(mUpdateReceiver, new IntentFilter(MainActivity.ACTION_UPDATE));
    }

    /**
     * Return the currently selected week.
     */
    private int getCurrentSelection() {
        SqlHandler handler = new SqlHandler(getActivity());
        try {
            handler.open();
            return handler.getSelectionForNavigation();
        } catch (SQLException e) {
            WendlerizedLog.e("Failed to get current selection", e);
        } finally {
            handler.close();
        }
        return 0;
    }

    /**
     * Receiver for listening for updates on workouts.
     */
    private final BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mAdapter != null) {
                mAdapter.recreateFragments();
            }
        }
    };
}
