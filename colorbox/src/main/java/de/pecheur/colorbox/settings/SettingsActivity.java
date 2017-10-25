package de.pecheur.colorbox.settings;

import android.app.ActionBar;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

import java.util.List;

import de.pecheur.colorbox.R;

public class SettingsActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set subtitle and back-button
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setIcon(android.R.color.transparent);
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        // Populate the activity with the top-level headers.
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem nItem) {
        switch (nItem.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return false;
    }
}