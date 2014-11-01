package se.johan.wendler.activity;

import android.os.Bundle;

import se.johan.wendler.R;
import se.johan.wendler.activity.base.BaseActivity;
import se.johan.wendler.fragment.AboutFragment;

/**
 * Activity for displaying the About-view
 */
public class AboutActivity extends BaseActivity {

    /**
     * Called when the activity is created.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_about);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, AboutFragment.newInstance(), AboutFragment.TAG)
                .commit();
    }

    @Override
    protected int getNavigationResource() {
        return R.drawable.ic_arrow_back_black_24dp;
    }

    @Override
    protected String getToolbarTitle() {
        return getString(R.string.title_about);
    }

    @Override
    protected int getToolbarHelpMessage() {
        return 0;
    }
}
