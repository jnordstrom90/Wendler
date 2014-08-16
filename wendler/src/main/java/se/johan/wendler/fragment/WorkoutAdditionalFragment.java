package se.johan.wendler.fragment;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.SimpleFloatViewManager;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;

import se.johan.wendler.R;
import se.johan.wendler.R.layout;
import se.johan.wendler.adapter.AdditionalExerciseAdapter;
import se.johan.wendler.dialog.AdditionalExerciseDialog;
import se.johan.wendler.fragment.base.WorkoutFragment;
import se.johan.wendler.model.AdditionalExercise;
import se.johan.wendler.model.TapToUndoItem;
import se.johan.wendler.model.Workout;
import se.johan.wendler.sql.SqlHandler;
import se.johan.wendler.util.CardsOptionHandler;
import se.johan.wendler.util.WendlerizedLog;
import se.johan.wendler.view.UndoBarController;

/**
 * WorkoutFragment for additional exercises.
 */
public class WorkoutAdditionalFragment extends WorkoutFragment implements
        View.OnClickListener,
        DragSortListView.DropListener, DragSortListView.RemoveListener,
        AdditionalExerciseDialog.onConfirmClickedListener,
        UndoBarController.UndoListener {

    public static final String TAG = WorkoutAdditionalFragment.class.getName();

    private static final String EXTRA_EXERCISE_ITEM = "exerciseItem";
    private static final String EXTRA_WORKOUT_NAME = "workoutName";

    private static ArrayList<AdditionalExercise> sAdditionalExercises
            = new ArrayList<AdditionalExercise>();
    private View mFooterView;
    private AdditionalExerciseAdapter mAdapter;
    private DragSortListView mListView;
    private UndoBarController mUndoController;

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

        View view = inflater.inflate(R.layout.drag_list_view_view, container, false);

        if (savedInstanceState != null && savedInstanceState.containsKey(EXTRA_EXERCISE_ITEM)) {
            sAdditionalExercises = savedInstanceState.getParcelableArrayList(EXTRA_EXERCISE_ITEM);
        } else if (getArguments() != null) {
            sAdditionalExercises = getArguments().getParcelableArrayList(EXTRA_EXERCISE_ITEM);
        }

        mListView = (DragSortListView) view.findViewById(R.id.list_drag);
        mAdapter = new AdditionalExerciseAdapter(
                getActivity(),
                sAdditionalExercises,
                mCardHandler,
                true);

        setupFooter(inflater);
        mListView.addFooterView(mFooterView);
        mListView.setAdapter(mAdapter);
        mListView.setDragEnabled(true);
        mListView.setDropListener(this);
        buildController();
        mUndoController = new UndoBarController(view.findViewById(R.id.undobar), this);
        return view;
    }

    /**
     * Called to save the current instance state.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mUndoController.onSaveInstanceState(outState);
        outState.putParcelableArrayList(EXTRA_EXERCISE_ITEM, sAdditionalExercises);
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
                    && pos  == -1;
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
        updateFooterTitle();
    }

    /**
     * Called when a deletion is undone.
     */
    @Override
    public void onUndo(Parcelable token) {
        TapToUndoItem item = (TapToUndoItem) token;
        if (item != null) {
            sAdditionalExercises.add(item.getPosition(), (AdditionalExercise) item.getObject());
            mAdapter.notifyDataSetChanged();
            updateFooterTitle();
        }
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
     * Return the additional exercises.
     */
    public ArrayList<AdditionalExercise> getAdditionalExercises() {
        return sAdditionalExercises;
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
        sAdditionalExercises.remove(which);
        String text = String.format(getString(R.string.tap_to_undo), exercise.getName());
        mUndoController.showUndoBar(true, text, new TapToUndoItem(exercise, which));
        mAdapter.notifyDataSetChanged();
        updateFooterTitle();
    }

    /**
     * Build the controller for the ListView.
     */
    private void buildController() {
        DragSortController controller = new DragSortController(mListView);
        controller.setDragHandleId(R.id.drag_handle);
        SimpleFloatViewManager simpleFloatViewManager = new SimpleFloatViewManager(mListView);
        simpleFloatViewManager.setBackgroundColor(Color.TRANSPARENT);
        mListView.setFloatViewManager(simpleFloatViewManager);
        mListView.setRemoveListener(this);
    }

    /**
     * Initialize the footer.
     */
    private void setupFooter(LayoutInflater inflater) {
        mFooterView = inflater.inflate(layout.additional_exercise_footer, null, false);
        mFooterView.setOnClickListener(this);
        updateFooterTitle();
    }

    /**
     * Update the title of the footer.
     */
    private void updateFooterTitle() {
        String res = getResources().getString(R.string.add_additional_exercise);
        String text = MessageFormat.format(res, sAdditionalExercises.size());
        ((TextView) mFooterView.findViewById(R.id.textView)).setText(text);
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
