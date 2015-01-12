package se.johan.wendler.ui.adapter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import se.johan.wendler.R;
import se.johan.wendler.model.MainExercise;
import se.johan.wendler.model.Workout;
import se.johan.wendler.sql.SqlHandler;
import se.johan.wendler.activity.MainActivity;
import se.johan.wendler.activity.WorkoutActivity;
import se.johan.wendler.ui.view.TextDrawable;
import se.johan.wendler.util.CardsOptionHandler;
import se.johan.wendler.util.ColorGenerator;
import se.johan.wendler.util.Utils;
import se.johan.wendler.util.WendlerizedLog;
import se.johan.wendler.util.WorkoutHolder;

/**
 * Adapter for the workouts.
 */
public class WorkoutListAdapter extends BaseAdapter {

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("d LLLL yyyy");

    public static final int TYPE_WORKOUTS = 0;
    public static final int TYPE_OLD_WORKOUTS = 1;

    private final LayoutInflater mInflater;
    private final ArrayList<Workout> mListOfWorkouts;
    private final Context mContext;
    private int mType;
    private CardsOptionHandler mHandler;

    /**
     * Constructor for the adapter.
     */
    public WorkoutListAdapter(
            Context context,
            ArrayList<Workout> listOfWorkouts,
            int type,
            CardsOptionHandler handler) {
        mListOfWorkouts = listOfWorkouts;
        mInflater = LayoutInflater.from(context);
        mContext = context;
        mType = type;
        mHandler = handler;
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
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        Workout workout = mListOfWorkouts.get(position);
        if (convertView == null) {
            convertView = mInflater.inflate(getResource(), parent, false);
            holder = new ViewHolder();

            holder.exercise = (TextView) convertView.findViewById(R.id.textExercise);
            holder.weekCycle = (TextView) convertView.findViewById(R.id.textWeekCycle);
            holder.goal = (TextView) convertView.findViewById(R.id.textGoal);
            holder.date = (TextView) convertView.findViewById(R.id.textDate);
            holder.overflow = (ImageButton) convertView.findViewById(R.id.overflow_button);
            holder.imageView = (ImageView) convertView.findViewById(R.id.image_view);
            if (holder.imageView != null) {
                String text = workout.getDisplayName().substring(0, 1);
                int color = ColorGenerator.DEFAULT.getColor(text);
                holder.textDrawable = TextDrawable.builder().buildRound(text, color);
            }

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        setCardClickListener(position, convertView);

        if (workout.getMainExercise() == null) {
            workout.setMainExercise(getMainExercise(workout));
        }

        holder.exercise.setText(workout.getDisplayName());
        if (holder.imageView != null) {
            holder.imageView.setImageDrawable(holder.textDrawable);
        }

        setWeekCycleText(holder, workout);
        setGoalText(holder, workout);

        setCompletionStatus(holder, workout);
        createOverflowMenu(holder, position);
        setWorkoutDate(holder, workout);
        return convertView;
    }

    /**
     * Get the resource for the view.
     */
    private int getResource() {
        switch (mType) {
            case TYPE_OLD_WORKOUTS:
                return R.layout.card_workout;
            default :
                return R.layout.item_workout;
        }
    }

    /**
     * Display the date label if needed.
     */
    private void setWorkoutDate(ViewHolder holder, Workout workout) {
        if (mType != TYPE_OLD_WORKOUTS) {
            return;
        }

        Time time = workout.getWorkoutTime();
        holder.date.setText(FORMAT.format(new Date(time.normalize(false))));
        holder.date.setVisibility(View.VISIBLE);
    }

    /**
     * Display the overflow menu if needed.
     */
    private void createOverflowMenu(final ViewHolder holder, final int position) {
        if (mType != TYPE_OLD_WORKOUTS) {
            return;
        }

        holder.overflow.setVisibility(View.VISIBLE);

        holder.overflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleOverflowClick(holder, position);
            }
        });
    }

    /**
     * Called when the overflow menu has been clicked.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void handleOverflowClick(ViewHolder holder, final int position) {
        PopupMenu popup = new PopupMenu(mContext, holder.overflow);
        popup.getMenuInflater().inflate(R.menu.menu_delete, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.delete:
                        mHandler.onDelete(position);
                        return true;
                }
                return false;
            }
        });
        if (Utils.hasKitKat()) {
            holder.overflow.setOnTouchListener(popup.getDragToOpenListener());
        }
        popup.show();
    }

    /**
     * Sets the completion status of the view.
     */
    private void setCompletionStatus(ViewHolder holder, Workout workout) {

        if (workout.isComplete()) {
            Drawable drawable = workout.isWon()
                    ? mContext.getResources().getDrawable(R.drawable.ic_check_black_24dp)
                    : mContext.getResources().getDrawable(R.drawable.ic_close_black_24dp);
            if (mType != TYPE_OLD_WORKOUTS) {
                holder.exercise.setPaintFlags(
                        holder.exercise.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
            holder.exercise.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
        } else {
            holder.exercise.setCompoundDrawables(null, null, null, null);
            if (mType != TYPE_OLD_WORKOUTS) {
                holder.exercise.setPaintFlags(
                        holder.exercise.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }
        }
    }

    /**
     * Set the text of the goal label.
     */
    private void setGoalText(ViewHolder holder, Workout workout) {
        String text;

        if (mType == TYPE_WORKOUTS && !workout.isComplete()) {
            text = mContext.getString(
                    R.string.main_exercise_goal,
                    workout.getMainExercise().getGoal(),
                    workout.getMainExercise().getLastSetWeight());
        } else {
            int reps = workout.getMainExercise().getLastSetProgress();
            reps = reps == -1 ? 0 : reps;
            double lastSetWeight = workout.getMainExercise().getLastSetWeight();

            String quantity = mContext
                    .getResources()
                    .getQuantityString(R.plurals.performance_text, reps);
            if (reps == 1) {
                text = String.format(quantity, lastSetWeight);
            } else {
                text = String.format(quantity, reps, lastSetWeight);
            }
        }
        WendlerizedLog.d("Set Goal: " + text);
        holder.goal.setText(text);
    }

    /**
     * Set the cycle text and possibly week as well.
     */
    private void setWeekCycleText(ViewHolder holder, Workout workout) {

        String text = "";
        int cycle = workout.getCycleDisplayName() == 0
                ? workout.getCycle() : workout.getCycleDisplayName();
        switch (mType) {
            case TYPE_WORKOUTS:
                text = mContext.getString(R.string.cycle) + " " + cycle;
                break;
            case TYPE_OLD_WORKOUTS:
                text = String.format(
                        mContext.getString(R.string.week_cycle_string),
                        String.valueOf(workout.getWeek()),
                        String.valueOf(cycle));
                break;
        }
        holder.weekCycle.setText(text);
    }

    /**
     * Handle clicks on the CardView.
     */
    private void setCardClickListener(final int position, View convertView) {
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SqlHandler handler = new SqlHandler(mContext);
                try {
                    handler.open();

                    Workout workout = mListOfWorkouts.get(position);

                    // Not initialized so do it!
                    if (workout.getMainExercise() == null) {
                        workout.setMainExercise(handler.getMainExerciseForWorkout(workout));
                    }

                    if (workout.getAdditionalExercises() == null
                            || workout.getAdditionalExercises().isEmpty()) {
                        workout.setAdditionalExercises(handler.getAdditionalExercisesForWorkout(workout));
                    }

                    Intent intent = new Intent(mContext, WorkoutActivity.class);
                    WorkoutHolder.getInstance().putWorkout(getWorkoutItem(workout));
                    ((Activity) mContext)
                            .startActivityForResult(intent, MainActivity.REQUEST_WORKOUT_RESULT);
                } catch (SQLException e) {
                    WendlerizedLog.e("Failed to get main exercise for workout", e);
                } finally {
                    handler.close();
                }
            }
        });
    }

    private WorkoutHolder.WorkoutItem getWorkoutItem(Workout workout) {
        return new WorkoutHolder.WorkoutItem(workout, 0, false, -1);
    }

    private MainExercise getMainExercise(Workout workout) {
        SqlHandler handler = new SqlHandler(mContext);
        try {
            handler.open();
            return handler.getMainExerciseForWorkout(workout);
        } catch (SQLException e) {
            WendlerizedLog.e("Failed to get main exercise for workout", e);
        } finally {
            handler.close();
        }
        return null;
    }

    /**
     * ViewHolder ot increase performance.
     */
    private static class ViewHolder {
        TextView exercise;
        TextView weekCycle;
        TextView goal;
        TextView date;
        ImageButton overflow;
        TextDrawable textDrawable;
        ImageView imageView;
    }
}
