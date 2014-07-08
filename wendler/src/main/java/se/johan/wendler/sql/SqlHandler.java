package se.johan.wendler.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.text.format.Time;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.sql.SQLException;
import java.util.ArrayList;

import se.johan.wendler.model.AdditionalExercise;
import se.johan.wendler.model.DeloadItem;
import se.johan.wendler.model.ExerciseSet;
import se.johan.wendler.model.MainExercise;
import se.johan.wendler.model.SetType;
import se.johan.wendler.model.Workout;
import se.johan.wendler.util.Constants;
import se.johan.wendler.util.PreferenceUtil;
import se.johan.wendler.util.StringHelper;
import se.johan.wendler.util.WendlerConstants;
import se.johan.wendler.util.WendlerMath;
import se.johan.wendler.util.WendlerizedLog;

/**
 * A class for handling queries against the SQL database
 */
@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class SqlHandler {

    public static final String DATABASE_NAME = "WendlerizedDb";
    private static final int DATABASE_VERSION = 6;

    /**
     * Stats table *
     */
    private static final String DATABASE_TABLE_WENDLER_STATS = "wendler_stats";
    private static final String KEY_ROW_ID = "_id";
    private static final String KEY_TRAINING_PERCENTAGE = "working_percentage";
    private static final String KEY_1RM = "one_rm";
    private static final String KEY_ORDER = "order_in_week";
    private static final String KEY_INCREMENT = "increment";
    private static final String KEY_WEEK = "week";
    private static final String KEY_CYCLE = "cycle";
    private static final String KEY_CYCLE_NAME = "cycle_name";
    private static final String KEY_NAME = "name";
    private static final String KEY_SHOULD_DELOAD = "should_deload";

    /**
     * Percentage table *
     */
    private static final String DATABASE_TABLE_PERCENT = "wendler_percentages";
    private static final String KEY_WEEK_ONE = "percent_week_1";
    private static final String KEY_WEEK_TWO = "percent_week_2";
    private static final String KEY_WEEK_THREE = "percent_week_3";
    private static final String KEY_WEEK_FOUR = "percent_week_4";

    /**
     * Workout table *
     */
    private static final String DATABASE_TABLE_WENDLER_WORKOUT = "wendler_workout";
    private static final String KEY_WORKOUT_ID = "workout_id";
    private static final String KEY_INSERT_TIME = "insert_time";
    private static final String KEY_WORKOUT_YEAR = "workout_year";
    private static final String KEY_WORKOUT_MONTH = "workout_month";
    private static final String KEY_WORKOUT_DAY = "workout_day";
    private static final String KEY_WORKOUT_EXERCISE = "exercise";
    private static final String KEY_WORKOUT_REPS = "reps";
    private static final String KEY_WORKOUT_ONE_RM = "one_rm";
    private static final String KEY_WORKOUT_LAST_SET = "last_set_weight";
    private static final String KEY_WORKOUT_WEEK = "week";
    private static final String KEY_WORKOUT_CYCLE = "cycle";
    private static final String KEY_WORKOUT_NOTES = "notes";
    private static final String KEY_WORKOUT_WON = "workout_won";
    private static final String KEY_WORKOUT_COMPLETED = "workout_completed";

    /**
     * Extra workout table *
     */
    private static final String DATABASE_TABLE_WENDLER_EXTRA = "workout_extra";
    private static final String KEY_EXERCISE_NAME = "exercise_name";
    private static final String KEY_NUMBER_OF_SETS_OR_REPS_TO_BE_DONE = "nbr_of_sets";
    private static final String KEY_EXTRA_WEIGHT = "extra_weight";
    private static final String KEY_EXTRA_REPS_OR_SETS_COMPLETED = "extra_reps";
    private static final String KEY_EXTRA_EXERCISE_ID = "extra_exercise_id";

    /**
     * List of extra exercises, commented out ones are used but simply here for simplicity
     */
    private static final String DATABASE_TABLE_WENDLER_EXTRA_LIST = "workout_extra_list";
    //    private static final String KEY_EXERCISE_NAME = "exercise_name";
//    private static final String KEY_NUMBER_OF_SETS_OR_REPS_TO_BE_DONE = "nbr_of_sets";
//    private static final String KEY_EXTRA_WEIGHT = "extra_weight";
//    private static final String KEY_EXTRA_REPS_OR_SETS_COMPLETED = "extra_reps";
    private static final String KEY_EXTRA_PERCENTAGE_OF_MAIN_EXERCISE
            = "extra_percentage_of_main_exercise";
    private static final String KEY_EXTRA_ORDER_IN_LIST = "extra_order_in_list";
    private static final String KEY_EXTRA_MAIN_EXERCISE_NAME =
            "extra_main_exercise_name";
    private static final String KEY_IS_BBB = "is_bbb";

    private final Context mContext;
    private DbHelper mDbHelper;
    private SQLiteDatabase mDatabase;
    private boolean mIsOpen;

    /**
     * Constructor
     */
    public SqlHandler(Context context) {
        mContext = context;
    }

    /**
     * Open the connection to the SQLite database.
     */
    public void open() throws SQLException {
        if (mIsOpen && mDbHelper != null && mDatabase != null) {
            return;
        }
        mDbHelper = new DbHelper(mContext);
        mDatabase = mDbHelper.getWritableDatabase();
        mIsOpen = true;
    }

    /**
     * Close the connection to the database.
     */
    public void close() {
        if (mIsOpen && mDbHelper != null) {
            mDbHelper.close();
            mIsOpen = false;
        }
    }

    /**
     * Insert one rm for all exercises as well as the workout percentage.
     */
    public void insertOneRmAndWorkoutPercentage(int pressOneRm,
                                                int deadliftOneRm,
                                                int benchOneRm,
                                                int squatOneRm,
                                                int workoutPercentage) {
        ContentValues cv = new ContentValues();
        cv.put(KEY_1RM, pressOneRm);
        cv.put(KEY_CYCLE, 1);
        cv.put(KEY_CYCLE_NAME, 1);
        cv.put(KEY_WEEK, 1);
        cv.put(KEY_TRAINING_PERCENTAGE, workoutPercentage);
        cv.put(KEY_NAME, Constants.EXERCISES[0]);
        mDatabase.insert(DATABASE_TABLE_WENDLER_STATS, null, cv);

        cv.clear();
        cv.put(KEY_1RM, deadliftOneRm);
        cv.put(KEY_CYCLE, 1);
        cv.put(KEY_CYCLE_NAME, 1);
        cv.put(KEY_WEEK, 1);
        cv.put(KEY_TRAINING_PERCENTAGE, workoutPercentage);
        cv.put(KEY_NAME, Constants.EXERCISES[1]);
        mDatabase.insert(DATABASE_TABLE_WENDLER_STATS, null, cv);

        cv.clear();
        cv.put(KEY_1RM, benchOneRm);
        cv.put(KEY_CYCLE, 1);
        cv.put(KEY_CYCLE_NAME, 1);
        cv.put(KEY_WEEK, 1);
        cv.put(KEY_TRAINING_PERCENTAGE, workoutPercentage);
        cv.put(KEY_NAME, Constants.EXERCISES[2]);
        mDatabase.insert(DATABASE_TABLE_WENDLER_STATS, null, cv);

        cv.clear();
        cv.put(KEY_1RM, squatOneRm);
        cv.put(KEY_CYCLE, 1);
        cv.put(KEY_CYCLE_NAME, 1);
        cv.put(KEY_WEEK, 1);
        cv.put(KEY_TRAINING_PERCENTAGE, workoutPercentage);
        cv.put(KEY_NAME, Constants.EXERCISES[3]);
        mDatabase.insert(DATABASE_TABLE_WENDLER_STATS, null, cv);
    }

    /**
     * Insert the order of workouts.
     */
    public void insertExerciseOrder(int pressDay, int deadliftDay, int benchDay, int squatDay) {

        ContentValues cv = new ContentValues();

        cv.put(KEY_ORDER, pressDay);
        mDatabase.update(DATABASE_TABLE_WENDLER_STATS, cv, KEY_NAME + "=?",
                new String[]{Constants.EXERCISES[0]});

        cv.clear();
        cv.put(KEY_ORDER, deadliftDay);
        mDatabase.update(DATABASE_TABLE_WENDLER_STATS, cv, KEY_NAME + "=?",
                new String[]{Constants.EXERCISES[1]});

        cv.clear();
        cv.put(KEY_ORDER, benchDay);
        mDatabase.update(DATABASE_TABLE_WENDLER_STATS, cv, KEY_NAME + "=?",
                new String[]{Constants.EXERCISES[2]});

        cv.clear();
        cv.put(KEY_ORDER, squatDay);
        mDatabase.update(DATABASE_TABLE_WENDLER_STATS, cv, KEY_NAME + "=?",
                new String[]{Constants.EXERCISES[3]});

    }

    /**
     * Insert the percentages for our workouts.
     */
    public void insertWeekPercentages(
            int[] weekOne, int[] weekTwo, int[] weekThree, int[] weekFour) {
        ContentValues cv = new ContentValues();

        cv.put(KEY_WEEK_ONE, weekOne[0]);
        cv.put(KEY_WEEK_TWO, weekTwo[0]);
        cv.put(KEY_WEEK_THREE, weekThree[0]);
        cv.put(KEY_WEEK_FOUR, weekFour[0]);

        mDatabase.insert(DATABASE_TABLE_PERCENT, null, cv);

        cv.clear();
        cv.put(KEY_WEEK_ONE, weekOne[1]);
        cv.put(KEY_WEEK_TWO, weekTwo[1]);
        cv.put(KEY_WEEK_THREE, weekThree[1]);
        cv.put(KEY_WEEK_FOUR, weekFour[1]);

        mDatabase.insert(DATABASE_TABLE_PERCENT, null, cv);

        cv.clear();
        cv.put(KEY_WEEK_ONE, weekOne[2]);
        cv.put(KEY_WEEK_TWO, weekTwo[2]);
        cv.put(KEY_WEEK_THREE, weekThree[2]);
        cv.put(KEY_WEEK_FOUR, weekFour[2]);

        mDatabase.insert(DATABASE_TABLE_PERCENT, null, cv);
    }

    /**
     * Update the percentages of our workouts.
     */
    public void updatePercentages(int[] weekOne, int[] weekTwo, int[] weekThree, int[] weekFour) {
        ContentValues cv = new ContentValues();

        cv.put(KEY_WEEK_ONE, weekOne[0]);
        cv.put(KEY_WEEK_TWO, weekTwo[0]);
        cv.put(KEY_WEEK_THREE, weekThree[0]);
        cv.put(KEY_WEEK_FOUR, weekFour[0]);

        mDatabase.update(DATABASE_TABLE_PERCENT, cv, KEY_ROW_ID + "=?", new String[]{"1"});

        cv.clear();
        cv.put(KEY_WEEK_ONE, weekOne[1]);
        cv.put(KEY_WEEK_TWO, weekTwo[1]);
        cv.put(KEY_WEEK_THREE, weekThree[1]);
        cv.put(KEY_WEEK_FOUR, weekFour[1]);

        mDatabase.update(DATABASE_TABLE_PERCENT, cv, KEY_ROW_ID + "=?", new String[]{"2"});

        cv.clear();
        cv.put(KEY_WEEK_ONE, weekOne[2]);
        cv.put(KEY_WEEK_TWO, weekTwo[2]);
        cv.put(KEY_WEEK_THREE, weekThree[2]);
        cv.put(KEY_WEEK_FOUR, weekFour[2]);

        mDatabase.update(DATABASE_TABLE_PERCENT, cv, KEY_ROW_ID + "=?", new String[]{"3"});
    }

    /**
     * Insert the initial increments for workouts.
     */
    public void insertIncrements(double pressIncrement,
                                 double deadliftIncrement,
                                 double benchIncrement,
                                 double squatIncrement) {

        ContentValues cv = new ContentValues();
        cv.put(KEY_INCREMENT, pressIncrement);
        mDatabase.update(DATABASE_TABLE_WENDLER_STATS, cv, KEY_NAME + "=?",
                new String[]{Constants.EXERCISES[0]});

        cv.clear();
        cv.put(KEY_INCREMENT, deadliftIncrement);
        mDatabase.update(DATABASE_TABLE_WENDLER_STATS, cv, KEY_NAME + "=?",
                new String[]{Constants.EXERCISES[1]});

        cv.clear();
        cv.put(KEY_INCREMENT, benchIncrement);
        mDatabase.update(DATABASE_TABLE_WENDLER_STATS, cv, KEY_NAME + "=?",
                new String[]{Constants.EXERCISES[2]});

        cv.clear();
        cv.put(KEY_INCREMENT, squatIncrement);
        mDatabase.update(DATABASE_TABLE_WENDLER_STATS, cv, KEY_NAME + "=?",
                new String[]{Constants.EXERCISES[3]});
    }

    /**
     * Return if the data base is initialized.
     */
    public boolean isInitialized() {
        Cursor c = null;
        try {
            c = mDatabase.query(DATABASE_TABLE_WENDLER_STATS, new String[]{KEY_ROW_ID},
                    null, null, null, null, null);
            return c != null && c.moveToFirst();
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    /**
     * Return a list of workouts for a given week.
     */
    public ArrayList<Workout> getWorkoutsForList(int week) {
        ArrayList<Workout> list = new ArrayList<Workout>();

        String[] names = getExerciseNamesInOrder();

        for (String name : names) {
            int cycle = getCurrentCycle(name);
            int cycleName = getCurrentCycleName(name);
            int id = exerciseHasBeenDone(name, week, cycle);

            if (id != -1) {
                boolean isWorkoutComplete = isWorkoutComplete(id);
                list.add(new Workout(name, StringHelper.getTranslatableName(mContext, name),
                        isWorkoutComplete, exerciseWon(id),
                        week, cycle, cycleName, id, getMainExerciseForWorkout(isWorkoutComplete, id)
                        , getExtraExerciseForWorkout(name, id, isWorkoutComplete),
                        getInsertTimeForId(id), getTimeForWorkout(id), getNotesForWorkout(id)));
            } else {
                list.add(new Workout(name, StringHelper.getTranslatableName(mContext, name),
                        week, cycle, cycleName, -1));
            }
        }
        return list;
    }

    /**
     * Return the names of our exercises in order converted to references a string item
     */
    public String[] getExerciseNamesInOrder() {
        String[] arr = new String[4];

        Cursor cursor = null;
        try {
            cursor = mDatabase.query(DATABASE_TABLE_WENDLER_STATS, new String[]{KEY_NAME},
                    null, null, null, null, KEY_ORDER + " ASC ");
            int i = 0;
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    arr[i] = cursor.getString(cursor.getColumnIndex(KEY_NAME));
                    i++;
                } while (cursor.moveToNext() && i < arr.length);
            }
            return arr;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Return main exercise for a given workout
     */
    public MainExercise getMainExerciseForWorkout(Workout workout) {
        String[] columns =
                new String[]{KEY_TRAINING_PERCENTAGE, KEY_1RM, KEY_INCREMENT, KEY_NAME};

        Cursor cursor = null;
        try {
            cursor = mDatabase.query(DATABASE_TABLE_WENDLER_STATS, columns,
                    KEY_NAME + "=?", new String[]{workout.getName()}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                String name = workout.getName();
                double increment = cursor.getFloat(cursor.getColumnIndex(KEY_INCREMENT));

                int workoutPercentage = cursor.getInt(cursor.getColumnIndex
                        (KEY_TRAINING_PERCENTAGE));
                double oneRm = cursor.getFloat(cursor.getColumnIndex(KEY_1RM));

                int[] setPercentages = getSetPercentages(workout.getWeek());

                boolean showWarmUp = PreferenceUtil.getBoolean(
                        mContext, PreferenceUtil.KEY_SHOW_WARM_UP, true);

                ArrayList<ExerciseSet> sets = new ArrayList<ExerciseSet>();

                if (showWarmUp) {
                    String serialized = PreferenceUtil.getString(
                            mContext,
                            PreferenceUtil.KEY_WARM_UP_SETS,
                            WendlerConstants.DEFAULT_WARMUP_PERCENTAGES);

                    sets.addAll(WendlerMath.getWarmupSets(
                            mContext, oneRm, serialized.split(","), workoutPercentage, -1));
                }

                sets.addAll(WendlerMath.getWorkoutSets(
                        mContext, oneRm, setPercentages, workoutPercentage, workout.getWeek(), -1));

                return new MainExercise(name, oneRm, increment, sets, workoutPercentage);
            }
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Return the set percentages for a given week.
     */
    public int[] getSetPercentages(int week) {
        int[] percentages = new int[3];

        String[] columns = new String[]{"percent_week_" + week};
        Cursor cursor = null;
        try {
            cursor = mDatabase.query(DATABASE_TABLE_PERCENT, columns, null, null, null, null,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                for (int i = 0; i < percentages.length; i++) {
                    percentages[i] = cursor.getInt(cursor.getColumnIndex(columns[0]));
                    cursor.moveToNext();
                }
            }
            return percentages;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Return the extra exercises for a given workout.
     */
    public ArrayList<AdditionalExercise> getExtraExerciseForWorkout(Workout workout) {
        ArrayList<AdditionalExercise> exercises = new ArrayList<AdditionalExercise>();

        Cursor cursor = null;

        try {
            cursor = mDatabase.query(DATABASE_TABLE_WENDLER_EXTRA_LIST, null,
                    KEY_WORKOUT_EXERCISE + "=?", new String[]{workout.getName()}, null, null,
                    KEY_EXTRA_ORDER_IN_LIST + " ASC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String name = cursor.getString(cursor.getColumnIndex(KEY_EXERCISE_NAME));

                    int sets = cursor.getInt(cursor.getColumnIndex
                            (KEY_NUMBER_OF_SETS_OR_REPS_TO_BE_DONE));
                    double weight = cursor.getDouble(cursor.getColumnIndex(KEY_EXTRA_WEIGHT));
                    int percentage = cursor.getInt(cursor.getColumnIndex
                            (KEY_EXTRA_PERCENTAGE_OF_MAIN_EXERCISE));
                    String mainExerciseName = cursor.getString(cursor.getColumnIndex
                            (KEY_EXTRA_MAIN_EXERCISE_NAME));

                    int exerciseId = cursor.getInt(cursor.getColumnIndex(KEY_EXTRA_EXERCISE_ID));

                    ArrayList<ExerciseSet> exerciseSets = new ArrayList<ExerciseSet>();

                    if (!TextUtils.isEmpty(mainExerciseName)) {
                        weight = WendlerMath.calculateSetWeight(mContext,
                                getTrainingMax(mainExerciseName), percentage, 100);
                    }

                    ExerciseSet set = new ExerciseSet(SetType.REGULAR, weight, sets, 0, false);
                    exerciseSets.add(set);

                    AdditionalExercise exercise = new AdditionalExercise(
                            name,
                            exerciseSets,
                            mainExerciseName,
                            percentage,
                            exerciseId);
                    exercises.add(exercise);
                } while (cursor.moveToNext());
            }

            return exercises;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Get the current one rm for a given exercise.
     */
    public double getOneRmForExercise(String name) {
        double weight = 100;
        Cursor cursor = null;
        try {
            cursor = mDatabase.query(DATABASE_TABLE_WENDLER_STATS, new String[]{KEY_1RM},
                    KEY_NAME + "=?", new String[]{name}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                weight = cursor.getDouble(cursor.getColumnIndex(KEY_1RM));
            }
            return weight;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Get the current training max for a given exercise.
     */
    public double getTrainingMax(String name) {
        double weight = 100;
        int workoutPercentage = 100;
        String[] columns = new String[]{KEY_1RM, KEY_TRAINING_PERCENTAGE};
        Cursor cursor = null;
        try {
            cursor = mDatabase.query(DATABASE_TABLE_WENDLER_STATS, columns, KEY_NAME + "=?",
                    new String[]{name}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                weight = cursor.getDouble(cursor.getColumnIndex(KEY_1RM));
                workoutPercentage = cursor.getInt(cursor.getColumnIndex(KEY_TRAINING_PERCENTAGE));
            }
            return WendlerMath.calculateSetWeight(mContext, weight, workoutPercentage, 100);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Return additional exercise names stored as permanent exercises.
     */
    public ArrayList<String> getAdditionalExerciseNames() {
        String[] column = new String[]{KEY_EXERCISE_NAME};
        ArrayList<String> names = new ArrayList<String>();
        Cursor cursor = null;
        try {
            cursor = mDatabase.query(DATABASE_TABLE_WENDLER_EXTRA_LIST, column, null, null,
                    null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    names.add(cursor.getString(cursor.getColumnIndex(KEY_EXERCISE_NAME)));
                } while (cursor.moveToNext());
            }
            return names;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Return the next workout id.
     */
    public int getNextWorkoutId() {
        int nextWorkoutId = 1;
        Cursor cursor = null;
        try {
            cursor = mDatabase.query(DATABASE_TABLE_WENDLER_WORKOUT, new String[]{KEY_ROW_ID},
                    null, null, null, null, KEY_ROW_ID + " ASC");
            if (cursor != null && cursor.moveToLast()) {
                nextWorkoutId = cursor.getInt(cursor.getColumnIndex(KEY_ROW_ID));
                nextWorkoutId++;
            }
            return nextWorkoutId;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

    }

    /**
     * Store the main exercise and return if it was successful.
     */
    public boolean storeMainExercise(Workout workout, boolean complete) {
        ContentValues cv = new ContentValues();

        double oneRm = workout.getMainExercise().getWeight();
        int size = workout.getMainExercise().getExerciseSets().size();
        double lastSet = workout.getMainExercise().getExerciseSet(size - 1).getWeight();

        cv.put(KEY_WORKOUT_ID, workout.getWorkoutId());
        cv.put(KEY_WORKOUT_EXERCISE, workout.getName());
        cv.put(KEY_WORKOUT_REPS, workout.getMainExercise().getLastSetProgress());
        cv.put(KEY_WORKOUT_LAST_SET, lastSet);
        cv.put(KEY_WORKOUT_ONE_RM, oneRm);
        cv.put(KEY_WORKOUT_WEEK, workout.getWeek());
        cv.put(KEY_WORKOUT_CYCLE, workout.getCycle());
        cv.put(KEY_CYCLE_NAME, workout.getCycleDisplayName());
        cv.put(KEY_WORKOUT_NOTES, workout.getNotes());
        cv.put(KEY_WORKOUT_WON, workout.isWon() ? 1 : 0);
        cv.put(KEY_INSERT_TIME, workout.getInsertTime());
        cv.put(KEY_WORKOUT_YEAR, workout.getWorkoutTime().year);
        cv.put(KEY_WORKOUT_MONTH, workout.getWorkoutTime().month);
        cv.put(KEY_WORKOUT_DAY, workout.getWorkoutTime().monthDay);

        cv.put(KEY_TRAINING_PERCENTAGE, workout.getMainExercise().getWorkoutPercentage());

        if (!workout.isComplete()) {
            cv.put(KEY_WORKOUT_COMPLETED, complete ? 1 : 0);
        } else {
            cv.put(KEY_WORKOUT_COMPLETED, 1);
        }
        if (isWorkoutNew(workout.getWorkoutId())) {
            return mDatabase.insert(DATABASE_TABLE_WENDLER_WORKOUT, null, cv) != -1;
        }
        WendlerizedLog.d("Update! " + workout.getMainExercise().getLastSetProgress());
        return mDatabase.update(DATABASE_TABLE_WENDLER_WORKOUT, cv, KEY_WORKOUT_ID + "=?",
                new String[]{String.valueOf(workout.getWorkoutId())}) != -1;
    }

    /**
     * Return if the workout is new.
     */
    private boolean isWorkoutNew(int workoutId) {
        Cursor cursor = null;
        try {
            cursor = mDatabase.query(DATABASE_TABLE_WENDLER_WORKOUT, null,
                    KEY_WORKOUT_ID + "=?", new String[]{String.valueOf(workoutId)},
                    null, null, null);
            return cursor != null && cursor.getCount() != 1;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Store the additional workouts for a workout.
     */
    public boolean storeAdditionalExercise(int workoutId, ArrayList<AdditionalExercise> exercises) {
        mDatabase.delete(
                DATABASE_TABLE_WENDLER_EXTRA, KEY_WORKOUT_ID + "=?",
                new String[]{String.valueOf(workoutId)});

        for (AdditionalExercise exercise : exercises) {
            ContentValues cv = new ContentValues();
            cv.put(KEY_WORKOUT_ID, workoutId);
            cv.put(KEY_EXERCISE_NAME, exercise.getName());
            cv.put(KEY_NUMBER_OF_SETS_OR_REPS_TO_BE_DONE, exercise.getExerciseSet(0).getGoal());
            cv.put(KEY_EXTRA_REPS_OR_SETS_COMPLETED, exercise.getExerciseSet(0).getProgress());
            cv.put(KEY_EXTRA_WEIGHT, exercise.getExerciseSet(0).getWeight());
            cv.put(KEY_EXTRA_MAIN_EXERCISE_NAME, exercise.getMainExerciseName());
            cv.put(KEY_EXTRA_PERCENTAGE_OF_MAIN_EXERCISE, exercise.getMainExercisePercentage());
            cv.put(KEY_EXTRA_EXERCISE_ID, exercise.getExerciseId());
            mDatabase.insert(DATABASE_TABLE_WENDLER_EXTRA, null, cv);
        }

        if (exercises.isEmpty()) {
            mDatabase.delete(DATABASE_TABLE_WENDLER_EXTRA, KEY_WORKOUT_ID + "=?",
                    new String[]{String.valueOf(workoutId)});
        }

        return true;
    }

    /**
     * Update the stats for an exercise.
     */
    public void updateWorkoutStats(Workout workout, boolean isNew) {
        if (!workout.isComplete()) {
            return;
        }
        if (isNew) {
            updateNewWorkoutStats(workout);
        } else if (isWorkoutLatest(workout)) {
            updateLatestWorkoutStats(workout);
        }
    }

    /**
     * Return the increment for an exercise.
     */
    public double getIncrement(String exercise) {
        Cursor cursor = null;
        try {
            cursor = mDatabase.query(DATABASE_TABLE_WENDLER_STATS, new String[]{KEY_INCREMENT},
                    KEY_NAME + "=?", new String[]{exercise}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                String increment = cursor.getString(cursor.getColumnIndex(KEY_INCREMENT));
                if (increment == null) {
                    return 2.5;
                }
                return Double.parseDouble(increment);
            }
            return 2.5;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Return the selection for the navigation.
     */
    public int getSelectionForNavigation() {
        Cursor cursor = null;
        try {
            cursor = mDatabase.query(DATABASE_TABLE_WENDLER_STATS, null,
                    null, null, null, null, KEY_CYCLE + " ASC, " + KEY_WEEK + " ASC");
            if (cursor != null && cursor.moveToFirst()) {
                return Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_WEEK))) - 1;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return 0;
    }

    /**
     * Store an additional exercise in the table for permanent exercises.
     */
    public void storeAdditionalExercise(AdditionalExercise exercise,
                                        String workoutExercise,
                                        boolean isNew,
                                        int position) {

        ContentValues cv = new ContentValues();
        cv.put(KEY_EXERCISE_NAME, exercise.getName());
        cv.put(KEY_WORKOUT_EXERCISE, workoutExercise);
        cv.put(KEY_NUMBER_OF_SETS_OR_REPS_TO_BE_DONE, exercise.getExerciseSet(0).getGoal());
        cv.put(KEY_EXTRA_WEIGHT, exercise.getExerciseSet(0).getWeight());
        cv.put(KEY_EXTRA_MAIN_EXERCISE_NAME, exercise.getMainExerciseName());
        cv.put(KEY_EXTRA_PERCENTAGE_OF_MAIN_EXERCISE, exercise.getMainExercisePercentage());
        cv.put(KEY_EXTRA_ORDER_IN_LIST, position);
        cv.put(KEY_EXTRA_EXERCISE_ID, exercise.getExerciseId());

        if (isNew) {
            mDatabase.insert(DATABASE_TABLE_WENDLER_EXTRA_LIST, null, cv);
        } else {
            mDatabase.update(DATABASE_TABLE_WENDLER_EXTRA_LIST, cv,
                    KEY_EXTRA_EXERCISE_ID + "=? " + "AND " + KEY_WORKOUT_EXERCISE + "=?",
                    new String[]{String.valueOf(exercise.getExerciseId()), workoutExercise}
            );
        }
    }

    /**
     * Return if a given extra exercise id already exists for a workout.
     */
    public boolean extraExerciseIsNew(String workoutName, int exerciseId) {
        Cursor cursor = null;
        try {
            cursor = mDatabase.query(DATABASE_TABLE_WENDLER_EXTRA_LIST, null,
                    KEY_WORKOUT_EXERCISE + "=? AND " + KEY_EXTRA_EXERCISE_ID + "=?",
                    new String[]{workoutName, String.valueOf(exerciseId)}, null, null, null);
            return cursor != null && cursor.getCount() == 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Delete an additional exercise for a given workout.
     */
    public void deleteAdditionalExercise(String workoutName, AdditionalExercise exercise) {
        mDatabase.delete(DATABASE_TABLE_WENDLER_EXTRA_LIST, KEY_WORKOUT_EXERCISE + "=? AND " +
                        KEY_EXTRA_EXERCISE_ID + "=?",
                new String[]{workoutName, String.valueOf(exercise.getExerciseId())}
        );
    }

    /**
     * Reorder the additional exercises for a given workout.
     */
    public void doReorderAdditionalExercise(ArrayList<AdditionalExercise> exercises,
                                            String workoutName) {
        for (int i = 0; i < exercises.size(); i++) {
            ContentValues values = new ContentValues();
            values.put(KEY_EXTRA_ORDER_IN_LIST, i);
            mDatabase.update(DATABASE_TABLE_WENDLER_EXTRA_LIST, values,
                    KEY_EXTRA_EXERCISE_ID + "=? AND " + KEY_WORKOUT_EXERCISE + "=?",
                    new String[]{String.valueOf(exercises.get(i).getExerciseId()), workoutName});
        }
    }

    /**
     * Return a list of old workouts.
     */
    public ArrayList<Workout> getOldWorkouts(int limit) {
        ArrayList<Workout> workouts = new ArrayList<Workout>();

        Cursor cursor = null;
        try {
            cursor = mDatabase.query(DATABASE_TABLE_WENDLER_WORKOUT, null,
                    KEY_WORKOUT_COMPLETED + "=?", new String[]{"1"}, null, null,
                    KEY_INSERT_TIME + " DESC", String.valueOf(limit));
            if (cursor.moveToFirst()) {
                do {
                    String name = cursor.getString(cursor.getColumnIndex(KEY_WORKOUT_EXERCISE));
                    boolean isWon =
                            cursor.getInt(cursor.getColumnIndex(KEY_WORKOUT_WON)) == 1;
                    int week = cursor.getInt(cursor.getColumnIndex(KEY_WORKOUT_WEEK));
                    int cycle = cursor.getInt(cursor.getColumnIndex(KEY_WORKOUT_CYCLE));
                    int cycleName = cursor.getInt(cursor.getColumnIndex(KEY_CYCLE_NAME));
                    int id = cursor.getInt(cursor.getColumnIndex(KEY_WORKOUT_ID));
                    MainExercise mainExercise = getMainExerciseForWorkout(true, id);

                    ArrayList<AdditionalExercise> additionalExercises =
                            getExtraExerciseForWorkout(name, id, true);

                    long insertTime = Long.parseLong(
                            cursor.getString(cursor.getColumnIndex(KEY_INSERT_TIME)));

                    Time time = new Time();
                    time.year = Integer.parseInt(
                            cursor.getString(cursor.getColumnIndex(KEY_WORKOUT_YEAR)));
                    time.month = Integer.parseInt(
                            cursor.getString(cursor.getColumnIndex(KEY_WORKOUT_MONTH)));
                    time.monthDay = Integer.parseInt(
                            cursor.getString(cursor.getColumnIndex(KEY_WORKOUT_DAY)));

                    String notes = cursor.getString(cursor.getColumnIndex(KEY_WORKOUT_NOTES));

                    Workout workout = new Workout(name, StringHelper.getTranslatableName(mContext,
                            name), true, isWon, week, cycle, cycleName, id,
                            mainExercise, additionalExercises, insertTime, time, notes
                    );

                    workouts.add(workout);
                } while (cursor.moveToNext());
            }
            return workouts;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Delete an old workout.
     */
    public void deleteWorkout(Workout workout) {
        if (isWorkoutLatest(workout)) {

            int week = workout.getWeek();
            int cycle = workout.getCycle();
            int cycleName = workout.getCycleDisplayName();
            double weight = workout.getMainExercise().getWeight();

            if (workout.isWon() && workout.getWeek() == 4) {
                weight = getOneRmForExercise(workout.getName());
                weight -= getIncrement(workout.getName());
            }
            updateWithStats(workout.getName(), cycle, week, weight, cycleName);
            setWorkoutShouldDeload(workout.getName(), getWorkoutShouldDeload(workout));
        }

        mDatabase.delete(DATABASE_TABLE_WENDLER_WORKOUT, KEY_WORKOUT_ID + "=?",
                new String[]{String.valueOf(workout.getWorkoutId())});
        mDatabase.delete(DATABASE_TABLE_WENDLER_EXTRA, KEY_WORKOUT_ID + "=?",
                new String[]{String.valueOf(workout.getWorkoutId())});
    }

    /**
     * Return the number of old workouts.
     */
    public int getOldWorkoutsCount() {
        Cursor cursor = null;
        try {
            cursor = mDatabase.query(DATABASE_TABLE_WENDLER_WORKOUT, null,
                    KEY_WORKOUT_COMPLETED + "=?", new String[]{"1"}, null,
                    null, null);
            return cursor == null ? 0 : cursor.getCount();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

    }

    /**
     * Store a workout.
     */
    public void storeWorkout(Workout workout) {
        storeMainExercise(workout, workout.isComplete());
        storeAdditionalExercise(workout.getWorkoutId(), workout.getAdditionalExercises());

        if (isWorkoutLatest(workout)) {
            updateWorkoutStats(workout, true);
        }
    }

    /**
     * Update the one rm maximum for all exercises.
     */
    public void updateOneRm(
            double pressOneRm, double deadliftOneRm, double benchOneRm, double squatOneRm) {

        ContentValues cv = new ContentValues();
        cv.put(KEY_1RM, pressOneRm);
        mDatabase.update(DATABASE_TABLE_WENDLER_STATS, cv, KEY_NAME + "=?",
                new String[]{Constants.EXERCISES[0]});

        cv.clear();
        cv.put(KEY_1RM, deadliftOneRm);
        mDatabase.update(DATABASE_TABLE_WENDLER_STATS, cv, KEY_NAME + "=?",
                new String[]{Constants.EXERCISES[1]});

        cv.clear();
        cv.put(KEY_1RM, benchOneRm);
        mDatabase.update(DATABASE_TABLE_WENDLER_STATS, cv, KEY_NAME + "=?",
                new String[]{Constants.EXERCISES[2]});

        cv.clear();
        cv.put(KEY_1RM, squatOneRm);
        mDatabase.update(DATABASE_TABLE_WENDLER_STATS, cv, KEY_NAME + "=?",
                new String[]{Constants.EXERCISES[3]});
    }

    /**
     * Delete all extra exercises.
     */
    public void purgeExtraExercises() {
        mDatabase.delete(DATABASE_TABLE_WENDLER_EXTRA_LIST, null, null);
    }

    /**
     * Return if the workout should deload.
     */
    public boolean doDeload(Workout workout) {

        if (workout.getWeek() < 4) {
            return false;
        }
        Cursor cursor = null;
        try {
            cursor = mDatabase.query(DATABASE_TABLE_WENDLER_STATS, null,
                    KEY_NAME + "=? AND " + KEY_SHOULD_DELOAD + "=?",
                    new String[]{workout.getName(), "1"}, null, null, null);
            boolean bool = cursor != null && cursor.getCount() == 1;

            if (bool) {
                setWorkoutShouldDeload(workout.getName(), false);
            }

            return bool;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

    }

    /**
     * Update the cycle name for all workouts.
     */
    public void updateCycleName() {
        ContentValues cv = new ContentValues();

        int cycle = getCurrentCycle(Constants.EXERCISES[0]);
        cv.put(KEY_CYCLE_NAME, cycle);
        mDatabase.update(DATABASE_TABLE_WENDLER_STATS, cv, KEY_NAME + "=?",
                new String[]{Constants.EXERCISES[0]});

        cv.clear();
        cycle = getCurrentCycle(Constants.EXERCISES[1]);
        cv.put(KEY_CYCLE_NAME, cycle);
        mDatabase.update(DATABASE_TABLE_WENDLER_STATS, cv, KEY_NAME + "=?",
                new String[]{Constants.EXERCISES[1]});

        cv.clear();
        cycle = getCurrentCycle(Constants.EXERCISES[2]);
        cv.put(KEY_CYCLE_NAME, cycle);
        mDatabase.update(DATABASE_TABLE_WENDLER_STATS, cv, KEY_NAME + "=?",
                new String[]{Constants.EXERCISES[2]});

        cv.clear();
        cycle = getCurrentCycle(Constants.EXERCISES[3]);
        cv.put(KEY_CYCLE_NAME, cycle);
        mDatabase.update(DATABASE_TABLE_WENDLER_STATS, cv, KEY_NAME + "=?",
                new String[]{Constants.EXERCISES[3]});

    }

    /**
     * Restore the database from a save file.
     */
    public void restoreDbFromFile(File file) {
        mDbHelper.restoreDatabase(file);
    }

    /**
     * Return if the next workout should deload.
     */
    private boolean getWorkoutShouldDeload(Workout workout) {
        Cursor cursor = null;
        try {
            cursor = mDatabase.query(DATABASE_TABLE_WENDLER_WORKOUT, null,
                    KEY_WORKOUT_EXERCISE + "=? AND " + KEY_WORKOUT_COMPLETED + "=?",
                    new String[]{workout.getName(), "1"}, null,
                    null, KEY_CYCLE + " DESC, " + KEY_WEEK + " DESC");
            return cursor != null
                    && cursor.moveToPosition(1)
                    && cursor.getInt(cursor.getColumnIndex(KEY_WORKOUT_WON)) == 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Return if a given workout is complete.
     */
    private boolean isWorkoutComplete(int id) {
        Cursor c = null;
        try {
            c = mDatabase.query(DATABASE_TABLE_WENDLER_WORKOUT, null,
                    KEY_WORKOUT_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
            return c != null
                    && c.moveToFirst()
                    && c.getInt(c.getColumnIndex(KEY_WORKOUT_COMPLETED)) == 1;
        } finally {
            if (c != null) {
                c.close();
            }
        }

    }


    /**
     * Return if a given workout was won.
     */
    private boolean exerciseWon(int id) {
        Cursor c = null;
        try {
            c = mDatabase.query(DATABASE_TABLE_WENDLER_WORKOUT, null,
                    KEY_WORKOUT_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
            return c != null
                    && c.moveToFirst()
                    && c.getInt(c.getColumnIndex(KEY_WORKOUT_WON)) == 1;
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    /**
     * Return if a given workout for a specific cycle and week has been done.
     */
    private int exerciseHasBeenDone(String sqlExerciseName, int week, int cycle) {
        Cursor c = null;
        try {
            c = mDatabase.query(DATABASE_TABLE_WENDLER_WORKOUT, new String[]{KEY_WORKOUT_ID},
                    KEY_WORKOUT_EXERCISE + "=? AND " + KEY_CYCLE + "=? AND " + KEY_WEEK + "=?",
                    new String[]{sqlExerciseName, String.valueOf(cycle), String.valueOf(week)},
                    null, null, null);
            if (c != null && c.moveToFirst()) {
                return c.getInt(c.getColumnIndex(KEY_WORKOUT_ID));
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return -1;
    }

    /**
     * Return the current cycle for a given exercise.
     */
    private int getCurrentCycle(String sqlExerciseName) {
        Cursor c = null;
        try {
            c = mDatabase.query(DATABASE_TABLE_WENDLER_STATS, new String[]{KEY_CYCLE},
                    KEY_NAME + "=?", new String[]{sqlExerciseName}, null, null, null);
            if (c != null && c.moveToFirst()) {
                return c.getInt(c.getColumnIndex(KEY_CYCLE));
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return 1;
    }

    /**
     * Return the current cycle name for a given exercise.
     */
    private int getCurrentCycleName(String sqlExerciseName) {
        Cursor c = null;
        try {
            c = mDatabase.query(DATABASE_TABLE_WENDLER_STATS, new String[]{KEY_CYCLE_NAME},
                    KEY_NAME + "=?", new String[]{sqlExerciseName}, null, null, null);
            if (c != null && c.moveToFirst()) {
                return c.getInt(c.getColumnIndex(KEY_CYCLE_NAME));
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return 1;
    }


    /**
     * Return the insert time for a given workout id.
     */
    private long getInsertTimeForId(int workoutId) {
        Cursor cursor = null;
        try {
            cursor = mDatabase.query(DATABASE_TABLE_WENDLER_WORKOUT,
                    new String[]{KEY_INSERT_TIME}, KEY_WORKOUT_ID + "=?",
                    new String[]{String.valueOf(workoutId)}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                return Long.parseLong(cursor.getString(cursor.getColumnIndex(KEY_INSERT_TIME)));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return System.currentTimeMillis();
    }

    /**
     * Return the time the workout was completed for a given workout id.
     */
    private Time getTimeForWorkout(int workoutId) {
        Cursor cursor = null;
        try {
            cursor = mDatabase.query(DATABASE_TABLE_WENDLER_WORKOUT,
                    new String[]{KEY_WORKOUT_YEAR, KEY_WORKOUT_MONTH, KEY_WORKOUT_DAY},
                    KEY_WORKOUT_ID + "=?", new String[]{String.valueOf(workoutId)}, null, null,
                    null);

            Time time = new Time();
            time.setToNow();
            if (cursor != null && cursor.moveToFirst()) {
                time.year =
                        Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_WORKOUT_YEAR)));
                time.month = Integer.parseInt(
                        cursor.getString(cursor.getColumnIndex(KEY_WORKOUT_MONTH)));
                time.monthDay =
                        Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_WORKOUT_DAY)));
            }
            return time;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Return the notes for a given workout id.
     */
    private String getNotesForWorkout(int workoutId) {
        Cursor cursor = null;
        try {
            cursor = mDatabase.query(DATABASE_TABLE_WENDLER_WORKOUT,
                    new String[]{KEY_WORKOUT_NOTES}, KEY_WORKOUT_ID + "=?",
                    new String[]{String.valueOf(workoutId)}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndex(KEY_WORKOUT_NOTES));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return "";
    }

    /**
     * Return the main exercise for a given workout id.
     */
    private MainExercise getMainExerciseForWorkout(boolean isComplete, int workoutId) {
        MainExercise mainExercise = null;
        Cursor cursor = null;
        try {
            cursor = mDatabase.query(DATABASE_TABLE_WENDLER_WORKOUT, null,
                    KEY_WORKOUT_ID + "=?", new String[]{String.valueOf(workoutId)},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {

                int week = cursor.getInt(cursor.getColumnIndex(KEY_WORKOUT_WEEK));
                String name = cursor.getString(cursor.getColumnIndex(KEY_WORKOUT_EXERCISE));
                int repsPerformed = cursor.getInt(cursor.getColumnIndex(KEY_WORKOUT_REPS));

                double increment = getIncrement(name);
                int workoutPercentage =
                        cursor.getInt(cursor.getColumnIndex(KEY_TRAINING_PERCENTAGE));
                double oneRm = cursor.getFloat(cursor.getColumnIndex(KEY_1RM));
                int[] setPercentages = getSetPercentages(week);

                boolean showWarmUp = PreferenceUtil.getBoolean(
                        mContext, PreferenceUtil.KEY_SHOW_WARM_UP, true);

                ArrayList<ExerciseSet> sets = new ArrayList<ExerciseSet>();

                if (showWarmUp) {
                    String serialized = PreferenceUtil.getString(
                            mContext,
                            PreferenceUtil.KEY_WARM_UP_SETS,
                            WendlerConstants.DEFAULT_WARMUP_PERCENTAGES);

                    sets.addAll(WendlerMath.getWarmupSets(
                            mContext,
                            oneRm,
                            serialized.split(","),
                            workoutPercentage,
                            repsPerformed));
                }

                sets.addAll(WendlerMath.getWorkoutSets(
                        mContext, oneRm, setPercentages, workoutPercentage, week, repsPerformed));

                mainExercise = new MainExercise(name, oneRm, increment, sets, workoutPercentage);
            }

            if (!isComplete && mainExercise != null && mainExercise.getLastSetProgress() < 0) {
                return null;
            }

            return mainExercise;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Return a list of additional exercises for a given workout id.
     */
    private ArrayList<AdditionalExercise> getExtraExerciseForWorkout(
            String workoutName, int workoutId, boolean isComplete) {

        ArrayList<AdditionalExercise> exercises = new ArrayList<AdditionalExercise>();
        boolean isStarted = false;
        Cursor cursor = null;
        try {
            cursor = mDatabase.query(DATABASE_TABLE_WENDLER_EXTRA, null,
                    KEY_WORKOUT_ID + "=?", new String[]{String.valueOf(workoutId)},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String name = cursor.getString(cursor.getColumnIndex(KEY_EXERCISE_NAME));

                    int sets = cursor.getInt(cursor.getColumnIndex
                            (KEY_NUMBER_OF_SETS_OR_REPS_TO_BE_DONE));
                    double weight = cursor.getDouble(cursor.getColumnIndex(KEY_EXTRA_WEIGHT));
                    int repsPerformed = cursor.getInt(cursor.getColumnIndex
                            (KEY_EXTRA_REPS_OR_SETS_COMPLETED));
                    if (!isStarted && repsPerformed > 0) {
                        isStarted = true;
                    }
                    int exerciseId = cursor.getInt(cursor.getColumnIndex(KEY_EXTRA_EXERCISE_ID));
                    String mainExerciseName = cursor.getString(cursor.getColumnIndex
                            (KEY_EXTRA_MAIN_EXERCISE_NAME));
                    int percentage = cursor.getInt(cursor.getColumnIndex
                            (KEY_EXTRA_PERCENTAGE_OF_MAIN_EXERCISE));

                    ArrayList<ExerciseSet> exerciseSets = new ArrayList<ExerciseSet>();

                    if (!TextUtils.isEmpty(mainExerciseName)) {
                        weight = WendlerMath.calculateSetWeight(mContext,
                                getTrainingMax(mainExerciseName), percentage, 100);
                    }

                    ExerciseSet set = new ExerciseSet(SetType.REGULAR, weight, sets, repsPerformed);
                    exerciseSets.add(set);

                    AdditionalExercise exercise = new AdditionalExercise(
                            name,
                            exerciseSets,
                            mainExerciseName,
                            percentage,
                            exerciseId);
                    exercises.add(exercise);

                } while (cursor.moveToNext());
            }

            ArrayList<AdditionalExercise> additionalExercises = getExtraExerciseForWorkout(new
                    Workout(workoutName, StringHelper.getTranslatableName(mContext, workoutName)));
            if (!isStarted && !isComplete && additionalExercises.equals(exercises)) {
                return additionalExercises;
            }

            return exercises;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Update the workout stats for a new workout.
     */
    private void updateNewWorkoutStats(Workout workout) {
        int currentCycle;
        int cycleName = workout.getCycleDisplayName();
        int week;
        double increment = getIncrement(workout.getName());
        double weight = getOneRmForExercise(workout.getName());
        currentCycle = workout.getCycle();
        week = workout.getWeek();
        if (workout.isComplete() && workout.isWon()) {
            if (week < 4) {
                week++;
            } else {
                week = 1;
                currentCycle++;
                weight += increment;
                cycleName++;
            }
        } else if (workout.isComplete()) {
            DeloadItem deloadItem = new DeloadItem(week, currentCycle, weight, cycleName);
            deloadItem = WendlerMath.doDeload(
                    mContext,
                    deloadItem,
                    workout.getMainExercise().getWorkoutPercentage(),
                    increment);

            week = deloadItem.getWeek();
            currentCycle = deloadItem.getCycle();
            weight = deloadItem.getWeight();
            setWorkoutShouldDeload(workout.getName(), deloadItem.doDelayedDeload());
            cycleName = deloadItem.getCycleName();
        }
        updateWithStats(workout.getName(), currentCycle, week, weight, cycleName);
    }

    /**
     * Update the latest workout stats.
     */
    private void updateLatestWorkoutStats(Workout workout) {
        int currentCycle;
        int cycleName;
        int week;
        double increment = getIncrement(workout.getName());
        double weight;
        boolean hasWorkoutOutcomeChanged = hasWorkoutOutComeChanged(workout);

        WendlerizedLog.d("Update " + hasWorkoutOutcomeChanged);

        if (hasWorkoutOutcomeChanged) {
            currentCycle = workout.getCycle();
            cycleName = workout.getCycleDisplayName();
            weight = getOneRmForExercise(workout.getName());
            week = workout.getWeek();
            if (workout.isComplete() && workout.isWon()) {
                week++;
                weight = WendlerMath.calculateNewWeight(weight, mContext, increment, true);
            } else if (workout.isComplete()) {
                week = 1;
                currentCycle++;
                cycleName++;
                weight = WendlerMath.calculateNewWeight(weight, mContext, increment, false);
            }
            updateWithStats(workout.getName(), currentCycle, week, weight, cycleName);
        }
    }

    /**
     * Set a given workout to deload when next possible.
     */
    private void setWorkoutShouldDeload(String workoutName, boolean value) {

        ContentValues cv = new ContentValues();

        cv.put(KEY_SHOULD_DELOAD, value ? 1 : 0);
        mDatabase.update(DATABASE_TABLE_WENDLER_STATS, cv, KEY_NAME + "=?",
                new String[]{workoutName});
    }

    /**
     * Return if the outcome has changed for a given workout.
     */
    private boolean hasWorkoutOutComeChanged(Workout workout) {
        Cursor cursor = null;
        try {
            cursor = mDatabase.query(DATABASE_TABLE_WENDLER_WORKOUT, null,
                    KEY_WORKOUT_ID + "=?", new String[]{String.valueOf(workout.getWorkoutId())},
                    null, null, null);

            return cursor != null && cursor.moveToFirst() &&
                    (cursor.getInt(cursor.getColumnIndex(KEY_WORKOUT_WON)) == 1) != workout.isWon();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Update a exercise with given stats.
     */
    private void updateWithStats(
            String name, int cycle, int week, double weight, int cycleName) {
        if (cycle < 1) {
            cycle = 1;
            cycleName = 1;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_WORKOUT_ONE_RM, weight);
        contentValues.put(KEY_WEEK, String.valueOf(week));
        contentValues.put(KEY_CYCLE, String.valueOf(cycle));
        contentValues.put(KEY_CYCLE_NAME, String.valueOf(cycleName));
        mDatabase.update(
                DATABASE_TABLE_WENDLER_STATS, contentValues, KEY_NAME + "=?", new String[]{name});
    }

    /**
     * Return if the workout is the latest performed.
     */
    private boolean isWorkoutLatest(Workout workout) {
        Cursor cursor = null;
        try {
            cursor = mDatabase.query(DATABASE_TABLE_WENDLER_WORKOUT, null,
                    KEY_WORKOUT_EXERCISE + "=? AND " + KEY_WORKOUT_COMPLETED + "=?",
                    new String[]{workout.getName(), "1"}, null,
                    null, KEY_CYCLE + " DESC, " + KEY_WEEK + " DESC");
            return cursor != null
                    && cursor.moveToFirst()
                    && workout.getWorkoutId()
                    == cursor.getInt(cursor.getColumnIndex(KEY_WORKOUT_ID));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Inner class for handling the connection to the SQLiteDatabase.
     */
    private static class DbHelper extends SQLiteOpenHelper {

        private final Context mContext;

        /**
         * Constructor.
         */
        public DbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            mContext = context;
        }

        /**
         * Called when the helper is created.
         */
        @Override
        public void onCreate(SQLiteDatabase db) {

            /**
             * Table for managing all the percentages for the 4 weeks.
             */
            db.execSQL("CREATE TABLE " + DATABASE_TABLE_PERCENT + " ("
                    + KEY_ROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + KEY_WEEK_ONE + " INTEGER NOT NULL, "
                    + KEY_WEEK_TWO + " INTEGER NOT NULL, "
                    + KEY_WEEK_THREE + " INTEGER NOT NULL, "
                    + KEY_WEEK_FOUR + " INTEGER NOT NULL);");

            /**
             * Table for managing the stats for each workout.
             */
            db.execSQL("CREATE TABLE " + DATABASE_TABLE_WENDLER_STATS + " (" +
                    KEY_ROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KEY_TRAINING_PERCENTAGE + " INTEGER NOT NULL, " +
                    KEY_1RM + " INTEGER NOT NULL, " +
                    KEY_INCREMENT + " TEXT, " +
                    KEY_WEEK + " TEXT, " +
                    KEY_CYCLE + " TEXT, " +
                    KEY_CYCLE_NAME + " TEXT, " +
                    KEY_ORDER + " INTEGER, " +
                    KEY_SHOULD_DELOAD + " INTEGER, " +
                    KEY_NAME + " TEXT);");

            /**
             * Table for saving the main exercise of a workout.
             */
            db.execSQL("CREATE TABLE " + DATABASE_TABLE_WENDLER_WORKOUT + " (" +
                    KEY_ROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KEY_INSERT_TIME + " TEXT NOT NULL, " +
                    KEY_WORKOUT_YEAR + " INTEGER NOT NULL, " +
                    KEY_WORKOUT_MONTH + " INTEGER NOT NULL, " +
                    KEY_WORKOUT_DAY + " INTEGER NOT NULL, " +
                    KEY_WORKOUT_EXERCISE + " TEXT NOT NULL, " +
                    KEY_WORKOUT_REPS + " TEXT NOT NULL, " +
                    KEY_WORKOUT_ONE_RM + " TEXT NOT NULL, " +
                    KEY_WORKOUT_LAST_SET + " TEXT NOT NULL, " +
                    KEY_WORKOUT_WEEK + " TEXT NOT NULL, " +
                    KEY_WORKOUT_CYCLE + " TEXT NOT NULL, " +
                    KEY_CYCLE_NAME + " TEXT NOT NULL, " +
                    KEY_WORKOUT_ID + " INTEGER NOT NULL, " +
                    KEY_WORKOUT_NOTES + " TEXT NOT NULL, " +
                    KEY_WORKOUT_COMPLETED + " INTEGER NOT NULL, " +
                    KEY_TRAINING_PERCENTAGE + " INTEGER NOT NULL, " +
                    KEY_WORKOUT_WON + " INTEGER);");

            /**
             * Table for managing extra exercises for a specific workout.
             */
            db.execSQL("CREATE TABLE " + DATABASE_TABLE_WENDLER_EXTRA + " (" +
                    KEY_ROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KEY_WORKOUT_ID + " INTEGER NOT NULL, " +
                    KEY_EXERCISE_NAME + " TEXT NOT NULL, " +
                    KEY_EXTRA_EXERCISE_ID + " INTEGER NOT NULL, " +
                    KEY_NUMBER_OF_SETS_OR_REPS_TO_BE_DONE + " TEXT NOT NULL, " +
                    KEY_EXTRA_WEIGHT + " TEXT NOT NULL, " +
                    KEY_EXTRA_PERCENTAGE_OF_MAIN_EXERCISE + " TEXT, " +
                    KEY_EXTRA_MAIN_EXERCISE_NAME + " TEXT, " +
                    KEY_EXTRA_REPS_OR_SETS_COMPLETED + " TEXT NOT NULL);");

            /**
             * Table for storing all extra exercises used so they can be reused.
             */
            db.execSQL("CREATE TABLE " + DATABASE_TABLE_WENDLER_EXTRA_LIST + " (" +
                    KEY_ROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KEY_EXERCISE_NAME + " TEXT NOT NULL, " +
                    KEY_EXTRA_EXERCISE_ID + " TEXT NOT NULL, " +
                    KEY_WORKOUT_EXERCISE + " TEXT NOT NULL, " +
                    KEY_NUMBER_OF_SETS_OR_REPS_TO_BE_DONE + " TEXT NOT NULL, " +
                    KEY_EXTRA_WEIGHT + " TEXT, " +
                    KEY_EXTRA_PERCENTAGE_OF_MAIN_EXERCISE + " TEXT, " +
                    KEY_EXTRA_MAIN_EXERCISE_NAME + " TEXT, " +
                    KEY_INCREMENT + "TEXT, " +
                    KEY_IS_BBB + "INTEGER, " +
                    KEY_EXTRA_ORDER_IN_LIST + " TEXT NOT NULL);");
        }

        /**
         * Called when the database is upgraded.
         */
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try {
                db.execSQL("ALTER TABLE " + DATABASE_TABLE_WENDLER_EXTRA_LIST + " ADD COLUMN " +
                        KEY_IS_BBB + " INTEGER DEFAULT 0");
            } catch (Exception e) {
                WendlerizedLog.v("Failed to add column " + KEY_IS_BBB + " in " +
                        DATABASE_TABLE_WENDLER_EXTRA_LIST);
            }

            try {
                db.execSQL("ALTER TABLE " + DATABASE_TABLE_WENDLER_STATS + " ADD COLUMN "
                        + KEY_SHOULD_DELOAD + " INTEGER DEFAULT 0");
            } catch (Exception e) {
                WendlerizedLog.v("Failed to add column " + KEY_SHOULD_DELOAD + " in " +
                        DATABASE_TABLE_WENDLER_STATS);
            }

            try {
                db.execSQL("ALTER TABLE " + DATABASE_TABLE_WENDLER_STATS + " ADD COLUMN "
                        + KEY_CYCLE_NAME + " INTEGER DEFAULT 0");
            } catch (Exception e) {
                WendlerizedLog.v("Failed to add column " + KEY_CYCLE_NAME + " in " +
                        DATABASE_TABLE_WENDLER_STATS);
            }

            try {
                db.execSQL("ALTER TABLE " + DATABASE_TABLE_WENDLER_WORKOUT + " ADD COLUMN "
                        + KEY_CYCLE_NAME + " INTEGER DEFAULT 0");
            } catch (Exception e) {
                WendlerizedLog.v("Failed to add column " + KEY_CYCLE_NAME + " in " +
                        DATABASE_TABLE_WENDLER_WORKOUT);
            }
        }

        /**
         * Restore the database from a file.
         */
        @SuppressWarnings("ConstantConditions")
        public void restoreDatabase(File file) {
            close();
            File oldDb = new File(String.valueOf(mContext.getDatabasePath(getDatabaseName())));
            if (file.exists()) {
                try {
                    copyFile(new FileInputStream(file), new FileOutputStream(oldDb));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                getWritableDatabase().close();
            }
        }

        /**
         * Copy a given file.
         */
        private static void copyFile(FileInputStream fromFile, FileOutputStream toFile)
                throws IOException {
            FileChannel fromChannel = null;
            FileChannel toChannel = null;
            try {
                fromChannel = fromFile.getChannel();
                toChannel = toFile.getChannel();
                fromChannel.transferTo(0, fromChannel.size(), toChannel);
            } finally {
                try {
                    if (fromChannel != null) {
                        fromChannel.close();
                    }
                } finally {
                    if (toChannel != null) {
                        toChannel.close();
                    }
                }
            }
        }
    }
}
