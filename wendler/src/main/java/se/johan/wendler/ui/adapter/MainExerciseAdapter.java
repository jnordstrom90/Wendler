package se.johan.wendler.ui.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import se.johan.wendler.R;
import se.johan.wendler.model.ExerciseSet;
import se.johan.wendler.model.MainExercise;
import se.johan.wendler.model.SetType;

/**
 * Adapter used in the list for displaying the main exercise during a workout.
 */
public class MainExerciseAdapter extends BaseAdapter {
    private final MainExercise mMainExercise;
    private final LayoutInflater mInflater;
    private final Resources mRes;

    /**
     * Constructor for the adapter.
     */
    public MainExerciseAdapter(Context context, MainExercise exercise) {
        mMainExercise = exercise;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRes = context.getResources();
    }

    /**
     * Return the number of sets to perform.
     */
    @Override
    public int getCount() {
        return mMainExercise.getExerciseSets().size();
    }

    /**
     * Return the item at a certain position.
     */
    @Override
    public Object getItem(int position) {
        return null;
    }

    /**
     * Return the item id of a certain position.
     */
    @Override
    public long getItemId(int position) {
        return 0;
    }

    /**
     * Return the view for a given position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_text_view, parent, false);
            holder = new ViewHolder();
            holder.textView = (TextView) convertView.findViewById(R.id.textView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String text = null;
        String plusSet = "";
        ExerciseSet set = mMainExercise.getExerciseSet(position);
        switch (set.getType()) {
            case WARM_UP:
                text = String.format(mRes.getString(R.string.warm_up),
                        String.valueOf(set.getWeight()),
                        String.valueOf(set.getGoal()));
                break;
            case PLUS_SET:
                plusSet = "+";
            case REGULAR:
                text = String.format(
                        mRes.getStringArray(R.array.sets)[position - getPosition()],
                        String.valueOf(set.getWeight()),
                        String.valueOf(set.getGoal() + plusSet));
                break;
        }

        holder.textView.setText(text);

        if (set.isWon() || set.isComplete()) {
            holder.textView.setPaintFlags(
                    holder.textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.textView.setPaintFlags(
                    holder.textView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
        return convertView;
    }

    /**
     * Return the position of the first set of regular exercises.
     */
    private int getPosition() {
        int pos = 0;
        for (ExerciseSet set : mMainExercise.getExerciseSets()) {
            if (set.getType().equals(SetType.WARM_UP)) {
                pos++;
            }
        }
        return pos;
    }

    /**
     * ViewHolder to increase performance.
     */
    private static class ViewHolder {
        public TextView textView;
    }
}
