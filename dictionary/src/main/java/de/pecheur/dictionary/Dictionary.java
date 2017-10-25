package de.pecheur.dictionary;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * Created by fischejo on 09.11.14.
 */
public class Dictionary implements ServiceConnection {
    private static final String ACTION = "com.github.woodenbox.DICTIONARY";
    private static final String TYPE = "language/%1$s";


    private IDictionaryInterface mTarget;

    private DictionaryCallback mCallback;

    private String from;
    private String to;
    private String[] types;
    private Context context;

    private static final Comparator<ResolveInfo> mDictionaryComparator = new Comparator<ResolveInfo>() {
        @Override
        public int compare(ResolveInfo lhs, ResolveInfo rhs) {
            Integer lcc = lhs.filter.countCategories();
            Integer rcc = rhs.filter.countCategories();
            return lcc.compareTo(rcc);
        }
    };




    public static Dictionary getDictionary(Context context, String from, String to, String[] types) {
        Intent intent = new Intent();
        intent.setAction(ACTION);
        intent.setType("language/dela");    // String.format(TYPE, unit.getCode())

        // query all services, which support the mime (language) type
        List<ResolveInfo> dictionaries = context.getPackageManager().queryIntentServices(intent, PackageManager.GET_RESOLVED_FILTER);
        Collections.sort(dictionaries, mDictionaryComparator);

        // right now we just take the best one and ignore error handling
        ResolveInfo info = dictionaries.get(0);
        Intent service = new Intent();
        service.setComponent(new ComponentName(
                info.serviceInfo.applicationInfo.packageName,
                info.serviceInfo.name));

        return new Dictionary(context, service, from, to, types);
    }


    private Dictionary(Context context, Intent intent, String from, String to, String[] types) {
        context = context;
        from = from;
        to = to;
        types = types;

        context.bindService(intent, this, Context.BIND_AUTO_CREATE);
    }


    public void unbind() {
        context.unbindService(this);
    }


    public void setDictionaryCallback(DictionaryCallback callback) {
        mCallback = callback;
    }


    public void query(int id, String query) {
        try {
            mTarget.query(id, query, from, to, types, iCallback);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }



    /**
     * This implementation is used to receive callbacks from the remote
     * service.
     */
    private IDictionaryCallback iCallback = new IDictionaryCallback.Stub() {
        @Override
        public void onCompilation(int id, Bundle bundle) throws RemoteException {
           mHandler.obtainMessage(COMPILATION_MSG, id, 0, bundle);
        }

        @Override
        public void onError(int id, int code) throws RemoteException {
            mHandler.obtainMessage(ERROR_MSG, id, code);
        }
        /**
         * This is called by the dictionary service regularly to tell us about
         * new values.  Note that IPC calls are dispatched through a thread
         * pool running in each process, so the code executing here will
         * NOT be running in our main thread like most other things -- so,
         * to update the UI, we need to use a Handler to hop over there.
         */

    };


    private static final int COMPILATION_MSG = 1;
    private static final int ERROR_MSG = 2;

    private Handler mHandler = new Handler() {
        @Override public void handleMessage(Message msg) {
            switch (msg.what) {
                case COMPILATION_MSG:
                    if(mCallback != null) mCallback.onCompilation(msg.arg1, (Bundle) msg.obj);
                    break;
                case ERROR_MSG:
                    if(mCallback != null) mCallback.onError(msg.arg1, msg.arg2);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }

    };


    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mTarget = IDictionaryInterface.Stub.asInterface(service);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

}
