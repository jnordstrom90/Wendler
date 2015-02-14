package se.johan.wendler.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import se.johan.wendler.model.base.Exercise;
import se.johan.wendler.util.WendlerMath;

/**
 * Object representing a main exercise.
 */
public class MainExercise extends Exercise implements Parcelable {

    private double mIncrement;
    private double mWeight;
    private int mWorkoutPercentage;
    private List<SetGroup> mSetGroups = new ArrayList<>();
    private int mRepsToBeat;
    private int mEstOneRm;

    /**
     * Constructor.
     */
    public MainExercise(String name,
                        double weight,
                        double increment,
                        ArrayList<ExerciseSet> exerciseSets,
                        List<SetGroup> setGroups,
                        int workoutPercentage,
                        int estOneRm,
                        int repsToBeat) {
        mName = name;
        mWeight = weight;
        mIncrement = increment;
        mExerciseSets = exerciseSets;
        mWorkoutPercentage = workoutPercentage;
        mSetGroups = setGroups;
        mEstOneRm = estOneRm;

        mRepsToBeat = repsToBeat;

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
     * Return the goal of the last set.
     */
    public int getGoal() {
        int size = mExerciseSets.size();
        return mExerciseSets.get(size - 1).getGoal();
    }

    /**
     * Return the weight of the last set.
     */
    public double getLastSetWeight() {
        int size = mExerciseSets.size();
        return mExerciseSets.get(size - 1).getWeight();
    }

    /**
     * Return the reps to beat for a new PR
     */
    public int getRepsToBeat() {
        return mRepsToBeat;
    }

    /**
     * Return the estimated One RM based on the last set
     */
    public int getEstOneRm() {
        return mEstOneRm;
    }

    /**
     * Recalculates the estimated One RM based on last set weight and reps
     */
    public void recalculateEstOneRm(){
        mEstOneRm = WendlerMath.calculateOneRm(getLastSetWeight(), getLastSetProgress());
    }

    /**
     * Returns the set groups.
     */
    public List<SetGroup> getSetGroups() {
        return mSetGroups;
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
        if (mSetGroups == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(mSetGroups);
        }
        dest.writeInt(mRepsToBeat);
        dest.writeInt(mEstOneRm);
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
        if (in.readByte() == 0x01) {
            mSetGroups = new ArrayList<>();
            in.readList(mSetGroups, SetGroup.class.getClassLoader());
        } else {
            mSetGroups = null;
        }
        mRepsToBeat = in.readInt();
        mEstOneRm = in.readInt();
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
