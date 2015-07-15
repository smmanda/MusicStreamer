package com.sri.myomusicstreamer;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * Created by smanda on 5/30/15.
 */
public class SettingsActivity extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener {
    private List<String> available_markets;
    private final String LOG_TAG = MusicPlayerFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add 'general' preferences, defined in the XML file
        addPreferencesFromResource(R.xml.pref_general);

        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_country_code)));
    }

    /**
     * Attaches a listener so the summary is always updated with the preference value.
     * Also fires the listener once, to initialize the summary (so it shows up before the value
     * is changed.)
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

        // Trigger the listener immediately with the preference's
        // current value.
        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            setListPreferenceData(listPreference);

        }
        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    private void setListPreferenceData(ListPreference listPref) {
        if(available_markets == null)
            fetchAvailableLocations();
        CharSequence[] entries = available_markets.toArray(new CharSequence[available_markets.size()]);

        listPref.setKey(getString(R.string.pref_country_code)); //Refer to get the pref value
        listPref.setEntries(entries);
        listPref.setEntryValues(entries);
        listPref.setDialogTitle(getString(R.string.pref_location_label));
        listPref.setTitle(getString(R.string.pref_location_label));
        listPref.setSummary(listPref.getEntries()[0]);
        listPref.setDefaultValue(getString(R.string.pref_loc_label_us));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else {
            // For other preferences, set the summary to the value's simple string representation.
            preference.setSummary(stringValue);
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Intent getParentActivityIntent() {
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

    private void fetchAvailableLocations() {
        available_markets = new ArrayList<>();
        FetchAvailableLocationsTask artistsTask = new FetchAvailableLocationsTask(getApplicationContext(),available_markets);
        try {
            available_markets = artistsTask.execute().get();
        } catch (InterruptedException e) {
            Log.e(LOG_TAG, String.format("InterruptedException: %s", e));
        } catch (ExecutionException e) {
            Log.e(LOG_TAG, String.format("ExecutionException: %s", e));
        }
    }
}
