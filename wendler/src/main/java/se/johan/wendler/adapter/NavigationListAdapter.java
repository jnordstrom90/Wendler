package se.johan.wendler.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import se.johan.wendler.R;
import se.johan.wendler.model.Workout;

/**
 * Adapter for the NavigationDrawer.
 */
public class NavigationListAdapter extends BaseAdapter {

    private final LayoutInflater mInflater;
    private final ArrayList<Workout> mListOfWorkouts;
    private final Context mContext;

    /**
     * Constructor for the adapter.
     */
    public NavigationListAdapter(Context context, ArrayList<Workout> listOfWorkouts) {
        mListOfWorkouts = listOfWorkouts;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
    }

    /**
     * Return the number of workouts displayed.
     */
    @Override
    public int getCount() {
        return mListOfWorkouts.size();
    }

    /**
     * Return the item at a given position.
     */
    @Override
    public Object getItem(int position) {
        return null;
    }

    /**
     * Return the item id of a given position.
     */
    @Override
    public long getItemId(int position) {
        return 0;
    }

    /**
     * Return the view of a given position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.navigation_list_item, parent, false);
            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.textView);

            holder.subTitle = (TextView) convertView.findViewById(R.id.textViewSmall);

            holder.image = (ImageView) convertView.findViewById(R.id.imageView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Workout workout = mListOfWorkouts.get(position);
        holder.title.setText(workout.getDisplayName());
        int cycle = workout.getCycleDisplayName() == 0 ?
                workout.getCycle() : workout.getCycleDisplayName();
        holder.subTitle.setText(mContext.getString(R.string.cycle) + " " + cycle);

        if (workout.isComplete()) {
            int resourceId = workout.isWon() ? R.drawable.check : R.drawable.clear;
            holder.image.setImageResource(resourceId);
            holder.image.setVisibility(View.VISIBLE);
            holder.title.setPaintFlags(holder.title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.image.setVisibility(View.GONE);
            holder.title.setPaintFlags(
                    holder.title.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
        }
        return convertView;
    }

    /**
     * ViewHolder ot increase performance.
     */
    private static class ViewHolder {
        TextView title;
        TextView subTitle;
        ImageView image;
    }
}
