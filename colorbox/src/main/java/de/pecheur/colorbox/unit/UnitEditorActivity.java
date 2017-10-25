package de.pecheur.colorbox.unit;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.Locale;

import de.pecheur.colorbox.database.columns.UnitColumn;
import de.pecheur.colorbox.database.VocabularyProvider;
import de.pecheur.colorbox.R;
import de.pecheur.colorbox.models.Unit;

// TODO increase language selection; custom types are missing
// TODO "create shortcut"-checkbox option

public class UnitEditorActivity extends Activity implements OnClickListener {
	public static final int REQUEST_CODE = 1;
    public static final int RESULT_DELETE = 3;
	public static final String EXTRA_UNIT = "unit";


	private static final String[] defaultLanguages = {
		"de","en","fr","es","it","ro","ko","hi","ar","sq",
		"bg","zh","cs","da","el","hu","ja","nl","no","pl",
		"pt","ru","sv","sl","th","tr"
	};


	private EditText mTitle;
	private Spinner frontSpinner;
	private Spinner backSpinner;
	private Unit mUnit;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.unit_editor_activity);

        // set up custom done-actionbar
        ActionBar actionBar = getActionBar();
        
        // inflate custom layout & set as actionbar
		LayoutInflater inflater = (LayoutInflater) getActionBar().getThemedContext()
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		View actionBarView = inflater.inflate(R.layout.unit_editor_actionbar, null);
		actionBarView.findViewById(R.id.actionbar_done).setOnClickListener(this);
		actionBar.setCustomView( actionBarView);
		
		// hide the normal Home icon and title.
		actionBar.setDisplayOptions( 
				ActionBar.DISPLAY_SHOW_CUSTOM,
				ActionBar.DISPLAY_SHOW_CUSTOM |
				ActionBar.DISPLAY_SHOW_HOME |
				ActionBar.DISPLAY_SHOW_TITLE);

		
		// find views
		mTitle = (EditText) findViewById(R.id.title);
     	frontSpinner = (Spinner) findViewById(R.id.a_language);
     	backSpinner = (Spinner) findViewById(R.id.b_language);
        Button deleteButton = (Button) findViewById(R.id.delete);
     	
     	// language spinner adapters
     	ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this,
                android.R.layout.simple_spinner_item);
     	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        for(String code : defaultLanguages)
     		adapter.add( new Locale(code).getDisplayName());

     	frontSpinner.setAdapter(adapter);
     	backSpinner.setAdapter(adapter);
     	
    
        // load unit
        mUnit = getIntent().getParcelableExtra(EXTRA_UNIT);
     	
        if (mUnit != null) {
        	if (mUnit.hasId()) {
                // show delete button
                deleteButton.setVisibility(View.VISIBLE);
                deleteButton.setOnClickListener(this);
        	}
        	
	     	// set title
	     	mTitle.setText( mUnit.getTitle());
	     	
	     	// set languages
	     	int index = indexOfIsoCode( mUnit.getFrontCode());
	     	frontSpinner.setSelection(index);
	        frontSpinner.setEnabled(index == -1);
        		
	        index = indexOfIsoCode( mUnit.getBackCode());
	     	backSpinner.setSelection(index);
	        backSpinner.setEnabled(index == -1);
     	} else {
            mUnit = new Unit();
        }

	}
	

	@Override
	public void onClick(View view) {
        switch(view.getId()) {
            case R.id.actionbar_done:
                save();
                finish();
                break;

            case R.id.delete:
                delete();
                break;
        }
	}


    private void delete() {
        new AlertDialog.Builder( this)
            .setMessage(getString(R.string.unit_editor_dialog_delete, mUnit.getTitle()))
            .setNegativeButton(android.R.string.no, null)
            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // delete unit in database
                    getContentResolver().delete(mUnit.getUri(), null, null);
                    Unit.removeShortcut(UnitEditorActivity.this, mUnit);
                    setResult(RESULT_DELETE);
                    finish();
                }
            })
            .create()
            .show();
    }


    private void save() {
        String title = mTitle.getText().toString();

        // creating an unit without title is invalid
        if (title.isEmpty()) {
            setResult(RESULT_CANCELED);
            return;
        }

        mUnit.setTitle(title);

        // store language codes
        if(!mUnit.hasId()) {
            String front = defaultLanguages[frontSpinner.getSelectedItemPosition()];
            String back = defaultLanguages[backSpinner.getSelectedItemPosition()];

            mUnit.setFrontCode(front.compareToIgnoreCase(back) < 0 ? front : back);
            mUnit.setBackCode(front.compareToIgnoreCase(back) < 0 ? back : front);
        }

        // insert or update
        if (mUnit.hasId()) {
            getContentResolver().update(
                    VocabularyProvider.UNIT_URI,
                    mUnit.getContentValues(),
                    UnitColumn._ID +" = ?",
                    new String[] {String.valueOf(mUnit.getId())});
        } else {
            getContentResolver().insert(
                    VocabularyProvider.UNIT_URI,
                    mUnit.getContentValues());
        }

        // return unit
        Intent result = new Intent();
        result.putExtra(EXTRA_UNIT, mUnit);
        setResult(RESULT_OK, result);
    }


	private int indexOfIsoCode( String iso ) {
	    for ( int i = 0; i < defaultLanguages.length; i++ ) {
	        if ( defaultLanguages[i].equals( iso)) return i;
	    }
	    return -1;
	}
}
