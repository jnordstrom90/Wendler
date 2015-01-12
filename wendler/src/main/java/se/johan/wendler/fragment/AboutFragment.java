package se.johan.wendler.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import de.psdev.licensesdialog.LicensesDialogFragment;
import se.johan.wendler.R;
import se.johan.wendler.model.ListItem;
import se.johan.wendler.model.ListItemType;
import se.johan.wendler.ui.adapter.AboutAdapter;
import se.johan.wendler.ui.dialog.ChangelogDialog;
import se.johan.wendler.util.Utils;

/**
 * About fragment which displays helpful information.
 */
public class AboutFragment extends Fragment implements AdapterView.OnItemClickListener {

    public static final String TAG = AboutFragment.class.getName();

    public AboutFragment() {
    }

    /**
     * Static creation to avoid problems on rotation.
     */
    public static AboutFragment newInstance() {
        return new AboutFragment();
    }

    /**
     * Called when the view is created.
     */
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ListView view = (ListView) inflater.inflate(R.layout.list_empty, container, false);

        view.setAdapter(new AboutAdapter(getActivity(), generateListItems()));
        view.setOnItemClickListener(this);
        return view;
    }

    /**
     * Create the Toolbar.
     */
    private void initToolbar(View view) {
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.tool_bar);
        toolbar.setTitle(R.string.title_about);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        ((ActionBarActivity) getActivity()).setSupportActionBar(toolbar);
    }

    /**
     * Called when a list item is clicked.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                ChangelogDialog.newInstance()
                        .show(getActivity().getSupportFragmentManager(), ChangelogDialog.TAG);
                break;
            case 1:
                Intent sendIntent = new Intent(
                        Intent.ACTION_SENDTO,
                        Uri.fromParts("mailto",
                                "lolbolljohan@gmail.com",
                                null)
                );

                sendIntent.putExtra(
                        Intent.EXTRA_SUBJECT,
                        getString(R.string.wendlerized) + " " +
                                Utils.getCurrentAppVersion(getActivity()) + " feedback"
                );
                startActivity(
                        Intent.createChooser(sendIntent, getString(R.string.send_email_title)));
                break;
            case 2:
                LicensesDialogFragment.newInstance(R.raw.notices, false)
                        .show(getActivity().getSupportFragmentManager(), null);
                break;
        }
    }

    /**
     * Generate a list of items to be displayed in the list.
     */
    private ArrayList<ListItem> generateListItems() {
        ArrayList<ListItem> items = new ArrayList<ListItem>();
        for (ListItem item : ListItem.values()) {
            if (item.getItemType().equals(ListItemType.ABOUT)) {
                items.add(item);
            }
        }
        return items;
    }
}
