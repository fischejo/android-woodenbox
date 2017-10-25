package de.pecheur.colorbox.port;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import de.pecheur.colorbox.database.VocabularyProvider;
import de.pecheur.colorbox.models.Word;

public class AsyncImportTask extends AsyncTask<UnitMap, Integer, Integer> {

    private ContentResolver cr;
    private ProgressDialog pd;
    private Context context;

    public AsyncImportTask(Context context) {
        this.context = context;
        cr = context.getContentResolver();
    }

    @Override
    protected void onPreExecute() {
        pd = new ProgressDialog(context);
        pd.setTitle("Importieren");
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setCancelable(false);
        pd.setCanceledOnTouchOutside(false);
        pd.setProgressNumberFormat("%1d/%2d Vokabeln");
        pd.show();
    }

    protected Integer doInBackground(UnitMap... units) {
        int total = 0;


        for(UnitMap unit : units)
            total+=unit.getContent().size();
        pd.setMax(total);

        for (int u = 0; u < units.length; u++) {
            // insert unit
            UnitMap unit = units[u];
            Uri unitId = cr.insert(VocabularyProvider.UNIT_URI,
                    unit.getContentValues());
            unit.setId(ContentUris.parseId(unitId));

            // insert words
            List<Word> words = unit.getContent();

            for(int w = 0; w < words.size(); w++) {
                Word word = words.get(w);
                /*
                try {
                    if (word.hasAudio(true))
                        word.setAudio(insertFile(word.getAudio(true)));

                    if (word.hasAudio(false))
                        word.setAudio(insertFile(word.getAudio(false)));

                    cr.insert(VocabularyProvider.WORD_URI, word.getContentValues());
                    publishProgress();
                } catch (IOException e) {

                    e.printStackTrace();
                }
                */
            }
         }
         return total-pd.getProgress();
     }

     protected void onProgressUpdate(Integer... progress) {
        pd.incrementProgressBy(1);
     }

    protected void onPostExecute(Integer result) {
        Log.d("error-import", result + "");
        pd.dismiss();

        if(result > 0) {
            new AlertDialog.Builder(context)
                    .setTitle("Importieren")
                    .setMessage("Die Datei ist fehlerhaft und es konnten "+ result +
                    "Vokabeln nicht hinzugefügt werden")
                    .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create()
                    .show();
        }
     }

    /*
    private Uri insertFile(Uri uri) throws IOException {
        Uri id = cr.insert(VocabularyProvider.FILE_URI,null);
        OutputStream os = cr.openOutputStream(id);
        InputStream is = cr.openInputStream(uri);
        copy(is, os);
        os.close();
        is.close();
        return id;
    }
*/

    private int copy(InputStream in, OutputStream out) throws IOException {
        int count = 0;
        int read = 0;
        byte[] buffer = new byte[1024];
        while (read != -1) {
            read = in.read(buffer);
            if (read != -1) {
                count += read;
                out.write(buffer,0,read);
            }
        }
        return count;
    }
 }
 