package se.johan.wendler.ui.view;

import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;

/**
 * DrawerLayout used in the application. Override default due to avoiding NullPointer and
 * providing back navigation.
 */
@SuppressWarnings("UnusedDeclaration")
public class MyDrawerLayout extends DrawerLayout {

    private OnHideListener mOnHideListener;

    /**
     * Constructor.
     */
    public MyDrawerLayout(Context context) {
        super(context);
    }

    /**
     * Constructor.
     */
    public MyDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Constructor.
     */
    public MyDrawerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Set the listener for the DrawerLayout.
     */
    public void setOnHideListener(OnHideListener hideListener) {
        mOnHideListener = hideListener;
    }

    /**
     * Called when a key is pressed.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mOnHideListener != null) {
            mOnHideListener.onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Called to intercept touch events. Override to catch a NullPointer
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (NullPointerException ignored) {
            return true;
        }
    }

    /**
     * Interface for catching the back key when the DrawerLayout is locked open.
     */
    public interface OnHideListener {

        /**
         * Called when the back key is pressed.
         */
        public void onBackPressed();
    }
}
