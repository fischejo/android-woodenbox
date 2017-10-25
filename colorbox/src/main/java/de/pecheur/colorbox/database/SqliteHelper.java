package de.pecheur.colorbox.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import de.pecheur.colorbox.database.columns.*;

public class SqliteHelper extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "6vocabulary.db";

	public SqliteHelper(Context nContext) {
		super(nContext, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
		db.execSQL("PRAGMA foreign_keys=ON");
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
	    // create unit table
		db.execSQL("CREATE TABLE " + UnitColumn.TABLE + "("
				+ UnitColumn._ID + " INTEGER PRIMARY KEY,"
				+ UnitColumn.TITLE + " TEXT,"
				+ UnitColumn.FRONT + " TEXT " +
                    "CHECK ("+ UnitColumn.FRONT+" LIKE '__' AND "+ UnitColumn.FRONT+" < "+ UnitColumn.BACK+"), "
				+ UnitColumn.BACK + " TEXT " +
                    "CHECK ("+ UnitColumn.BACK+" LIKE '__' ), "
                + "UNIQUE("+ UnitColumn.FRONT+", "+ UnitColumn.BACK+"));");


		// create word table
		db.execSQL("CREATE TABLE " + WordColumn.TABLE + "("
				+ WordColumn._ID + " INTEGER PRIMARY KEY,"
				+ WordColumn.UNIT + " INTEGER NOT NULL,"
				+ WordColumn.BOX + " INTEGER" +
                    " DEFAULT "+ WordColumn.MIN_BOX_VALUE+
                    " CHECK ("+ WordColumn.BOX+" BETWEEN "+ WordColumn.MIN_BOX_VALUE+" AND "+ WordColumn.MAX_BOX_VALUE+"),"
				+ WordColumn.TIME + " INTEGER DEFAULT 0 NOT NULL,"
				+ "FOREIGN KEY (" + WordColumn.UNIT + ") REFERENCES "
				+ UnitColumn.TABLE + "(" + UnitColumn._ID
				+ ") ON DELETE CASCADE ON UPDATE CASCADE);");

        // TODO unique is missing
        // create text table
        db.execSQL("CREATE TABLE " + TextColumn.TABLE + "("
                + TextColumn._ID + " INTEGER PRIMARY KEY,"
                + TextColumn.WORD + " INTEGER NOT NULL,"
                + TextColumn.SIDE + " INTEGER DEFAULT 0 CHECK ( "+TextColumn.SIDE+" IN (0, 1)),"
                + TextColumn.DATA + " TEXT, "

                + "FOREIGN KEY (" + TextColumn.WORD + ") REFERENCES "
                + WordColumn.TABLE + "(" + WordColumn._ID
                + ") ON DELETE CASCADE ON UPDATE CASCADE);");

        // TODO unique is missing
        // create example table
        db.execSQL("CREATE TABLE " + ExampleColumn.TABLE + "("
                + ExampleColumn._ID + " INTEGER PRIMARY KEY,"
                + ExampleColumn.WORD + " INTEGER NOT NULL,"
                + ExampleColumn.SIDE + " INTEGER DEFAULT 0 CHECK ( "+ExampleColumn.SIDE+" IN (0, 1)),"
                + ExampleColumn.DATA + " TEXT, "

                + "FOREIGN KEY (" + ExampleColumn.WORD + ") REFERENCES "
                + WordColumn.TABLE + "(" + WordColumn._ID
                + ") ON DELETE CASCADE ON UPDATE CASCADE);");

        // TODO unique is missing
        // create audio table
        db.execSQL("CREATE TABLE " + AudioColumn.TABLE + "("
                + AudioColumn._ID + " INTEGER PRIMARY KEY,"
                + AudioColumn.WORD + " INTEGER NOT NULL,"
                + AudioColumn.SIDE + " INTEGER DEFAULT 0 CHECK ( "+AudioColumn.SIDE+" IN (0, 1)),"
                + AudioColumn.DATA + " TEXT, "
                + AudioColumn.MIME + " TEXT CHECK( "+AudioColumn.MIME+" LIKE 'audio/%'),"
                + AudioColumn.DESCRIPTION + " TEXT, "
                + AudioColumn.SOURCE + " TEXT, "

                // TODO: add 'ON DELETE CASCADE' clause
                + "FOREIGN KEY (" + AudioColumn.WORD + ") REFERENCES "
                + WordColumn.TABLE + "(" + WordColumn._ID
                + ") ON UPDATE CASCADE);");
	}



	@Override
	public void onUpgrade(SQLiteDatabase db, int old, int now) {

	}



}