package de.pecheur.colorbox.editor;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.util.Log;
import de.pecheur.dictionary.Dictionary;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Created by fischejo on 09.11.14.
 */
public class DictionaryResolver implements ServiceConnection {
    private static final String ACTION = "com.github.woodenbox.DICTIONARY";
    private static final String TYPE = "language/%1$s";

    private List<ResolveInfo> unbound;
    private HashMap<ComponentName, Dictionary> bound;

    public interface DictionaryCallback {
        public void onPublish();
        public void onError();
    }

    private Activity mActivity;


    private Comparator<ResolveInfo> mDictionaryComparator = new Comparator<ResolveInfo>() {
        @Override
        public int compare(ResolveInfo lhs, ResolveInfo rhs) {
            Integer lcc = lhs.filter.countCategories();
            Integer rcc = rhs.filter.countCategories();
            return lcc.compareTo(rcc);
        }
    };


    public DictionaryResolver(Activity activity) {
        mActivity = activity;
        bound = new HashMap<ComponentName, Dictionary>();
    }





    public void query(CharSequence cs, DictionaryCallback dc) {


    }



    public void rebind() {
        Intent intent = new Intent();
        intent.setAction(ACTION);

        intent.setType("language/dela");    // String.format(TYPE, unit.getCode())

        // query all services, which support the mime (language) type
        unbound = mActivity.getPackageManager().queryIntentServices(intent, PackageManager.GET_RESOLVED_FILTER);



        // sort them by the category (TEXT, EXAMPLE, AUDIO) count to reduce redundant server queries later.
        Collections.sort(unbound, mDictionaryComparator);


        // reuse bound service

        // TODO:
        // - bidirectional vs single language queries
        // - same service for multiple languages
        // - thread handling neccessary?

        // bind service
        ResolveInfo info = unbound.get(0);

        Intent aintent = new Intent();
        aintent.setComponent(new ComponentName(
                info.serviceInfo.applicationInfo.packageName,
                info.serviceInfo.name));

        mActivity.bindService(aintent, this, Context.BIND_AUTO_CREATE);
    }



    public void unbindDictionaries() {

    }



    @Override
    public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance

        Dictionary dict = new Dictionary(service);

        bound.put(className, dict);


        //mBound = true;
        Log.d("p1", "onServiceConnected");
    }

    @Override
    public void onServiceDisconnected(ComponentName className) {
        bound.remove(className);

        Log.d("p1", "onServiceDisconnected");
    }

}
