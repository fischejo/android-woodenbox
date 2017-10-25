package de.pecheur.dictionary;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

public abstract class DictionaryService extends Service  {


    public abstract Bundle doInBackground(String query, String from, String to, String[] types) ;


    private final IDictionaryInterface.Stub mBinder = new IDictionaryInterface.Stub() {
        @Override
        public void query(
                final int id,
                final String query,
                final String from,
                final String to,
                final String[] types,
                final IDictionaryCallback idc) throws RemoteException {


            // start background threads
            new Thread() {
                @Override
                public void run() {
                    try {
                        idc.onCompilation(id, doInBackground(query, from, to, types));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            };

            //new QueryThread(query, idc).start();
        }
    };


    @Override
    public IBinder onBind(Intent intent) {
        // Return the interface
        return mBinder;
    }
}