package de.pecheur.colorbox.settings;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import de.pecheur.colorbox.R;
import de.pecheur.colorbox.card.CardActivity;
import de.pecheur.colorbox.database.columns.UnitColumn;
import de.pecheur.colorbox.database.VocabularyProvider;
import de.pecheur.colorbox.database.columns.WordColumn;
import de.pecheur.colorbox.unit.UnitListActivity;


// TODO learning aim (for example: you need to learn 30 vocabulary today)
// TODO remove notification, when app is running

public class NotificationService extends Service {
    private static final String ACTION_SNOOZE = "snooze";
    private static final int NOTIFICATION_ID = 1;

    private static final int REQUEST_CODE_SNOOZE = 1;
    public static final int REQUEST_CODE_DAILY = 0;

    private static final long SNOOZE_TIME = 1000*60*60;

    public int onStartCommand(Intent intent, int flags, int startId) {
        if(ACTION_SNOOZE.equals(intent.getAction())) {
            PendingIntent pendingIntent = PendingIntent.getService(this, 0,
                    new Intent(this, NotificationService.class), 0);

            ((AlarmManager) getSystemService(Context.ALARM_SERVICE)).set(
                    AlarmManager.RTC,
                    System.currentTimeMillis()+SNOOZE_TIME,
                    pendingIntent);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                    .cancel(NOTIFICATION_ID);
        } else {

            showNotification();
        }

        return START_NOT_STICKY;
    }


    private void showNotification() {
        int boxCount = Settings.getBoxCount(this);

        // build available-where string for available-count query
        String available = System.currentTimeMillis() +"- CASE "+ WordColumn.BOX +
                " WHEN 0 THEN '0'";
        for(int i = 1; i < boxCount; i++)
            available += " WHEN "+i+" THEN '"+Settings.getBoxDelay(this, i)+"'";
        available += " ELSE '"+Settings.getBoxDelay(this, boxCount)+"' END";

        String selection = "(SELECT COUNT(*)"+
                " FROM "+ WordColumn.TABLE +
                " WHERE " + WordColumn.UNIT + " = "+ UnitColumn._ID+
                " AND "+ WordColumn.TIME + " < " + available +")"+
                " > " + Settings.getQueryCount(this);

        Cursor c = getContentResolver().query(
                VocabularyProvider.UNIT_URI,
                new String[]{"COUNT(*)"},
                selection,
                null,
                null);

        int count = c.moveToFirst() ? c.getInt(0) : 0;
        c.close();

        if(count > 0) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.stat_notify_query)
                    .setContentTitle(getString(R.string.notification_title))
                    .setContentText(getString(R.string.notification_text))
                    .setContentInfo(String.valueOf(count))
                    .setAutoCancel(true)
                    .setOnlyAlertOnce(true)
                    .setContentIntent(getContentIntent())
                    .addAction(
                            R.drawable.ic_action_alarms,
                            getString(R.string.notification_snooze),
                            getSnoozeIntent());

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                    .notify(NOTIFICATION_ID, builder.build());
        }

    }


    private PendingIntent getSnoozeIntent() {
        Intent intent = new Intent(this, NotificationService.class);
        intent.setAction(ACTION_SNOOZE);
        return PendingIntent.getService(this, REQUEST_CODE_SNOOZE, intent, 0);
    }


    private PendingIntent getContentIntent() {
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, UnitListActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity. This ensures that navigating backward from the Activity
        // leads out of your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(CardActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}