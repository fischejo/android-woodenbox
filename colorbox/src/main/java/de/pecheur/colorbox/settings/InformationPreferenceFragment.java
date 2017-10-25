package de.pecheur.colorbox.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import de.pecheur.colorbox.R;

public class InformationPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.information_preferences);
        findPreference("feedback").setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        Intent intent = new Intent(Intent.ACTION_SENDTO,
        Uri.fromParts("mailto", getString(R.string.settings_information_feedback_mailto), null));
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.settings_information_feedback_subject));
        startActivity(Intent.createChooser(intent, getString(R.string.settings_information_feedback_dialog)));
        return true;
    }
}