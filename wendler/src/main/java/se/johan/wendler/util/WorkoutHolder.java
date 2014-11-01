package se.johan.wendler.util;

import se.johan.wendler.model.Workout;

/**
 * Workaround for activities recreating themselves without saving the state. Store the workout in
 * a singleton and destroy it when finished.
 */
public class WorkoutHolder {

    private static WorkoutHolder sInstance;
    private WorkoutItem mWorkout;

    /**
     * Private constructor.
     */
    private WorkoutHolder() {
    }

    /**
     * Returns an instance of WorkoutHolder.
     */
    public static WorkoutHolder getInstance() {

        if (sInstance == null) {
            sInstance = new WorkoutHolder();
        }
        return sInstance;
    }

    /**
     * Save a WorkoutItem.
     */
    public void putWorkout(WorkoutItem workout) {
        mWorkout = workout;
    }

    /**
     * Returns a stored WorkoutItem.
     */
    public WorkoutItem getWorkout() {
        return mWorkout;
    }

    /**
     * Destroy references.
     */
    public void destroy() {
        mWorkout = null;
        sInstance = null;
    }

    /**
     * Representation of a Workout and it's parameters.
     */
    public static class WorkoutItem {

        private Workout mWorkout;
        private int mCurrentPage;
        private boolean mTimerIsRunning;
        private long mElapsedTime;

        /**
         * Constructor
         */
        public WorkoutItem(
                Workout workout, int currentPage, boolean timerIsRunning, long elapsedTime) {
            mWorkout = workout;
            mCurrentPage = currentPage;
            mTimerIsRunning = timerIsRunning;
            mElapsedTime = elapsedTime;
        }

        /**
         * Returns the workout of this item.
         */
        public Workout getWorkout() {
            return mWorkout;
        }

        /**
         * Returns the current page in the ViewPager.
         */
        public int getCurrentPage() {
            return mCurrentPage;
        }

        /**
         * Returns true if the timer is running.
         */
        public boolean isTimerRunning() {
            return mTimerIsRunning;
        }

        /**
         * Returns the elapsed time of the timer.
         */
        public long getElapsedTime() {
            return mElapsedTime;
        }
    }
}
