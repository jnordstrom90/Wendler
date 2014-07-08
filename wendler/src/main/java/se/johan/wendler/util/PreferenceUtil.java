package se.johan.wendler.util;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Utility class for handling preferences.
 */
public class PreferenceUtil {

    public static final String KEY_HAS_SEEN_FIRST_TIME_DIALOG = "key_has_seen_first_time_dialog";
    public static final String KEY_SHOW_WARM_UP = "key_show_warm_up";
    public static final String KEY_WARM_UP_SETS = "key_warm_up_sets";
    public static final String KEY_WARM_UP_REPS = "key_warm_up_reps";
    public static final String KEY_ROUND_TO = "key_round";
    public static final String KEY_DELOAD_REPS = "key_deload_reps";
    public static final String KEY_USE_VOLUME_BUTTONS = "key_use_volume_button";
    public static final String KEY_KEEP_SCREEN_ON_STOPWATCH = "key_keep_screen_on_stopwatch";
    public static final String KEY_HAS_SEEN_SHOWCASE_WORKOUTS = "key_has_seen_showcase_workouts";
    public static final String KEY_HAS_SEEN_SHOWCASE_FINISH_WORKOUTS =
            "key_has_seen_showcase_workout";
    public static final String KEY_HAS_SEEN_SHOWCASE_OLD_WORKOUTS =
            "key_has_seen_showcase_old_workouts";
    public static final String KEY_VERSION = "key_version";

    public static final String KEY_HAS_PURGED = "key_has_purged";
    public static final String KEY_HAS_CYCLE_NAME = "key_has_updated_cycle_name";
    public static final String KEY_AUTO_DELOAD = "key_auto_deload";
    public static final String KEY_RESET_CYCLE_DELOAD = "key_reset_cycle_deload";
    public static final String KEY_WEIGHT_TYPE_DELOAD = "key_weight_type_deload";
    public static final String KEY_DELOAD_TYPE = "key_deload_type";
    public static final String KEY_CUSTOM_DELOAD_TYPE = "key_custom_deload_type";
    public static final String KEY_CUSTOM_DELOAD_TYPE_VALUE = "key_custom_deload_type_value";
    public static final String KEY_CLEAR_DATA = "key_clear_data";
    public static final String KEY_TIME_OF_LAST_BACKUP = "key_time_of_last_backup";

    /**
     * Get a boolean stored in preferences. Default value is false.
     */
    public static boolean getBoolean(Context context, String tag) {
        return getBoolean(context, tag, false);
    }

    /**
     * Get a boolean stored in preferences with a default value provided.
     */
    public static boolean getBoolean(Context context, String tag, boolean defaultValue) {
        boolean bool = PreferenceManager
                .getDefaultSharedPreferences(context).getBoolean(tag, defaultValue);
        WendlerizedLog.i("Load value: " + bool + " for " + tag);
        return bool;
    }

    /**
     * Write a boolean value to preferences.
     */
    public static void putBoolean(Context context, String tag, boolean value) {
        WendlerizedLog.i("Store " + value + " for " + tag);
        PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(tag, value)
                .apply();
    }

    /**
     * Get a String stored in preferences with a default value provided.
     */
    public static String getString(Context context, String tag, String defaultValue) {
        String string = PreferenceManager.getDefaultSharedPreferences(context).getString(tag,
                defaultValue);
        WendlerizedLog.i("Load value: " + string + " for " + tag);
        return string;
    }

    /**
     * Write a String value to preferences.
     */
    public static void putString(Context context, String tag, String value) {
        WendlerizedLog.i("Store " + value + " for " + tag);
        PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit()
                .putString(tag, value)
                .apply();
    }

    /**
     * Get a float stored in preferences with a default value provided
     */
    public static float getFloat(Context context, String tag, float defaultValue) {
        Float value = Float.parseFloat(PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString(tag, String.valueOf(defaultValue)));

        WendlerizedLog.i("Load value: " + value + " for " + tag);
        return value;
    }

    public static long getLong(Context context, String tag) {
        return getLong(context, tag, 0);
    }

    public static long getLong(Context context, String tag, long defaultValue) {
        long value = Long.parseLong(PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString(tag, String.valueOf(defaultValue)));
        WendlerizedLog.i("Load value: " + value + " for " + tag);
        return value;
    }

    /**
     * Write a float value to preferences.
     */
    public static void putLong(Context context, String tag, long value) {
        WendlerizedLog.i("Store " + value + " for " + tag);
        PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit()
                .putString(tag, String.valueOf(value))
                .apply();
    }
}
