package se.johan.wendler.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.SimpleFloatViewManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import se.johan.wendler.R;
import se.johan.wendler.fragment.base.EditFragment;
import se.johan.wendler.sql.SqlHandler;
import se.johan.wendler.util.WendlerizedLog;

/**
 * Editable fragment where the order can be changed.
 */
public class EditOrderFragment extends EditFragment implements DragSortListView.DropListener {

    private static final String EXTRA_EXERCISES = "exercises";
    private DragSortListView mListView;
    private ArrayList<String> mListOfExercises;
    private ArrayAdapter<String> mAdapter;
    private String[] mArrayOfExercises;

    public EditOrderFragment() {
    }

    /**
     * Static creation to avoid problems on rotation.
     */
    public static EditOrderFragment newInstance() {
        return new EditOrderFragment();
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
     * Called when we reorder an item in the list.
     */
    @Override
    public void drop(int from, int to) {

        String item = mAdapter.getItem(from);
        mAdapter.remove(item);
        mAdapter.insert(item, to);

        int pressDay = mListOfExercises.indexOf(mArrayOfExercises[0]);
        int deadliftDay = mListOfExercises.indexOf(mArrayOfExercises[1]);
        int benchDay = mListOfExercises.indexOf(mArrayOfExercises[2]);
        int squatDay = mListOfExercises.indexOf(mArrayOfExercises[3]);

        SqlHandler mHandler = new SqlHandler(getActivity());

        try {
            mHandler.open();
            mHandler.insertExerciseOrder(pressDay, deadliftDay, benchDay, squatDay);
        } catch (SQLException e) {
           WendlerizedLog.e("Failed to insert order" , e);
        } finally {
            mHandler.close();
        }
    }

    /**
     * Called when we need to save the instance state.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(EXTRA_EXERCISES, mListOfExercises);
    }

    /**
     * Called when we need to save the data.
     */
    @Override
    public void saveData(SqlHandler handler) {
        // Not used here, data saved dynamically.
    }


    /**
     * Return the list of exercises in order.
     */
    private ArrayList<String> getListInOrder() {
        SqlHandler sqlHandler = new SqlHandler(getActivity());
        try {
            sqlHandler.open();
            return new ArrayList<String>(Arrays.asList(sqlHandler.getExerciseNamesInOrder()));
        } catch (SQLException e) {
            WendlerizedLog.e("Failed to get exercise names in order ", e);
        } finally {
            sqlHandler.close();
        }
        return new ArrayList<String>(Arrays.asList(mArrayOfExercises));
    }

    /**
     * Create and set the adapter for the ListView.
     */
    private void setListAdapter(Bundle savedInstanceState) {
        mArrayOfExercises = getResources().getStringArray(R.array.exercises);

        if (savedInstanceState == null) {
            mListOfExercises = getListInOrder();
        } else {
            mListOfExercises = savedInstanceState.getStringArrayList(EXTRA_EXERCISES);
        }

        mAdapter = new ArrayAdapter<String>(
                getActivity(), R.layout.item_dslv, R.id.text, mListOfExercises);
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
