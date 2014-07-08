package se.johan.wendler.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import se.johan.wendler.R;
import se.johan.wendler.model.AdditionalExercise;
import se.johan.wendler.util.CardsOptionHandler;

/**
 * Adapter for additional exercises displayed in a list.
 */
public class AdditionalExerciseAdapter extends BaseAdapter {
    private final ArrayList<AdditionalExercise> mAdditionalExercises;
    private final Context mContext;
    private final CardsOptionHandler mHandler;
    private final boolean mIsProgressEnabled;
    private final LayoutInflater mInflater;

    public AdditionalExerciseAdapter(Context context,
                                     ArrayList<AdditionalExercise> additionalExercises,
                                     CardsOptionHandler handler,
                                     boolean isProgressEnabled) {
        mAdditionalExercises = additionalExercises;
        mContext = context;
        mHandler = handler;
        mIsProgressEnabled = isProgressEnabled;
        mInflater = LayoutInflater.from(context);
    }

    /**
     * Return the number of additional exercises in the adapter.
     */
    @Override
    public int getCount() {
        return mAdditionalExercises.size();
    }

    /**
     * Return the item at a given position.
     */
    @Override
    public Object getItem(int position) {
        return null;
    }

    /**
     * Return the id of the item at a given position.
     */
    @Override
    public long getItemId(int position) {
        return 0;
    }

    /**
     * Return the view of a given position.
     */
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.additional_exercise_item, parent, false);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.textViewName);
            holder.weight = (TextView) convertView.findViewById(R.id.textViewWeight);
            holder.goal = (TextView) convertView.findViewById(R.id.textViewProgress);
            holder.delete = (ImageButton) convertView.findViewById(R.id.delete_button);
            holder.edit = (ImageButton) convertView.findViewById(R.id.edit_button);
            holder.decreaseButton = (Button) convertView.findViewById(R.id.button_decrease);
            holder.increaseButton = (Button) convertView.findViewById(R.id.button_increase);

            int visibility = mIsProgressEnabled ? View.VISIBLE : View.GONE;
            holder.decreaseButton.setVisibility(visibility);
            holder.increaseButton.setVisibility(visibility);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        AdditionalExercise exercise = mAdditionalExercises.get(position);
        Resources res = mContext.getResources();

        holder.name.setText(exercise.getName());

        setWeightText(holder, res, exercise, position);

        setGoalText(holder, res, exercise, position);

        setOnClickListeners(holder, exercise, position);

        return convertView;
    }

    /**
     * Set the text for the weight of the exercise.
     */
    private void setWeightText(
            ViewHolder holder, Resources res, AdditionalExercise exercise, int position) {

        double weight = exercise.getExerciseSet(position).getWeight();
        String weightAsText = weight < 1 ?
                res.getString(R.string.not_available) : String.valueOf(weight);
        String text =
                String.format(res.getString(R.string.extra_exercise_weight), weightAsText);

        holder.weight.setText(text);
    }

    /**
     * Set the text for the goal of the exercise.
     */
    private void setGoalText(
            ViewHolder holder, Resources res, AdditionalExercise exercise, int position) {
        String text;
        int goal = exercise.getExerciseSet(position).getGoal();
        if (mIsProgressEnabled) {
            text = String.format(res.getString(R.string.extra_exercise_progress),
                    exercise.getProgress(0), goal);
        } else {
            text = String.format(res.getString(R.string.extra_exercise_goal), goal);
        }
        holder.goal.setText(text);

        if (goal == exercise.getProgress(0)) {
            holder.name.setPaintFlags(holder.name.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else if ((holder.name.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) > 0) {
            holder.name.setPaintFlags(holder.name.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    /**
     * Set the OnClickListener for various buttons.
     */
    private void setOnClickListeners(
            ViewHolder holder, final AdditionalExercise exercise, final int position) {
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.onDelete(position);
            }
        });

        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.onEdit(position);
            }
        });

        holder.increaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if ((exercise.getProgress(0) + 1) > exercise.getExerciseSet(0).getGoal()) {
                    return;
                }
                exercise.setProgress(0, exercise.getProgress(0) + 1);
                notifyDataSetChanged();
            }
        });

        holder.decreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((exercise.getProgress(0) - 1) < 0) {
                    return;
                }

                exercise.setProgress(0, exercise.getProgress(0) - 1);
                notifyDataSetChanged();
            }
        });
    }

    /**
     * Static ViewHolder to improve performance.
     */
    private static class ViewHolder {
        TextView name;
        TextView weight;
        TextView goal;
        ImageButton edit;
        ImageButton delete;
        Button increaseButton;
        Button decreaseButton;
    }
}
