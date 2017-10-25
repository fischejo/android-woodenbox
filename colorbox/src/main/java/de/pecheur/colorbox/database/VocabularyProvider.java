package de.pecheur.colorbox.database;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import de.pecheur.colorbox.database.columns.*;


public class VocabularyProvider extends ContentProvider {
    // public provider authority
	public static final String AUTHORITY = "de.pecheur.colorbox.provider";


    // public provider uris
	public static final Uri UNIT_URI = Uri.parse("content://" + AUTHORITY + "/" + UnitColumn.TABLE);
	public static final Uri WORD_URI = Uri.parse("content://" + AUTHORITY + "/" + WordColumn.TABLE);
	public static final Uri TEXT_URI = Uri.parse("content://" + AUTHORITY + "/" + TextColumn.TABLE);
    public static final Uri EXAMPLE_URI = Uri.parse("content://" + AUTHORITY + "/" + ExampleColumn.TABLE);
    public static final Uri AUDIO_URI = Uri.parse("content://" + AUTHORITY + "/" + AudioColumn.TABLE);


	// private identifier for uris
        private static final int UNIT_TYPE = 1, UNIT_SINGLE_TYPE = 10;
    private static final int WORD_TYPE = 2, WORD_SINGLE_TYPE = 20;
    private static final int TEXT_TYPE = 3, TEXT_SINGLE_TYPE = 30;
    private static final int EXAMPLE_TYPE = 4, EXAMPLE_SINGLE_TYPE = 40;
    private static final int AUDIO_TYPE = 5, AUDIO_SINGLE_TYPE = 50;


    // uri matcher
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
	    sURIMatcher.addURI(AUTHORITY, UnitColumn.TABLE, UNIT_TYPE);
	    sURIMatcher.addURI(AUTHORITY, WordColumn.TABLE, WORD_TYPE);
	    sURIMatcher.addURI(AUTHORITY, TextColumn.TABLE, TEXT_TYPE);
        sURIMatcher.addURI(AUTHORITY, ExampleColumn.TABLE, EXAMPLE_TYPE);
        sURIMatcher.addURI(AUTHORITY, AudioColumn.TABLE, AUDIO_TYPE);
	    
	    // allow single row selection
	    sURIMatcher.addURI(AUTHORITY, UnitColumn.TABLE+"/#" , UNIT_SINGLE_TYPE);
	    sURIMatcher.addURI(AUTHORITY, WordColumn.TABLE+"/#", WORD_SINGLE_TYPE);
	    sURIMatcher.addURI(AUTHORITY, TextColumn.TABLE+"/#" , TEXT_SINGLE_TYPE);
        sURIMatcher.addURI(AUTHORITY, ExampleColumn.TABLE+"/#" , EXAMPLE_SINGLE_TYPE);
        sURIMatcher.addURI(AUTHORITY, AudioColumn.TABLE+"/#" , AUDIO_SINGLE_TYPE);
	}


	private SqliteHelper mSqliteHelper;
	private ContentResolver contentResolver;
	//private Context mContext;


	@Override
	public boolean onCreate() {
		mSqliteHelper = new SqliteHelper( getContext());
		//mContext = getContext();
		contentResolver = getContext().getContentResolver();
		return true;
	}


	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		// Using SQLiteQueryBuilder instead of query() method
	    SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
	    SQLiteDatabase db = mSqliteHelper.getWritableDatabase();

	    switch ( sURIMatcher.match(uri)) {
	    case UNIT_SINGLE_TYPE:
	    	// adapt selection
	    	selection = UnitColumn._ID +" = ?"+ uri.getLastPathSegment();
	    	selectionArgs = new String[] {uri.getLastPathSegment()};

        case UNIT_TYPE:
	    	builder.setTables(UnitColumn.TABLE);
	    	break;

	    case WORD_SINGLE_TYPE:
	    	// adapt selection
            selection = WordColumn._ID +" = ?"+ uri.getLastPathSegment();
            selectionArgs = new String[] {uri.getLastPathSegment()};
	    		
	    case WORD_TYPE:
	        builder.setTables(WordColumn.TABLE);
	    	break;

        case TEXT_SINGLE_TYPE:
            // adapt selection
            selection = TextColumn._ID +" = ?"+ uri.getLastPathSegment();
            selectionArgs = new String[] {uri.getLastPathSegment()};

        case TEXT_TYPE:
            builder.setTables(TextColumn.TABLE);
            break;

        case EXAMPLE_SINGLE_TYPE:
            selection = TextColumn._ID +" = ?"+ uri.getLastPathSegment();
            selectionArgs = new String[] {uri.getLastPathSegment()};

        case EXAMPLE_TYPE:
            builder.setTables(TextColumn.TABLE);
            break;

        case AUDIO_SINGLE_TYPE:
            selection = TextColumn._ID +" = ?"+ uri.getLastPathSegment();
            selectionArgs = new String[] {uri.getLastPathSegment()};

        case AUDIO_TYPE:
            builder.setTables(AudioColumn.TABLE);
            break;

        default:
	    	throw new IllegalArgumentException("Unknown DESCRIPTION: " + uri);
	    }

	    Cursor cursor = builder.query(
	    		db, 
	    		projection, 
	    		selection,  
	    		selectionArgs,
                null,
	    		null, 
	    		sortOrder);

	    return cursor;
	}

	
	@Override
	public String getType(Uri uri) {
        // TODO: not sure this is the right way to handle this method
		switch ( sURIMatcher.match(uri)) {
            case TEXT_TYPE:
            case TEXT_SINGLE_TYPE:
                return "text/plain";    // html yet not supported

            case EXAMPLE_TYPE:
            case EXAMPLE_SINGLE_TYPE:
                return "text/example";

            // not implemented
            case UNIT_TYPE:
            case WORD_TYPE:

            case AUDIO_TYPE:
                return "vnd.android.cursor.dir/vnd."+uri.toString();

            case AUDIO_SINGLE_TYPE:
            case UNIT_SINGLE_TYPE:
		    case WORD_SINGLE_TYPE:
			    return "vnd.android.cursor.item/vnd."+uri.toString();
		default:
			return null;
		}		
	}

	  
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = mSqliteHelper.getWritableDatabase();
		long id = -1;

		switch ( sURIMatcher.match(uri)) {
		case UNIT_TYPE:
			id = db.insert(UnitColumn.TABLE, null, values);
		   	break;

		case WORD_TYPE:
		    id = db.insert(WordColumn.TABLE, null, values);
		    break;
	
		case TEXT_TYPE:
            id = db.insert(WordColumn.TABLE, null, values);
            break;

		default:
		    throw new IllegalArgumentException("Unknown DESCRIPTION: " + uri);
		}

		// notify listeners about change
		if(id != -1) contentResolver.notifyChange(uri, null);

		// return id as uri
		return ContentUris.withAppendedId(uri, id);
    }

	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
	    SQLiteDatabase db = mSqliteHelper.getWritableDatabase();
	    int rowsDeleted = 0;

	    switch ( sURIMatcher.match(uri)) {
	    	case UNIT_SINGLE_TYPE:
                selection = AudioColumn._ID +" = ?";
                selectionArgs = new String[] {uri.getLastPathSegment()};

            case UNIT_TYPE:
                rowsDeleted = db.delete(UnitColumn.TABLE, selection, selectionArgs);
                break;

            case WORD_SINGLE_TYPE:
                selection = AudioColumn._ID +" = ?";
                selectionArgs = new String[] {uri.getLastPathSegment()};

            case WORD_TYPE:
                rowsDeleted = db.delete(WordColumn.TABLE, selection, selectionArgs);
                break;

            case TEXT_SINGLE_TYPE:
                selection = AudioColumn._ID +" = ?";
                selectionArgs = new String[] {uri.getLastPathSegment()};

            case TEXT_TYPE:
                rowsDeleted = db.delete(TextColumn.TABLE, selection, selectionArgs);
                break;

            case EXAMPLE_SINGLE_TYPE:
                selection = AudioColumn._ID +" = ?";
                selectionArgs = new String[] {uri.getLastPathSegment()};

            case EXAMPLE_TYPE:
                rowsDeleted = db.delete(ExampleColumn.TABLE, selection, selectionArgs);
                break;

            case AUDIO_SINGLE_TYPE:
                selection = AudioColumn._ID +" = ?";
                selectionArgs = new String[] {uri.getLastPathSegment()};

            case AUDIO_TYPE:
                rowsDeleted = db.delete(AudioColumn.TABLE, selection, selectionArgs);
                break;

	    	default:
	    		throw new IllegalArgumentException("Unknown DESCRIPTION: " + uri);
	    }
	    
	    // notify listeners about change
	    if(rowsDeleted > 0) contentResolver.notifyChange(uri, null);
	 		
	 	// return number of affected  rows
	    return rowsDeleted;
	}


	
	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mSqliteHelper.getWritableDatabase();

		int rowsUpdated = 0;
        switch ( sURIMatcher.match(uri)) {
            case UNIT_SINGLE_TYPE:
                selection = AudioColumn._ID +" = ?";
                selectionArgs = new String[] {uri.getLastPathSegment()};

            case UNIT_TYPE:
                rowsUpdated = db.update(UnitColumn.TABLE, values, selection, selectionArgs);
                break;

            case WORD_SINGLE_TYPE:
                selection = AudioColumn._ID +" = ?";
                selectionArgs = new String[] {uri.getLastPathSegment()};

            case WORD_TYPE:
                rowsUpdated = db.update(WordColumn.TABLE, values, selection, selectionArgs);
                break;

            case TEXT_SINGLE_TYPE:
                selection = AudioColumn._ID +" = ?";
                selectionArgs = new String[] {uri.getLastPathSegment()};

            case TEXT_TYPE:
                rowsUpdated = db.update(TextColumn.TABLE, values, selection, selectionArgs);
                break;

            case EXAMPLE_SINGLE_TYPE:
                selection = AudioColumn._ID +" = ?";
                selectionArgs = new String[] {uri.getLastPathSegment()};

            case EXAMPLE_TYPE:
                rowsUpdated = db.update(ExampleColumn.TABLE, values, selection, selectionArgs);
                break;

            case AUDIO_SINGLE_TYPE:
                selection = AudioColumn._ID +" = ?";
                selectionArgs = new String[] {uri.getLastPathSegment()};

            case AUDIO_TYPE:
                rowsUpdated = db.update(AudioColumn.TABLE, values, selection, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown DESCRIPTION: " + uri);
        }

		 // notify listeners about change
	    if(rowsUpdated > 0) contentResolver.notifyChange(uri, null);
	 		
	 	// return number of affected  rows
	    return rowsUpdated;
	}



	/*
	@Override
	public int bulkInsert(Uri uri, ContentValues[] bulkValues) {
		int rowsInserted = 0; 
	    SQLiteDatabase db = mSqliteHelper.getWritableDatabase();
	    
	    if (sURIMatcher.match(uri) == WORD_TYPE) {
		    db.beginTransaction();
		    try {
		        for (ContentValues values : bulkValues) {
		        	// check unit
				    if (!values.containsKey(WordColumn.UNIT))
				    	throw new IllegalArgumentException("The key unit is not contained!");
				    		
				    // complete/correct box value
				    Integer box = values.getAsInteger(WordColumn.BOX);
				    if ( box == null || box < 0)
				    	values.put(WordColumn.BOX, 0);
				    	
				    // insert rows
				    if(db.insert(WordColumn.TABLE, null, values) != -1)
				    	rowsInserted++;
				   
				    // let other threads doInBackground
		            db.yieldIfContendedSafely();
		        }
		        db.setTransactionSuccessful();
		    } finally {
		        db.endTransaction();
		        
		        // notify observers
		        contentResolver.notifyChange(WORD_URI, null);
		    }
	    } else {
	    	throw new IllegalArgumentException("Unknown DESCRIPTION: " + uri);
	    }
	    return rowsInserted;
	}
    */

} 
