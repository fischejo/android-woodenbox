package de.pecheur.colorbox.unit;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;


import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;


import com.hb.views.PinnedSectionListView;
import de.pecheur.colorbox.R;
import de.pecheur.colorbox.database.columns.UnitColumn;
import de.pecheur.colorbox.database.VocabularyProvider;
import de.pecheur.colorbox.database.columns.WordColumn;
import de.pecheur.colorbox.models.Unit;
import de.pecheur.colorbox.section.Section;
import de.pecheur.colorbox.settings.Settings;


public class UnitListAdapter extends BaseAdapter implements PinnedSectionListView.PinnedSectionListAdapter,LoaderManager.LoaderCallbacks<Cursor> {
    static final int TYPE_UNIT = 0;
    static final int TYPE_SECTION = 1;
    
    // similar unit
    private Unit mUnit;
    private int mLayout;
    private LinkedHashMap<Integer, Section> sectionsIndexer;
    private Context mContext;
    private List<UnitItem> mUnits;

    public String code = "%";


	private class UnitItem extends Unit {
		public UnitItem(long id) {
			super(id);
		}

		public int progress;
		public String subtitle;
		public int subicon;
	}

	
	public UnitListAdapter(Context context, int layout) {
		this(context, layout, null);
	}
	
	
	public UnitListAdapter(Context context, int layout, Unit unit) {
		mUnits = new ArrayList<UnitItem>();
		sectionsIndexer = new LinkedHashMap<Integer, Section>();
		
		mContext = context;
		mUnit = unit;
		mLayout = layout;
	}
	
	
	@Override
	public int getCount() {
		return mUnits.size() + sectionsIndexer.size();
	}


    @Override
    public Unit getItem(int position) {
    	return mUnits.get(getSectionForPosition(position));
    }

 
    @Override
    public long getItemId(int position) {
    	return getItem(position).getId();
    }

    
    @Override
	public int getItemViewType(int position) {
		if (position == getPositionForSection(position)) {
			return TYPE_UNIT;
		}
		return TYPE_SECTION;
	}
    
    @Override
	public boolean isItemViewTypePinned(int viewType) {
		return viewType == TYPE_SECTION;
	}

    
    @Override
    public int getViewTypeCount() {
        return 2;
    }

    
    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) == TYPE_UNIT;
    }

    
    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    
    @Override
    public View getView(int position, View view, ViewGroup parent) {
    	if (getItemViewType(position) == TYPE_UNIT) {
    		position = getSectionForPosition(position);
    		UnitItem unit = mUnits.get(position);
    		
            if (view == null) {
                view = LayoutInflater.from(mContext).inflate(mLayout, parent, false);
            }

            // title
           TextView title = (TextView) view.findViewById(R.id.text1);
           title.setText(unit.getTitle());
           
           // subtitle + subicon
           TextView count = (TextView) view.findViewById(R.id.text2);
           if (count != null) {
        	   if (unit.subtitle != null) {
        		   count.setVisibility(View.VISIBLE);
        		   count.setText(unit.subtitle);
        		   count.setCompoundDrawablesWithIntrinsicBounds(unit.subicon, 0, 0, 0);
        	   } else {
        		   count.setVisibility(View.GONE);
        	   }
           }
           
           // progress
           ProgressBar bar = (ProgressBar) view.findViewById(R.id.progress);
           if (bar != null) {
        	   bar.setProgress(unit.progress);
           }
        } else {	// header view
        	Section section = sectionsIndexer.get(position);
        	
            if (view == null) {
                view = LayoutInflater.from(mContext).inflate(R.layout.list_section, parent, false);
                
            }

            view.setBackgroundResource(section.getColor());
            
            TextView title = (TextView) view.findViewById(R.id.title);
            title.setText(section.getTitle());
        }

        return view;
    }
	 
	
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        int boxCount = Settings.getBoxCount(mContext);

        // build available-where string for available-count query
		String available = System.currentTimeMillis() +"- CASE "+ WordColumn.BOX +
                " WHEN 0 THEN '0'";
        for(int i = 1; i < boxCount; i++)
            available += " WHEN "+i+" THEN '"+Settings.getBoxDelay(mContext, i)+"'";
        available += " ELSE '"+Settings.getBoxDelay(mContext, boxCount)+"' END";


        // build available-count query
		String count = "(SELECT COUNT(*)"+
				" FROM "+ WordColumn.TABLE +
				" WHERE " + WordColumn.UNIT + " = "+ UnitColumn._ID+
				" AND "+ WordColumn.TIME + " < " + available +")";


        // build average-box query for progressbar
		String avg = "(SELECT AVG(MIN("+boxCount+","+ WordColumn.BOX +"))" +
                " FROM "+ WordColumn.TABLE+
                " WHERE "+ WordColumn.UNIT + " = "+ UnitColumn._ID+")";
		
		
		String[] projection = {
				UnitColumn._ID,
				UnitColumn.TITLE,
				UnitColumn.FRONT,
				UnitColumn.BACK,
				count,
				avg};   


		String selection = null;
		String[] selectionArgs = null;
		String sortOrder = UnitColumn.TITLE;
		

			// units with similar first language
			selection =	UnitColumn.BACK +" = ?";
	    
			selectionArgs = new String[]{code};
				

			sortOrder = UnitColumn.FRONT +" || "+
						UnitColumn.BACK + " || "+
						sortOrder;

		
		return new CursorLoader( 
				mContext, 
				VocabularyProvider.UNIT_URI,
				projection, 
				selection, 
				selectionArgs, 
				sortOrder); 
	}
	

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
		mUnits.clear();
		sectionsIndexer.clear();
		Unit previousUnit = null;
		if (c != null) {
			
			// resources object for subtitles
			Resources res = mContext.getResources();
			
			// todays date for subtitles
			Date date = new Date();
			date.setHours(0);
			date.setMinutes(0);
			date.setSeconds(0);
			
			int sectionIndex = 0;
		
			int queryCount = Settings.getQueryCount(mContext);
			for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
				// create list unit
				UnitItem unit = new UnitItem( c.getLong(0));
				unit.setTitle( c.getString(1));
				unit.setFrontCode(c.getString(2));
				unit.setBackCode(c.getString(3));
				
				unit.progress = (int) ((c.getFloat(5)/ WordColumn.MAX_BOX_VALUE)*100);

				// subtitle
				int available = c.getInt(4);
				
				unit.subtitle = MessageFormat.format(
                        res.getString(R.string.unit_list_item_subtitle),
                        available);
				
				unit.subicon = available >= queryCount ?
						R.drawable.ic_list_time :
						R.drawable.ic_list_accept;
		
				// add unit
				mUnits.add(unit);
				
				
				// add header
				if ( previousUnit == null || !previousUnit.getCode().equals(unit.getCode())) {
					previousUnit = unit;
					Section section = new Section();
					section.setTitle(mContext.getString(
                            R.string.word_activity_subtitle,
                            unit.getFrontLanguage(),
                            unit.getBackLanguage()));
					section.setColor(unit.getColor(mContext));
					sectionsIndexer.put(c.getPosition() + sectionIndex, section);
					sectionIndex++;
				}
			} 
		}
		notifyDataSetChanged();
	}
	
  
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mUnits.clear();
		sectionsIndexer.clear();
		notifyDataSetChanged();
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