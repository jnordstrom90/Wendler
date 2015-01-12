package se.johan.wendler.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.sql.SQLException;

import se.johan.wendler.R;
import se.johan.wendler.fragment.WorkoutMainFragment;
import se.johan.wendler.model.MainExercise;
import se.johan.wendler.sql.SqlHandler;
import se.johan.wendler.util.Utils;
import se.johan.wendler.util.WendlerMath;

/**
 * Footer for our main exercise.
 */
@SuppressWarnings("UnusedDeclaration")
public class MainExerciseFooterView extends CardView {
    /**
     * Constructor.
     */
    public MainExerciseFooterView(Context context) {
        super(context, null, -1);
    }

    /**
     * Constructor.
     */
    public MainExerciseFooterView(Context context, AttributeSet attrs) {
        super(context, attrs, -1);
    }

    /**
     * Constructor.
     */
    public MainExerciseFooterView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Update the information for the view and return if it should be shown.
     */
    public boolean updateInfo(
            int week,
            MainExercise exercise,
            boolean isComplete,
            final WorkoutMainFragment callBack) {

        Resources res = getResources();

        if (exercise.getLastSetProgress() != -1) {
            setVisibility(View.VISIBLE);
            TextView oneRm = (TextView) findViewById(R.id.textView);
            TextView reps = (TextView) findViewById(R.id.textView2);
            ImageView image = (ImageView) findViewById(R.id.image_view);

            boolean isWon = WendlerMath.isWorkoutWon(week, exercise);

            int imageResource = isWon
                    ? R.drawable.ic_emoticon_white_36dp
                    : R.drawable.ic_emoticon_sad_white_36dp;
            image.setImageResource(imageResource);

            int color = isWon
                    ? res.getColor(android.R.color.holo_green_light)
                    : res.getColor(android.R.color.holo_red_light);

            image.setBackgroundDrawable(TextDrawable.builder().buildRound("", color));

            int percentage = getLastSetPercentage(week);

            double exerciseWeight = WendlerMath.calculateWeight(
                    getContext(),
                    exercise.getWeight(),
                    percentage);

            double weight = WendlerMath.calculateOneRm(
                    exerciseWeight,
                    exercise.getLastSetProgress());

            String oneRmText = String.format(res.getString(R.string.one_rm_with_number), weight);

            oneRm.setText(oneRmText);

            String repsText = String.format(
                    res.getString(R.string.performed_reps_with_number),
                    exercise.getLastSetProgress());
            reps.setText(repsText);
            createOverflowMenu(isComplete, callBack);
            return true;
        } else {
            setVisibility(View.GONE);
            return false;
        }
    }

    /**
     * Display the overflow menu if needed.
     */
    private void createOverflowMenu(boolean isComplete, final WorkoutMainFragment callBack) {
        if (isComplete) return;
        findViewById(R.id.overflow_button).setVisibility(VISIBLE);
        findViewById(R.id.overflow_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleOverflowClick(v, callBack);
            }
        });
    }


    /**
     * Called when the overflow menu has been clicked.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void handleOverflowClick(View view, final WorkoutMainFragment callBack) {
        PopupMenu popup = new PopupMenu(getContext(), view);
        popup.getMenuInflater().inflate(R.menu.menu_clear, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.clear:
                        callBack.updateProgress(-1);
                        return true;
                }
                return false;
            }
        });
        if (Utils.hasKitKat()) {
            view.setOnTouchListener(popup.getDragToOpenListener());
        }
        popup.show();
    }

    /**
     * Get the last set percentage for a given week.
     */
    private int getLastSetPercentage(int week) {
        SqlHandler handler = new SqlHandler(getContext());
        try {
            handler.open();
            return handler.getSetPercentages(week)[2];
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            handler.close();
        }
        return 100;
    }
}
