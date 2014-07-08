package se.johan.wendler.dialog.base;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.TextView;

import se.johan.wendler.R;

/**
 * Base dialog which adds an enter and exit animation.
 */
public abstract class AnimationDialog extends DialogFragment {

    /**
     * Brand the dialog with our theme color.
     */
    @Override
    public void onStart() {
        super.onStart();
        final Resources res = getResources();
        final int themeColor = res.getColor(R.color.theme_color);

        // Title
        final int titleId = res.getIdentifier("alertTitle", "id", "android");
        final View title = getDialog().findViewById(titleId);
        if (title != null) {
            ((TextView) title).setTextColor(themeColor);
        }

        // Title divider
        final int titleDividerId = res.getIdentifier("titleDivider", "id", "android");
        final View titleDivider = getDialog().findViewById(titleDividerId);
        if (titleDivider != null) {
            titleDivider.setBackgroundColor(themeColor);
        }
    }

    /**
     * Add animations.
     */
    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);
        getDialog().getWindow()
                .getAttributes().windowAnimations = R.style.DialogAnimation;
    }
}
