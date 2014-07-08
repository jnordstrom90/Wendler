package se.johan.wendler.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.sql.SQLException;

import se.johan.wendler.R;
import se.johan.wendler.fragment.WorkoutNavigationListFragment;
import se.johan.wendler.sql.SqlHandler;
import se.johan.wendler.util.WendlerizedLog;

/**
 * Adapter used for the workout navigation sFragments.
 */
public class WorkoutNavigationAdapter extends FragmentStatePagerAdapter {

    private static final WorkoutNavigationListFragment[]
            sFragments = new WorkoutNavigationListFragment[4];

    private final Context mContext;

    public WorkoutNavigationAdapter(FragmentManager fm, Context context) throws SQLException {
        super(fm);
        mContext = context;
        initFragments(context);
    }

    /**
     * Initialize our fragments.
     */
    private void initFragments(Context context) throws SQLException {
        SqlHandler handler = new SqlHandler(context);
        try {
            handler.open();
            for (int i = 0; i < sFragments.length; i++) {
                sFragments[i] = WorkoutNavigationListFragment.newInstance(i + 1, handler);
            }
        } finally {
            handler.close();
        }
    }

    /**
     * Return the fragment at a given position.
     */
    @Override
    public Fragment getItem(int position) {
        return sFragments[position];
    }

    /**
     * Return the number of fragments in the array.
     */
    @Override
    public int getCount() {
        return sFragments.length;
    }

    /**
     * Return the title of a given position.
     */
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return mContext.getString(R.string.tv_week_one).toUpperCase();
            case 1:
                return mContext.getString(R.string.tv_week_two).toUpperCase();
            case 2:
                return mContext.getString(R.string.tv_week_three).toUpperCase();
            case 3:
                return mContext.getString(R.string.tv_week_four).toUpperCase();
        }
        return null;
    }

    /**
     * Return the item position of a given object.
     */
    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    /**
     * Recreate fragments.
     */
    public void recreateFragments() {
        try {
            initFragments(mContext);
        } catch (SQLException e) {
            WendlerizedLog.e("Failed to mark children as dirty", e);
        }
        notifyDataSetChanged();
    }
}
