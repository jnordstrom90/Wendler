package se.johan.wendler.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortListView.DropListener;
import com.mobeta.android.dslv.SimpleFloatViewManager;

import java.util.ArrayList;
import java.util.Arrays;

import se.johan.wendler.R;
import se.johan.wendler.R.array;
import se.johan.wendler.R.layout;
import se.johan.wendler.sql.SqlHandler;
import se.johan.wendler.fragment.base.InitFragment;

/**
 * Set the order for the exercises.
 * TODO Consolidate with EditOrderFragment
 */
public class InitOrderFragment extends InitFragment implements DropListener {

    public static final String TAG = InitOrderFragment.class.getName();

    private static final String EXTRA_EXERCISES = "exercises";
    private DragSortListView mListView;
    private ArrayList<String> mListOfExercises;
    private ArrayAdapter<String> mAdapter;
    private String[] mArrayOfExercises;

    public InitOrderFragment() {
    }

    /**
     * Static creation to avoid problems on rotation.
     */
    public static InitOrderFragment newInstance() {
        return new InitOrderFragment();
    }

    /**
     * Called when the activity has been created.
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setListAdapter(savedInstanceState);
        buildController();
    }

    /**
     * Called when the view is created.
     */
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_init_order, container, false);

        mListView = (DragSortListView) view.findViewById(R.id.list_workout_order);
        mListView.setDropListener(this);
        mListView.setDragEnabled(true);

        return view;
    }

    /**
     * Return if all data is ok to be saved.
     */
    @Override
    public boolean allDataIsOk() {
        return true;
    }

    /**
     * Return the helping message for this view.
     */
    @Override
    public String getHelpingMessage() {
        return getString(R.string.help_order_dialog);
    }

    /**
     * Return the helping message of the view.
     */
    @Override
    public int getHelpingMessageRes() {
        return R.string.help_order_dialog;
    }

    /**
     * Notify the user of any error.
     */
    @Override
    public void notifyError() {
        // Not used here, changes are saved automatically.
    }

    /**
     * Called when an item is reordered in the list.
     */
    @Override
    public void drop(int from, int to) {
        String item = mAdapter.getItem(from);
        mAdapter.remove(item);
        mAdapter.insert(item, to);
    }

    /**
     * Called when the fragment needs to save its' state.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(EXTRA_EXERCISES, mListOfExercises);
    }

    /**
     * Save the data for the view.
     */
    @Override
    public void saveData(SqlHandler handler) {

        int pressDay = mListOfExercises.indexOf(mArrayOfExercises[0]);
        int deadliftDay = mListOfExercises.indexOf(mArrayOfExercises[1]);
        int benchDay = mListOfExercises.indexOf(mArrayOfExercises[2]);
        int squatDay = mListOfExercises.indexOf(mArrayOfExercises[3]);

        handler.insertExerciseOrder(pressDay, deadliftDay, benchDay, squatDay);
    }

    /**
     * Create and set the ListAdapter for the ListView
     */
    private void setListAdapter(Bundle savedInstanceState) {
        mArrayOfExercises = getResources().getStringArray(array.exercises);

        if (savedInstanceState == null) {
            mListOfExercises = new ArrayList<String>(Arrays.asList(mArrayOfExercises));
        } else {
            mListOfExercises = savedInstanceState.getStringArrayList(EXTRA_EXERCISES);
        }

        mAdapter = new ArrayAdapter<String>(
                getActivity(), layout.item_dslv, R.id.text, mListOfExercises);
        mListView.setAdapter(mAdapter);
    }

    /**
     * Create the controller for the ListView, can be further customized if needed
     */
    private void buildController() {
        DragSortController controller = new DragSortController(mListView);
        controller.setDragHandleId(R.id.drag_handle);
        SimpleFloatViewManager simpleFloatViewManager = new SimpleFloatViewManager(mListView);
        simpleFloatViewManager.setBackgroundColor(Color.TRANSPARENT);
        mListView.setFloatViewManager(simpleFloatViewManager);
    }
}
