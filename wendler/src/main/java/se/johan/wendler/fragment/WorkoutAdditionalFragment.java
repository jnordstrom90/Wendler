package se.johan.wendler.fragment;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

import com.melnykov.fab.FloatingActionButton;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.SimpleFloatViewManager;
import com.williammora.snackbar.Snackbar;

import java.sql.SQLException;
import java.util.ArrayList;

import se.johan.wendler.R;
import se.johan.wendler.ui.adapter.AdditionalExerciseAdapter;
import se.johan.wendler.model.AdditionalExercise;
import se.johan.wendler.model.TapToUndoItem;
import se.johan.wendler.model.Workout;
import se.johan.wendler.sql.SqlHandler;
import se.johan.wendler.ui.dialog.AdditionalExerciseDialog;
import se.johan.wendler.fragment.base.WorkoutFragment;
import se.johan.wendler.util.CardsOptionHandler;
import se.johan.wendler.util.WendlerizedLog;

/**
 * WorkoutFragment for additional exercises.
 */
public class WorkoutAdditionalFragment extends WorkoutFragment implements
        View.OnClickListener,
        DragSortListView.DropListener, DragSortListView.RemoveListener,
        AdditionalExerciseDialog.onConfirmClickedListener {

    public static final String TAG = WorkoutAdditionalFragment.class.getName();

    private static final String EXTRA_EXERCISE_ITEM = "exerciseItem";
    private static final String EXTRA_WORKOUT_NAME = "workoutName";

    private static ArrayList<AdditionalExercise>
            sAdditionalExercises = new ArrayList<AdditionalExercise>();
    private AdditionalExerciseAdapter mAdapter;
    private DragSortListView mListView;

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

        FloatingActionButton floatingActionButton =
                (FloatingActionButton) view.findViewById(R.id.button_floating_action);
        floatingActionButton.setOnClickListener(this);

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
    }

    /**
     * Called when the footer is clicked.
     */
    @Override
    public void onClick(View v) {
        showAdditionalExerciseDialog(null, getNextExerciseId());
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

        SqlHandler handler = new SqlHandler(getActivity());
        boolean isNew = true;
        try {
            handler.open();
            isNew = handler.extraExerciseIsNew(
                    getArguments().getString(EXTRA_WORKOUT_NAME), exercise.getExerciseId())
                    && pos == -1;
        } catch (SQLException e) {
            WendlerizedLog.e("Failed to add new exercise", e);
        } finally {
            handler.close();
        }

        if (isNew) {
            sAdditionalExercises.add(exercise);
        } else if (pos > -1) {
            sAdditionalExercises.set(pos, exercise);
        }

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
        return handler.storeAdditionalExercise(workout.getWorkoutId(), sAdditionalExercises);
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
        Snackbar.with(getActivity())
                .text(getSnackBarText(exercise))
                .actionLabel(getString(R.string.undo))
                .actionListener(getActionListener(item))
                .eventListener(getEventListener())
                .show(getActivity());
    }

    /**
     * Returns the EventListener for displaying and hiding the snack bar
     */
    private Snackbar.EventListener getEventListener() {
        final View view = getActivity().findViewById(R.id.button_floating_action);
        return new Snackbar.EventListener() {
            @Override
            public void onShow(int height) {
                view.animate()
                        .translationY(view.getTranslationY() - (height * 2))
                        .setInterpolator(getInterpolator())
                        .start();
            }

            @Override
            public void onDismiss(int height) {
                view.animate().translationY(view.getTranslationY() + (height * 2)).start();
            }
        };
    }

    /**
     * Load the interpolator used for animating the FAB up.
     */
    private Interpolator getInterpolator() {
        return AnimationUtils.loadInterpolator(
                getActivity(), android.R.interpolator.decelerate_quad);
    }

    /**
     * Returns an ActionListener for undoing the deletion.
     */
    private Snackbar.ActionClickListener getActionListener(final TapToUndoItem item) {
        return new Snackbar.ActionClickListener() {
            @Override
            public void onActionClicked() {
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
