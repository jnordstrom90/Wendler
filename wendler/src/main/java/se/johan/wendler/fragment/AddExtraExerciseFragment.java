package se.johan.wendler.fragment;


import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
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
import se.johan.wendler.adapter.AdditionalExerciseAdapter;
import se.johan.wendler.dialog.AdditionalExerciseDialog;
import se.johan.wendler.model.AdditionalExercise;
import se.johan.wendler.model.TapToUndoItem;
import se.johan.wendler.model.Workout;
import se.johan.wendler.sql.SqlHandler;
import se.johan.wendler.util.CardsOptionHandler;
import se.johan.wendler.util.StringHelper;
import se.johan.wendler.util.WendlerizedLog;
import se.johan.wendler.view.UndoBarController;

/**
 * Fragments which enables adding additional mExercises to workouts.
 * TODO MAKE THIS SAME AS WORKOUT ADDITIONAL
 */
public class AddExtraExerciseFragment extends Fragment implements
        UndoBarController.UndoListener,
        DragSortListView.DropListener,
        View.OnClickListener,
        DragSortListView.RemoveListener,
        AdditionalExerciseDialog.onConfirmClickedListener {

    private static final String EXTRA_KEY_NAME = "keyName";

    private ArrayList<AdditionalExercise> mExercises;

    private UndoBarController mUndoController;
    private AdditionalExerciseAdapter mAdapter;
    private View mFooterView;

    public AddExtraExerciseFragment() {
    }

    /**
     * Static creation to avoid problems on rotation.
     */
    public static AddExtraExerciseFragment newInstance(String name) {

        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_KEY_NAME, name);
        AddExtraExerciseFragment fragment = new AddExtraExerciseFragment();
        fragment.setArguments(bundle);
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

        SqlHandler sqlHandler = new SqlHandler(getActivity());
        try {
            sqlHandler.open();
            Workout workout = new Workout(
                    getArguments().getString(EXTRA_KEY_NAME),
                    StringHelper.getTranslatableName(getActivity(),
                            getArguments().getString(EXTRA_KEY_NAME))
            );

            mExercises = sqlHandler.getExtraExerciseForWorkout(workout);
        } catch (SQLException e) {
            WendlerizedLog.e("Failed to get extra mExercises for " +
                    getArguments().getString(EXTRA_KEY_NAME), e);
        } finally {
            sqlHandler.close();
        }

        mAdapter = new AdditionalExerciseAdapter(getActivity(), mExercises, mOptionsHandler, false);

        createFooterView(inflater);

        DragSortListView dragSortListView = (DragSortListView) view.findViewById(R.id.list_drag);
        dragSortListView.addFooterView(mFooterView);
        dragSortListView.setAdapter(mAdapter);
        dragSortListView.setDragEnabled(true);
        dragSortListView.setDropListener(this);
        buildController(dragSortListView);

        mUndoController = new UndoBarController(view.findViewById(R.id.undobar), this);

        return view;
    }

    /**
     * Called when the view needs to save it's state.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mUndoController.onSaveInstanceState(outState);
    }

    /**
     * Called when we click on the footer.
     */
    @Override
    public void onClick(View v) {
        launchAddDialog(null, getNextExtraExerciseId());
    }

    /**
     * Called to undo a deletion of an exercise.
     */
    @Override
    public void onUndo(Parcelable token) {
        TapToUndoItem item = (TapToUndoItem) token;
        AdditionalExercise exercise = (AdditionalExercise) item.getObject();
        mExercises.add(item.getPosition(), exercise);
        mAdapter.notifyDataSetChanged();
        updateFooterTitle();

        SqlHandler sqlHandler = new SqlHandler(getActivity());
        try {
            sqlHandler.open();
            sqlHandler.storeAdditionalExercise(
                    exercise,
                    getArguments().getString(EXTRA_KEY_NAME),
                    true,
                    getPosForAdditionalExercise(exercise.getExerciseId()));
        } catch (SQLException e) {
            WendlerizedLog.e("Failed to store exercise after undo", e);
        } finally {
            sqlHandler.close();
        }
    }

    /**
     * Called when the ListView is reordered.
     */
    @Override
    public void drop(int from, int to) {
        AdditionalExercise exercise = mExercises.get(from);
        mExercises.remove(exercise);
        mExercises.add(to, exercise);
        SqlHandler sql = new SqlHandler(getActivity());
        try {
            sql.open();
            sql.doReorderAdditionalExercise(mExercises, getArguments().getString(EXTRA_KEY_NAME));
        } catch (SQLException e) {
            WendlerizedLog.e("Failed to reorder exercises after drop", e);
        } finally {
            sql.close();
        }
        mAdapter.notifyDataSetChanged();
    }

    /**
     * Called when an exercise is removed.
     */
    @Override
    public void remove(int which) {
        onRemove(which);
    }

    /**
     * Called when we confirm adding an additional exercise.
     */
    @Override
    public void onConfirmClicked(AdditionalExercise exercise) {

        SqlHandler sqlHandler = new SqlHandler(getActivity());

        try {
            sqlHandler.open();
            int pos = getPosForAdditionalExercise(exercise.getExerciseId());
            boolean isNew = sqlHandler.extraExerciseIsNew(
                    getArguments().getString(EXTRA_KEY_NAME), exercise.getExerciseId())
                    && pos == -1;

            if (isNew) {
                mExercises.add(exercise);
            } else if (pos > -1) {
                mExercises.set(pos, exercise);
            }

            sqlHandler.storeAdditionalExercise(
                    exercise,
                    getArguments().getString(EXTRA_KEY_NAME),
                    isNew,
                    pos == -1 ? mExercises.size() - 1 : pos);
        } catch (SQLException e) {
            WendlerizedLog.e("Failed to update additional exercise", e);
        } finally {
            sqlHandler.close();
        }

        mAdapter.notifyDataSetChanged();
        updateFooterTitle();
    }

    /**
     * Build the controller for the ListView.
     */
    private void buildController(DragSortListView dragSortListView) {
        DragSortController controller = new DragSortController(dragSortListView);
        controller.setDragHandleId(R.id.drag_handle);
        SimpleFloatViewManager simpleFloatViewManager = new SimpleFloatViewManager(dragSortListView);
        simpleFloatViewManager.setBackgroundColor(Color.TRANSPARENT);
        dragSortListView.setFloatViewManager(simpleFloatViewManager);
        dragSortListView.setRemoveListener(this);
    }

    /**
     * Create the footer for the view.
     */
    @SuppressLint("InflateParams")
    private void createFooterView(LayoutInflater inflater) {
        mFooterView = inflater.inflate(R.layout.additional_exercise_footer, null, false);
        mFooterView.setOnClickListener(this);
        updateFooterTitle();
    }

    /**
     * Update the title of the footer.
     */
    private void updateFooterTitle() {
        String res = getResources().getString(R.string.add_additional_exercise);
        String text = MessageFormat.format(res, mExercises.size());
        ((TextView) mFooterView.findViewById(R.id.textView)).setText(text);
    }

    /**
     * Called to launch the adding additional exercise dialog.
     */
    private void launchAddDialog(AdditionalExercise exercise, int id) {
        AdditionalExerciseDialog.newInstance(
                getString(R.string.add_exercise),
                exercise,
                id,
                this).show(getActivity().getSupportFragmentManager().beginTransaction(),
                AdditionalExerciseDialog.TAG);

    }

    /**
     * Return the next available additional exercise id.
     */
    private int getNextExtraExerciseId() {
        int id = 0;
        for (AdditionalExercise exercise : mExercises) {
            if (exercise.getExerciseId() > id) {
                id = exercise.getExerciseId();
            }
        }
        return ++id;
    }

    /**
     * Called to remove an additional exercise.
     */
    private void onRemove(int which) {
        AdditionalExercise exercise = mExercises.get(which);
        mExercises.remove(which);

        String text = String.format(getString(R.string.tap_to_undo), exercise.getName());
        mUndoController.showUndoBar(true, text, new TapToUndoItem(exercise, which));
        mAdapter.notifyDataSetChanged();
        updateFooterTitle();

        SqlHandler sqlHandler = new SqlHandler(getActivity());
        try {
            sqlHandler.open();
            sqlHandler.deleteAdditionalExercise(getArguments().getString(EXTRA_KEY_NAME), exercise);
        } catch (SQLException e) {
            WendlerizedLog.e("Failed to remove additional exercise", e);
        } finally {
            sqlHandler.close();
        }
    }

    /**
     * Return the position of an additional exercise. If it doesn't exist return -1.
     */
    private int getPosForAdditionalExercise(int id) {
        for (int i = 0; i < mExercises.size(); i++) {
            if (mExercises.get(i).getExerciseId() == id) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Handler for clicks on the items on additional exercise cards.
     */
    private final CardsOptionHandler mOptionsHandler = new CardsOptionHandler() {
        @Override
        public void onDelete(int position) {
            onRemove(position);
        }

        @Override
        public void onEdit(int position) {
            AdditionalExercise exercise = mExercises.get(position);
            launchAddDialog(exercise, exercise.getExerciseId());
        }
    };


}
