// IDictionaryInterface.aidl
package de.pecheur.dictionary;

import de.pecheur.dictionary.IDictionaryCallback;

// Declare any non-default types here with import statements

oneway interface IDictionaryInterface {
       void query(int id,
                String query,
                String from,
                String to,
                in String[] types,
                IDictionaryCallback idc);
}
