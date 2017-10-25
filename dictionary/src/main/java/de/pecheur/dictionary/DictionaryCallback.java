package de.pecheur.dictionary;

import android.os.Bundle;

/**
 * Created by fischejo on 20.11.14.
 */
public interface DictionaryCallback {

    public void onCompilation(int id, Bundle bundle);

    public void onError(int id, int code);
}
