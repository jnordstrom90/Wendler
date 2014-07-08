package se.johan.wendler.activity.base;

import android.support.v4.app.FragmentActivity;

import se.johan.wendler.util.AnalyticsHelper;

/**
 * Custom Activity which has animations and Google Analytics support.
 */
public abstract class BaseActivity extends FragmentActivity {

    /**
     * Called when the activity is paused.
     */
    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /**
     * Called when the activity is started.
     */
    @Override
    protected void onStart() {
        super.onStart();
        AnalyticsHelper.getInstance(this).setLoggingEnabled(this);
    }

    /**
     * Called when the activity is stopped.
     */
    @Override
    protected void onStop() {
        super.onStop();
        AnalyticsHelper.getInstance(this).setLoggingDisabled(this);
    }
}
