package se.johan.wendler.util;

/**
 * General interface for returning values from asynchronus tasks.
 */
public interface IAsyncHandler {

    /**
     * Called when the task is complete and has a value to return.
     */
    @SuppressWarnings("UnusedParameters")
    public void onResult(Object key, Object value);
}
