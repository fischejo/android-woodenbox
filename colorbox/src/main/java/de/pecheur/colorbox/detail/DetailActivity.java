package de.pecheur.colorbox.detail;

import android.app.ActionBar;
import android.content.ContentUris;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MenuItem;

import de.pecheur.colorbox.database.columns.UnitColumn;
import de.pecheur.colorbox.database.VocabularyProvider;
import de.pecheur.colorbox.models.Unit;
import de.pecheur.colorbox.R;

/**
 * An extra Activity, containing WordListFragment.
 * 
 * @author Johannes Fischer
 * @version 1.0
 */

public class DetailActivity extends FragmentActivity {
	public static final String EXTRA_UNIT = "unit";

	private Unit mUnit;
	
	private ContentObserver unitObserver = new ContentObserver(null) {
		@Override
		public void onChange(boolean selfChange) {
			// reload unit
			Cursor c = getContentResolver().query(
					VocabularyProvider.UNIT_URI,
					null, 
					UnitColumn._ID +" = ?",
					new String[] {String.valueOf(mUnit.getId())},
					null);
			
			if ( c != null && c.moveToFirst()) {
				Unit unit = new Unit( c.getLong(0));
				unit.setTitle(c.getString(1));
				unit.setFrontCode(c.getString(2));
				unit.setBackCode(c.getString(3));
				c.close();	
				setUnit(unit);
			} else {
				finish();
			}
		}
	};
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	   
	   setContentView(R.layout.word_list_activity);
	 
	    ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    actionBar.setIcon(android.R.color.transparent);
	    
	    mUnit = getIntent().getParcelableExtra(EXTRA_UNIT);
	    setUnit(mUnit);


        Cursor c = getContentResolver().query(VocabularyProvider.TEXT_URI, null, null, null, null);
        for ( c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            String a = "";
            for(int i = 0; i < c.getColumnCount(); i++) {
                a += c.getString(i) + ", ";
            }
            Log.d("p1", a);
        }
	}
	
	
	@Override
    public void onStart() {
		super.onStart();
		
		// register unit observer for title changes
	    getContentResolver().registerContentObserver(
	    		ContentUris.withAppendedId(VocabularyProvider.UNIT_URI, mUnit.getId()),
	    		false,
	    		unitObserver);
	
	}
	
	
	@Override
	public void onStop() {
		super.onStop();

		// unregister word observer
		getContentResolver().unregisterContentObserver(unitObserver);
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem nItem) {
		switch(nItem.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
		}
		return false;
	}	
	

	private void setUnit(Unit unit) {
			ActionBar ab = getActionBar();
			ab.setTitle(unit.getTitle());		
			ab.setSubtitle( getString(
					R.string.word_activity_subtitle, 
					unit.getFrontLanguage(),
					unit.getBackLanguage()));
	}
}
