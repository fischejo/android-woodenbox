package de.pecheur.colorbox.detail;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;
import android.view.ActionProvider;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.pecheur.colorbox.database.VocabularyProvider;
import de.pecheur.colorbox.models.Unit;
import de.pecheur.colorbox.R;

// TODO change package to de.pecheur.woodenbox
// TODO handle onActivityResult in a seperate thread
// TODO file-handling not implemented

public class AddonActionProvider extends ActionProvider implements MenuItem.OnMenuItemClickListener {
    private static final String ACTION = "com.github.woodenbox.DICTIONARY";
    private static final String RESULT = "com.github.colorbox.RESULT";
    private static final String TYPE = "language/%1$s";
    private static final String FRONT = "front";
    private static final String BACK = "back";
    private static final String ID = "id";
    private static final String TITLE = "title";
    private static final String COLOR = "color";

    private static final int REQUEST_CODE = 2;


    private Activity mActivity;
    private Intent mIntent;
    PackageManager mPackageManager;

    public AddonActionProvider(Context context) {
        super(context);
        mPackageManager = context.getPackageManager();
    }

    public void setUnit(Unit unit) {
        if(unit == null) {
            mIntent = null;
            return;
        }

        mIntent = new Intent();
        mIntent.setAction(ACTION);
        mIntent.setType(String.format(TYPE, unit.getCode()));
        mIntent.putExtra(FRONT, unit.getFrontCode());
        mIntent.putExtra(BACK, unit.getBackCode());
        mIntent.putExtra(ID, unit.getId());
        mIntent.putExtra(TITLE, unit.getTitle());
        mIntent.putExtra(COLOR, mActivity.getResources().getColor(
                unit.getColor(mActivity)));
    }

    public void setActivity(Activity activity) {
        Log.d("addon", "setActivity");
        mActivity = activity;
    }
    @Override
    public View onCreateActionView() {
        return null;
    }

    @Override
    public boolean hasSubMenu() {
        if(mIntent == null) {
            Log.e("AddonActionProvider", "setUnit(...) need to be called before hasSubMenu()");
            return false;
        }

        List<ResolveInfo> addons = mPackageManager.queryIntentActivities(
                mIntent, PackageManager.MATCH_DEFAULT_ONLY);

        return !addons.isEmpty();
    }

    @Override
    public void onPrepareSubMenu(SubMenu subMenu){
        subMenu.clear();

        List<ResolveInfo> addons = mPackageManager.queryIntentActivities(
                mIntent, PackageManager.MATCH_DEFAULT_ONLY);

        // add subitems
        for (int i = 0; i < addons.size(); i++) {
            ResolveInfo info = addons.get(i);
            ActivityInfo activity = info.activityInfo;
            Intent dicIntent = new Intent(mIntent);
            // set component
            dicIntent.setComponent(new ComponentName(
                activity.applicationInfo.packageName,
                activity.name));

            MenuItem subItem = subMenu.add(info.loadLabel(mPackageManager));
            subItem.setIcon(info.loadIcon(mPackageManager));
            subItem.setIntent(dicIntent);
            subItem.setOnMenuItemClickListener(this);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        mActivity.startActivityForResult(item.getIntent(), REQUEST_CODE);
        return true;
    }


    public static void onActivityResult(Context context, int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && data != null) {
            ArrayList<ContentValues> list = data.getParcelableArrayListExtra(RESULT);
            if (list != null && !list.isEmpty()) {
                ContentValues[] values = list.toArray(new ContentValues[list.size()]);

                int count = context.getContentResolver().bulkInsert(VocabularyProvider.WORD_URI, values);

                if (count == values.length) {
                    Toast.makeText(context, context.getResources().getQuantityString(
                                    R.plurals.addon_success_toast, count, count),
                             Toast.LENGTH_SHORT
                     ).show();
                } else {
                    Toast.makeText(context, R.string.addon_error_toast,
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}