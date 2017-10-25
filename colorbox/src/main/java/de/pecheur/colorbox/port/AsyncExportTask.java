package de.pecheur.colorbox.port;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import de.pecheur.colorbox.database.columns.WordColumn;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import de.pecheur.colorbox.R;
import de.pecheur.colorbox.database.VocabularyProvider;
import de.pecheur.colorbox.models.Unit;



// TODO better progress indicator
// TODO SD-card unmounted unhandled

public abstract class AsyncExportTask extends AsyncTask<Unit, Integer, Boolean> {
    private ContentResolver cr;
    private ProgressDialog pd;
    protected Context context;
    protected File output;
    private XmlSerializer serializer;
    private ZipOutputStream zip;


    public AsyncExportTask(Context context, File out) {
        this.context = context;
        output = out;
        cr = context.getContentResolver();
        serializer = android.util.Xml.newSerializer();
    }


    @Override
    protected void onPreExecute() {
        pd = new ProgressDialog(context);
        pd.setTitle(R.string.export_dialog_title);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setCancelable(false);
        pd.setCanceledOnTouchOutside(false);
        pd.setProgressNumberFormat(context.getString(
                R.string.export_dialog_progress));
        pd.show();


    }


    @Override
    protected Boolean doInBackground(Unit... units) {
        if(units == null || units.length == 0) {
            units = queryAllUnits();
        }

        pd.setMax(units.length);

        try {
            ArrayList<Integer> res = new ArrayList<Integer>();

            // create zip stream
            zip = new ZipOutputStream(new FileOutputStream(output));
            zip.putNextEntry(new ZipEntry("content.xml"));

            serializer.setOutput(zip, "UTF-8");
            serializer.startDocument("UTF-8", true);
            serializer.startTag("", "units");
            for(Unit unit : units) {
                // query words
                Cursor c = context.getContentResolver().query(
                        VocabularyProvider.WORD_URI,
                        null,
                        WordColumn.UNIT + " = ?",
                        new String[]{String.valueOf(unit.getId())},
                        null);

                // write words & unit to content file
                serializer.startTag("", Xml.UNIT);
                serializer.attribute("", Xml.TITLE, unit.getTitle());
                serializer.attribute("", Xml.FRONT, unit.getFrontCode());
                serializer.attribute("", Xml.BACK, unit.getBackCode());


                for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
                    // word tag
                    serializer.startTag("", Xml.WORD);
                    // front tag
                    serializer.startTag("", Xml.FRONT);
                    // text
                    serializer.startTag("", Xml.TEXT);
                    serializer.text( c.getString( 2));
                    serializer.endTag("", Xml.TEXT);

                    // audio
                    if(!c.isNull(4)) {
                        serializer.startTag("", Xml.AUDIO);
                        serializer.text(c.getString(4));
                        serializer.endTag("", Xml.AUDIO);
                        res.add(c.getInt(4));
                    }
                    serializer.endTag("", Xml.FRONT);

                    // back tag
                    serializer.startTag("", Xml.BACK);
                    // text
                    serializer.startTag("", Xml.TEXT);
                    serializer.text( c.getString( 3));
                    serializer.endTag("", Xml.TEXT);

                    // audio
                    if(!c.isNull(5)) {
                        serializer.startTag("", Xml.AUDIO);
                        serializer.text(c.getString(5));
                        serializer.endTag("", Xml.AUDIO);
                        res.add(c.getInt(5));
                    }

                    serializer.endTag("", Xml.BACK);
                    serializer.endTag("", Xml.WORD);

                    publishProgress();  // increment progress
                }
                serializer.endTag("", Xml.UNIT);
                c.close();
            }
            serializer.endTag("", "units");
            serializer.endDocument();
            zip.closeEntry();

/*
            // adding resources
            for(Integer r : res) {
                Uri uri = ContentUris.withAppendedId(
                        VocabularyProvider.FILE_URI, r);

                InputStream is = context.getContentResolver().openInputStream(uri);
                BufferedInputStream buffer = new BufferedInputStream(is, 2048);

                String name = String.valueOf(r);
                zip.putNextEntry(new ZipEntry(name));

                byte data[] = new byte[1024];
                while(buffer.read(data, 0, 1024) != -1) {
                    zip.write(data);
                }
                is.close();
                zip.closeEntry();
            }
*/
            zip.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        pd.dismiss();
         return true;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        pd.incrementProgressBy(1);
     }


    @Override
    protected void onPostExecute(Boolean result) {
        if(!result) new AlertDialog.Builder(context)
            .setTitle(R.string.export_dialog_title)
            .setMessage(R.string.export_dialog_error)
            .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            })
            .create()
            .show();
    }

    private Unit[] queryAllUnits() {
        // load units
        Cursor c = context.getContentResolver().query(
                VocabularyProvider.UNIT_URI,
                null,
                null,
                null,
                null);

        Unit[] units = new Unit[c.getCount()];
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            Unit unit = new Unit(c.getLong(0));
            unit.setTitle(c.getString(1));
            unit.setFrontCode(c.getString(2));
            unit.setBackCode(c.getString(3));
            units[c.getPosition()] = unit;
        }
        c.close();

        Log.d("units", units.toString());
        return units;
    }
 }
 