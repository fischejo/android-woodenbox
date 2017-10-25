package de.pecheur.colorbox.settings;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.ArrayAdapter;

import de.pecheur.colorbox.R;


public class BoxPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener, OnNavigationListener {
    static final int DEFAULT_BOX_COUNT = 6;
    static final int MIN_BOX_COUNT = 5;
    static final int MAX_BOX_COUNT = 10;
    static final String BOX_COUNT_KEY = "box_count";

    private SharedPreferences mPreferences;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.box_preferences);


        ActionBar ab = getActivity().getActionBar();
        ab.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        mPreferences = getPreferenceManager().getSharedPreferences();

        ab.setListNavigationCallbacks(ArrayAdapter.createFromResource(
                ab.getThemedContext(),
                R.array.settings_box_navigation,
                android.R.layout.simple_list_item_1), this);


        ab.setSelectedNavigationItem( mPreferences.getInt(
                BOX_COUNT_KEY,
                DEFAULT_BOX_COUNT) - MIN_BOX_COUNT);

        for(int i = 1; i <= MAX_BOX_COUNT; i++)
            findPreference(Settings.getBoxKey(i)).setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        int count = MIN_BOX_COUNT + itemPosition;
        mPreferences.edit().putInt(BOX_COUNT_KEY, count).apply();

        for(int i = 1; i <= MAX_BOX_COUNT; i++)
            findPreference(Settings.getBoxKey(i)).setEnabled(i <= count);

        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        // Workaround for summary-update after preference change
        preference.setSummary("");
        preference.setSummary(R.string.settings_box_list_subtitle);
        return true;
    }


}