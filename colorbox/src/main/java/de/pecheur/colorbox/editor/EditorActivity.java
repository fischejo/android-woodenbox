package de.pecheur.colorbox.editor;

import android.app.ActionBar;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import de.pecheur.colorbox.R;
import de.pecheur.colorbox.database.VocabularyProvider;
import de.pecheur.colorbox.database.columns.WordColumn;
import de.pecheur.colorbox.models.Unit;
import de.pecheur.colorbox.models.Word;
import de.pecheur.dictionary.Dictionary;
import de.pecheur.dictionary.DictionaryCallback;

import java.util.Comparator;

// TODO toggle input fields
// TODO handle words without unit
// TODO tags support
// TODO bold, italic, underline text
// TODO SD-card unmounted unhandled
// TODO better input field hints

public class EditorActivity extends FragmentActivity implements OnClickListener, DictionaryCallback {
	public static final String EXTRA_WORD = "word";
	public static final String EXTRA_UNIT = "unit";

    private boolean temporarily;
    private boolean repeat = false;

	private Word mWord;
	private SideFragment frontFragment;
	private SideFragment backFragment;
	private ViewPager mPager;



    private Dictionary mDictionary;
    private ServiceConnection mConnection;
    private boolean unBindService = true;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editor_activity);

		if (!handleIntent( getIntent())) {
			finish();
			return;
		}

        // set up custom done-actionbar
        LayoutInflater inflater = (LayoutInflater) getActionBar().getThemedContext().getSystemService(LAYOUT_INFLATER_SERVICE);
		View customActionBarView = inflater.inflate(R.layout.editor_actionbar, null);
		customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(this);
		customActionBarView.findViewById(R.id.actionbar_cancel).setOnClickListener(this);
			                
		// Show the custom action bar view and hide the normal Home icon and title.
		ActionBar actionBar = getActionBar();

		actionBar.setDisplayOptions( 
				ActionBar.DISPLAY_SHOW_CUSTOM,
				ActionBar.DISPLAY_SHOW_CUSTOM |
				ActionBar.DISPLAY_SHOW_HOME |
				ActionBar.DISPLAY_SHOW_TITLE);
			
		actionBar.setCustomView(customActionBarView, new ActionBar.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));


		mPager = (ViewPager) findViewById(R.id.pager);

        Unit unit = mWord.getUnit();
		if(mPager != null) {
			// single page mode
            frontFragment = SideFragment.newInstance(mWord, true);
            backFragment = SideFragment.newInstance(mWord, false);
            mPager.setAdapter(new FragmentAdapter());
			
			// color pager strip
			PagerTitleStrip strip = (PagerTitleStrip) findViewById(R.id.pager_title_strip);
			strip.setBackgroundResource(unit.getColor(this));
		} else {
            // dual page mode

			// strip
			View strip = findViewById(R.id.title_strip);
			strip.setBackgroundResource(unit.getColor(this));

			TextView text = (TextView) findViewById(R.id.front_language);
			text.setText(unit.getFrontLanguage());
			text = (TextView) findViewById(R.id.back_language);
			text.setText(unit.getBackLanguage());
			
			// dual fragment layout
			FragmentManager fm = getSupportFragmentManager();
			frontFragment = (SideFragment) fm.findFragmentById(R.id.a_language);
			backFragment = (SideFragment) fm.findFragmentById(R.id.b_language);
		}

        frontFragment.onLoadWord(mWord);
        backFragment.onLoadWord(mWord);
	}



	private boolean handleIntent(Intent intent) {
		if (intent.hasExtra(EXTRA_WORD)) {
			// case 0: unit is handed over by word object.
			mWord = intent.getParcelableExtra(EXTRA_WORD);

            // only update db, if word is inserted
            temporarily = !mWord.hasId();

            // do not stay in the editor after saving this word
            repeat = false;
		} else if (intent.hasExtra(EXTRA_UNIT)) {
			// case 1: unit is handed over by intent object.
            Unit unit = (Unit) intent.getParcelableExtra(EXTRA_UNIT);
			mWord = new Word(unit);

            // if unit is in db, then we assume that the new word need to be inserted
            temporarily = !unit.hasId();

            // allow multiple inserts
            repeat = true;
		} else {
            return false;
        }
        return true;


	}


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        unBindService = false;
        super.onSaveInstanceState(outState);
    }


    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return mDictionary;
    }


    @Override
    protected void onStart() {
        super.onStart();
        mDictionary = (Dictionary) getLastCustomNonConfigurationInstance();

        if(mConnection == null) {
            final String from = mWord.getUnit().getFrontCode();
            final String to = mWord.getUnit().getBackCode();
            final String[] types = new String[] {"test type"};

            mDictionary = Dictionary.getDictionary(this, from, to, types);
        }
        mDictionary.setDictionaryCallback(EditorActivity.this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Is the activity just being rotated or destroyed for good?
        //We check if onRetainNonConfigurationInstance is called to understand
        mDictionary.setDictionaryCallback(null);
        if(unBindService) {
            mDictionary.unbind();
            mDictionary = null;

        }
    }

    public Dictionary getDictionary() {
        return mDictionary;
    }

    @Override
	public void onClick(View view) {
		switch ( view.getId()) {
		case R.id.actionbar_done:
            done();

			break;	
		case R.id.actionbar_cancel:
			finish();
			break;
		}
	}



    private void done() {
        // insert word
        if(!mWord.hasId()) {
            ContentValues cv = new ContentValues();
            cv.put(WordColumn.UNIT, mWord.getUnit().getId());
            Uri uri  = getContentResolver().insert(VocabularyProvider.WORD_URI, cv);
            mWord.setId(ContentUris.parseId(uri));
        }


        if(frontFragment != null) frontFragment.onSaveWord(mWord);
        if(backFragment != null) backFragment.onSaveWord(mWord);

        if(repeat) {
            // show toast
            Toast.makeText(this, R.string.editor_insert_toast, Toast.LENGTH_SHORT).show();


            // go back to the first fragment
            if (mPager != null) mPager.setCurrentItem(0);
        } else {
            setResult(RESULT_OK);
            finish();
        }
    }

    @Override
    public void onCompilation(int id, Bundle bundle) {
        frontFragment.onCompilation(id, bundle);
        backFragment.onCompilation(id, bundle);
    }

    @Override
    public void onError(int id, int code) {
        frontFragment.onError(id, code);
        backFragment.onError(id, code);
    }


    private class FragmentAdapter extends FragmentPagerAdapter   {
		public FragmentAdapter() {
			super(getSupportFragmentManager());
		}
		 
		@Override
		public Fragment getItem(int position) {
			return position == 0 ? frontFragment : backFragment;
		}
		 
		@Override
		public CharSequence getPageTitle(int position) {
			return position == 0 ? 
				mWord.getUnit().getFrontLanguage() :
				mWord.getUnit().getBackLanguage();
		}

		@Override
		public int getCount() {
			return 2;
		}
	}


    private Comparator<ResolveInfo> mDictionaryComparator = new Comparator<ResolveInfo>() {
        @Override
        public int compare(ResolveInfo lhs, ResolveInfo rhs) {
            Integer lcc = lhs.filter.countCategories();
            Integer rcc = rhs.filter.countCategories();
            return lcc.compareTo(rcc);
        }
    };

}
