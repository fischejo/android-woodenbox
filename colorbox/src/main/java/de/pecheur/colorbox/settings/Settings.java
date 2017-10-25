package de.pecheur.colorbox.settings;

import android.content.Context;
import android.preference.PreferenceManager;


// TODO more efficient PreferenceManager handling

public class Settings {
    public static final String OPTION_BEGINNING = "beginning";
    public static final String OPTION_NOTHING = "nothing";
    public static final String OPTION_BACK = "back";

    public static String getBoxKey(int box) {
        return "box_"+ box;
    }

    public static int getBoxCount(Context c) {
        return PreferenceManager.getDefaultSharedPreferences(c).getInt(
                BoxPreferenceFragment.BOX_COUNT_KEY,
                BoxPreferenceFragment.DEFAULT_BOX_COUNT);
    }


    public static Long getBoxDelay(Context context, int box) {
        return box == 0 ? 0 : Long.valueOf(
                PreferenceManager.getDefaultSharedPreferences(context)
                        .getString(getBoxKey(box), null)
        );
    }

    public static int getQueryCount(Context context) {
        return Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getString(
                QueryPreferenceFragment.QUERY_COUNT_KEY,
                QueryPreferenceFragment.QUERY_COUNT_DEFAULT));
    }


    public static String getQueryOption(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(
                QueryPreferenceFragment.QUERY_OPTION_KEY,
                OPTION_BEGINNING);

    }


}
