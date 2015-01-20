package se.johan.wendler.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Class which maps a list of sets to a set type.
 */
public class SetGroup implements Parcelable {

    private SetType mSetType;
    private List<ExerciseSet> mSets;

    /**
     * Constructor.
     */
    public SetGroup(SetType setType, List<ExerciseSet> sets) {
        mSetType = setType;
        mSets = sets;
    }

    /**
     * Returns the set type associated with the group.
     */
    public SetType getSetType() {
        return mSetType;
    }
    /**
     *
     * Returns the list of sets associated with the group.
     */
    public List<ExerciseSet> getSets() {
        return mSets;
    }

    /**
     * Parcelable constructor.
     */
    protected SetGroup(Parcel in) {
        mSetType = (SetType) in.readValue(SetType.class.getClassLoader());
        if (in.readByte() == 0x01) {
            mSets = new ArrayList<>();
            in.readList(mSets, ExerciseSet.class.getClassLoader());
        } else {
            mSets = null;
        }
    }

    /**
     * Required for parcelable interface.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Write our information to a parcel.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(mSetType);
        if (mSets == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(mSets);
        }
    }

    /**
     * Parcelable creator.
     */
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<SetGroup> CREATOR = new Parcelable.Creator<SetGroup>() {
        @Override
        public SetGroup createFromParcel(Parcel in) {
            return new SetGroup(in);
        }

        @Override
        public SetGroup[] newArray(int size) {
            return new SetGroup[size];
        }
    };
}