package se.johan.wendler.activity.base;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import se.johan.wendler.R;
import se.johan.wendler.util.AnalyticsHelper;
import se.johan.wendler.util.Utils;

/**
 * Custom Activity which has animations and Google Analytics support.
 */
public abstract class BaseActivity extends ActionBarActivity {

    /**
     * Called when the activity is created.
     */
    protected void onCreate(Bundle savedInstanceState, int layoutResource) {
        super.onCreate(savedInstanceState);
        setContentView(layoutResource);
        initToolbar();
    }

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

    /**
     * Update the title of the Toolbar.
     */
    protected void updateTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    /**
     * Update the subtitle of the Toolbar.
     */
    protected void updateHelpMessage(int messageRes) {
        if (messageRes == 0) {
            return;
        }
        ((TextView) findViewById(R.id.help_message_text)).setText(messageRes);
    }

    /**
     * Update the subtitle of the Toolbar.
     */
    protected void updateHelpMessage(String message) {
        ((TextView) findViewById(R.id.help_message_text)).setText(message);
    }

    /**
     * Return the navigation resource to be used by the child activity.
     */
    protected abstract int getNavigationResource();

    /**
     * Return the title to be used by the child activity.
     */
    protected abstract String getToolbarTitle();

    /**
     * Return the subtitle to be used by the child activity.
     */
    protected abstract int getToolbarHelpMessage();

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void overrideElevation(float elevation) {
        if (Utils.hasLollipop()) {
            findViewById(R.id.tool_bar).setElevation(elevation);
            findViewById(R.id.help_message_text).setElevation(elevation);
        }
    }

    /**
     * Initialize the Toolbar.
     */
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);

        if (getNavigationResource() != 0) {
            toolbar.setNavigationIcon(getNavigationResource());
        }

        toolbar.setTitle(getToolbarTitle());

        updateHelpMessage(getToolbarHelpMessage());

        setSupportActionBar(toolbar);
    }
}
