package se.johan.wendler.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dd.CircularProgressButton;

import java.text.SimpleDateFormat;
import java.util.Date;

import se.johan.wendler.R;
import se.johan.wendler.fragment.base.DrawerFragment;
import se.johan.wendler.util.BackupManager;
import se.johan.wendler.util.IAsyncHandler;
import se.johan.wendler.util.PreferenceUtil;
import se.johan.wendler.util.RestoreManager;

/**
 * Backup fragment
 */
public class DrawerBackupManagerFragment extends DrawerFragment {

    public static final String TAG = DrawerBackupManagerFragment.class.getName();

    private static final int STATE_SUCCESS = 100;
    private static final int STATE_FAILURE = -1;

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("d LLLL yyyy");


    private CircularProgressButton mBtnBackup;
    private CircularProgressButton mBtnRestore;
    private TextView mLastBackupText;

    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Override
    public int getMessageText() {
        return R.string.help_backup_and_restore;
    }

    public DrawerBackupManagerFragment() {
    }

    /**
     * Static creation to avoid problems on rotation.
     */
    public static DrawerBackupManagerFragment newInstance() {
        return new DrawerBackupManagerFragment();
    }

    /**
     * Called when the view is created.
     */
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_backup_restore, container, false);
        mBtnBackup = (CircularProgressButton) view.findViewById(R.id.btnBackup);
        mBtnBackup.setOnClickListener(mBackupClickListener);
        mBtnRestore = (CircularProgressButton) view.findViewById(R.id.btnRestore);
        mBtnBackup.setIndeterminateProgressMode(true);
        mBtnRestore.setIndeterminateProgressMode(true);
        mBtnRestore.setOnClickListener(mRestoreClickListener);

        mLastBackupText = (TextView) view.findViewById(R.id.lastBackup);
        updateLastBackupText(
                PreferenceUtil.getLong(getActivity(), PreferenceUtil.KEY_TIME_OF_LAST_BACKUP));

        return view;
    }

    /**
     * Create the TextView displaying the last successful backup.
     */
    private void updateLastBackupText(long lastBackup) {
        if (lastBackup > 0) {
            String time = FORMAT.format(new Date(lastBackup));
            String text = String.format(getString(R.string.last_successful_backup), time);
            mLastBackupText.setText(text);
            mLastBackupText.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Listener for clicks on the backup button.
     */
    private final View.OnClickListener mBackupClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            BackupManager.getInstance().requestLocalBackup(getActivity(), new IAsyncHandler() {
                @Override
                public void onResult(Object key, Object value) {
                    int progress = (Boolean) key ? STATE_SUCCESS : STATE_FAILURE;
                    mBtnBackup.setProgress(progress);

                    if ((Boolean) key) {
                        long time = System.currentTimeMillis();
                        PreferenceUtil.putLong(
                                getActivity(),
                                PreferenceUtil.KEY_TIME_OF_LAST_BACKUP,
                                time);
                        updateLastBackupText(time);
                    }
                }
            });
        }
    };

    /**
     * Listener for clicks on the restore button.
     */
    private final View.OnClickListener mRestoreClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            RestoreManager.getInstance().requestLocalRestore(getActivity(), new IAsyncHandler() {
                @Override
                public void onResult(Object key, Object value) {
                    int progress = (Boolean) key ? STATE_SUCCESS : STATE_FAILURE;
                    mBtnRestore.setProgress(progress);
                }
            });
        }
    };

}
