package de.pecheur.colorbox.unit;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

import de.pecheur.colorbox.models.Unit;
import de.pecheur.colorbox.settings.SettingsActivity;
import de.pecheur.colorbox.detail.DetailActivity;
import de.pecheur.colorbox.detail.DetailFragment;
import de.pecheur.colorbox.R;


public class UnitListActivity extends FragmentActivity implements UnitSelectionListener {

    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.unit_list_activity);
        setTitle(R.string.unit_activity_title);
        
        PreferenceManager.setDefaultValues(this, R.xml.box_preferences, false);
    }
    

    @Override
	public void onUnitSelected(Unit unit) {
		DetailFragment detailFragment = (DetailFragment) getSupportFragmentManager()
				.findFragmentById(R.id.word_fragment);
		
		if (detailFragment != null && detailFragment.isInLayout()) {
			detailFragment.onUnitSelected( unit);
		} else {
			Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra(DetailActivity.EXTRA_UNIT, unit);
            startActivity(intent);
		}
	}
    
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.unit_activity_options, menu);
		return true;
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
     		case R.id.unit_activity_menu_settings:
     			startActivity(new Intent( this, SettingsActivity.class));
                return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
