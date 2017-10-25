package de.pecheur.colorbox.card;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

public class BoxSpinner extends Spinner {
	BaseAdapter adapter;
	
	public BoxSpinner(Context context) {
		super(context);
	}

    public BoxSpinner(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

    @Override
    public void setAdapter(SpinnerAdapter adapter) {
    	super.setAdapter(adapter);
    	
    	if (adapter instanceof BaseAdapter)
    		this.adapter = (BaseAdapter) adapter;
    }

    @Override
    public boolean performClick() {
    	if (adapter != null) adapter.notifyDataSetChanged();
        return super.performClick();
    }
}