package se.johan.wendler.util;

/**
 * Interface for handling actions on an additional exercise card.
 */
public interface CardsOptionHandler {

    /**
     * Called when an exercise should be deleted.
     */
    public void onDelete(int position);

    /**
     * Called when an exercise should be edited.
     */
    public void onEdit(int position);
}
