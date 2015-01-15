package se.johan.wendler.ui.view;

import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * DrawerLayout used in the application. Override default due to avoiding NullPointer and
 * providing back navigation.
 */
@SuppressWarnings("UnusedDeclaration")
public class MyDrawerLayout extends DrawerLayout {

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
}
