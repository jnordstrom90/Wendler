package se.johan.wendler.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Deload item.
 */
public class DeloadItem implements Parcelable {

    private int mWeek;
    private int mCycle;
    private double mWeight;
    private boolean mDoDelayedDeload;
    private int mCycleName;

    /**
     * Constructor.
     */
    public DeloadItem(int week, int cycle, double weight, int cycleName) {
        mWeek = week;
        mCycle = cycle;
        mWeight = weight;
        mCycleName = cycleName;
    }

    /**
     * Return the week.
     */
    public int getWeek() {
        return mWeek;
    }

    /**
     * Set the current week.
     */
    public void setWeek(int week) {
        mWeek = week;
    }

    /**
     * Return the cycle.
     */
    public int getCycle() {
        return mCycle;
    }

    /**
     * Set the current cycle.
     */
    public void setCycle(int cycle) {
        mCycle = cycle;
    }

    /**
     * Return the cycle name.
     */
    public int getCycleName() {
        return mCycleName;
    }

    /**
     * Set the current cycle name.
     */
    public void setCycleName(int cycleName) {
        mCycleName = cycleName;
    }

    /**
     * Return if we should do a delayed deload.
     */
    public boolean doDelayedDeload() {
        return mDoDelayedDeload;
    }

    /**
     * Set the delayed deload to true.
     */
    public void setDoDelayedDeload() {
        mDoDelayedDeload = true;
    }

    /**
     * Return the weight.
     */
    public double getWeight() {
        return mWeight;
    }

    /**
     * Set the weight.
     */
    public void setWeight(double weight) {
        mWeight = weight;
    }

    /**
     * Used for parcelable.
     */
    protected DeloadItem(Parcel in) {
        mWeek = in.readInt();
        mCycle = in.readInt();
        mWeight = in.readDouble();
        mDoDelayedDeload = in.readByte() != 0x00;
        mCycleName = in.readInt();
    }

    /**
     * Used for parcelable.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Used for parcelable.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mWeek);
        dest.writeInt(mCycle);
        dest.writeDouble(mWeight);
        dest.writeByte((byte) (mDoDelayedDeload ? 0x01 : 0x00));
        dest.writeInt(mCycleName);
    }

    /**
     * Used for parcelable.
     */
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<DeloadItem>
            CREATOR = new Parcelable.Creator<DeloadItem>() {
        @Override
        public DeloadItem createFromParcel(Parcel in) {
            return new DeloadItem(in);
        }

        @Override
        public DeloadItem[] newArray(int size) {
            return new DeloadItem[size];
        }
    };
}