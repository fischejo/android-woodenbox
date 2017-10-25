package de.pecheur.colorbox.detail;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;

import de.pecheur.colorbox.database.SqliteUtils;
import de.pecheur.colorbox.database.columns.UnitColumn;
import de.pecheur.colorbox.database.VocabularyProvider;
import de.pecheur.colorbox.database.columns.WordColumn;
import de.pecheur.colorbox.models.Unit;
import de.pecheur.colorbox.R;


class WordDialog {
	
	/**
	 * Dialog for moving words to another unit with similar language codes
	 * @param ids of words, which should be treated
	 */
	 static Builder MoveWordBuilder(final Context context, Unit unit, final long[] ids) {

		// define query
	    String[] projection = {
	    		UnitColumn._ID + " AS "+ BaseColumns._ID,
	    		UnitColumn.TITLE };
	    
	    String selection =
	    		// units with similar first language
	    		UnitColumn.FRONT +" = ? AND "+
	    		UnitColumn.BACK +" = ? AND "+
	    		UnitColumn._ID + " != ?";
	    
	    String[] selectionArgs = {
	    		unit.getFrontCode(),
	    		unit.getBackCode(),
	    		String.valueOf(unit.getId())};
				
	    // query
		Cursor cursor = context.getContentResolver().query(
				VocabularyProvider.UNIT_URI,
				projection, 
				selection, 
				selectionArgs, 
				null);
		
		
		
		// check if there are available sections
		if (cursor == null || cursor.getCount() == 0) {
			return new AlertDialog.Builder( context)
			.setMessage(R.string.word_dialog_move_not_available)
			.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) {}
			});

		} else {
			// create adapter with query results
			final CursorAdapter adapter = new SimpleCursorAdapter(
					context,
					android.R.layout.simple_list_item_1,
					cursor,
					new String[] { UnitColumn.TITLE },
					new int[] { android.R.id.text1 },
					CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

			// create list dialog with adapter
			return new AlertDialog.Builder( context)
			.setTitle(R.string.word_dialog_move_title)
			.setCursor(cursor, new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) {
		        	// set new unit id (like moving to another unit)
		        	ContentValues values = new ContentValues();
					values.put( WordColumn.UNIT, adapter.getItemId(which));

					// update card entry
		        	context.getContentResolver().update(
		        			VocabularyProvider.WORD_URI,
		        			values,
		        			WordColumn._ID +" IN ("+ SqliteUtils.convertIDs(ids) +")" ,
		        			null);
		        }}, UnitColumn.TITLE)
		    .setNegativeButton(android.R.string.cancel, null);
		}
	}



	/**
	 * @param ids of words, which should be deleted
	 */
	 static Builder DeleteWordsBuilder(final Context context, final long[] ids) {
		// create dialog message
		String message = context.getResources().getQuantityString(
				R.plurals.word_dialog_delete,
				ids.length, ids.length);

		// create dialog
		return new AlertDialog.Builder( context)
		.setMessage(message)
	    .setNegativeButton(android.R.string.cancel, null)
	    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) {
	        	// delete card in database
	        	context.getContentResolver().delete(
	        			VocabularyProvider.WORD_URI,
	        			WordColumn._ID +" IN ("+ SqliteUtils.convertIDs(ids) +")" ,
	        			null);
	        }
		});
	}


	/**
	 * @param ids of words, whose progress should be cleared (set box back to 0)
	 */
	 static Builder UndoWordBuilder(final Context context, final long[] ids) {
		// create dialog message
		String message = context.getResources().getQuantityString(
				R.plurals.word_dialog_undo,
				ids.length, ids.length);

		// create dialog
		return new AlertDialog.Builder( context)
		.setMessage(message)
	    .setNegativeButton(android.R.string.cancel, null)
	    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	    	 public void onClick(DialogInterface dialog, int which) {
		        	ContentValues values = new ContentValues();
					values.put(WordColumn.BOX, WordColumn.MIN_BOX_VALUE);	// box
					values.put(WordColumn.TIME, 0);	// time bug fixing
					
					// update database
					context.getContentResolver().update(
							VocabularyProvider.WORD_URI, 
							values, WordColumn._ID +" IN ("+ SqliteUtils.convertIDs(ids) +")" ,
							null);
				}
		});
	}

}
