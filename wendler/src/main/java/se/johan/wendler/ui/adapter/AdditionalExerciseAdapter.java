package se.johan.wendler.ui.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.ArrayList;

import se.johan.wendler.R;
import se.johan.wendler.model.AdditionalExercise;
import se.johan.wendler.util.CardsOptionHandler;
import se.johan.wendler.util.Utils;

/**
 * Adapter for additional exercises displayed in a list.
 */
public class AdditionalExerciseAdapter extends BaseAdapter {

    public static final int TYPE_WORKOUT = 0;
    public static final int TYPE_ADD_WORKOUT = 1;

    private final ArrayList<AdditionalExercise> mAdditionalExercises;
    private final Context mContext;
    private final CardsOptionHandler mHandler;
    private final LayoutInflater mInflater;
    private int mType;

    public AdditionalExerciseAdapter(Context context,
                                     ArrayList<AdditionalExercise> additionalExercises,
                                     CardsOptionHandler handler,
                                     int type) {
        mAdditionalExercises = additionalExercises;
        mContext = context;
        mHandler = handler;
        mType = type;
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
            convertView = mInflater.inflate(R.layout.card_additional_exercise, parent, false);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.textViewName);
            holder.weight = (TextView) convertView.findViewById(R.id.textViewWeight);
            holder.goal = (TextView) convertView.findViewById(R.id.textViewProgress);
            holder.decreaseButton = (Button) convertView.findViewById(R.id.button_decrease);
            holder.increaseButton = (Button) convertView.findViewById(R.id.button_increase);
            holder.menu = (ImageButton) convertView.findViewById(R.id.overflow_button);

            int visibility = mType == TYPE_WORKOUT ? View.VISIBLE : View.GONE;
            convertView.findViewById(R.id.button_bar).setVisibility(visibility);
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

        String weightAsText;

        if (mType == TYPE_ADD_WORKOUT && !TextUtils.isEmpty(exercise.getMainExerciseName())) {
            weightAsText = res.getString(
                    R.string.extra_exercise_percentage,
                    String.valueOf(exercise.getMainExercisePercentage()),
                    exercise.getMainExerciseName());
        } else {
            double weight = exercise.getExerciseSet(position).getWeight();
            weightAsText = weight < 1 ?
                    res.getString(R.string.not_available) : String.valueOf(weight);

        }
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
        if (mType == TYPE_WORKOUT) {
            text = String.format(res.getString(R.string.extra_exercise_progress),
                    exercise.getProgress(0), goal);
        } else {
            text = String.format(res.getString(R.string.exercise_goal), goal);
        }
        holder.goal.setText(text);

        if (goal == exercise.getProgress(0)) {
            holder.name.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    mContext.getResources().getDrawable(R.drawable.ic_check_black_24dp),
                    null);
            holder.name.setPaintFlags(holder.name.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else if ((holder.name.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) > 0) {
            holder.name.setPaintFlags(holder.name.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.name.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        }
    }

    /**
     * Set the OnClickListener for various buttons.
     */
    private void setOnClickListeners(
            ViewHolder holder, final AdditionalExercise exercise, final int position) {
        holder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayPopupMenu(v, position, exercise);
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
     * Display the popup menu of additional exercises.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void displayPopupMenu(View v, final int position, final AdditionalExercise exercise) {
        PopupMenu popup = new PopupMenu(v.getContext(), v);
        popup.getMenuInflater()
                .inflate(R.menu.menu_additional_exercise_item, popup.getMenu());
        popup.getMenu().getItem(1).setVisible(mType == TYPE_WORKOUT);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.edit:
                        mHandler.onEdit(position);
                        return true;
                    case R.id.delete:
                        mHandler.onDelete(position);
                        return true;
                    case R.id.complete:
                        exercise.setProgress(0, exercise.getExerciseSet(0).getGoal());
                        notifyDataSetChanged();
                        return true;
                }
                return false;
            }
        });
        if (Utils.hasKitKat()) {
            v.setOnTouchListener(popup.getDragToOpenListener());
        }
        popup.show();
    }

    /**
     * Static ViewHolder to improve performance.
     */
    private static class ViewHolder {
        TextView name;
        TextView weight;
        TextView goal;
        ImageButton menu;
        Button increaseButton;
        Button decreaseButton;
    }
}
