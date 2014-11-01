package se.johan.wendler.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.sql.SQLException;

import se.johan.wendler.R;
import se.johan.wendler.animation.ZoomOutPageTransformer;
import se.johan.wendler.fragment.base.DrawerFragment;
import se.johan.wendler.fragment.base.EditFragment;
import se.johan.wendler.sql.SqlHandler;
import se.johan.wendler.util.Util;
import se.johan.wendler.util.WendlerizedLog;
import se.johan.wendler.ui.view.SlidingTabLayout;

/**
 * Parent fragment used for editable fragments in the drawer.
 */
public class DrawerEditFragment extends DrawerFragment {

    public static final String TAG = DrawerEditFragment.class.getName();

    private static final String EXTRA_CURRENT_PAGE = "currentPage";

    private ViewPager mViewPager;
    private SectionsPagerAdapter mAdapter;

    /**
     * Return the tag of the fragment
     */
    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Override
    public int getMessageText() {
        return R.string.help_edit;
    }

    public DrawerEditFragment() {
    }

    /**
     * Static creation to avoid problems on rotation.
     */
    public static DrawerEditFragment newInstance(int currentPage) {
        Bundle args = new Bundle();
        args.putInt(EXTRA_CURRENT_PAGE, currentPage);
        DrawerEditFragment fragment = new DrawerEditFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Called when the view is created.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_pager, container, false);

        mAdapter = new SectionsPagerAdapter(getActivity().getSupportFragmentManager());
        mViewPager = (ViewPager) view.findViewById(R.id.pager);
        mViewPager.setPageTransformer(true, new ZoomOutPageTransformer());
        mViewPager.setAdapter(mAdapter);

        SlidingTabLayout slidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        slidingTabLayout.setViewPager(mViewPager);

        int pos = 0;
        if (getArguments() != null) {
            pos = getArguments().getInt(EXTRA_CURRENT_PAGE, 0);
        }
        mViewPager.setCurrentItem(pos);

        return view;
    }

    /**
     * Called when the view is stopped.
     */
    @Override
    public void onStop() {
        super.onStop();
        SqlHandler handler = new SqlHandler(getActivity());
        try {
            handler.open();
            for (EditFragment fragment : mAdapter.getFragments()) {
                fragment.saveData(handler);
            }
        } catch (SQLException e) {
            WendlerizedLog.e("Failed to save data ", e);
        } catch (NullPointerException ignored) {
            WendlerizedLog.e("Failed to store data" , ignored);
        } finally {
            handler.close();
        }

        Util.hideKeyboard(getActivity());
    }

    /**
     * Return the current page of the fragment.
     */
    public int getCurrentPage() {
        return mViewPager.getCurrentItem();
    }

    /**
     * Adapter for the ViewPager.
     */
    private class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        private final EditFragment[] fragments;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            fragments = new EditFragment[]{
                    EditOrderFragment.newInstance(),
                    EditPercentageFragment.newInstance(),
                    EditOtherFragment.newInstance()};
        }

        /**
         * Return the child fragments.
         */
        public EditFragment[] getFragments() {
            return fragments;
        }

        /**
         * Return a fragment at a given position.
         */
        @Override
        public Fragment getItem(int position) {
            return fragments[position];
        }

        /**
         * Return the number of fragments in the view.
         */
        @Override
        public int getCount() {
            return fragments.length;
        }

        /**
         * Return the title of a given position.
         */
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.edit_order_title).toUpperCase();
                case 1:
                    return getString(R.string.edit_percentages_title).toUpperCase();
                default:
                    return getString(R.string.edit_other_title).toUpperCase();
            }
        }
    }
}
