package se.johan.wendler.util;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import se.johan.wendler.R;
import se.johan.wendler.model.DeloadItem;
import se.johan.wendler.model.ExerciseSet;
import se.johan.wendler.model.MainExercise;
import se.johan.wendler.model.SetType;

/**
 * Class with mathematical functions.
 */
public class WendlerMath {

    /**
     * Calculate one rm for a given weight and repetitions.
     */
    public static int calculateOneRm(double weight, int reps) {
        if(reps <= 0){
            return -1;
        }
        double mOneRm = weight * reps * 0.0333 + weight;
        return (int) Math.round(mOneRm);
    }

    /**
     * Calculate the training max.
     */
    public static double calculateWeight(Context context,
                                         double weight,
                                         int percentage) {
        double trainingMax = weight * ((double) percentage / 100);

        return roundToClosest(trainingMax, MathHelper.getInstance().getRoundToValue(context));
    }

    /**
     * Return if the given workout for was won.
     */
    public static boolean isWorkoutWon(int week, MainExercise exercise) {
        return exercise.isWon() || week == 4;
    }

    /**
     * Return if inserted percentages are equal to the heavy percentages.
     */
    public static boolean arePercentagesHeavy(
            int[] weekOne, int[] weekTwo, int[] weekThree, int[] weekFour) {

        return Arrays.equals(weekOne, WendlerConstants.HEAVY_PERCENTAGES_W_1)
                && Arrays.equals(weekTwo, WendlerConstants.HEAVY_PERCENTAGES_W_2)
                && Arrays.equals(weekThree, WendlerConstants.HEAVY_PERCENTAGES_W_3)
                && Arrays.equals(weekFour, WendlerConstants.HEAVY_PERCENTAGES_W_4);
    }

    /**
     * Return if inserted percentages are equal to the fresh percentages.
     */
    public static boolean arePercentagesFresh(
            int[] weekOne, int[] weekTwo, int[] weekThree, int[] weekFour) {
        return Arrays.equals(weekOne, WendlerConstants.FRESH_PERCENTAGES_W_1)
                && Arrays.equals(weekTwo, WendlerConstants.FRESH_PERCENTAGES_W_2)
                && Arrays.equals(weekThree, WendlerConstants.FRESH_PERCENTAGES_W_3)
                && Arrays.equals(weekFour, WendlerConstants.FRESH_PERCENTAGES_W_4);
    }

    /**
     * Do a deload with given settings
     */
    public static DeloadItem doDeload(Context context,
                                      DeloadItem deloadItem,
                                      int trainingPercentage,
                                      double increment) {

        if (PreferenceUtil.getBoolean(context, PreferenceUtil.KEY_AUTO_DELOAD, true)
                || deloadItem.getWeek() == 4) {
            deloadItem.setWeek(1);

            if (PreferenceUtil.getBoolean(context, PreferenceUtil.KEY_RESET_CYCLE_DELOAD)) {
                deloadItem.setCycleName(1);
            } else {
                deloadItem.setCycleName(deloadItem.getCycleName() + 1);
            }

            deloadItem.setCycle(deloadItem.getCycle() + 1);

            if (!PreferenceUtil.getBoolean(context, PreferenceUtil.KEY_WEIGHT_TYPE_DELOAD, true)) {
                deloadItem.setWeight(
                        calculateWeight(context,
                                deloadItem.getWeight(),
                                100 + (100 - trainingPercentage))
                );
            }
            deloadItem.setWeight(calculateDeloadWeight(
                    deloadItem.getWeight(),
                    context,
                    increment));

        } else {
            deloadItem.setWeek(deloadItem.getWeek() + 1);
            deloadItem.setDoDelayedDeload();
        }
        return deloadItem;
    }

    /**
     * Calculate the weight after a deload.
     */
    private static double calculateDeloadWeight(
            double oldWeight, Context context, double increment) {

        String deloadType = PreferenceUtil.getString(
                context, PreferenceUtil.KEY_DELOAD_TYPE,
                context.getString(R.string.deload_type_default_value));

        String[] deloadValues =
                context.getResources().getStringArray(R.array.deload_type_entry_values);

        if (deloadType.equals(deloadValues[0])) {
            oldWeight -= (increment * 3);

        } else if (deloadType.equals(deloadValues[1])) {
            int percentage = 100 - WendlerConstants.DEFAULT_DELOAD_PERCENTAGE;

            oldWeight = calculateWeight(context, oldWeight, percentage);

        } else if (deloadType.equals(deloadValues[2])) {
            int percentage = Integer.parseInt(
                    PreferenceUtil.getString(context, PreferenceUtil.KEY_CUSTOM_DELOAD_TYPE_VALUE,
                            String.valueOf(WendlerConstants.DEFAULT_DELOAD_PERCENTAGE)
                    )
            );
            percentage = 100 - percentage;

            oldWeight = calculateWeight(context, oldWeight, percentage);
        }
        return oldWeight;
    }

    /**
     * Calculate the weight after undoing a failed workout
     */
    private static double calculateReverseDeloadWeight(
            double oldWeight, Context context, double increment) {

        String deloadType = PreferenceUtil.getString(
                context, PreferenceUtil.KEY_DELOAD_TYPE,
                context.getString(R.string.deload_type_default_value));
        String[] deloadValues =
                context.getResources().getStringArray(R.array.deload_type_entry_values);
        if (deloadType.equals(deloadValues[0])) {
            oldWeight += (increment * 3);

        } else if (deloadType.equals(deloadValues[1])) {
            int percentage = 100 - WendlerConstants.DEFAULT_DELOAD_PERCENTAGE;

            oldWeight /= percentage;
            oldWeight *= 100;
            oldWeight = Math.round(oldWeight);

        } else if (deloadType.equals(deloadValues[2])) {
            int percentage = Integer.parseInt(
                    PreferenceUtil.getString(context, PreferenceUtil.KEY_CUSTOM_DELOAD_TYPE_VALUE,
                            String.valueOf(WendlerConstants.DEFAULT_DELOAD_PERCENTAGE)
                    )
            );
            percentage = 100 - percentage;

            oldWeight /= percentage;
            oldWeight *= 100;
            oldWeight = Math.round(oldWeight);
        }

        return oldWeight;
    }

    /**
     * Calculate new weight after updating a previous workout deload.
     */
    public static double calculateNewWeight(
            double weight, Context context, double increment, boolean unDeload) {
        if (unDeload) {
            return calculateReverseDeloadWeight(weight, context, increment);
        } else {
            return calculateDeloadWeight(weight, context, increment);
        }

    }

    /**
     * Return the workout sets for a given workout.
     */
    public static ArrayList<ExerciseSet> getWorkoutSets(Context context,
                                                        double oneRm,
                                                        int[] setPercentages,
                                                        int week,
                                                        int progress) {
        ArrayList<ExerciseSet> sets = new ArrayList<ExerciseSet>();
        int[] setReps = getSetReps(context, week);
        for (int i = 0; i < setPercentages.length; i++) {
            SetType type = i == setPercentages.length - 1 ? SetType.PLUS_SET : SetType.REGULAR;

            sets.add(new ExerciseSet(
                    type,
                    calculateWeight(context, oneRm, setPercentages[i]),
                    setReps[i],
                    progress,
                    progress > -1));
        }

        return sets;
    }

    /**
     * Return the warmup sets for a workout.
     */
    public static ArrayList<ExerciseSet> getWarmupSets(Context context,
                                                       double oneRm,
                                                       String[] percentages,
                                                       int progress) {
        ArrayList<ExerciseSet> sets = new ArrayList<ExerciseSet>();

        int[] percentagesAsInt = convertStringArrayToInt(percentages);

        String[] warmupReps = PreferenceUtil.getString(
                context, PreferenceUtil.KEY_WARM_UP_REPS, WendlerConstants.DEFAULT_WARMUP_REPS)
                .split(",");

        int[] warmupRepsAsInt = convertStringArrayToInt(warmupReps);

        for (int i = 0; i < percentagesAsInt.length; i++) {

            sets.add(new ExerciseSet(
                    SetType.WARM_UP,
                    calculateWeight(context, oneRm, percentagesAsInt[i]),
                    warmupRepsAsInt[i],
                    progress,
                    progress != -1));
        }

        return sets;
    }


    /**
     * Round a value to the closest given number.
     */
    private static double roundToClosest(double value, double roundTo) {
        return Math.round(value / roundTo) * roundTo;
    }

    /**
     * Return the reps to perform for a given week.
     */
    private static int[] getSetReps(Context context, int week) {
        switch (week) {
            case 1:
                return new int[]{5, 5, 5};
            case 2:
                return new int[]{3, 3, 3};
            case 3:
                return new int[]{5, 3, 1};
            default:
                String repsAsString = PreferenceUtil.getString(
                        context,
                        PreferenceUtil.KEY_DELOAD_REPS,
                        WendlerConstants.DEFAULT_DELOAD_REPS);
                String[] repsAsStringArray = repsAsString.split(",");
                return convertStringArrayToInt(repsAsStringArray);
        }
    }

    /**
     * Convert a String array to int;
     */
    private static int[] convertStringArrayToInt(String[] stringArray) {
        int[] intArray = new int[stringArray.length];
        for (int i = 0; i < stringArray.length; i++) {
            intArray[i] = Integer.parseInt(stringArray[i]);
        }
        return intArray;
    }

    public static int getRepsToBeat(List<ExerciseSet> sets, int highestEstimated1RM) {
        if(highestEstimated1RM == -1){
            return -1;
        }

        ExerciseSet lastSet = sets.get(sets.size()-1);

        double oneRmReps = calculateOneRmReps(lastSet.getWeight(), highestEstimated1RM);
        int ceilOneRmReps = (int) Math.ceil(oneRmReps);

        if (calculateOneRm(lastSet.getWeight(), ceilOneRmReps) <= highestEstimated1RM) {
            return ceilOneRmReps + 1;
        } else {
            return ceilOneRmReps;
        }
    }

    public static double calculateOneRmReps(double weight, double oneRm){
        double constant = 0.0333;
        return 1 / ((weight * constant) / (oneRm - weight));

        /**
         * Proof (probably horrible math):
         * O = (w*r*c) + w
         * O–w = w*r*c
         * (o-w)/r = w*c
         * (o-w)*(1/r) = w*c
         * 1/r = (w*c)/(o-w)
         * R = 1/((w*c)/(o-w))
         * QED
         */
    }
}
