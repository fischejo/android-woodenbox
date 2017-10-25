package de.pecheur.colorbox.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import de.pecheur.colorbox.R;


public class QueryPreferenceFragment extends PreferenceFragment {
    static final String QUERY_COUNT_KEY = "query_count";
    static final String QUERY_COUNT_DEFAULT = "10";
    static final String QUERY_OPTION_KEY = "query_option";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.query_preferences);
    }
}