package se.johan.wendler.util;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.ExceptionReporter;
import com.google.analytics.tracking.android.GAServiceManager;
import com.google.analytics.tracking.android.StandardExceptionParser;

/**
 * Helper class for managing Google Analytics.
 */
@SuppressWarnings({"PointlessBooleanExpression", "ConstantConditions"})
public class AnalyticsHelper {

    private static AnalyticsHelper sInstance;

    private AnalyticsHelper() {
    }

    public static AnalyticsHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new AnalyticsHelper();
            ExceptionReporter myHandler =
                    new ExceptionReporter(EasyTracker.getInstance(context),
                            GAServiceManager.getInstance(),
                            Thread.getDefaultUncaughtExceptionHandler(), context);

            StandardExceptionParser exceptionParser =
                    new StandardExceptionParser(context.getApplicationContext(), null) {
                        @Override
                        public String getDescription(String threadName, Throwable t) {
                            return "{" + threadName + "} " + Log.getStackTraceString(t);
                        }
                    };

            myHandler.setExceptionParser(exceptionParser);

            // Make myHandler the new default uncaught exception handler.
            Thread.setDefaultUncaughtExceptionHandler(myHandler);
        }
        return sInstance;
    }

    /**
     * Enable logging for a given activity.
     */
    public void setLoggingEnabled(Activity activity) {
        if (!Constants.DEBUG) {
            EasyTracker.getInstance(activity).activityStart(activity);
        }
    }

    /**
     * Disable logging for a given activity.
     */
    public void setLoggingDisabled(Activity activity) {
        if (!Constants.DEBUG) {
            EasyTracker.getInstance(activity).activityStop(activity);
        }
    }
}
