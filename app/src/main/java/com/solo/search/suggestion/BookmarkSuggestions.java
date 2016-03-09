package com.solo.search.suggestion;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Browser;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.solo.search.util.ResourceUtil;
import com.solo.search.util.SearchPatternLevel;

public class BookmarkSuggestions extends Suggestions {

	private static final String NAME = "BookmarkSuggestions";

	private static final String[] BOOKMARKS_PROJECTION = new String[] { Browser.BookmarkColumns._ID,
			Browser.BookmarkColumns.TITLE, Browser.BookmarkColumns.URL };

	private static final int INDEX_COLUMN_ID = 0;
	private static final int INDEX_COLUMN_TITLE = 1;
	private static final int INDEX_COLUMN_URL = 2;

	private Context mContext;
	private ContentResolver mContentResolver;
	private Drawable mBookmarkIcon;

	public BookmarkSuggestions(Context context) {
		mContext = context;
		mContentResolver = context.getContentResolver();
		mBookmarkIcon = ContextCompat.getDrawable(mContext, ResourceUtil.getDrawableId(mContext, "ssearch_bookmark"));
	}

	@Override
	public String getName() {
		return NAME;
	}

	public Drawable getIcon() {
		return mBookmarkIcon;
	}

	@SuppressWarnings("deprecation")
	@Override
	public Intent getIntent(Suggestion suggestion) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(((BookmarkSuggestion) suggestion).getUrl()));
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		return intent;
	}

	@Override
	public boolean launch(Suggestion suggestion) {
		if (suggestion instanceof BookmarkSuggestion) {
			Intent intent = getIntent(suggestion);
			try {
				mContext.startActivity(intent);
			} catch (Exception e) {
				Toast.makeText(
						mContext,
						mContext.getResources().getString(ResourceUtil.getStringId(mContext, "ssearch_launch_fail"),
								suggestion.getTitle()), Toast.LENGTH_SHORT).show();
				return false;
			}

		}
		return true;
	}

	@Override
	public Suggestion getSuggestion(int id) {
		Uri uri = ContentUris.withAppendedId(Browser.BOOKMARKS_URI, id);
		Cursor cursor = mContentResolver.query(uri, BOOKMARKS_PROJECTION, null, null, null);
		Suggestion suggestion = null;
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				suggestion = new BookmarkSuggestion(this, cursor.getInt(INDEX_COLUMN_ID), cursor.getString(INDEX_COLUMN_TITLE),
						cursor.getString(INDEX_COLUMN_URL));
			}
			cursor.close();
		}
		return suggestion;
	}

	@Override
	public ArrayList<Suggestion> getSuggestions(String searchText, int searchPatternLevel, int offset, int limit) {

		Cursor cursor = null;
		switch (searchPatternLevel) {
		case SearchPatternLevel.SEARCH_STARTS_WITH_TEXT:
			cursor = mContentResolver.query(Browser.BOOKMARKS_URI, BOOKMARKS_PROJECTION, Browser.BookmarkColumns.BOOKMARK
					+ " == 1" + " AND LOWER(" + Browser.BookmarkColumns.TITLE + ") LIKE ?", new String[] { searchText + "%" },
					Browser.BookmarkColumns.TITLE + " ASC");
			break;
		case SearchPatternLevel.SEARCH_CONTAINS_WORD_STARTS_WITH_TEXT:
			cursor = mContentResolver.query(Browser.BOOKMARKS_URI, BOOKMARKS_PROJECTION, Browser.BookmarkColumns.BOOKMARK
					+ " == 1" + " AND LOWER(" + Browser.BookmarkColumns.TITLE + ") LIKE ?", new String[] { "% " + searchText
					+ "%" }, Browser.BookmarkColumns.TITLE + " ASC");
			break;
		case SearchPatternLevel.SEARCH_CONTAINS_TEXT:
			cursor = mContentResolver.query(Browser.BOOKMARKS_URI, BOOKMARKS_PROJECTION, Browser.BookmarkColumns.BOOKMARK
					+ " == 1" + " AND LOWER(" + Browser.BookmarkColumns.TITLE + ") LIKE ?" + " AND LOWER("
					+ Browser.BookmarkColumns.TITLE + ") NOT LIKE ?" + " AND LOWER(" + Browser.BookmarkColumns.TITLE
					+ ") NOT LIKE ?", new String[] { "%" + searchText + "%", searchText + "%", "%	" + searchText + "%" },
					Browser.BookmarkColumns.TITLE + " ASC");
			break;
		case SearchPatternLevel.SEARCH_CONTAINS_EACH_CHAR:
			StringBuilder sb = new StringBuilder();
			for (char c : searchText.toCharArray()) {
				sb.append("%").append(c);
			}
			sb.append("%");
			cursor = mContentResolver.query(Browser.BOOKMARKS_URI, BOOKMARKS_PROJECTION, Browser.BookmarkColumns.BOOKMARK
					+ " == 1" + " AND LOWER(" + Browser.BookmarkColumns.TITLE + ") LIKE ?" + " AND LOWER("
					+ Browser.BookmarkColumns.TITLE + ") NOT LIKE?", new String[] { sb.toString(), "%" + searchText + "%" },
					Browser.BookmarkColumns.TITLE + " ASC");
			break;
		}
		ArrayList<Suggestion> suggestions = new ArrayList<Suggestion>();
		if (cursor != null) {
			if (cursor.getCount() > offset) {
				cursor.moveToFirst();
				cursor.move(offset);
				int i = 0;
				while (!cursor.isAfterLast() && i++ < limit) {
					BookmarkSuggestion bookmarkSuggestion = new BookmarkSuggestion(this, cursor.getInt(INDEX_COLUMN_ID), cursor
							.getString(INDEX_COLUMN_TITLE), cursor.getString(INDEX_COLUMN_URL));
					suggestions.add(bookmarkSuggestion);
					cursor.moveToNext();
				}
			}
			cursor.close();
		}

		return suggestions;
	}

	public Drawable getThumbnail(Suggestion launchable) {
		return mBookmarkIcon;
	}

}
