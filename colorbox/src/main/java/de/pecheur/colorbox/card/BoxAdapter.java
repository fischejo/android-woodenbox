package de.pecheur.colorbox.card;


import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import de.pecheur.colorbox.database.VocabularyProvider;
import de.pecheur.colorbox.database.columns.WordColumn;
import de.pecheur.colorbox.settings.Settings;
import de.pecheur.colorbox.models.Unit;
import de.pecheur.colorbox.R;


public class BoxAdapter extends BaseAdapter {
	private class Box {
		public String title;
		public int count;
		public int box;
	}
	
    private Box[] mBoxes;
    private Context mContext;
    private Unit mUnit;
    
    public BoxAdapter(Context context, Unit unit) {
        mContext = context;
        mUnit = unit;
        
        int[] headers = {
				R.string.box_0,
				R.string.box_1,
				R.string.box_2,
				R.string.box_3,
				R.string.box_4,
				R.string.box_5,
				R.string.box_6};
        
        mBoxes = new Box[WordColumn.MAX_BOX_VALUE+1];
        for(int i = 0; i < mBoxes.length; i++) {
        	mBoxes[i] = new Box();
        	mBoxes[i].box = i;
        	mBoxes[i].title = context.getString(headers[i]);
        	mBoxes[i].count = countAvailable(i);
        }
    }

    public int getCount() {
        return mBoxes.length;
    }


    public Integer getItem(int position) {
        return mBoxes[position].box;
    }

 
    
    public long getItemId(int position) {
        return mBoxes[position].box;
    }


    @Override
    public boolean isEnabled(int position) {
        return mBoxes[position].count > 0;
    }

    
    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }


    public View getView(int position, View view, ViewGroup parent) {
    	if (view == null) {
        	view = LayoutInflater.from(mContext).
        			inflate(android.R.layout.simple_list_item_1, parent, false);
        }
    	
        TextView title = (TextView) view.findViewById(android.R.id.text1);
        title.setText( mBoxes[position].title);

        return view;
    }
    
    @Override
    public View getDropDownView(int position, View view, ViewGroup parent) {
    	if (view == null) {
        	view = LayoutInflater.from(mContext).
        			inflate(R.layout.box_dropdown_item, parent, false);
        }
    	
        TextView title = (TextView) view.findViewById(android.R.id.text1);
        title.setText( mBoxes[position].title);
        
        TextView badge = (TextView) view.findViewById(R.id.count);
        badge.setText(String.valueOf(mBoxes[position].count));

        return view;
    }
    
    @Override
    public void notifyDataSetChanged() {
    	for (int i = 0; i < mBoxes.length; i++) {
    		Box box = mBoxes[i];
    		box.count = countAvailable(box.box);
    	}
    	super.notifyDataSetChanged();
    }

    
    private int countAvailable(int box) {
		// box_0 is an exception, even if cards are out of delay-range, they get selected.
		long delay = Settings.getBoxDelay(mContext, box);
		long time = System.currentTimeMillis()-delay;

		String[] projection = new String[]{"count(*)"};
		
		String selection = 	WordColumn.UNIT +" =  ? AND "+
							WordColumn.BOX + " = ? AND "+
							WordColumn.TIME + " < ?";
		
		String[] args = new String[] {
				String.valueOf(mUnit.getId()),
					String.valueOf(box),
					String.valueOf(time) };
		
		
		Cursor c = mContext.getContentResolver().query(
				VocabularyProvider.WORD_URI,
				projection, 
				selection, 
				args, 
				null);
		
		c.moveToFirst();
		
		int count = c.getInt(0);
		c.close();
		return count;
	}
}