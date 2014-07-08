package se.johan.wendler.util;

import android.content.Context;

import se.johan.wendler.R;

/**
 * Helper class for fetching translatable names of our exercises.
 */
public class StringHelper {

    public static String getTranslatableName(Context context, String workoutName) {

        if (workoutName.equals(Constants.EXERCISES[0])) {
            return context.getString(R.string.press);
        } else if (workoutName.equals(Constants.EXERCISES[1])) {
            return context.getString(R.string.deadlift);
        } else if (workoutName.equals(Constants.EXERCISES[2])) {
            return context.getString(R.string.bench);
        } else {
            return context.getString(R.string.squat);
        }
    }
}
