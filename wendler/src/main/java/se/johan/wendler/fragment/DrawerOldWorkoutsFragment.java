package se.johan.wendler.fragment;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.AdapterView;

import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.SimpleFloatViewManager;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.listeners.ActionClickListener;

import java.sql.SQLException;
import java.util.ArrayList;

import se.johan.wendler.R;
import se.johan.wendler.activity.MainActivity;
import se.johan.wendler.activity.WorkoutActivity;
import se.johan.wendler.fragment.base.DrawerFragment;
import se.johan.wendler.model.TapToUndoItem;
import se.johan.wendler.model.Workout;
import se.johan.wendler.sql.SqlHandler;
import se.johan.wendler.ui.adapter.WorkoutListAdapter;
import se.johan.wendler.util.CardsOptionHandler;
import se.johan.wendler.util.Constants;
import se.johan.wendler.util.WendlerizedLog;

/**
 * Fragment for displaying old workouts.
 */
public class DrawerOldWorkoutsFragment extends DrawerFragment implements
        AdapterView.OnItemClickListener,
        View.OnClickListener,
        CardsOptionHandler, DragSortListView.RemoveListener {

    public static final String TAG = DrawerOldWorkoutsFragment.class.getName();

    private static final String EXTRA_KEY_LIMIT = "keyLimit";

    private static final ArrayList<Workout> sWorkouts = new ArrayList<Workout>();

    private int mLimit = 10;
    private View mFooterView;
    private WorkoutListAdapter mAdapter;
    private DragSortListView mDragSortListView;
    private View mNoItemsView;

    /**
     * Return the tag of the fragment.
     */
    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Override
    public int getMessageText() {
        return R.string.help_old_workouts;
    }

    @Override
    public boolean needsDefaultElevation() {
        return true;
    }

    public DrawerOldWorkoutsFragment() {
    }

    /**
     * Static creation to avoid problems on rotation.
     */
    public static DrawerOldWorkoutsFragment newInstance() {
        return new DrawerOldWorkoutsFragment();
    }

    /**
     * Called when the fragment is destroyed.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mUpdateReceiver);
    }

    /**
     * Called when the fragment is created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(mUpdateReceiver, new IntentFilter(MainActivity.ACTION_UPDATE));
    }

    /**
     * Called when the view is created.
     */
    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_old_workout, container, false);

        if (savedInstanceState != null) {
            mLimit = savedInstanceState.getInt(EXTRA_KEY_LIMIT, 10);
        }

        SqlHandler sqlHandler = new SqlHandler(getActivity());
        int count = 0;
        try {
            sqlHandler.open();
            count = sqlHandler.getOldWorkoutsCount();
            sWorkouts.clear();
            sWorkouts.addAll(sqlHandler.getOldWorkouts(mLimit));
        } catch (SQLException e) {
            WendlerizedLog.e("Failed to get old workouts", e);
        } finally {
            sqlHandler.close();
        }

        mDragSortListView = (DragSortListView) view.findViewById(R.id.list_drag);

        mAdapter = new WorkoutListAdapter(
                getActivity(),
                sWorkouts,
                WorkoutListAdapter.TYPE_OLD_WORKOUTS,
                this);

        if (count > mLimit) {
            mFooterView = inflater.inflate(R.layout.footer_load_more, null);
            mFooterView.setOnClickListener(this);
            mDragSortListView.addFooterView(mFooterView);
        }

        mDragSortListView.setAdapter(mAdapter);
        mDragSortListView.setOnItemClickListener(this);
        buildController();

        mNoItemsView = view.findViewById(R.id.no_items_view);

        setVisibilityOfViews(sWorkouts.isEmpty());

        return view;
    }

    /**
     * Called when an item in the list is clicked.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), WorkoutActivity.class);
        intent.putExtra(Constants.BUNDLE_EXERCISE_ITEM, sWorkouts.get(position));
        startActivityForResult(intent, MainActivity.REQUEST_WORKOUT_RESULT);
    }

    /**
     * Called when we click on the footer.
     */
    @Override
    public void onClick(View v) {
        SqlHandler sqlHandler = new SqlHandler(getActivity());
        try {
            sqlHandler.open();

            mLimit += 10;
            int count = sqlHandler.getOldWorkoutsCount();

            sWorkouts.clear();
            sWorkouts.addAll(sqlHandler.getOldWorkouts(mLimit));

            mAdapter.notifyDataSetChanged();

            int visibility = count > mLimit ? View.VISIBLE : View.GONE;
            mFooterView.setVisibility(visibility);

        } catch (SQLException e) {
            WendlerizedLog.e("Failed to get more old workouts", e);
        } finally {
            sqlHandler.close();
        }
    }

    /**
     * Called to save the instance state.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_KEY_LIMIT, mLimit);
    }

    /**
     * Called when a workout is deleted via the overflow menu.
     */
    @Override
    public void onDelete(int position) {
        if (position >= sWorkouts.size()) {
            return;
        }

        Workout workout = sWorkouts.get(position);
        sWorkouts.remove(position);

        createSnackBar(workout, new TapToUndoItem(workout, position));
        mAdapter.notifyDataSetChanged();
        setVisibilityOfViews(sWorkouts.isEmpty());
        SqlHandler sql = new SqlHandler(getActivity());
        try {
            sql.open();
            sql.deleteWorkout(workout);
        } catch (SQLException e) {
            WendlerizedLog.e("Failed to remove old workout", e);
        } finally {
            sql.close();
        }
    }

    /**
     * Called when a workout is deleted via a swipe.
     */
    @Override
    public void remove(int which) {
        onDelete(which);
    }
    /**
     * Called when a workout is edited via the overflow menu.
     */
    @Override
    public void onEdit(int position) {
        // Not used here
    }

    /**
     * Create a snack bar where the user can undo the deletion.
     */
    private void createSnackBar(Workout workout, TapToUndoItem item) {
        Snackbar.with(getActivity())
                .text(getSnackBarText(workout))
                .actionLabel(getString(R.string.undo))
                .actionListener(getActionListener(item))
                .show(getActivity());
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
    private ActionClickListener getActionListener(final TapToUndoItem item) {
        return new ActionClickListener() {
            @Override
            public void onActionClicked(Snackbar snackbar) {
                onUndo(item);
            }
        };
    }
    /**
     * Returns the text for the snack bar.
     */
    private String getSnackBarText(Workout workout) {
        return String.format(getString(R.string.snack_bar_deleted), workout.getName());
    }

    /**
     * Called when we undo a deletion of a workout.
     */
    private void onUndo(Parcelable token) {
        TapToUndoItem item = (TapToUndoItem) token;
        Workout workout = (Workout) item.getObject();

        sWorkouts.add(item.getPosition(), workout);
        mAdapter.notifyDataSetChanged();

        SqlHandler sql = new SqlHandler(getActivity());
        try {
            sql.open();
            sql.storeWorkout(workout);
        } catch (SQLException e) {
            WendlerizedLog.e("Failed to undo delete of old workout", e);
        } finally {
            sql.close();
        }
    }

    /**
     * Build the controller for the DragSortListView.
     */
    private void buildController() {
        SimpleFloatViewManager
                simpleFloatViewManager = new SimpleFloatViewManager(mDragSortListView);
        simpleFloatViewManager.setBackgroundColor(Color.TRANSPARENT);
        mDragSortListView.setFloatViewManager(simpleFloatViewManager);
        mDragSortListView.setRemoveListener(this);
    }

    /**
     * Set the visibility of certain items.
     */
    private void setVisibilityOfViews(boolean emptyList) {
        mDragSortListView.setVisibility(emptyList ? View.GONE : View.VISIBLE);
        mNoItemsView.setVisibility(emptyList ? View.VISIBLE : View.GONE);
    }

    /**
     * Receiver for listening for updates on when a workout was updated.
     */
    private final BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            SqlHandler sqlHandler = new SqlHandler(getActivity());
            try {
                sqlHandler.open();
                sWorkouts.clear();
                sWorkouts.addAll(sqlHandler.getOldWorkouts(mLimit));
                mAdapter.notifyDataSetChanged();
            } catch (SQLException e) {
                WendlerizedLog.e("Failed to add old workouts", e);
            } finally {
                sqlHandler.close();
            }
        }
    };
}
