package se.johan.wendler.activity;

import android.os.Bundle;

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
        super.onCreate(savedInstanceState);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, AboutFragment.newInstance(), AboutFragment.TAG)
                .commit();
    }
}
