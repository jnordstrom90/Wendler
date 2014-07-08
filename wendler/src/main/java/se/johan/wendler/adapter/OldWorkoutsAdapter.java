package se.johan.wendler.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import se.johan.wendler.R;
import se.johan.wendler.model.Workout;

/**
 * Adapter for the list of old workouts.
 */
public class OldWorkoutsAdapter extends BaseAdapter {

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("d LLLL yyyy");

    private final LayoutInflater mInflater;
    private final ArrayList<Workout> mWorkouts;
    private final Resources mResources;

    /**
     * Constructor for the adapter.
     */
    public OldWorkoutsAdapter(Context context, ArrayList<Workout> workouts) {
        mInflater = LayoutInflater.from(context);
        mWorkouts = workouts;
        mResources = context.getResources();
    }

    /**
     * Return the number of items in the list.
     */
    @Override
    public int getCount() {
        return mWorkouts.size();
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.old_workouts_card, parent, false);
            holder.title = (TextView) convertView.findViewById(R.id.textTitle);
            holder.date = (TextView) convertView.findViewById(R.id.textDate);
            holder.performance = (TextView) convertView.findViewById(R.id.textPerformance);
            holder.weekCycle = (TextView) convertView.findViewById(R.id.textWeekCycle);
            holder.additional = (TextView) convertView.findViewById(R.id.textAdditional);
            holder.success = (ImageView) convertView.findViewById(R.id.imageSuccess);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Workout workout = mWorkouts.get(position);
        holder.title.setText(workout.getDisplayName());
        Time time = workout.getWorkoutTime();
        holder.date.setText(FORMAT.format(new Date(time.normalize(false))));
        int imageResource = workout.isWon() ? R.drawable.check : R.drawable.clear;
        holder.success.setImageResource(imageResource);
        int cycle = workout.getCycleDisplayName() == 0 ?
                workout.getCycle() : workout.getCycleDisplayName();
        String text = String.format(
                mResources.getString(R.string.week_cycle_string),
                String.valueOf(workout.getWeek()),
                String.valueOf(cycle));
        holder.weekCycle.setText(text);

        int size = workout.getAdditionalExercises().size();
        String quantity;
        if (size > 0) {
            quantity = mResources.getQuantityString(R.plurals.nbr_additional_exercises, size);
            text = String.format(quantity, size);
            holder.additional.setText(text);
            holder.additional.setVisibility(View.VISIBLE);
        } else {
            holder.additional.setVisibility(View.GONE);
        }
        int reps = workout.getMainExercise().getLastSetProgress();
        reps = reps == -1 ? 0 : reps;
        int nbrOfSets = workout.getMainExercise().getExerciseSets().size();
        double lastSetWeight = workout.getMainExercise().getExerciseSet(nbrOfSets - 1).getWeight();

        quantity = mResources.getQuantityString(R.plurals.performance_text, reps);
        if (reps == 1) {
            text = String.format(quantity, lastSetWeight);
        } else {
            text = String.format(quantity, reps, lastSetWeight);
        }
        holder.performance.setText(text);

        return convertView;
    }

    /**
     * ViewHolder to increase performance.
     */
    private static class ViewHolder {
        private TextView title;
        private TextView date;
        private TextView performance;
        private TextView weekCycle;
        private TextView additional;
        private ImageView success;
    }
}
