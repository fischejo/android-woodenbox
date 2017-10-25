
package de.pecheur.colorbox.detail;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import java.util.LinkedHashMap;
import java.util.Locale;

import com.hb.views.PinnedSectionListView;
import de.pecheur.colorbox.R;
import de.pecheur.colorbox.database.VocabularyProvider;
import de.pecheur.colorbox.database.columns.TextColumn;
import de.pecheur.colorbox.database.columns.WordColumn;
import de.pecheur.colorbox.models.Unit;
import de.pecheur.colorbox.models.Word;
import de.pecheur.colorbox.settings.Settings;
import de.pecheur.colorbox.section.Section;



public class WordCursorAdapter extends CursorAdapter implements PinnedSectionListView.PinnedSectionListAdapter, LoaderManager.LoaderCallbacks<Cursor> {

	private static final int TYPE_ITEM = 1;
	private static final int TYPE_SECTION = 0;
	private static final int LOADER_ID = 1;

	
	private LoaderManager mLoaderManager;
	private Context mContext;
	private String mFilter;
	private boolean mFiltering = false;
	private int mColor;
	private Unit mUnit;
    private int mLayout;
	private LinkedHashMap<Integer, Section> sectionsIndexer;
	
	
	private class WordViewHolder {
		TextView frontText;
		TextView backText;
	}
	

	public WordCursorAdapter(Activity activity, int layout) {
		super(activity, null, false);
		mContext = activity;
        mLayout = layout;
		sectionsIndexer = new LinkedHashMap<Integer, Section>();
        mColor = activity.getResources().getColor( android.R.color.holo_blue_light);
		mLoaderManager = activity.getLoaderManager();
	}

	
	public void setUnit(Unit unit) {
		mUnit = unit;
		
		if (unit != null) {
			// init or restart async word loader
			if (mLoaderManager.getLoader( LOADER_ID) == null) {
				mLoaderManager.initLoader( LOADER_ID, null, this);
			} else {
				mLoaderManager.restartLoader(LOADER_ID, null, this);
			}
		} else {
			changeCursor(null);
			mLoaderManager.destroyLoader(LOADER_ID);
		}
	}

	
	@Override
	public int getCount() {
		return super.getCount() + sectionsIndexer.size();
	}
	

	@Override
	public Word getItem(int position) {
		Cursor c = getCursor();
		c.moveToPosition(getSectionForPosition(position));
		
		// id & unit
		Word word = new Word(c.getLong(0));
		word.setUnit(mUnit);
		word.setBox(c.getInt(3));

		return word;
	}

	
	@Override
	public long getItemId(int position) {
		return super.getItemId( getSectionForPosition(position));
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}
	
	
	@Override
	public int getItemViewType(int position) {
		if (position == getPositionForSection(position)) {
			return TYPE_ITEM;
		}
		return TYPE_SECTION;
	}

	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		int viewType = getItemViewType(position);

		if (viewType == TYPE_ITEM) {
			// calculate cursor position
			int mapCursorPos = getSectionForPosition(position);
			return super.getView(mapCursorPos, convertView, parent);
		} else {
			return getHeaderView(position, convertView, parent);
		}
	}
 
	
	private View getHeaderView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.list_section, parent, false);
		}

		Section section = sectionsIndexer.get(position);

		convertView.setBackgroundResource(mUnit.getColor(mContext));
		
		// title
		TextView title = (TextView) convertView.findViewById(R.id.title);
		title.setText( section.getTitle());
		
		// info
		TextView count = (TextView) convertView.findViewById(R.id.info);
		count.setText( section.getInfo());
		
		return convertView;
	}

	
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		// get view holder
		WordViewHolder holder = (WordViewHolder) view.getTag();
		
		// text
		if (mFiltering) {	
			holder.frontText.setText( markFilter( cursor.getString(2)));
			holder.backText.setText( markFilter( cursor.getString(3)));
		} else {
			holder.frontText.setText( cursor.getString(2));
			holder.backText.setText( cursor.getString(3));
		}

	}

	
	
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = LayoutInflater.from(context).inflate(mLayout, parent, false);
		
		// view holder
		WordViewHolder holder = new WordViewHolder();
		holder.frontText = (TextView) view.findViewById(R.id.text1);
		holder.backText = (TextView) view.findViewById(R.id.text2);
		

		
		view.setTag(holder);
		return view;
	}


	@Override
	public boolean isItemViewTypePinned(int viewType) {
		return viewType == TYPE_SECTION;
	}


	@Override
	public Cursor swapCursor(Cursor c) {
		sectionsIndexer.clear();

		// insert sections
		if (c != null) {
			int sectionIndex = 0;
            Resources res = mContext.getResources();
			String[] boxes = res.getStringArray(R.array.boxes);

			int previousBox = -1;
			int previousCount = 0;
			Section previousSection = null;


			for ( c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
				int box = c.getInt(1);

				if (previousBox != box) {
					if(previousSection != null) {
						// box is finished and we can set the final count as info.
						previousSection.setInfo( res.getQuantityString(
								R.plurals.word_list_header_info,
								previousCount, previousCount));
					}

					// add a new section with the box as title
					previousSection = new Section(boxes[box]);
					sectionsIndexer.put(c.getPosition() + sectionIndex, previousSection);
					sectionIndex++;
					previousCount = 0;	// reset counter
					previousBox = box;	// update previous box
				}
				previousCount++;
			}
			
			// set the final count of the last box
			if (previousSection != null) previousSection.setInfo(res.getQuantityString( 
					R.plurals.word_list_header_info,
					previousCount, 
					previousCount));
		}
		return super.swapCursor(c);	
	}
	
	
	@Override
	public boolean isEnabled(int position) {
		return getItemViewType(position) == TYPE_ITEM;
	}

	    
	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}


	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		// load section
        int boxCount = Settings.getBoxCount(mContext);

        // TODO search function not implemented yet
		String[] projection = {
				WordColumn._ID + " AS "+ BaseColumns._ID,
                "MIN("+ boxCount +","+ WordColumn.BOX+")",
                // front
                "(SELECT "+ TextColumn.DATA+" FROM "+ TextColumn.TABLE+
                        " WHERE "+ TextColumn.WORD + " = " + WordColumn._ID+
                        " AND "+ TextColumn.SIDE +
                        " LIMIT 1)",

                // back
                "(SELECT "+ TextColumn.DATA+" FROM "+ TextColumn.TABLE+
                        " WHERE "+ TextColumn.WORD + " = " + WordColumn._ID+
                        " AND NOT "+ TextColumn.SIDE +
                        " LIMIT 1)"};

        /*
		String selection = 	WordColumns.UNIT + " =  ? AND "+ "(" +
				WordColumns.FRONT_TEXT + " LIKE ? OR " + WordColumns.BACK_TEXT + " LIKE ? )";
		
		String filter = (mFilter == null) ? "" : mFilter;
		String[] args = new String[] { String.valueOf(mUnit.getId()), "%"+filter+"%", "%"+filter+"%"};
		*/


		return new CursorLoader(
				mContext, 
				VocabularyProvider.WORD_URI,
				projection,
                WordColumn.UNIT + " =  ?",
				new String[] { String.valueOf(mUnit.getId())},
				WordColumn.BOX);	// sort by box
	}

	
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		changeCursor(cursor);
	}

	
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		changeCursor(null);
	}

	
	/**
	 * marks all filtered subsequences
	 * @param text
	 * @return marked CharSequence
	 */
	private CharSequence markFilter(String text) {
		// mark filtered subsequences
		SpannableStringBuilder spannable = new SpannableStringBuilder( text);
		text = text.toLowerCase(Locale.US);
		int length = mFilter.length();
		for (int i = text.indexOf(mFilter); i != -1; i = text.indexOf(mFilter, i+length)) {
			spannable.setSpan( new BackgroundColorSpan( mColor), i, i+length, Spanned.SPAN_MARK_MARK);
		}
		return spannable;
	}

	
	/**
	 * custom way to filter database, because Loader can't be combined with
	 * setFilterQueryProvider.
	 * @param filter
	 */
	public void filter(String filter) {
		mFilter = filter;
		mFiltering = (filter != null && !filter.isEmpty());
		mLoaderManager.restartLoader(LOADER_ID, null, this);
	}
	
	
	private int getPositionForSection(int section) {
		if (sectionsIndexer.containsKey(section)) {
			return section + 1;
		}
		return section;
	}

	
	private int getSectionForPosition(int position) {
		int offset = 0;
		for (Integer key : sectionsIndexer.keySet()) {
			if (position > key) {
				offset++;
			} else {
				break;
			}
		}

		return position - offset;
	}
}