package se.johan.wendler.activity;

import android.os.Bundle;

import se.johan.wendler.R;
import se.johan.wendler.activity.base.BaseActivity;
import se.johan.wendler.fragment.SettingsFragment;

/**
 * The activity for displaying our settings.
 */
public class SettingsActivity extends BaseActivity {

    /**
     * Called when the Activity is created.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_settings);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, SettingsFragment.newInstance(), SettingsFragment.TAG)
                .commit();
    }

    @Override
    protected int getNavigationResource() {
        return R.drawable.ic_arrow_back_black_24dp;
    }

    @Override
    protected String getToolbarTitle() {
        return getString(R.string.menu_settings);
    }

    @Override
    protected int getToolbarHelpMessage() {
        return 0;
    }
}
