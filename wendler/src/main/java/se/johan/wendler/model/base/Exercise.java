package se.johan.wendler.model.base;

import java.util.ArrayList;

import se.johan.wendler.model.ExerciseSet;

/**
 * Exercise base class
 */
public abstract class Exercise {

    protected String mName;
    protected ArrayList<ExerciseSet> mExerciseSets;

    /**
     * Return the name of the main exercise.
     */
    public String getName() {
        return mName;
    }

    /**
     * Return the progress of the exercise.
     */
    public int getProgress(int position) {
        return mExerciseSets.get(position).getProgress();
    }

    /**
     * Update the progress for the exercise.
     */
    public void setProgress(int position, int progress) {
        mExerciseSets.get(position).setProgress(progress);
    }

    /**
     * Return the set at a given position.
     */
    public ExerciseSet getExerciseSet(int position) {
        return mExerciseSets.get(position);
    }

    /**
     * Return the sets
     */
    public ArrayList<ExerciseSet> getExerciseSets() {
        return mExerciseSets;
    }
}
