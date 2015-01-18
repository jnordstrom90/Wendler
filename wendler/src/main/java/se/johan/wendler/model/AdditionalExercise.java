package se.johan.wendler.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

import se.johan.wendler.model.base.Exercise;

/**
 * Object representing an additional exercise.
 */
public class AdditionalExercise extends Exercise implements Parcelable {

    private String mMainExerciseName;
    private int mMainExercisePercentage;
    private double mMainExerciseWeight;
    private int mExerciseId;
    private boolean mIsStarted;

    /**
     * Constructor for the additional exercise
     */
    public AdditionalExercise(String name,
                              ArrayList<ExerciseSet> exerciseSets,
                              String mainExerciseName,
                              int mainExercisePercentage,
                              double mainExerciseWeight,
                              int exerciseId,
                              boolean isStarted) {
        mName = name;
        mExerciseSets = exerciseSets;
        mMainExerciseName = mainExerciseName;
        mMainExercisePercentage = mainExercisePercentage;
        mExerciseId = exerciseId;
        mMainExerciseWeight = mainExerciseWeight;
        mIsStarted = isStarted;
    }

    public boolean isStarted() {
        return mIsStarted;
    }
    /**
     * Always get the first set now since only straight sets are supported.
     */
    @Override
    public ExerciseSet getExerciseSet(int position) {
        return super.getExerciseSet(0);
    }

    /**
     * Return the main exercise name which the additional exercise uses a percentage of for
     * weight calculation.
     */
    public String getMainExerciseName() {
        return mMainExerciseName;
    }

    /**
     * Return the main exercise percentage used for weight calculation.
     */
    public int getMainExercisePercentage() {
        return mMainExercisePercentage;
    }

    /**
     * Return this objects' exercise id
     */
    public int getExerciseId() {
        return mExerciseId;
    }

    /**
     * Parcelable constructor
     */
    public AdditionalExercise(Parcel in) {
        readFromParcel(in);
    }

    /**
     * Returns the weight of the main exercise.
     */
    public double getMainExerciseWeight() {
        return mMainExerciseWeight;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Used when writing the values to a parcel.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        if (mExerciseSets == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(mExerciseSets);
        }
        dest.writeString(mMainExerciseName);
        dest.writeInt(mMainExercisePercentage);
        dest.writeInt(mExerciseId);
        dest.writeDouble(mMainExerciseWeight);
        dest.writeInt(mIsStarted ? 1 : 0);
    }

    /**
     * Used when reading the values from a parcel.
     */
    private void readFromParcel(Parcel in) {
        mName = in.readString();
        if (in.readByte() == 0x01) {
            mExerciseSets = new ArrayList<ExerciseSet>();
            in.readList(mExerciseSets, ExerciseSet.class.getClassLoader());
        } else {
            mExerciseSets = null;
        }
        mMainExerciseName = in.readString();
        mMainExercisePercentage = in.readInt();
        mExerciseId = in.readInt();
        mMainExerciseWeight = in.readDouble();
        mIsStarted = in.readInt() == 1;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof AdditionalExercise) {
            AdditionalExercise other = (AdditionalExercise) o;
            return other.getName().equals(mName)
                    && other.getExerciseSets().equals(getExerciseSets())
                    && other.getMainExerciseName().equals(mMainExerciseName)
                    && other.getMainExercisePercentage() == mMainExercisePercentage;
        }
        return false;
    }

    /**
     * Used for creating parcelable objects.
     */
    public static final Parcelable.Creator<AdditionalExercise> CREATOR = new Parcelable
            .Creator<AdditionalExercise>() {
        public AdditionalExercise createFromParcel(Parcel in) {
            return new AdditionalExercise(in);
        }

        public AdditionalExercise[] newArray(int size) {
            return new AdditionalExercise[size];
        }
    };
}
