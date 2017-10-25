package de.pecheur.colorbox.models;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Locale;

import de.pecheur.colorbox.R;
import de.pecheur.colorbox.card.CardActivity;
import de.pecheur.colorbox.database.columns.UnitColumn;
import de.pecheur.colorbox.database.VocabularyProvider;

// TODO getFrontLanguage(), getFrontCode(): NullPointerException possible
// TODO getColor(...): better color algorithms desirable

public class Unit implements Parcelable {
	public static final long INVALID_ROW_ID = Long.MIN_VALUE;
	
	private static final int[] COLORS = new int[] {
        R.color.green_light,
        R.color.orange_light,
        R.color.blue_light, 
        R.color.red_light, 
        R.color.purple_light};

	
	private long id;
	private String title;
	private Locale frontLanguage;
	private Locale backLanguage;


	public Unit() {
		this.id = INVALID_ROW_ID;
	}

	
	public Unit(long id) {
		this.id = id;
	}

	public Unit(Parcel in) {
		this.id = in.readLong();
		this.title = in.readString();
		setFrontCode(in.readString());
		setBackCode(in.readString());
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getFrontLanguage() {
		return frontLanguage.getDisplayName();
	}

	public void setFrontCode(String frontCode) {
		this.frontLanguage = new Locale(frontCode);
	}

	public String getBackLanguage() {
		return backLanguage.getDisplayName();
	}

	public void setBackCode(String backCode) {
		this.backLanguage = new Locale(backCode);
	}
	
	public String getFrontCode() {
		return frontLanguage.getLanguage();
	}

	public String getBackCode() {
		return backLanguage.getLanguage();
	}
	
	public long getId() {
		return id;
	}

    public Uri getUri() {
        return ContentUris.withAppendedId( VocabularyProvider.UNIT_URI, id);
    }
	
	public boolean hasId() {
		return id != INVALID_ROW_ID;
	}
	
	public void setId(long id) {
		this.id = id;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeString(title);
		dest.writeString( getFrontCode());
		dest.writeString( getBackCode());
	}

	public static final Parcelable.Creator<Unit> CREATOR = new Parcelable.Creator<Unit>() {
		public Unit createFromParcel(Parcel in) {
			return new Unit(in);
		}

		public Unit[] newArray(int size) {
			return new Unit[size];
		}
	};

	
	@Override
	public String toString() {
		return "id: "+ id +", title: "+ title;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof Unit) {
			return id == ((Unit) o).id;
		} else {
			return false;
		}
	}
	
	
	public String getCode() {
		return getFrontCode() + getBackCode();
	}


	public int getColor(Context context) {
		return hasId() ? COLORS[(int) (id%COLORS.length)] : android.R.color.black;
	}
	
	
	
	public static void addShortcut(Context context, Unit unit) {
		context = context.getApplicationContext();

	    Intent intent = new Intent();
	    intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, getShortcutIntent(context, unit));
	    intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, unit.getTitle());

	    intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
	            Intent.ShortcutIconResource.fromContext(context,
	            		getIconFromColor(unit.getColor(context))));
	  
	    intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
	    context.sendBroadcast(intent);
	}
	
	
	public static void removeShortcut(Context context, Unit unit) {
	    Intent intent = new Intent();
	    intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, getShortcutIntent(context, unit));
	    intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, unit.getTitle());
	 
	    intent.setAction("com.android.launcher.action.UNINSTALL_SHORTCUT");
	    context.sendBroadcast(intent);
	}
	
	
	private static Intent getShortcutIntent(Context context, Unit unit) {
		Intent intent = new Intent(context, CardActivity.class);
		intent.setAction(Intent.ACTION_MAIN);
		intent.putExtra(CardActivity.EXTRA_UNIT_ID, unit.getId());
		return intent;
	}
	
	private static int getIconFromColor(int color) {
		switch (color) {
		case R.color.green_light:
			return R.drawable.ic_shortcut_green;
		case R.color.orange_light:
			return R.drawable.ic_shortcut_orange;
		case R.color.red_light:
			return R.drawable.ic_shortcut_red;
		case R.color.purple_light:
			return R.drawable.ic_shortcut_purple;
		default:
			return R.drawable.ic_shortcut_blue;
		}
	}

    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put(UnitColumn.TITLE, title);
        if(frontLanguage != null)
            values.put(UnitColumn.FRONT, getFrontCode());

        if(backLanguage != null)
            values.put(UnitColumn.BACK, getBackCode());
        return values;
    }
}
