package se.johan.wendler.fragment.base;

import android.support.v4.app.Fragment;

import se.johan.wendler.model.Workout;
import se.johan.wendler.sql.SqlHandler;

/**
 * Superclass for the workout fragments, used to easily store the data.
 */
public abstract class WorkoutFragment extends Fragment {

    /**
     * Called to store the workout.
     */
    public abstract boolean storeWorkout(boolean complete,
                                         Workout workout,
                                         SqlHandler handler,
                                         boolean delayedDeload);
}
