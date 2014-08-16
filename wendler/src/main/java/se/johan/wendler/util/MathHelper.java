package se.johan.wendler.util;

import android.content.Context;

/**
 * Helper class for loading static values.
 */
public class MathHelper {

    private static MathHelper sInstance;

    private static float roundToValue = 0.0f;

    /**
     * Private constructor.
     */
    private MathHelper() {
    }


    /**
     * Return a static instance of the class.
     */
    public static MathHelper getInstance() {

        if (sInstance == null) {
            sInstance = new MathHelper();
        }
        return sInstance;
    }

    /**
     * Return the used rounding value.
     */
    public float getRoundToValue(Context context) {

        if (roundToValue == 0.0f) {
            roundToValue =
                    PreferenceUtil.getFloat(
                            context,
                            PreferenceUtil.KEY_ROUND_TO_VALUE,
                            WendlerConstants.DEFAULT_ROUND_TO);
        }

        return roundToValue;
    }

    /**
     * Reset the rounding value.
     */
    public void resetRoundToValue() {
        roundToValue = 0.0f;
    }

}
