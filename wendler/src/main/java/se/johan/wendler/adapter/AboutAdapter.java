package se.johan.wendler.adapter;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import se.johan.wendler.R;
import se.johan.wendler.model.WendlerListItem;

/**
 * The adapter used in the list in the AboutActivity.
 */
public class AboutAdapter extends BaseAdapter {

    private final ArrayList<WendlerListItem> mItems;
    private final LayoutInflater mInflater;

    /**
     * Constructor for the adapter.
     */
    public AboutAdapter(Context context, ArrayList<WendlerListItem> items) {
        mItems = items;
        mInflater = LayoutInflater.from(context);
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
            convertView = mInflater.inflate(R.layout.about_list_item, parent, false);
            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.textView);
            holder.subtitle = (TextView) convertView.findViewById(R.id.textViewSmall);
            holder.icon = (ImageView) convertView.findViewById(R.id.imageView);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.title.setText(mItems.get(position).getTitle());
        if (mItems.get(position).hasSubtitle()) {
            holder.subtitle.setText(mItems.get(position).getSubtitle());
            holder.subtitle.setVisibility(View.VISIBLE);
        } else {
            holder.title.setGravity(Gravity.CENTER_VERTICAL);
            holder.subtitle.setVisibility(View.GONE);
        }
        holder.icon.setImageResource(mItems.get(position).getIconRes());

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
