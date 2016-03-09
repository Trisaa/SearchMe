package com.solo.search.suggestion;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.solo.search.db.AppDBHelper;
import com.solo.search.util.LogUtils;
import com.solo.search.util.ResourceUtil;
import com.solo.search.util.SearchPatternLevel;

public class AppSuggestions extends Suggestions {

	public static final String TAG = LogUtils.makeLogTag(AppSuggestions.class);

	public static final String[] APPS_PROJECTION = new String[] { AppDBHelper.COLUMN_ID, AppDBHelper.COLUMN_TITLE,
			AppDBHelper.COLUMN_PACKAGE, AppDBHelper.COLUMN_CLASS };

	public static final int INDEX_COLUMN_ID = 0;
	public static final int INDEX_COLUMN_TITLE = 1;
	public static final int INDEX_COLUMN_PACKAGE = 2;
	public static final int INDEX_COLUMN_CLASS = 3;

	private Context mContext;
	private PackageManager mPackageManager;

	public AppSuggestions(Context context) {
		mContext = context;
		mPackageManager = context.getPackageManager();
	}

	@Override
	public String getName() {
		return TAG;
	}

	@Override
	public Intent getIntent(Suggestion suggestion) {
		AppSuggestion appSuggestion = (AppSuggestion) suggestion;
		return appSuggestion.getIntent();
	}

	@Override
	public boolean launch(Suggestion suggestion) {
		if (suggestion instanceof AppSuggestion) {
			AppSuggestion appLaunchable = (AppSuggestion) suggestion;
			List<ResolveInfo> list = mContext.getPackageManager().queryIntentActivities(appLaunchable.getIntent(),
					PackageManager.MATCH_DEFAULT_ONLY);
			if (list.size() > 0) {
				try {
					mContext.startActivity(appLaunchable.getIntent());
				} catch (Exception e) {
					Toast.makeText(mContext, "Sorry: Cannot launch \"" + appLaunchable.getTitle() + "\"", Toast.LENGTH_SHORT)
							.show();
					return false;
				}
				return true;
			}
		}
		return false;

	}

	@Override
	public Suggestion getSuggestion(int id) {
		AppDBHelper dbHelper = new AppDBHelper(mContext);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Suggestion launchable = null;
		Cursor cursor = db.query(AppDBHelper.APPS_TABLE, APPS_PROJECTION, AppDBHelper.COLUMN_ID + " = ?", new String[] { String
				.valueOf(id) }, null, null, null);
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_LAUNCHER);
				intent.setClassName(cursor.getString(INDEX_COLUMN_PACKAGE), cursor.getString(INDEX_COLUMN_CLASS));
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				Drawable thumbnail = getThumbnail(cursor.getInt(INDEX_COLUMN_ID), intent);
				launchable = new AppSuggestion(this, cursor.getInt(INDEX_COLUMN_ID), cursor.getString(INDEX_COLUMN_TITLE),
						intent, thumbnail);
			}
			cursor.close();
		}
		db.close();
		return launchable;

	}

	private Drawable getThumbnail(int id, Intent intent) {
		Drawable thumbnail = null;
		try {
			thumbnail = mPackageManager.getActivityIcon(intent);
		} catch (Exception e) {
			thumbnail = ContextCompat.getDrawable(mContext, ResourceUtil.getDrawableId(mContext, "ssearch_sym_def_app_icon"));

		}
		return thumbnail;
	}

	@Override
	public ArrayList<Suggestion> getSuggestions(String searchText, int searchPatternLevel, int offset, int limit) {
		AppDBHelper dbHelper = new AppDBHelper(mContext);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor cursor = null;
		switch (searchPatternLevel) {
		case SearchPatternLevel.SEARCH_STARTS_WITH_TEXT:
			cursor = db.query(AppDBHelper.APPS_TABLE, APPS_PROJECTION, "LOWER(" + AppDBHelper.COLUMN_TITLE + ") LIKE ?",
					new String[] { searchText + "%" }, null, null, AppDBHelper.COLUMN_TITLE + " ASC");
			break;
		case SearchPatternLevel.SEARCH_CONTAINS_WORD_STARTS_WITH_TEXT:
			cursor = db.query(AppDBHelper.APPS_TABLE, APPS_PROJECTION, "LOWER(" + AppDBHelper.COLUMN_TITLE + ") LIKE ?"
					+ " AND LOWER(" + AppDBHelper.COLUMN_TITLE + ") NOT LIKE ?", new String[] { "% " + searchText + "%",
					searchText + "%" }, null, null, AppDBHelper.COLUMN_TITLE + " ASC");
			break;
		case SearchPatternLevel.SEARCH_CONTAINS_TEXT:
			cursor = db.query(AppDBHelper.APPS_TABLE, APPS_PROJECTION, "LOWER(" + AppDBHelper.COLUMN_TITLE + ") LIKE ?"
					+ " AND LOWER(" + AppDBHelper.COLUMN_TITLE + ") NOT LIKE ?" + " AND LOWER(" + AppDBHelper.COLUMN_TITLE
					+ ") NOT LIKE ?", new String[] { "%" + searchText + "%", searchText + "%", "% " + searchText + "%" }, null,
					null, AppDBHelper.COLUMN_TITLE + " ASC");
			break;
		case SearchPatternLevel.SEARCH_CONTAINS_EACH_CHAR:
			StringBuilder searchPattern = new StringBuilder();

			for (char c : searchText.toCharArray()) {
				searchPattern.append("%").append(c);
			}
			searchPattern.append("%");
			cursor = db.query(AppDBHelper.APPS_TABLE, APPS_PROJECTION, "LOWER(" + AppDBHelper.COLUMN_TITLE
					+ ") LIKE ? AND LOWER(" + AppDBHelper.COLUMN_TITLE + ") NOT LIKE ?", new String[] {
					searchPattern.toString(), "%" + searchText + "%" }, null, null, AppDBHelper.COLUMN_TITLE + " ASC");
			break;
		}

		ArrayList<Suggestion> suggestions = new ArrayList<Suggestion>();

		if (cursor != null) {
			if (cursor.getCount() > offset) {
				cursor.moveToFirst();
				cursor.move(offset);
				int i = 0;
				while (!cursor.isAfterLast() && i++ < limit) {
					String packageName = cursor.getString(INDEX_COLUMN_PACKAGE);
					Intent intent = new Intent(Intent.ACTION_MAIN);
					intent.addCategory(Intent.CATEGORY_LAUNCHER);
					intent.setClassName(packageName, cursor.getString(INDEX_COLUMN_CLASS));
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
					Drawable thumbnail = getThumbnail(cursor.getInt(INDEX_COLUMN_ID), intent);
					suggestions.add(new AppSuggestion(this, cursor.getInt(INDEX_COLUMN_ID), cursor
							.getString(INDEX_COLUMN_TITLE), intent, thumbnail));
					cursor.moveToNext();
				}
			}
			cursor.close();
		}
		db.close();

		if (suggestions.size() > 1) {
			AppComparator comparator = new AppComparator();
			Collections.sort(suggestions, comparator);
		}
		return suggestions;

	}

	private class AppComparator implements Comparator<Suggestion> {

		private final Collator sCollator = Collator.getInstance();

		@Override
		public int compare(Suggestion app1, Suggestion app2) {
			String app1Name = app1.getTitle();
			String app2Name = app2.getTitle();
			return sCollator.compare(app1Name, app2Name);
		}
	}
}
