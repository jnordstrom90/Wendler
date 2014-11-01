package se.johan.wendler.ui.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import se.johan.wendler.R;
import se.johan.wendler.ui.dialog.base.AnimationDialog;
import se.johan.wendler.util.PreferenceUtil;
import se.johan.wendler.ui.view.CustomChronometer;

/**
 * Dialog with a running timer.
 * TODO SEEMS TO BEHAVE STRANGE SOMETIMES
 * TODO NOTIFICATION
 */
public class StopwatchDialog extends AnimationDialog implements View.OnClickListener {

    private static final String EXTRA_IS_RUNNING = "isRunning";
    private static final String EXTRA_TITLE = "title";
    private static final String EXTRA_TIME_ELAPSED = "onStopwatchDismissed";

    public static final String TAG = StopwatchDialog.class.getName();

    private CustomChronometer mChronometer;
    private Button mBtnStartStop;

    private long mTimeElapsed = -1;
    private boolean mWasRunning = false;

    public StopwatchDialog() {
    }

    /**
     * Static creation to avoid problems on rotation.
     */
    public static StopwatchDialog newInstance(String title, long timeElapsed, boolean isRunning) {
        StopwatchDialog dialog = new StopwatchDialog();
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_TITLE, title);
        bundle.putLong(EXTRA_TIME_ELAPSED, timeElapsed);
        bundle.putBoolean(EXTRA_IS_RUNNING, isRunning);
        dialog.setArguments(bundle);
        return dialog;
    }

    /**
     * Called prior to the dialog being created.
     */
    @NonNull
    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
       LayoutInflater inflater = LayoutInflater.from(getActivity());
       View view = inflater.inflate(R.layout.dialog_stopwatch, null);

        mChronometer = (CustomChronometer) view.findViewById(R.id.chronometer);

        Button mBtnReset = (Button) view.findViewById(R.id.btn_reset);
        mBtnStartStop = (Button) view.findViewById(R.id.btn_start_stop);
        mBtnStartStop.setOnClickListener(this);
        mBtnReset.setOnClickListener(this);

        if (savedInstanceState != null) {
            mWasRunning = savedInstanceState.getBoolean(EXTRA_IS_RUNNING, false);
            mTimeElapsed = savedInstanceState.getLong(EXTRA_TIME_ELAPSED, -1);
        }
        ((TextView) view.findViewById(R.id.title)).setText(getArguments().getString(EXTRA_TITLE));
        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .create();
    }

    /**
     * Called to save the current instance state.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(EXTRA_IS_RUNNING, mChronometer.isRunning());
        if (mChronometer.isRunning()) {
            mTimeElapsed = SystemClock.elapsedRealtime() - mChronometer.getBase();
            outState.putLong(EXTRA_TIME_ELAPSED, mTimeElapsed);
        }
    }

    /**
     * Called when the dialog is started.
     */
    @Override
    public void onStart() {
        super.onStart();
        if (mTimeElapsed != -1) {
            mChronometer.setBase(SystemClock.elapsedRealtime() - mTimeElapsed);
        } else if (getArguments().getLong(EXTRA_TIME_ELAPSED, -1) != -1) {
            mTimeElapsed = getArguments().getLong(EXTRA_TIME_ELAPSED);
            mChronometer.setBase(SystemClock.elapsedRealtime() - mTimeElapsed);
        }

        if (mWasRunning || getArguments().getBoolean(EXTRA_IS_RUNNING, false)) {
            onStartStop();
        }

        if (PreferenceUtil.getBoolean(getActivity(), PreferenceUtil.KEY_USE_VOLUME_BUTTONS, true)) {
            getDialog().setOnKeyListener(mOnKeyListener);
        }
    }

    /**
     * Called when a button is clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_reset:
                onReset();
                break;
            case R.id.btn_start_stop:
                onStartStop();
                break;
        }
    }

    /**
     * Called when the dialog is attached to an activity.
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (getStopWatchListener() == null) {
            throw new ClassCastException("Class doesn't implement StopWatchListener");
        }
    }

    /**
     * Called when the dialog is canceled.
     */
    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        disableScreenOn();
        if (mChronometer.isRunning()) {
            mTimeElapsed = SystemClock.elapsedRealtime() - mChronometer.getBase();
        }
        getStopWatchListener().onStopwatchDismissed(mTimeElapsed, mChronometer.isRunning());
    }

    /**
     * Called when to either start or stop the chronometer
     */
    private void onStartStop() {
        if (mChronometer.isRunning()) {
            mChronometer.stop();
            mBtnStartStop.setText(getString(R.string.btn_start));
            disableScreenOn();
            mTimeElapsed = SystemClock.elapsedRealtime() - mChronometer.getBase();
        } else {
            keepScreenOn();
            if (mTimeElapsed == -1) {
                mChronometer.setBase(SystemClock.elapsedRealtime());
            }
            mChronometer.start();
            mBtnStartStop.setText(getString(R.string.btn_stop));
            mTimeElapsed = -1;
        }
    }

    /**
     * Called to reset the chronometer.
     */
    private void onReset() {
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mTimeElapsed = -1;
    }

    /**
     * Enable keep screen on if wanted.
     */
    private void keepScreenOn() {
        if (PreferenceUtil.getBoolean(
                getActivity(), PreferenceUtil.KEY_KEEP_SCREEN_ON_STOPWATCH, true)) {
            getActivity().getWindow().addFlags(
                    android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    /**
     * Disable screen on if needed.
     */
    private void disableScreenOn() {
        if (PreferenceUtil.getBoolean(
                getActivity(), PreferenceUtil.KEY_KEEP_SCREEN_ON_STOPWATCH, true)) {
            getActivity().getWindow().clearFlags(
                    android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    /**
     * Return the listener for the stopwatch.
     */
    private StopWatchListener getStopWatchListener() {
        if (getActivity() instanceof StopWatchListener) {
            return (StopWatchListener) getActivity();
        } else if (getTargetFragment() instanceof StopWatchListener) {
            return (StopWatchListener) getTargetFragment();
        }
        return null;
    }

    /**
     * Key listener for volume key implementation.
     */
    private final DialogInterface.OnKeyListener
            mOnKeyListener = new DialogInterface.OnKeyListener() {
        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_VOLUME_DOWN:
                        onReset();
                        return true;

                    case KeyEvent.KEYCODE_VOLUME_UP:
                        onStartStop();
                        return true;
                }
            }
            return false;
        }
    };

    /**
     * Interface used for callbacks from the stopwatch.
     */
    public interface StopWatchListener {

        /**
         * Called when the dialog is dismissed.
         */
        public void onStopwatchDismissed(long timeElapsed, boolean isRunning);
    }
}
