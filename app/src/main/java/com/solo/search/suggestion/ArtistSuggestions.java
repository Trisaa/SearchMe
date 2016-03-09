package com.solo.search.suggestion;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;

import com.solo.search.util.ResourceUtil;
import com.solo.search.util.SearchPatternLevel;

public class ArtistSuggestions extends Suggestions {

	public static final String NAME = "ArtistSuggestions";

	private static final String[] ARTISTS_PROJECTION = new String[] { MediaStore.Audio.Artists._ID,
			MediaStore.Audio.Artists.ARTIST };

	private static final int INDEX_COLUMN_ID = 0;
	private static final int INDEX_COLUMN_ARTIST = 1;

	private Context mContext;
	private ContentResolver mContentResolver;
	private Drawable mIcon;

	public ArtistSuggestions(Context context) {
		mContext = context;
		mContentResolver = context.getContentResolver();
		mIcon = ContextCompat.getDrawable(mContext, ResourceUtil.getDrawableId(context, "ssearch_music"));
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public Intent getIntent(Suggestion suggestion) {
		return null;
	}

	@Override
	public boolean launch(Suggestion suggestion) {
		return false;
	}

	@Override
	public Suggestion getSuggestion(int id) {
		return null;
	}

	@Override
	public ArrayList<Suggestion> getSuggestions(String searchText, int searchPatternLevel, int offset, int limit) {
		Cursor cursor = null;
		try {
			switch (searchPatternLevel) {
			case SearchPatternLevel.SEARCH_STARTS_WITH_TEXT:
				cursor = mContentResolver.query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, ARTISTS_PROJECTION, "LOWER("
						+ MediaStore.Audio.Artists.ARTIST + ") LIKE ?", new String[] { searchText + "%" },
						MediaStore.Audio.Artists.ARTIST + " ASC");
				break;
			case SearchPatternLevel.SEARCH_CONTAINS_WORD_STARTS_WITH_TEXT:
				cursor = mContentResolver.query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, ARTISTS_PROJECTION, "LOWER("
						+ MediaStore.Audio.Artists.ARTIST + ") LIKE ?", new String[] { "%" + searchText + "%" },
						MediaStore.Audio.Artists.ARTIST + " ASC");
				break;
			case SearchPatternLevel.SEARCH_CONTAINS_TEXT:
				cursor = mContentResolver
						.query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, ARTISTS_PROJECTION, "LOWER("
								+ MediaStore.Audio.Artists.ARTIST + ") LIKE ?" + " AND LOWER("
								+ MediaStore.Audio.Artists.ARTIST + ") NOT LIKE ?" + " AND LOWER("
								+ MediaStore.Audio.Artists.ARTIST + ") NOT LIKE ?", new String[] { "%" + searchText + "%",
								searchText + "%", "%	" + searchText }, MediaStore.Audio.Artists.ARTIST + " ASC");
				break;
			case SearchPatternLevel.SEARCH_CONTAINS_EACH_CHAR:
				StringBuilder searchPattern = new StringBuilder();
				for (char c : searchText.toCharArray()) {
					searchPattern.append("%").append(c);
				}
				searchPattern.append("%");
				cursor = mContentResolver.query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, ARTISTS_PROJECTION, "LOWER ("
						+ MediaStore.Audio.Artists.ARTIST + ") LIKE ?" + " AND LOWER(" + MediaStore.Audio.Artists.ARTIST
						+ ") NOT LIKE ?", new String[] { searchPattern.toString(), "%" + searchText + "%" },
						MediaStore.Audio.Artists.ARTIST + " ASC");

				break;
			}
		} catch (SQLiteException ex) {
		}

		ArrayList<Suggestion> suggestions = new ArrayList<Suggestion>();

		if (cursor != null) {
			if (cursor.getCount() > offset) {
				cursor.moveToFirst();
				cursor.move(offset);
				int i = 0;
				while (!cursor.isAfterLast() && i++ < limit) {
					ArtistSuggestion artistSuggestion = new ArtistSuggestion(this, cursor.getInt(INDEX_COLUMN_ID), cursor
							.getString(INDEX_COLUMN_ARTIST));
					suggestions.add(artistSuggestion);
				}
			}
			cursor.close();
		}

		return suggestions;
	}

	public Drawable getIcon() {
		return mIcon;
	}

}
