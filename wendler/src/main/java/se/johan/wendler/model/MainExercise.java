package se.johan.wendler.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

import se.johan.wendler.model.base.Exercise;

/**
 * Object representing a main exercise.
 */
public class MainExercise extends Exercise implements Parcelable {

    private double mIncrement;
    private double mWeight;
    private int mWorkoutPercentage;

    /**
     * Constructor.
     */
    public MainExercise(String name,
                        double weight,
                        double increment,
                        ArrayList<ExerciseSet> exerciseSets,
                        int workoutPercentage) {
        mName = name;
        mWeight = weight;
        mIncrement = increment;
        mExerciseSets = exerciseSets;
        mWorkoutPercentage = workoutPercentage;
    }

    /**
     * Return the weight of the main exercise.
     */
    public double getWeight() {
        return mWeight;
    }

    /**
     * Return the workout percentage for the exercise.
     */
    public int getWorkoutPercentage() {
        return mWorkoutPercentage;
    }

    /**
     * Return the progress of the last set.
     */
    public int getLastSetProgress() {
        int size = mExerciseSets.size();
        return mExerciseSets.get(size - 1).getProgress();
    }

    /**
     * Set the progress of the last set.
     */
    public void setLastSetProgress(int progress) {
        int size = mExerciseSets.size();
        mExerciseSets.get(size - 1).setProgress(progress);
    }

    /**
     * Return if the main exercise is won.
     */
    public boolean isWon() {
        int size = mExerciseSets.size();
        return mExerciseSets.get(size - 1).isWon();
    }

    /**
     * Parcelable constructor
     */
    public MainExercise(Parcel in) {
        readFromParcel(in);
    }

    /**
     * Used for parcelable
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Save the variables to a parcel
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeDouble(mWeight);
        dest.writeDouble(mIncrement);
        dest.writeInt(mWorkoutPercentage);
        if (mExerciseSets == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(mExerciseSets);
        }
    }

    /**
     * Read the values from a parcel
     */
    private void readFromParcel(Parcel in) {
        mName = in.readString();
        mWeight = in.readDouble();
        mIncrement = in.readDouble();
        mWorkoutPercentage = in.readInt();
        if (in.readByte() == 0x01) {
            mExerciseSets = new ArrayList<ExerciseSet>();
            in.readList(mExerciseSets, ExerciseSet.class.getClassLoader());
        } else {
            mExerciseSets = null;
        }
    }

    /**
     * Used for creating parcelable objects.
     */
    public static final Parcelable.Creator<MainExercise> CREATOR = new Parcelable
            .Creator<MainExercise>() {
        public MainExercise createFromParcel(Parcel in) {
            return new MainExercise(in);
        }

        public MainExercise[] newArray(int size) {
            return new MainExercise[size];
        }
    };
}
