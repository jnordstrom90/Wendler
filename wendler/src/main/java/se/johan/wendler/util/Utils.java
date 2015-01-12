package se.johan.wendler.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * Generic utility functions.
 */
public class Utils {

    public static boolean hasKitKat() {
        return Build.VERSION.SDK_INT >= VERSION_CODES.KITKAT;
    }


    public static boolean hasLollipop() {
        return Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP;
    }

    /**
     * Hide the input keyboard.
     */
    public static void hideKeyboard(Activity activity) {
        View currentFocus = activity.getCurrentFocus();
        if (currentFocus != null) {
            InputMethodManager imm =
                    (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
        activity.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    }

    /**
     * Return an int from an EditText.
     */
    public static int getIntFromEditText(EditText editText) {
        if (editText.getText() == null) {
            return 0;
        }

        String text = editText.getText().toString().trim();
        if (text.length() == 0) {
            return 0;
        }
        return Integer.parseInt(text);
    }

    /**
     * Return a double from an EditText.
     */
    public static double getDoubleFromEditText(EditText editText) {

        if (editText.getText() == null) {
            return 0;
        }

        String text = editText.getText().toString().trim();
        if (text.length() == 0) {
            return 0;
        }
        return Double.parseDouble(text);
    }

    /**
     * Return the current app version.
     */
    public static String getCurrentAppVersion(Context context) {
        String currentVersion = Constants.NO_VERSION;
        try {
            currentVersion = context
                    .getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionName;
            return currentVersion;
        } catch (PackageManager.NameNotFoundException e) {
            WendlerizedLog.e("Error fetching the current app version", e);
        }
        return currentVersion;
    }
}
