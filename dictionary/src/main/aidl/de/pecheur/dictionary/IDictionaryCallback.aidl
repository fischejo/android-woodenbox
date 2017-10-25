// IDictionaryCallback.aidl
package de.pecheur.dictionary;

// Declare any non-default types here with import statements

/**
* Example of a callback interface used by IRemoteService to send
* synchronous notifications back to its clients. Note that this is a
* one-way interface so the server does not block waiting for the client.
*/
interface IDictionaryCallback {
    void onCompilation(int id, in Bundle bundle);


    void onError(int id, int code);
}
