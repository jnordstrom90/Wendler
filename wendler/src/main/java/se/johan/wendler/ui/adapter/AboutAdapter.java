package se.johan.wendler.ui.adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import se.johan.wendler.R;
import se.johan.wendler.model.ListItem;
import se.johan.wendler.util.Utils;

/**
 * The adapter used in the list in the AboutActivity.
 */
public class AboutAdapter extends BaseAdapter {

    private final ArrayList<ListItem> mItems;
    private Context mContext;

    /**
     * Constructor for the adapter.
     */
    public AboutAdapter(Context context, ArrayList<ListItem> items) {
        mItems = items;
        mContext = context;
    }

    /**
     * Return the number of items in the list.
     */
    @Override
    public int getCount() {
        return mItems.size();
    }

    /**
     * Return the item at a certain position.
     */
    @Override
    public Object getItem(int position) {
        return null;
    }

    /**
     * Return the item id of a position.
     */
    @Override
    public long getItemId(int position) {
        return 0;
    }

    /**
     * Return the view of a position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext)
                    .inflate(R.layout.list_item_about, parent, false);
            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.textView);
            holder.subtitle = (TextView) convertView.findViewById(R.id.textViewSmall);
            holder.icon = (ImageView) convertView.findViewById(R.id.imageView);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.title.setText(mItems.get(position).getTitle(mContext));
        if (mItems.get(position).hasSubtitle()) {
            // TODO Make this dynamic in the future
            holder.subtitle.setText(mItems.get(position).getSubtitle
                    (mContext, Utils.getCurrentAppVersion(mContext)));
            holder.subtitle.setVisibility(View.VISIBLE);
        } else {
            holder.title.setGravity(Gravity.CENTER_VERTICAL);
            holder.subtitle.setVisibility(View.GONE);
        }
        holder.icon.setImageDrawable(mItems.get(position).getIcon(mContext));
        holder.icon.setColorFilter(
                mContext.getResources().getColor(R.color.text_subtitle_color),
                PorterDuff.Mode.MULTIPLY);

        return convertView;
    }


    /**
     * Static class to increase performance
     */
    private static class ViewHolder {
        TextView title;
        TextView subtitle;
        ImageView icon;
    }
}
