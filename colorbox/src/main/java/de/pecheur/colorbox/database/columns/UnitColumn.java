package de.pecheur.colorbox.database.columns;

public class UnitColumn extends BaseColumn{
	/**
	 * Unit table with following rows:
	 * 
	 * {@link android.provider.UnitColumns#_ID}, {@link UnitColumn#TITLE},
	 * {@link UnitColumn#FRONT}, {@link UnitColumn#BACK}
	 */
	public static final String TABLE = "units";
	

	/**
	 * Title of unit, stored as data type TEXT
	 */
	public static final String TITLE = "title";
	
	/**
	 * Lexicographically bigger language code of the unit, stored as data type
	 * TEXT. Only two-letter ISO 639-1 language codes are allowed.
	 * 
	 * @see java.util.Locale
	 */
	public static final String FRONT = "front";
	
	/**
	 * Second language of the unit, stored as data type TEXT. Only two-letter
	 * ISO 639-1 language codes are allowed.
	 * 
	 * @see java.util.Locale
	 */
	public static final String BACK = "back";

}
