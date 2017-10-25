package de.pecheur.colorbox.unit;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import de.pecheur.colorbox.database.VocabularyProvider;
import de.pecheur.colorbox.database.columns.UnitColumn;

import java.util.Locale;


class NavigationAdapter extends SimpleCursorAdapter {

        public NavigationAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_1, null,
                    new String[] {UnitColumn.BACK},
                    new int[] {android.R.id.text1},
                    CursorAdapter.FLAG_AUTO_REQUERY);

            changeCursor(context.getContentResolver().query(
                    VocabularyProvider.UNIT_URI,
                    new String[]{UnitColumn._ID + " AS " + BaseColumns._ID, UnitColumn.BACK},
                    null,
                    null,
                    UnitColumn.FRONT + " || " + UnitColumn.BACK));

        }


        @Override
        public void setViewText(TextView v, String text) {
            text = new Locale(text).getDisplayLanguage();
            super.setViewText(v, text);
        }

        @Override
        public String getItem(int position) {
            Cursor c = getCursor();
            c.moveToPosition(position);
            return c.getString(1);
        }
    }