package se.johan.wendler.util;

import se.johan.wendler.BuildConfig;

/**
 * Class with constants.
 */
public class Constants {

    public static final boolean DEBUG = BuildConfig.DEBUG;

    public static final String[] EXERCISES = new String[]{"Press", "Deadlift", "Bench", "Squat"};

    /**
     * Default value for application version.
     */
    public static final String NO_VERSION = "-1";

    /**
     * Bundle constants
     */
    public static final String BUNDLE_EXERCISE_ITEM = "bundle_exercise_item";
    public static final String SETTINGS_BACKUP_NAME = "settings";
    public static final String WORKOUTS_BACKUP_NAME = "workouts";
}
