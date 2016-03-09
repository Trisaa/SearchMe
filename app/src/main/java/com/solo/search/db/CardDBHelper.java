package com.solo.search.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CardDBHelper extends SQLiteOpenHelper {

	private static final int DB_VERSION = 1;
	private static final String DB_NAME = "card_info.db";
	public static final String TABLE_NAME = "card_info";

	public CardDBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createDB(db);
	}

	private void createDB(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS card_info (_id INTEGER PRIMARY KEY, card_id TEXT, card_title TEXT, card_enable INTEGER, card_order INTEGER, update_interval LONG, update_time LONG, card_data TEXT);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

}
