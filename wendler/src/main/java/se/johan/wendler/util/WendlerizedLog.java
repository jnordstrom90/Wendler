package se.johan.wendler.util;

import android.util.Log;

/**
 * Helper class for logging.
 */
public class WendlerizedLog {
    private static final String TAG = "Wendlerized";

    /**
     * Log a verbose message.
     */
    public static void v(String message) {
        if (Constants.DEBUG) {
            Log.v(TAG, message);
        }
    }

    /**
     * Log an error message.
     */
    public static void e(String message, Throwable t) {
        if (Constants.DEBUG) {
            Log.e(TAG, message, t);
        }
    }

    /**
     * Log an info message.
     */
    public static void i(String message) {
        if (Constants.DEBUG) {
            Log.i(TAG, message);
        }
    }

    /**
     * Log a debug message.
     */
    public static void d(String message) {
        if (Constants.DEBUG) {
            Log.d(TAG, message);
        }
    }
}
