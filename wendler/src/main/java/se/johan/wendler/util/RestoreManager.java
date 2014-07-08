package se.johan.wendler.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.SQLException;
import java.util.Map;

import se.johan.wendler.sql.SqlHandler;

/**
 * Helper class for managing restore operations.
 */
public class RestoreManager {

    private static RestoreManager sInstance;

    /**
     * Private constructor.
     */
    private RestoreManager() {
    }

    /**
     * Return a static instance of the manager.
     */
    public static RestoreManager getInstance() {
        if (sInstance == null) {
            sInstance = new RestoreManager();
        }
        return sInstance;
    }

    /**
     * Request a restore from a local folder.
     */
    public void requestLocalRestore(Context context, final IAsyncHandler handler) {
        new AsyncTask<Context, Void, Boolean>() {

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                handler.onResult(aBoolean, null);

            }

            @Override
            protected Boolean doInBackground(Context... params) {
                File folder = new File(Environment.getExternalStorageDirectory()
                        + "/data/" + params[0].getPackageName());
                return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
                        && folder.exists()
                        && restoreSettings(params[0], folder)
                        && restoreDatabase(params[0], folder);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, context);
    }

    /**
     * Restore our settings.
     */
    @SuppressLint("CommitPrefEdits")
    @SuppressWarnings("unchecked")
    private boolean restoreSettings(Context context, File folder) {
        ObjectInputStream input = null;
        File file = new File(folder, Constants.SETTINGS_BACKUP_NAME);
        try {
            input = new ObjectInputStream(new FileInputStream(file));
            SharedPreferences.Editor editor =
                    PreferenceManager.getDefaultSharedPreferences(context).edit();
            editor.clear();
            editor.commit();
            Map<String, ?> entries = (Map<String, ?>) input.readObject();
            for (Map.Entry<String, ?> entry : entries.entrySet()) {
                Object v = entry.getValue();
                String key = entry.getKey();

                if (v instanceof Boolean)
                    editor.putBoolean(key, (Boolean) v);
                else if (v instanceof Float)
                    editor.putFloat(key, (Float) v);
                else if (v instanceof Integer)
                    editor.putInt(key, (Integer) v);
                else if (v instanceof Long)
                    editor.putLong(key, (Long) v);
                else if (v instanceof String)
                    editor.putString(key, ((String) v));
            }
            return editor.commit();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * Restore the database.
     */
    private boolean restoreDatabase(Context context, File folder) {
        SqlHandler handler = new SqlHandler(context);
        try {
            handler.open();
            handler.restoreDbFromFile(new File(folder, Constants.WORKOUTS_BACKUP_NAME));
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            handler.close();
        }
        return false;
    }
}
