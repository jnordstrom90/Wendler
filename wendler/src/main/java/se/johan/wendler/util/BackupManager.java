package se.johan.wendler.util;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.channels.FileChannel;

import se.johan.wendler.sql.SqlHandler;

/**
 * Helper class for managing backup operations.
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class BackupManager {

    private static BackupManager sInstance;

    /**
     * Private constructor.
     */
    private BackupManager() {
    }

    /**
     * Return an instance of the BackupManager.
     */
    public static BackupManager getInstance() {
        if (sInstance == null) {
            sInstance = new BackupManager();
        }
        return sInstance;
    }

    /**
     * Create a local backup on the external storage.
     */
    public void requestLocalBackup(Context context, final IAsyncHandler handler) {
        new AsyncTask<Context, Void, Boolean>() {

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                handler.onResult(aBoolean, null);
            }

            @Override
            protected Boolean doInBackground(Context... params) {
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    File folder = new File(Environment.getExternalStorageDirectory()
                            + "/data/" + params[0].getPackageName());
                    if (!folder.exists()) {
                        folder.mkdirs();
                    }
                    return backupSettings(params[0], folder) && backupWorkouts(params[0], folder);
                } else {
                    return false;
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, context);
    }

    /**
     * Backup our settings.
     */
    private boolean backupSettings(Context context, File folder) {
        ObjectOutputStream stream = null;

        try {
            File file = new File(folder, Constants.SETTINGS_BACKUP_NAME);
            stream = new ObjectOutputStream(new FileOutputStream(file));
            stream.writeObject(
                    PreferenceManager.getDefaultSharedPreferences(context).getAll());
        } catch (IOException exception) {
            exception.printStackTrace();
            return false;
        } finally {
            if (stream != null) {
                try {
                    stream.flush();
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    /**
     * Backup the database.
     */
    private boolean backupWorkouts(Context context, File folder) {
        File file = context.getDatabasePath(SqlHandler.DATABASE_NAME);

        File newFile = new File(folder, Constants.WORKOUTS_BACKUP_NAME);
        try {
            if (!newFile.exists()) {
                newFile.createNewFile();
            }
            FileChannel src = new FileInputStream(file).getChannel();
            FileChannel dst = new FileOutputStream(newFile).getChannel();
            dst.transferFrom(src, 0, src.size());
            src.close();
            dst.close();
        } catch (IOException e) {
            WendlerizedLog.e("Error backing up workouts", e);
            return false;
        }

        return true;
    }
}
