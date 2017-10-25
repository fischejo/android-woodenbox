package de.pecheur.dictionary;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by fischejo on 09.11.14.
 */
public class SimpleDictionaryService extends DictionaryService {
    private static final String[] PRONUNCIATIONS = new String[] {
            "Haus", "Käse", "Straße", "Bus", "Auto"
    };

    private static final String[] EXAMPLES = new String[] {
            "Belgium", "France", "Italy", "Germany", "Spain"
    };


    @Override
    public Bundle doInBackground(String query, String from, String to, String[] types) {
        Log.d("p1", "doInBackground");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Bundle b = new Bundle();

        // put examples
        b.putStringArrayList("examples", (ArrayList<String>) Arrays.asList(EXAMPLES));


        // put pronunciations
        ArrayList<Pronunciation> pronunciations = new ArrayList<Pronunciation>();
        for(int i = 0; i < PRONUNCIATIONS.length; i++) {
            Pronunciation pronunciation = new Pronunciation();
            pronunciation.setUri(Uri.parse("www.simple.de"));
            pronunciation.setContributor("simple-dictionary");
            pronunciation.setIpa(PRONUNCIATIONS[i]);
            pronunciations.add(pronunciation);
        }
        b.putParcelableArrayList("pronunciation", pronunciations);

        return b;
    }
}
