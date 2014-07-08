package se.johan.wendler.view;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.sql.SQLException;

import se.johan.wendler.R;
import se.johan.wendler.model.MainExercise;
import se.johan.wendler.sql.SqlHandler;
import se.johan.wendler.util.WendlerMath;

/**
 * Footer for our main exercise.
 */
@SuppressWarnings("UnusedDeclaration")
public class MainExerciseFooterView extends RelativeLayout {
    /**
     * Constructor.
     */
    public MainExerciseFooterView(Context context) {
        super(context);
    }

    /**
     * Constructor.
     */
    public MainExerciseFooterView(Context context, AttributeSet attrs) {
        super(context, attrs);
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
    public boolean updateInfo(int week, MainExercise exercise) {

        Resources res = getResources();

        if (exercise.getLastSetProgress() != -1) {
            setVisibility(View.VISIBLE);
            TextView oneRm = (TextView) findViewById(R.id.textView);
            TextView reps = (TextView) findViewById(R.id.textView2);
            ImageView image = (ImageView) findViewById(R.id.imageView);

            boolean isWon = WendlerMath.isWorkoutWon(week, exercise);

            int imageResource = isWon ? R.drawable.check : R.drawable.clear;
            image.setImageResource(imageResource);

            int percentage = getLastSetPercentage(week);

            double exerciseWeight = WendlerMath.calculateSetWeight(
                    getContext(),
                    exercise.getWeight(),
                    exercise.getWorkoutPercentage(),
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
            return true;
        } else {
            setVisibility(View.GONE);
            return false;
        }
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
