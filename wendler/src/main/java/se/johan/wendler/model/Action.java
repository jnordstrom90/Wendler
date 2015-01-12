package se.johan.wendler.model;

/**
 * Action performed by the Floating Action Button
 */
public enum Action {
    SET_REPS,
    ADD_EXERCISE;

    /**
     * Interface for listening for actions
     */
    public interface ActionListener {

        /**
         * Called when an action is taken.
         */
        public void onActionTaken(Action action);
    }
}
