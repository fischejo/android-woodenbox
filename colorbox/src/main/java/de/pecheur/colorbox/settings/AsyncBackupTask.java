package de.pecheur.colorbox.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

import de.pecheur.colorbox.port.AsyncExportTask;
import de.pecheur.colorbox.R;

public class AsyncBackupTask extends AsyncExportTask {
    public AsyncBackupTask(Context context) throws IOException {
        super(context, File.createTempFile("backup-",".zip",
                Environment.getExternalStorageDirectory()));
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);    // handle errors
        if(result) new AlertDialog.Builder(context)
            .setTitle(R.string.backup_dialog_title)
            .setMessage(context.getString(
                    R.string.backup_dialog_message,
                    output.getName(),
                    output.getParent()))
            .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                }
            })
            .create()
            .show();
    }
}
