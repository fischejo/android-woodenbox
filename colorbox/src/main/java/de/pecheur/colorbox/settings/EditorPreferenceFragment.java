package de.pecheur.colorbox.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import de.pecheur.colorbox.R;


public class EditorPreferenceFragment extends PreferenceFragment {
    public static final String PINNED_TEXT_KEY = "pinned_text_key";
    public static final String PINNED_AUDIO_KEY = "pinned_audio_key";
    public static final String PINNED_EXAMPLE_KEY = "pinned_example_key";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.editor_preferences);
    }
}