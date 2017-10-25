package de.pecheur.colorbox.port;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.RelativeLayout;

/**
 * A simple checkable RelativeLayout, which pass the checkable states to a
 * child android.R.id.checkbox.
 * 
 * @author Johannes Fischer
 * @version 1.0
 */

public class CheckableRelativeLayout extends RelativeLayout implements Checkable {
	private Checkable checkable;
	

	public CheckableRelativeLayout(Context context) {
		super(context);
	}	
	
	public CheckableRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);		
	}
	
	public CheckableRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);			
	}
	

	@Override
	protected void onFinishInflate() {		
		super.onFinishInflate();
		
		this.checkable = (Checkable) findViewById(android.R.id.checkbox);
	}

	
	@Override
	public boolean isChecked() {
		return (checkable != null) ? checkable.isChecked() : false;
	}
	
	
	@Override
	public void setChecked(boolean checked) {
		if (checkable != null) {
			checkable.setChecked(checked);
		}		
	}
	
	
	@Override
	public void toggle() {
		if (checkable != null) {
			checkable.setChecked(!checkable.isChecked());
		}		
	}
	
	@Override
	public boolean isActivated() {
		return isChecked();
	}
}
