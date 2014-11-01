package se.johan.wendler.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import se.johan.wendler.R;
import se.johan.wendler.animation.ZoomOutPageTransformer;
import se.johan.wendler.fragment.base.DrawerFragment;
import se.johan.wendler.util.Constants;
import se.johan.wendler.ui.view.SlidingTabLayout;

/**
 * Parent fragment for adding permanent additional exercises to a workout.
 */
public class DrawerAdditionalWorkoutsFragment extends DrawerFragment {

    private static final String TAG = DrawerAdditionalWorkoutsFragment.class.getName();

    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Override
    public int getMessageText() {
        return R.string.help_additional_exercises;
    }

    public DrawerAdditionalWorkoutsFragment() {
    }

    /**
     * Static creation to avoid problems on rotation.
     */
    public static DrawerAdditionalWorkoutsFragment newInstance() {
        return new DrawerAdditionalWorkoutsFragment();
    }

    /**
     * Called when the view is created.
     */
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.view_pager, container, false);

        SectionsPagerAdapter adapter =
                new SectionsPagerAdapter(getActivity().getSupportFragmentManager());
        ViewPager viewpAger = (ViewPager) view.findViewById(R.id.pager);
        viewpAger.setPageTransformer(true, new ZoomOutPageTransformer());
        viewpAger.setAdapter(adapter);

        SlidingTabLayout slidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        slidingTabLayout.setViewPager(viewpAger);
        return view;
    }

    /**
     * Adapter for the ViewPager.
     */
    private class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        final private AddExtraExerciseFragment[] fragments = new AddExtraExerciseFragment[4];

        /**
         * Constructor for the adapter.
         */
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            for (int i = 0; i < fragments.length; i++) {
                fragments[i] = AddExtraExerciseFragment.newInstance(Constants.EXERCISES[i]);
            }
        }

        /**
         * Return the Fragment at a given position.
         */
        @Override
        public Fragment getItem(int position) {
            return fragments[position];
        }

        /**
         * Return the number of fragments in the adapter.
         */
        @Override
        public int getCount() {
            return fragments.length;
        }

        /**
         * Return the title of the tab.
         */
        @Override
        public CharSequence getPageTitle(int position) {
            return getResources().getStringArray(R.array.exercises)[position].toUpperCase();
        }
    }
}
