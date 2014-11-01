package se.johan.wendler.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.sql.SQLException;
import java.util.ArrayList;

import se.johan.wendler.R;
import se.johan.wendler.R.id;
import se.johan.wendler.ui.adapter.WorkoutListAdapter;
import se.johan.wendler.model.Workout;
import se.johan.wendler.sql.SqlHandler;
import se.johan.wendler.activity.MainActivity;
import se.johan.wendler.activity.WorkoutActivity;
import se.johan.wendler.util.WendlerizedLog;
import se.johan.wendler.util.WorkoutHolder;

/**
 * Fragment which holds workouts for a list.
 */
public class WorkoutNavigationListFragment extends Fragment
        implements AdapterView.OnItemClickListener {

    private static final String EXTRA_WORKOUT_LIST = "workoutList";
    private ArrayList<Workout> mListOfWorkouts;

    public WorkoutNavigationListFragment() {
    }

    /**
     * Static creation to avoid problems on rotation.
     */
    public static WorkoutNavigationListFragment newInstance(int week, SqlHandler sqlHandler) {
        WorkoutNavigationListFragment fragment = new WorkoutNavigationListFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(EXTRA_WORKOUT_LIST, sqlHandler.getWorkoutsForList(week));
        fragment.setArguments(bundle);
        return fragment;
    }

    /**
     * Called when the view is created.
     */
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mListOfWorkouts = getArguments().getParcelableArrayList(EXTRA_WORKOUT_LIST);
        View view = inflater.inflate(R.layout.list_empty, container, false);

        ListView listView = (ListView) view.findViewById(id.listView);

        WorkoutListAdapter mAdapter = new WorkoutListAdapter(
                getActivity(),
                getArguments().<Workout>getParcelableArrayList(EXTRA_WORKOUT_LIST),
                WorkoutListAdapter.TYPE_WORKOUTS,
                null);

        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(this);

        return view;
    }

    /**
     * Called when a item is clicked in the list.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SqlHandler handler = new SqlHandler(getActivity());
        try {
            handler.open();

            Workout workout = mListOfWorkouts.get(position);

            // Not initialized so do it!
            if (workout.getMainExercise() == null) {
                workout.setMainExercise(handler.getMainExerciseForWorkout(workout));
            }

            if (workout.getAdditionalExercises() == null
                    || workout.getAdditionalExercises().isEmpty()) {
                workout.setAdditionalExercises(handler.getExtraExerciseForWorkout(workout));
            }

            Intent intent = new Intent(getActivity(), WorkoutActivity.class);
            WorkoutHolder.getInstance().putWorkout(getWorkoutItem(workout));
            startActivityForResult(intent, MainActivity.REQUEST_WORKOUT_RESULT);
        } catch (SQLException e) {
            WendlerizedLog.e("Failed to get main exercise for workout", e);
        } finally {
            handler.close();
        }
    }

    private WorkoutHolder.WorkoutItem getWorkoutItem(Workout workout) {
        return new WorkoutHolder.WorkoutItem(workout, 0, false, -1);
    }
}
