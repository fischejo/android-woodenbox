package de.pecheur.colorbox.card;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ActionBar;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import de.pecheur.colorbox.R;
import de.pecheur.colorbox.database.columns.UnitColumn;
import de.pecheur.colorbox.database.VocabularyProvider;
import de.pecheur.colorbox.database.columns.WordColumn;
import de.pecheur.colorbox.settings.Settings;
import de.pecheur.colorbox.models.Unit;


public class CardActivity extends Activity implements OnItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor>,CardView.OnItemSettleListener, OnItemClickListener {
	public static final String EXTRA_UNIT = "unit";
	public static final String EXTRA_UNIT_ID = "unit_id";
	
	private static final int LOADER_ID = 7;
	
	private static final int SIDE_GROUP = 201;
	private static final int SIDE_FRONT = 101;
	private static final int SIDE_BACK = 102;
	private static final int SIDE_BOTH = 103;
	

	private CardView mCardView;
	private CardAdapter mAdapter;
	
	private SharedPreferences mPreferences;
	private int mSide;
	private String mOption;
	
	
	// section-data
	private Unit mUnit;
	private int mBox = 0;
	


	@Override
	public void onCreate(Bundle nBundle) {
		super.onCreate(nBundle);
	
		// layout
		setContentView(R.layout.card_activity);

		
		// handle intent and load unit
		if(!handleIntent(getIntent())) {
			Toast.makeText(this, R.string.card_error_toast, Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		
		// actionbar
		ActionBar ab = getActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setTitle(mUnit.getTitle());

		
		// preferences
		mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		mSide = mPreferences.getInt(mUnit.getCode(), Card.FRONT);
		mOption = Settings.getQueryOption(this);
		
		
		// box spinner navigation
		BoxSpinner spinner = new BoxSpinner(ab.getThemedContext());
		spinner.setAdapter(new BoxAdapter(ab.getThemedContext(), mUnit));
		spinner.setOnItemSelectedListener(this);

		ab.setCustomView(spinner);
		ab.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP|
				ActionBar.DISPLAY_SHOW_CUSTOM |
				ActionBar.DISPLAY_SHOW_HOME |
				ActionBar.DISPLAY_SHOW_TITLE);

		
		// CardAdapter
		mAdapter = new CardAdapter(this, mUnit, Settings.getQueryCount(this));

		
		
		// CardView
	    mCardView = (CardView) findViewById(R.id.cardView);
	    mCardView.setOnItemSettleListener(this);
	    mCardView.setAdapter(mAdapter);
	    mCardView.setEmptyView( findViewById(R.id.emptyView));
	    mCardView.setOnItemClickListener(this);
	}

	
	/**
	 * handles the intent and tries to load the unit. 
	 * @param intent
	 * @return true, if loading was successful, false, if unit does
	 * not exist.
	 */
	private boolean handleIntent(Intent intent) {
		if(intent.hasExtra(EXTRA_UNIT)) {
			mUnit = intent.getParcelableExtra(EXTRA_UNIT);
		} else if (intent.hasExtra(EXTRA_UNIT_ID)) {
			mUnit = new Unit(intent.getLongExtra(EXTRA_UNIT_ID, Unit.INVALID_ROW_ID));
		} else {
			return false;
		}
		
		
    	if (!mUnit.hasId())
    		return false;
    	
    	String[] projection = {
				UnitColumn.TITLE,
				UnitColumn.FRONT,
				UnitColumn.BACK};

		Cursor c = getContentResolver().query(
				VocabularyProvider.UNIT_URI,
				projection, 
				UnitColumn._ID + " = ?",
				new String[] { String.valueOf(mUnit.getId())},
				null);
			
		if(c == null || !c.moveToFirst())
			return false;

		mUnit.setTitle(c.getString(0));
		mUnit.setFrontCode(c.getString(1));
		mUnit.setBackCode(c.getString(2));
		c.close();
		return true;
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if(mUnit == null) return false;
		
    	getMenuInflater().inflate(R.menu.card_options, menu);
    	
    	// side submenu
		Resources res = getResources();
		String front = mUnit.getFrontLanguage();
		String back = mUnit.getBackLanguage();
		
		
		SubMenu sideMenu = menu.findItem(R.id.card_side).getSubMenu();
    	// front
    	sideMenu.add(SIDE_GROUP, SIDE_FRONT, Menu.NONE,
    			res.getString(R.string.card_side_front, front,back))
    			.setCheckable(true)
    			.setChecked(mSide == Card.FRONT);
    	// back
    	sideMenu.add(SIDE_GROUP, SIDE_BACK, Menu.NONE,
    			res.getString(R.string.card_side_back, front,back))
    			.setCheckable(true)
    			.setChecked(mSide == Card.BACK);
    	// both
    	sideMenu.add(SIDE_GROUP, SIDE_BOTH, Menu.NONE,
    			res.getString(R.string.card_side_both, front,back))
    			.setCheckable(true)
    			.setChecked(mSide == Card.BOTH);
    	
    	sideMenu.setGroupCheckable(SIDE_GROUP, true, true);
		return true;
    }
    

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;

			case SIDE_FRONT:
				setSide(Card.FRONT);
				item.setChecked(true);
				return true;
				
			case SIDE_BACK:
				setSide(Card.BACK);
				item.setChecked(true);
				return true;
				
			case SIDE_BOTH:
				setSide(Card.BOTH);
				item.setChecked(true);
				return true;
		}
		return false;
	}	

	
	
	private void setSide(int side) {
		mSide = side;
		
		mPreferences.edit()
		.putInt(mUnit.getCode(), side)
		.commit();
		
		// notify adapter
		mAdapter.setSide(side);
	}
	

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		mBox = (Integer) parent.getItemAtPosition(position);
		// init or restart async word loader
		LoaderManager loaderManager = getLoaderManager();
		if (loaderManager.getLoader( LOADER_ID) == null) {
			loaderManager.initLoader( LOADER_ID, null, this);
		} else {
			loaderManager.restartLoader(LOADER_ID, null, this);
		}
	}

	
	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
	}

	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		// time 
		long delay = Settings.getBoxDelay(this, mBox);
		String time = String.valueOf(System.currentTimeMillis() - delay);
		
		// load section
		String[] projection = {
			WordColumn._ID,
			WordColumn.BOX};
				
		String selection = 	WordColumn.UNIT +" =  ? AND "+
					WordColumn.BOX + " = ? AND "+
					WordColumn.TIME + " < ?";

		String[] args = new String[] {
				String.valueOf(mUnit.getId()),
				String.valueOf(mBox),
				time};
		
		// we set the card limit to 1000. I want to meet the
		// person, who learns more than 1000 cards at a stretch.
		
		int limit = 1000;
		
		// SimpleCursorLoader does not implement an observer
		return new SimpleCursorLoader( 
				this,
				VocabularyProvider.WORD_URI, 
 				projection, 
 				selection, 
 				args, 
 				WordColumn._ID +" LIMIT " + limit);
	}

	
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
		mAdapter.clear();
		ArrayList<Card> cards = new ArrayList<Card>(c.getCount());
		for ( c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
			Card card = new Card(c.getLong(0));
			card.setBox(c.getInt(1));
			
			//if(!c.isNull(4)) card.setAudio(c.getLong(4), true);
			//if(!c.isNull(5)) card.setAudio(c.getLong(5), false);
			cards.add(card);
		}
		mAdapter.addAll(cards);
	}

	
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.clear();
	}
	
	
	@Override
	public void onItemUp(AdapterView<?> parent, View view, int position, long id) {
		Card card = mAdapter.getItem(position);
		card.correct();

		if(card.isFinished(mSide)) {
			// remove box
			mAdapter.remove(card);
			
			// increase box
			card.setBox(Math.min(
                    WordColumn.MAX_BOX_VALUE,    // need fix
                    card.getBox() + 1));
			
			// update box
			updateCard(card);
		} else {
			/* turn the card, because the 
			 * other side still need to be
			 * queried.
			 */
			card.setSide(!card.isFront());
		}
	}


	@Override
	public void onItemDown(AdapterView<?> parent, View view, int position, long id) {
		Card card = mAdapter.getItem(position);
		card.incorrect();
		
		// handle incorrectness
		if(Settings.OPTION_BEGINNING.equals(mOption)) {
			card.setBox(WordColumn.MIN_BOX_VALUE);
		} else if(Settings.OPTION_BACK.equals(mOption)) {
			card.setBox(Math.max(
                    WordColumn.MIN_BOX_VALUE,
                    card.getBox() - 1));
		}
		
		// box changed?
		if(card.getBox() != mBox) {
			mAdapter.remove(card);
			updateCard(card);
		}
	}

	
	private void updateCard(Card card) {
		// need to be fixed to doInBackground in a worker thread maybe.
		ContentValues values = new ContentValues();
		values.put(WordColumn.BOX, card.getBox());
		values.put(WordColumn.TIME, System.currentTimeMillis());
				
		getContentResolver().update(
				VocabularyProvider.WORD_URI, 
				values, 
				WordColumn._ID +" = ?",
				new String[] {String.valueOf(card.getId())});
	}

	
	@Override
	public void onItemClick(AdapterView<?> parent,final View view,int position, long id) {
		final Card card = mAdapter.getItem(position);
		
		// only handle click, if view is not animated.
		AnimatorSet set = (AnimatorSet) view.getTag();
		if(set != null && set.isRunning()) return;
		
		int in = card.isFront() ? R.animator.card_flip_left_in : R.animator.card_flip_right_in;
		int out = card.isFront() ? R.animator.card_flip_left_out : R.animator.card_flip_right_out;
		
		ObjectAnimator start = (ObjectAnimator) AnimatorInflater.loadAnimator(this, out);
		ObjectAnimator end = (ObjectAnimator) AnimatorInflater.loadAnimator(this, in);
		
		start.setTarget(view);
		end.setTarget(view);
		end.addListener( new CardFlipper(card, view));
		
		AnimatorSet turn = new AnimatorSet();
		turn.playSequentially(start, end);
		turn.start();
		view.setTag(turn);
		
	}
	
	
	private class CardFlipper implements AnimatorListener {
		private Card card;
		private View view;
		
		public CardFlipper(Card card, View view) {
			this.card = card;
			this.view = view;
		}
		
		@Override
		public void onAnimationStart(Animator animation) {
			// turn card ( need to be fixed, because this influence the BOTH-Side mode.
			card.turn();
			
			// update title
	        TextView title = (TextView) view.findViewById(R.id.text);
	        //title.setText(card.getText());
/*
	        // audio hack
	        ImageButton play = (ImageButton) view.findViewById(R.id.play);
	        play.setVisibility(card.hasAudio() ? View.VISIBLE : View.INVISIBLE);
	        play.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						mAdapter.mPlayer = new MediaPlayer();
						mAdapter.mPlayer.setDataSource( CardActivity.this, card.getAudio());
						mAdapter.mPlayer.prepare();
						mAdapter.mPlayer.start();
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				}
	        });
	     */
		}

		@Override
		public void onAnimationEnd(Animator animation) {}

		@Override
		public void onAnimationCancel(Animator animation) {}

		@Override
		public void onAnimationRepeat(Animator animation) {}
		
	}
}