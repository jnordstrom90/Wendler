package se.johan.wendler.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * Object used for displaying various list items.
 */
public class WendlerListItem implements Parcelable {

    private final String mTitle;
    private final String mSubtitle;
    private final int mIconRes;
    private final ListItemType mType;

    /**
     * Constructor.
     */
    public WendlerListItem(ListItemType type, String title, String subtitle, int iconRes) {
        mType = type;
        mTitle = title;
        mSubtitle = subtitle;
        mIconRes = iconRes;
    }

    /**
     * Return the type.
     */
    public ListItemType getType() {
        return mType;
    }

    /**
     * Return the title.
     */
    public String getTitle() {
        return mTitle;
    }

    /**
     * Return the subtitle.
     */
    public String getSubtitle() {
        return mSubtitle;
    }

    /**
     * Return the icon resource.
     */
    public int getIconRes() {
        return mIconRes;
    }

    /**
     * Return if the item has a subtitle.
     */
    public boolean hasSubtitle() {
        return !TextUtils.isEmpty(mSubtitle);
    }

    /**
     * Used for parcelable.
     */
    protected WendlerListItem(Parcel in) {
        mTitle = in.readString();
        mSubtitle = in.readString();
        mIconRes = in.readInt();
        mType = (ListItemType) in.readValue(ListItemType.class.getClassLoader());
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
        dest.writeString(mTitle);
        dest.writeString(mSubtitle);
        dest.writeInt(mIconRes);
        dest.writeValue(mType);
    }

    /**
     * Used for parcelable.
     */
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<WendlerListItem>
            CREATOR = new Parcelable.Creator<WendlerListItem>() {
        @Override
        public WendlerListItem createFromParcel(Parcel in) {
            return new WendlerListItem(in);
        }

        @Override
        public WendlerListItem[] newArray(int size) {
            return new WendlerListItem[size];
        }
    };
}