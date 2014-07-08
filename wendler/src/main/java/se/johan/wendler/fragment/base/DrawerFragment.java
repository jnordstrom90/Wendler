package se.johan.wendler.fragment.base;

import android.support.v4.app.Fragment;

/**
 * A class which all the fragments for the drawer extends.
 */
public abstract class DrawerFragment extends Fragment {

    /**
     * Get the tag for the currently displayed fragment
     */
    public abstract String getFragmentTag();
}
