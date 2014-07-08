package se.johan.wendler.fragment.base;

import android.support.v4.app.Fragment;

import se.johan.wendler.sql.SqlHandler;

/**
 * Base fragment for all the different edit fragments accessed form the drawer.
 */
public abstract class EditFragment extends Fragment {

    /**
     * Called to save the needed data in the edit fragments.
     */
    public abstract void saveData(SqlHandler handler);
}
