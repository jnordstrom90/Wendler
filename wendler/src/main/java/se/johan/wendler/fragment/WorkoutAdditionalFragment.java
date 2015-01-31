package se.johan.wendler.fragment;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.SimpleFloatViewManager;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.listeners.ActionClickListener;
import com.nispok.snackbar.listeners.EventListener;

import java.util.ArrayList;

import se.johan.wendler.R;
import se.johan.wendler.fragment.base.WorkoutFragment;
import se.johan.wendler.model.Action;
import se.johan.wendler.model.AdditionalExercise;
import se.johan.wendler.model.TapToUndoItem;
import se.johan.wendler.model.Workout;
import se.johan.wendler.sql.SqlHandler;
import se.johan.wendler.ui.adapter.AdditionalExerciseAdapter;
import se.johan.wendler.ui.dialog.AdditionalExerciseDialog;
import se.johan.wendler.util.CardsOptionHandler;

/**
 * WorkoutFragment for additional exercises.
 */
public class WorkoutAdditionalFragment extends WorkoutFragment implements
        DragSortListView.DropListener,
        DragSortListView.RemoveListener,
        AdditionalExerciseDialog.onConfirmClickedListener,
        Action.ActionListener {

    public static final String TAG = WorkoutAdditionalFragment.class.getName();

    private static final String EXTRA_EXERCISE_ITEM = "exerciseItem";
    private static final String EXTRA_WORKOUT_NAME = "workoutName";

    private static ArrayList<AdditionalExercise>
            sAdditionalExercises = new ArrayList<AdditionalExercise>();
    private AdditionalExerciseAdapter mAdapter;
    private DragSortListView mListView;
    private boolean mIsModified;

    public WorkoutAdditionalFragment() {
    }

    /**
     * Static creation to avoid problems on rotation.
     */
    public static WorkoutAdditionalFragment newInstance(
            ArrayList<AdditionalExercise> additionalExercises,
            String workoutName) {
        Bundle arguments = new Bundle();
        arguments.putParcelableArrayList(EXTRA_EXERCISE_ITEM, additionalExercises);
        arguments.putString(EXTRA_WORKOUT_NAME, workoutName);
        WorkoutAdditionalFragment fragment = new WorkoutAdditionalFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    /**
     * Called when the view is created.
     */
    @SuppressLint("WrongViewCast")
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.list_drag, container, false);

        sAdditionalExercises = getArguments().getParcelableArrayList(EXTRA_EXERCISE_ITEM);

        mListView = (DragSortListView) view.findViewById(R.id.list_drag);
        mAdapter = new AdditionalExerciseAdapter(
                getActivity(),
                sAdditionalExercises,
                mCardHandler,
                AdditionalExerciseAdapter.TYPE_WORKOUT);

        mListView.setAdapter(mAdapter);
        mListView.setDragEnabled(true);
        mListView.setDropListener(this);
        buildController();
        return view;
    }

    /**
     * Called when additional exercises are reordered.
     */
    @Override
    public void drop(int from, int to) {
        AdditionalExercise exercise = sAdditionalExercises.get(from);
        sAdditionalExercises.remove(exercise);
        sAdditionalExercises.add(to, exercise);
        mAdapter.notifyDataSetChanged();
        mIsModified = true;
    }

    /**
     * Called when an additional exercise is removed.
     */
    @Override
    public void remove(int which) {
        onRemove(which);
    }

    /**
     * Called when the confirmation dialog is confirmed.
     */
    @Override
    public void onConfirmClicked(AdditionalExercise exercise) {

        int pos = getPosForAdditionalExercise(exercise.getExerciseId());

        if (pos < 0) {
            sAdditionalExercises.add(exercise);
        } else {
            sAdditionalExercises.set(pos, exercise);
        }
        mIsModified = true;
        mAdapter.notifyDataSetChanged();
    }

    /**
     * Called when the workout should be stored.
     */
    @Override
    public boolean storeWorkout(boolean complete,
                                Workout workout,
                                SqlHandler handler,
                                boolean delayedDeload) {
        boolean isStarted = mIsModified || isAnyExerciseStarted();
        return handler.storeAdditionalExercise(
                workout.getWorkoutId(), sAdditionalExercises, isStarted);
    }

    /**
     * Called when an action in the floating action button is performed.
     */
    @Override
    public void onActionTaken(Action action) {
        switch (action) {
            case ADD_EXERCISE:
                showAdditionalExerciseDialog(null, getNextExerciseId());
                break;
        }
    }

    /**
     * Called to launch the additional exercise dialog.
     */
    private void showAdditionalExerciseDialog(AdditionalExercise exercise, int id) {
        AdditionalExerciseDialog.newInstance(
                getString(R.string.add_exercise),
                exercise,
                id,
                this).show(getFragmentManager(), AdditionalExerciseDialog.TAG);
    }

    /**
     * Called to remove an additional exercise at a given position.
     */
    private void onRemove(int which) {
        if (which >= sAdditionalExercises.size()) {
            return;
        }
        mIsModified = true;
        AdditionalExercise exercise = sAdditionalExercises.get(which);
        TapToUndoItem item = new TapToUndoItem(exercise, which);
        sAdditionalExercises.remove(which);

        createSnackBar(exercise, item);
        mAdapter.notifyDataSetChanged();
    }

    /**
     * Create a snack bar where the user can undo the deletion.
     */
    private void createSnackBar(AdditionalExercise exercise, TapToUndoItem item) {
        SnackbarManager.dismiss();
        Snackbar bar = Snackbar.with(getActivity())
                .text(getSnackBarText(exercise))
                .actionLabel(getString(R.string.undo))
                .actionListener(getActionListener(item))
                .eventListener(getEventListener());
        SnackbarManager.show(bar);
    }

    /**
     * Returns the event listener for the snack bar.
     */
    private EventListener getEventListener() {
        if (getActivity() instanceof EventListener) {
            return (EventListener) getActivity();
        }
        return null;
    }

    /**
     * Returns an ActionListener for undoing the deletion.
     */
    private ActionClickListener getActionListener(final TapToUndoItem item) {
        return new ActionClickListener() {
            @Override
            public void onActionClicked(Snackbar snackbar) {
                sAdditionalExercises.add(item.getPosition(), (AdditionalExercise) item.getObject());
                mAdapter.notifyDataSetChanged();
            }
        };
    }

    /**
     * Returns the text for the snack bar.
     */
    private String getSnackBarText(AdditionalExercise exercise) {
        return String.format(getString(R.string.snack_bar_deleted), exercise.getName());
    }

    /**
     * Build the controller for the ListView.
     */
    private void buildController() {
        SimpleFloatViewManager simpleFloatViewManager = new SimpleFloatViewManager(mListView);
        simpleFloatViewManager.setBackgroundColor(Color.TRANSPARENT);
        mListView.setFloatViewManager(simpleFloatViewManager);
        mListView.setRemoveListener(this);
    }


    /**
     * Return the next available exercise id.
     */
    private int getNextExerciseId() {
        int id = 1;
        for (AdditionalExercise exercise : sAdditionalExercises) {
            if (exercise.getExerciseId() > id) {
                id = exercise.getExerciseId();
            }
        }
        return ++id;
    }

    /**
     * Return the position or -1 for a given exercise id.
     */
    private int getPosForAdditionalExercise(int exerciseId) {
        for (int i = 0; i < sAdditionalExercises.size(); i++) {
            if (sAdditionalExercises.get(i).getExerciseId() == exerciseId) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns true if any of the exercises are started.
     */
    private boolean isAnyExerciseStarted() {
        for (AdditionalExercise exercise : sAdditionalExercises) {
            if (exercise.isStarted()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Handler for the additional exercise cards.
     */
    private final CardsOptionHandler mCardHandler = new CardsOptionHandler() {
        /**
         * Called when an additional exercise is deleted.
         */
        @Override
        public void onDelete(int position) {
            onRemove(position);
        }

        /**
         * Called when an additional exercise should be edited.
         */
        @Override
        public void onEdit(int position) {
            AdditionalExercise exercise = sAdditionalExercises.get(position);
            showAdditionalExerciseDialog(exercise, exercise.getExerciseId());
        }
    };
}
