package se.johan.wendler.fragment;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;
import com.nhaarman.listviewanimations.itemmanipulation.OnDismissCallback;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.SwipeDismissAdapter;

import java.sql.SQLException;
import java.util.ArrayList;

import se.johan.wendler.R;
import se.johan.wendler.activity.MainActivity;
import se.johan.wendler.activity.WorkoutActivity;
import se.johan.wendler.adapter.OldWorkoutsAdapter;
import se.johan.wendler.fragment.base.DrawerFragment;
import se.johan.wendler.model.TapToUndoItem;
import se.johan.wendler.model.Workout;
import se.johan.wendler.sql.SqlHandler;
import se.johan.wendler.util.Constants;
import se.johan.wendler.util.PreferenceUtil;
import se.johan.wendler.util.WendlerizedLog;
import se.johan.wendler.view.UndoBarController;

/**
 * Fragment for displaying old workouts.
 */
public class DrawerOldWorkoutsFragment extends DrawerFragment implements
        AdapterView.OnItemClickListener,
        UndoBarController.UndoListener,
        OnDismissCallback,
        View.OnClickListener {

    public static final String TAG = DrawerOldWorkoutsFragment.class.getName();

    private static final String EXTRA_KEY_LIMIT = "keyLimit";

    private static final ArrayList<Workout> sWorkouts = new ArrayList<Workout>();

    private UndoBarController mUndoController;
    private SwipeDismissAdapter mAdapter;
    private int mLimit = 10;
    private View mFooterView;

    /**
     * Return the tag of the fragment.
     */
    @Override
    public String getFragmentTag() {
        return TAG;
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
        View view = inflater.inflate(R.layout.old_workouts_list, container, false);

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

        final ListView listView = (ListView) view.findViewById(R.id.list_drag);

        mAdapter = new SwipeDismissAdapter(new OldWorkoutsAdapter(getActivity(), sWorkouts), this);
        mAdapter.setAbsListView(listView);

        if (count > mLimit) {
            mFooterView = inflater.inflate(R.layout.load_more_footer, null);
            mFooterView.setOnClickListener(this);
            listView.addFooterView(mFooterView);
        }

        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(this);

        mUndoController = new UndoBarController(view.findViewById(R.id.undobar), this);

        listView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressWarnings({"ConstantConditions", "deprecation"})
            @Override
            public void onGlobalLayout() {
                listView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                showShowcaseView(listView);
            }


        });
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
     * Called when we undo a deletion of a workout.
     */
    @Override
    public void onUndo(Parcelable token) {
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
     * Called when we swipe away a workout in the list.
     */
    @Override
    public void onDismiss(AbsListView listView, int[] reverseSortedPositions) {
        onRemove(reverseSortedPositions[0]);
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
        mUndoController.onSaveInstanceState(outState);
    }

    /**
     * Called to display the informative ShowcaseView.
     */
    private void showShowcaseView(final ListView listView) {
        if (mAdapter.getCount() > 0 && PreferenceUtil.getBoolean(getActivity(),
                PreferenceUtil.KEY_HAS_SEEN_SHOWCASE_OLD_WORKOUTS)) {

            final View child = listView.getChildAt(listView.getLastVisiblePosition());

            if (child == null) {
                return;
            }

            final int[] pos = new int[2];
            child.getLocationOnScreen(pos);

            Target target = new Target() {
                @Override
                public Point getPoint() {
                    return new Point(
                            (pos[0] + child.getWidth()) / 2,
                            pos[1] + child.getHeight() / 2);
                }
            };

            ShowcaseView showcaseView = new ShowcaseView.Builder(getActivity(), true)
                    .setTarget(target)
                    .setContentTitle(R.string.showcase_old_workout_title)
                    .setContentText(R.string.showcase_old_workout_detail)
                    .setShowcaseEventListener(mShowcaseListener)
                    .setStyle(R.style.CustomShowcaseTheme)
                    .hideOnTouchOutside()
                    .build();
            showcaseView.show();
        }
    }

    /**
     * Called when we remove a workout.
     */
    private void onRemove(int which) {
        if (which >= sWorkouts.size()) {
            return;
        }

        Workout workout = sWorkouts.get(which);
        sWorkouts.remove(which);

        String text = String.format(getString(R.string.tap_to_undo), workout.getName());
        mUndoController.showUndoBar(true, text, new TapToUndoItem(workout, which));
        mAdapter.notifyDataSetChanged();

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

    /**
     * Listener for the ShowcaseView lifecycle.
     */
    private final OnShowcaseEventListener mShowcaseListener = new OnShowcaseEventListener() {
        @Override
        public void onShowcaseViewHide(ShowcaseView showcaseView) {
        }

        @Override
        public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
            if (isAdded()) {
                PreferenceUtil.putBoolean(getActivity(),
                        PreferenceUtil.KEY_HAS_SEEN_SHOWCASE_OLD_WORKOUTS, true);
            }
        }

        @Override
        public void onShowcaseViewShow(ShowcaseView showcaseView) {

        }
    };
}
