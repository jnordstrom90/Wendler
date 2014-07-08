package se.johan.wendler.fragment.base;

import android.support.v4.app.Fragment;

import se.johan.wendler.sql.SqlHandler;

/**
 * Base fragment for the initial setup fragments, these methods are shared amongst those fragments.
 */
public abstract class InitFragment extends Fragment {

    /**
     * Save needed data to the database.
     */
    public abstract void saveData(SqlHandler handler);

    /**
     * Return if all data is ok to be saved.
     */
    public abstract boolean allDataIsOk();

    /**
     * Return the helping message for this fragment.
     */
    public abstract String getHelpingMessage();

    /**
     * Notify the user of an error in the input fields.
     */
    public abstract void notifyError();

}
