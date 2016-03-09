package com.solo.search.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.solo.search.util.LogUtils;

public class AppDBHelper extends SQLiteOpenHelper {

	private static final String TAG = LogUtils.makeLogTag(AppDBHelper.class);

	public static final int STATE_OUT_OF_SYNC = 0;
	public static final int STATE_SYNC = 1;

	private static final String DB_NAME = "applications.db";
	private static final int DB_VERSION = 1;
	public static final String APPS_TABLE = "apps";

	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_TITLE = "title";
	public static final String COLUMN_PACKAGE = "package";
	public static final String COLUMN_CLASS = "class";
	public static final String COLUMN_INTENT = "intent";
	public static final String COLUMN_CATEGORY = "category";

	public AppDBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		LogUtils.d(TAG, "AppDatabase...");
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		LogUtils.d(TAG, "AppDatabase...onCreate");

		db.execSQL("CREATE TABLE IF NOT EXISTS " + APPS_TABLE + " ( " + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ COLUMN_TITLE + " TEXT, " + COLUMN_PACKAGE + " TEXT, " + COLUMN_CLASS + " TEXT, " + COLUMN_INTENT + " TEXT, "
				+ COLUMN_CATEGORY + " TEXT " + ")");
		db.execSQL("CREATE INDEX IF NOT EXISTS AppsIndex ON Apps (" + COLUMN_TITLE + ")");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

}
