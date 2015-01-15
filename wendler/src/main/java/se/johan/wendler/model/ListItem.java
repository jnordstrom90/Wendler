package se.johan.wendler.model;

import android.content.Context;
import android.graphics.drawable.Drawable;

import se.johan.wendler.R;

/**
 * Item used for various lists.
 */
public enum ListItem {
    WORKOUTS(
            R.string.drawer_workouts,
            -1,
            R.drawable.ic_favorite_white_24dp,
            ListItemType.DRAWER),
    ADDITIONAL_EXERCISES(
            R.string.drawer_additional,
            -1,
            R.drawable.add_exercise_24dp,
            ListItemType.DRAWER),
    EDIT(
            R.string.drawer_edit,
            -1,
            R.drawable.ic_mode_edit_white_24dp,
            ListItemType.DRAWER),
    OLD_WORKOUTS(
            R.string.drawer_old_workouts,
            -1,
            R.drawable.ic_history_white_24dp,
            ListItemType.DRAWER),
    BACKUP(
            R.string.drawer_backup,
            -1,
            R.drawable.ic_settings_backup_restore_white_24dp,
            ListItemType.DRAWER),
    SETTINGS(
            R.string.action_item_settings,
            -1,
            R.drawable.ic_settings_white_24dp,
            ListItemType.DRAWER_EXTRA,
            true),
    ABOUT(
            R.string.action_item_about,
            -1,
            R.drawable.ic_info_white_24dp,
            ListItemType.DRAWER_EXTRA),
    CHANGE_LOG(
            R.string.change_log,
            R.string.version,
            R.drawable.ic_format_list_bulleted_black_24dp,
            ListItemType.ABOUT),
    CONTACT_DEVELOPER(
            R.string.contact,
            -1,
            R.drawable.ic_mail_black_24dp,
            ListItemType.ABOUT),
    LICENSES(
            R.string.licences,
            -1,
            R.drawable.ic_format_align_justify_black_24dp,
            ListItemType.ABOUT);

    private int mTitleRes;
    private int mSubtitleRes;
    private int mIconRes;
    private ListItemType mItemType;
    private boolean mHasDivider;

    /**
     * Constructor
     */
    private ListItem(
            int titleRes, int subtitleRes, int iconRes, ListItemType itemType, boolean hasDivider) {
        mTitleRes = titleRes;
        mIconRes = iconRes;
        mSubtitleRes = subtitleRes;
        mItemType = itemType;
        mHasDivider = hasDivider;
    }

    /**
     * Constructor
     */
    private ListItem(int titleRes, int subtitleRes, int iconRes, ListItemType itemType) {
        mTitleRes = titleRes;
        mIconRes = iconRes;
        mSubtitleRes = subtitleRes;
        mItemType = itemType;
    }

    /**
     * Returns the title of the item.
     */
    public String getTitle(Context context) {
        return context.getString(mTitleRes);
    }

    /**
     * Returns true if the view has a subtitle.
     */
    public boolean hasSubtitle() {
        return mSubtitleRes != -1;
    }

    /**
     * Returns the subtitle with a format argument.
     */
    public String getSubtitle(Context context, String args) {
        return context.getString(mSubtitleRes, args);
    }

    /**
     * Returns the icon of the item.
     */
    public Drawable getIcon(Context context) {
        return context.getResources().getDrawable(mIconRes);
    }

    /**
     * Returns the item type of the enum.
     */
    public ListItemType getItemType() {
        return mItemType;
    }

    /**
     * Returns true if the view should display a top divider.
     */
    public boolean hasDivider() {
        return mHasDivider;
    }
}