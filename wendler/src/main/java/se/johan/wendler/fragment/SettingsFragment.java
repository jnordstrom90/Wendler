package se.johan.wendler.fragment;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.doomonafireball.betterpickers.numberpicker.NumberPickerBuilder;
import com.doomonafireball.betterpickers.numberpicker.NumberPickerDialogFragment;

import se.johan.wendler.R;
import se.johan.wendler.activity.StartupActivity;
import se.johan.wendler.dialog.ConfirmationDialog;
import se.johan.wendler.sql.SqlHandler;
import se.johan.wendler.util.MathHelper;
import se.johan.wendler.util.PreferenceUtil;
import se.johan.wendler.util.Util;
import se.johan.wendler.util.WendlerConstants;

/**
 * Fragment to manage settings.
 */
public class SettingsFragment extends PreferenceFragment implements
        SharedPreferences.OnSharedPreferenceChangeListener,
        Preference.OnPreferenceClickListener,
        NumberPickerDialogFragment.NumberPickerDialogHandler,
        ConfirmationDialog.ConfirmationDialogListener {

    public static final String TAG = SettingsFragment.class.getName();

    public SettingsFragment() {
    }

    /**
     * Static creation to avoid problems on rotation.
     */
    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    /**
     * Called when the view is created.
     */
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        addPreferencesFromResource(R.xml.settings_xml);
        updateDefaultValues();
        updateNumberPref();
        findPreference(PreferenceUtil.KEY_CLEAR_DATA).setOnPreferenceClickListener(this);
        return super.onCreateView(inflater, container, savedInstanceState);

    }

    /**
     * Called when the fragment is resumed.
     */
    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager()
                .getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * Called when the fragment is paused.
     */
    @Override
    public void onPause() {
        getPreferenceManager()
                .getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    /**
     * Called when a preference is changed.
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);

        if (key.equals(PreferenceUtil.KEY_ROUND_TO)) {
            preference.setSummary(((ListPreference) preference).getEntry());
            MathHelper.getInstance().resetRoundToValue();
        } else if (key.equals(PreferenceUtil.KEY_DELOAD_TYPE)) {
            preference.setSummary(((ListPreference) preference).getEntry());
            updateNumberPref();
        }
    }

    /**
     * Called when a preference is clicked.
     */
    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == null || preference.getKey() == null) {
            return false;
        }

        if (preference.getKey().equals(PreferenceUtil.KEY_CUSTOM_DELOAD_TYPE)) {
            new NumberPickerBuilder()
                    .setFragmentManager(getActivity().getSupportFragmentManager())
                    .setMaxNumber(100)
                    .setMinNumber(1)
                    .setTargetFragment(this)
                    .setDecimalVisibility(View.GONE)
                    .setPlusMinusVisibility(View.GONE)
                    .setStyleResId(R.style.BetterPickersDialogFragment_Light)
                    .show();
            return true;
        } else if (preference.getKey().equals(PreferenceUtil.KEY_CLEAR_DATA)) {
            ConfirmationDialog.newInstance(
                    getString(R.string.pref_clear_dialog_summary),
                    getString(R.string.pref_clear_dialog_title),
                    getString(R.string.btn_ok),
                    getString(R.string.btn_cancel),
                    this).show(getActivity().getSupportFragmentManager(), ConfirmationDialog.TAG);
            return true;
        }
        return false;
    }

    /**
     * Called when a number has been set in the number picker.
     */
    @Override
    public void onDialogNumberSet(
            int reference, int number, double decimal, boolean isNegative, double fullNumber) {
        Preference pref = findPreference(PreferenceUtil.KEY_CUSTOM_DELOAD_TYPE);
        String value = String.format(getString(R.string.custom_deload_value), number);

        PreferenceUtil.putString(
                getActivity(),
                PreferenceUtil.KEY_CUSTOM_DELOAD_TYPE_VALUE,
                String.valueOf(number));
        pref.setSummary(value);
    }

    /**
     * Called when the confirmation dialog has been confirmed.
     */
    @Override
    @SuppressLint("NewApi")
    public void onDialogConfirmed(boolean confirmed) {
        if (confirmed && Util.hasKitKat()) {
            ActivityManager manager =
                    (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
            manager.clearApplicationUserData();
        } else if (confirmed) {
            PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().clear().apply();
            getActivity().deleteDatabase(SqlHandler.DATABASE_NAME);
            Intent activityIntent = new Intent(getActivity(), StartupActivity.class);
            activityIntent.setFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK |
                            Intent.FLAG_ACTIVITY_NEW_TASK
            );
            startActivity(activityIntent);
            getActivity().finish();
        }
    }

    /**
     * Update the number preference.
     */
    private void updateNumberPref() {
        Preference numberPref = findPreference(PreferenceUtil.KEY_CUSTOM_DELOAD_TYPE);
        ListPreference deloadType = (ListPreference) findPreference(PreferenceUtil.KEY_DELOAD_TYPE);

        String[] array = getResources().getStringArray(R.array.deload_type_entry_values);
        numberPref.setEnabled(deloadType.getValue().equals(array[array.length - 1]));
        numberPref.setOnPreferenceClickListener(this);

        updateNumberSummary(numberPref);
    }

    /**
     * Update the default values for the preferences.
     */
    private void updateDefaultValues() {
        Preference pref = findPreference(PreferenceUtil.KEY_ROUND_TO);

        if (pref != null) {
            CharSequence entry = ((ListPreference) pref).getEntry();
            if (entry == null) {
                entry = getString(R.string.round_default_value);
            }
            pref.setSummary(entry);
        }

        pref = findPreference(PreferenceUtil.KEY_DELOAD_TYPE);

        if (pref != null) {
            CharSequence entry = ((ListPreference) pref).getEntry();
            if (entry == null) {
                entry = getString(R.string.deload_type_default_value);
            }
            pref.setSummary(entry);
        }

        pref = findPreference(PreferenceUtil.KEY_CUSTOM_DELOAD_TYPE);
        updateNumberSummary(pref);
    }

    /**
     * Update the summary for the number preference.
     */
    private void updateNumberSummary(Preference pref) {
        String value = String.format(getString(R.string.custom_deload_value), getDeloadValue());
        pref.setSummary(value);
    }

    /**
     * Load the stored deload value.
     */
    private String getDeloadValue() {
        return PreferenceUtil.getString(
                getActivity(),
                PreferenceUtil.KEY_CUSTOM_DELOAD_TYPE_VALUE,
                String.valueOf(WendlerConstants.DEFAULT_DELOAD_PERCENTAGE));
    }
}
