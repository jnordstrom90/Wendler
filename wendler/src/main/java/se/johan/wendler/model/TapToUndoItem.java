package se.johan.wendler.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Tap to undo item, so we can access the position deleted
 */
public class TapToUndoItem implements Parcelable {

    private final Object mObject;
    private final int mPosition;

    /**
     * Constructor
     */
    public TapToUndoItem(Object exercise, int position) {
        mObject = exercise;
        mPosition = position;
    }

    /**
     * Return the object.
     */
    public Object getObject() {
        return mObject;
    }

    /**
     * Return the position.
     */
    public int getPosition() {
        return mPosition;
    }

    /**
     * Used for parcelable.
     */
    protected TapToUndoItem(Parcel in) {
        mObject = in.readValue(null);
        mPosition = in.readInt();
    }

    /**
     * Used for parcelable.
     */
    public int describeContents() {
        return 0;
    }

    /**
     * Used for parcelable.
     */
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(mObject);
        dest.writeInt(mPosition);
    }

    /**
     * Used for parcelable.
     */
    public static final Parcelable.Creator<TapToUndoItem>
            CREATOR = new Parcelable.Creator<TapToUndoItem>() {
        public TapToUndoItem createFromParcel(Parcel in) {
            return new TapToUndoItem(in);
        }

        public TapToUndoItem[] newArray(int size) {
            return new TapToUndoItem[size];
        }
    };
}
