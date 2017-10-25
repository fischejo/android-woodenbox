package de.pecheur.colorbox.settings;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import de.pecheur.colorbox.R;

// TODO register service by default with first doInBackground.
// TODO bug: service starts after registering

public class NotificationFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    private static final String TIME_KEY = "notification_time";
    private static final String CHECK_KEY = "notification_check";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.notification_preferences);

        findPreference(TIME_KEY).setOnPreferenceChangeListener(this);
        findPreference(CHECK_KEY).setOnPreferenceChangeListener(this);

        // test
        this.getActivity().startService(new Intent(getActivity(), NotificationService.class));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        Context context = getActivity();

        PendingIntent pendingIntent = PendingIntent.getService(
                context,
                NotificationService.REQUEST_CODE_DAILY,
                new Intent(context, NotificationService.class),
                0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);

        if (preference.getKey().equals(CHECK_KEY)) {
            if (!(Boolean) value) {
                // unregister alarm
                alarmManager.cancel(pendingIntent);
                return true;
            }
            // register with stored value
            value = getPreferenceManager().getSharedPreferences().getLong(TIME_KEY, 0);
        }

        // register alarm
        alarmManager.setRepeating(AlarmManager.RTC,
                (Long) value,
                1000 * 60 * 60 * 24,
                pendingIntent);
        return true;
    }
}