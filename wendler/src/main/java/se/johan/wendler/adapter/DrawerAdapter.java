package se.johan.wendler.adapter;

import android.content.Context;
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
 * Adapter for the items in the NavigationDrawer.
 */
public class DrawerAdapter extends BaseAdapter {

    private final LayoutInflater mInflater;
    private final ArrayList<WendlerListItem> mListItems;
    private int mSelectedIndex = -1;

    /**
     * Constructor for the adapter.
     */
    public DrawerAdapter(Context context, ArrayList<WendlerListItem> listItems) {
        mListItems = listItems;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * Return the number of items in the list.
     */
    @Override
    public int getCount() {
        return mListItems.size();
    }

    /**
     * Return the item at a given position.
     */
    @Override
    public Object getItem(int position) {
        return mListItems.get(position);
    }

    /**
     * Return the item id of a given position.
     */
    @Override
    public long getItemId(int position) {
        return 0;
    }

    /**
     * Return the view of the item.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.drawer_list_item, parent, false);
            holder.text = (TextView) convertView.findViewById(R.id.text);
            holder.smallText = (TextView) convertView.findViewById(R.id.textViewSmall);
            holder.selector = convertView.findViewById(R.id.selection);
            holder.icon = (ImageView) convertView.findViewById(R.id.icon);
            holder.bigLayout = convertView.findViewById(R.id.bigLayout);
            holder.smallLayout = convertView.findViewById(R.id.smallLayout);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        switch (mListItems.get(position).getType()) {
            case REGULAR:
                holder.smallLayout.setVisibility(View.GONE);
                holder.text.setText(mListItems.get(position).getTitle());
                int visibility = mSelectedIndex == position ? View.VISIBLE : View.INVISIBLE;
                holder.selector.setVisibility(visibility);
                holder.bigLayout.setVisibility(View.VISIBLE);
                break;
            case SMALL:
                holder.smallText.setText(mListItems.get(position).getTitle());
                holder.bigLayout.setVisibility(View.GONE);
                holder.icon.setImageResource(mListItems.get(position).getIconRes());
                holder.icon.setVisibility(View.VISIBLE);
                holder.smallLayout.setVisibility(View.VISIBLE);
                break;
        }

        return convertView;
    }

    /**
     * Return if a given position is selected. If it's not selected select it.
     */
    public boolean isPositionSelected(int position) {
        boolean isSelected = true;

        if (mSelectedIndex != position) {
            isSelected = false;
            mSelectedIndex = position;
            notifyDataSetChanged();
        }
        return isSelected;
    }

    /**
     * ViewHolder used to increase performance.
     */
    private static class ViewHolder {
        TextView text;
        View selector;
        ImageView icon;
        TextView smallText;
        View bigLayout;
        View smallLayout;
    }
}
