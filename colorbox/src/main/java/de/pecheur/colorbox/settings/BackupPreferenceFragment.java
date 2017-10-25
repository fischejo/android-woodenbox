package de.pecheur.colorbox.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;

import java.io.IOException;

import de.pecheur.colorbox.R;


public class BackupPreferenceFragment extends PreferenceFragment implements OnPreferenceClickListener {

	private static final String BACKUP_KEY = "backup_export";

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.backup_preferences);
		
		// export preference
		findPreference(BACKUP_KEY).setOnPreferenceClickListener(this);
	}

	
	@Override
	public boolean onPreferenceClick(Preference preference) {
        try {
            new AsyncBackupTask(preference.getContext()).execute();
        } catch(IOException e) {
            e.printStackTrace();
        }
		return true;
	}

}