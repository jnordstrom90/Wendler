package se.johan.wendler.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Chronometer;

/**
 * Custom chronometer with a is running method.
 */
@SuppressWarnings("UnusedDeclaration")
public class CustomChronometer extends Chronometer {

    private boolean mIsRunning = false;

    public CustomChronometer(Context context) {
        super(context);
    }

    public CustomChronometer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomChronometer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Called when the chronometer is started.
     */
    @Override
    public void start() {
        super.start();
        mIsRunning = true;
    }

    /**
     * Called when the chronometer is stopped.
     */
    @Override
    public void stop() {
        super.stop();
        mIsRunning = false;
    }

    /**
     * Return if the chronometer is running.
     */
    public boolean isRunning() {
        return mIsRunning;
    }
}
