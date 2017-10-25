package de.pecheur.colorbox.detail;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

import de.pecheur.colorbox.port.AsyncExportTask;
import de.pecheur.colorbox.R;


public class AsyncShareTask extends AsyncExportTask {
    public AsyncShareTask(Context context) throws IOException {
        super(context, File.createTempFile("share-",".zip",
            Environment.getExternalStorageDirectory()));
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        if(result) {
            Uri uri = Uri.fromFile(output);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.setType("application/.zip");
            context.startActivity(Intent.createChooser(shareIntent,
                    context.getString(R.string.share_dialog_title)));
        }
    }

}
