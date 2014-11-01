package se.johan.wendler.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.Time;

import java.util.ArrayList;

/**
 * An object representing a workout.
 */
public class Workout implements Parcelable {

    private final String mName;
    private final String mDisplayName;
    private int mCycle;
    private int mCycleDisplayName;
    private int mWeek;
    private boolean mIsComplete;
    private boolean mIsWon;
    private MainExercise mMainExercise;
    private ArrayList<AdditionalExercise> mAdditionalExercises;
    private int mWorkoutId = -1;
    private String mNotes = "";
    private long mInsertTime = 0;
    private int mWorkoutYear = -1;
    private int mWorkoutMonth = -1;
    private int mWorkoutDay = -1;

    /**
     * Constructor for a new workout.
     */
    public Workout(String name,
                   String displayName,
                   int week,
                   int cycle,
                   int cycleName,
                   int workoutId,
                   MainExercise mainExercise) {
        mName = name;
        mWeek = week;
        mCycle = cycle;
        mCycleDisplayName = cycleName;
        mWorkoutId = workoutId;
        mDisplayName = displayName;
        mMainExercise = mainExercise;
    }

    /**
     * Constructor for a completed workout.
     */
    public Workout(String name,
                   String displayName,
                   boolean isComplete,
                   boolean isWon,
                   int week,
                   int cycle,
                   int cycleName,
                   int workoutId,
                   MainExercise mainExercise,
                   ArrayList<AdditionalExercise> additionalExercises,
                   long insertTime,
                   Time time,
                   String notes) {
        mName = name;
        mIsComplete = isComplete;
        mIsWon = isWon;
        mWeek = week;
        mCycle = cycle;
        mCycleDisplayName = cycleName;
        mWorkoutId = workoutId;
        mMainExercise = mainExercise;
        mAdditionalExercises = additionalExercises;
        mInsertTime = insertTime;
        mWorkoutYear = time.year;
        mWorkoutMonth = time.month;
        mWorkoutDay = time.monthDay;
        mDisplayName = displayName;
        mNotes = notes;
    }

    /**
     * Constructor for adding additional exercise fragments.
     */
    public Workout(String name, String displayName) {
        mName = name;
        mDisplayName = displayName;
    }

    /**
     * Set the main exercise for the workout.
     */
    public void setMainExercise(MainExercise mainExerciseForWorkout) {
        mMainExercise = mainExerciseForWorkout;
    }

    /**
     * Set the additional exercises for the workout.
     */
    public void setAdditionalExercises(ArrayList<AdditionalExercise> additionalExercises) {
        mAdditionalExercises = additionalExercises;
    }

    /**
     * Return the name of the workout.
     */
    public String getName() {
        return mName;
    }

    /**
     * Return the display name of the workout.
     */
    public String getDisplayName() {
        return mDisplayName;
    }

    /**
     * Return the cycle of the workout.
     */
    public int getCycle() {
        return mCycle;
    }

    /**
     * Return the display name of the workout.
     */
    public int getCycleDisplayName() {
        return mCycleDisplayName;
    }

    /**
     * Return the week of the workout.
     */
    public int getWeek() {
        return mWeek;
    }

    /**
     * Return if the workout is completed.
     */
    public boolean isComplete() {
        return mIsComplete;
    }

    /**
     * Set the workout to complete.
     */
    public void setComplete() {
        mIsComplete = true;
    }

    /**
     * Return if the workout is won.
     */
    public boolean isWon() {
        return mIsWon;
    }

    /**
     * Set if the workout is done.
     */
    public void setIsWon(boolean isWon) {
        mIsWon = isWon;
    }

    /**
     * Return the main exercise of the workout.
     */
    public MainExercise getMainExercise() {
        return mMainExercise;
    }

    /**
     * Return the workout id associated with the workout.
     */
    public int getWorkoutId() {
        return mWorkoutId;
    }

    /**
     * Return the notes entered for the workout.
     */
    public String getNotes() {
        return mNotes;
    }

    /**
     * Update the notes of the workout.
     */
    public void updateNotes(String notes) {
        mNotes = notes;
    }

    /**
     * Return the milliseconds when the workout was saved the first time.
     */
    public long getInsertTime() {
        return mInsertTime;
    }

    /**
     * Update the insert time.
     */
    public void updateInsertTime(long insertTime) {
        mInsertTime = insertTime;
    }

    /**
     * Return the time of the workout.
     */
    public Time getWorkoutTime() {
        Time time = new Time();
        time.set(mWorkoutDay, mWorkoutMonth, mWorkoutYear);
        return time;
    }

    /**
     * Update the time of the workout.
     */
    public void updateWorkoutTime(int year, int month, int day) {
        mWorkoutYear = year;
        mWorkoutMonth = month;
        mWorkoutDay = day;
    }

    /**
     * Set the workout id of the workout.
     */
    public void setWorkoutId(int workoutId) {
        mWorkoutId = workoutId;
    }

    /**
     * Return the additional exercises of the workout. Empty list is returned if there are none.
     */
    public ArrayList<AdditionalExercise> getAdditionalExercises() {
        if (mAdditionalExercises == null) {
            return new ArrayList<AdditionalExercise>();
        }
        return mAdditionalExercises;
    }

    /**
     * Constructor for parcelable.
     */
    protected Workout(Parcel in) {
        mName = in.readString();
        mCycle = in.readInt();
        mCycleDisplayName = in.readInt();
        mWeek = in.readInt();
        mIsComplete = in.readByte() != 0x00;
        mIsWon = in.readByte() != 0x00;
        mMainExercise = (MainExercise) in.readValue(MainExercise.class.getClassLoader());
        if (in.readByte() == 0x01) {
            mAdditionalExercises = new ArrayList<AdditionalExercise>();
            in.readList(mAdditionalExercises, AdditionalExercise.class.getClassLoader());
        } else {
            mAdditionalExercises = null;
        }
        mWorkoutId = in.readInt();
        mNotes = in.readString();
        mInsertTime = in.readLong();
        mWorkoutYear = in.readInt();
        mWorkoutMonth = in.readInt();
        mWorkoutDay = in.readInt();
        mDisplayName = in.readString();
    }

    /**
     * Used for parcelable.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Used for saving values to a parcel.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeInt(mCycle);
        dest.writeInt(mCycleDisplayName);
        dest.writeInt(mWeek);
        dest.writeByte((byte) (mIsComplete ? 0x01 : 0x00));
        dest.writeByte((byte) (mIsWon ? 0x01 : 0x00));
        dest.writeValue(mMainExercise);
        if (mAdditionalExercises == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(mAdditionalExercises);
        }
        dest.writeInt(mWorkoutId);
        dest.writeString(mNotes);
        dest.writeLong(mInsertTime);
        dest.writeInt(mWorkoutYear);
        dest.writeInt(mWorkoutMonth);
        dest.writeInt(mWorkoutDay);
        dest.writeString(mDisplayName);
    }

    /**
     * Used for parcelable.
     */
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Workout> CREATOR = new Parcelable.Creator<Workout>() {
        @Override
        public Workout createFromParcel(Parcel in) {
            return new Workout(in);
        }

        @Override
        public Workout[] newArray(int size) {
            return new Workout[size];
        }
    };
}