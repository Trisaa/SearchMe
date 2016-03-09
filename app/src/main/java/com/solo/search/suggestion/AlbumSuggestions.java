package com.solo.search.suggestion;

import java.util.ArrayList;

import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.solo.search.util.ResourceUtil;
import com.solo.search.util.SearchPatternLevel;

public class AlbumSuggestions extends Suggestions {

	private static final String NAME = "AlbumSuggestions";

	private static final String[] ALBUMS_PROJECTION = new String[] { MediaStore.Audio.Albums._ID,
			MediaStore.Audio.Albums.ARTIST, MediaStore.Audio.Albums.ALBUM };

	private static final int INDEX_COLUMN_ID = 0;
	private static final int INDEX_COLUMN_ARTIST = 1;
	private static final int INDEX_COLUMN_ALBUM = 2;

	private Context mContext;
	private ContentResolver mContentResolver;
	private Drawable mIcon;

	public AlbumSuggestions(Context context) {
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
		if (suggestion instanceof AlbumSuggestion) {
			Intent intent = new Intent(MediaStore.INTENT_ACTION_MEDIA_SEARCH);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			String title = suggestion.getTitle();
			intent.putExtra(MediaStore.EXTRA_MEDIA_FOCUS, MediaStore.Audio.Albums.ENTRY_CONTENT_TYPE);
			intent.putExtra(SearchManager.QUERY, title);
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
		Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, id);
		Cursor cursor = mContentResolver.query(uri, ALBUMS_PROJECTION, null, null, null);
		Suggestion suggestion = null;
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				suggestion = new AlbumSuggestion(this, cursor.getInt(INDEX_COLUMN_ID), cursor.getString(INDEX_COLUMN_ALBUM),
						cursor.getString(INDEX_COLUMN_ARTIST));
			}
			cursor.close();
		}
		return suggestion;
	}

	@Override
	public ArrayList<Suggestion> getSuggestions(String searchText, int searchPatternLevel, int offset, int limit) {
		Cursor cursor = null;
		try {
			switch (searchPatternLevel) {
			case SearchPatternLevel.SEARCH_STARTS_WITH_TEXT:
				cursor = mContentResolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, ALBUMS_PROJECTION, "LOWER("
						+ MediaStore.Audio.Albums.ALBUM + ") LIKE ?", new String[] { searchText + "%" },
						MediaStore.Audio.Albums.ALBUM + " ASC");
				break;
			case SearchPatternLevel.SEARCH_CONTAINS_WORD_STARTS_WITH_TEXT:
				cursor = mContentResolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, ALBUMS_PROJECTION, "LOWER("
						+ MediaStore.Audio.Albums.ALBUM + ") LIKE ?", new String[] { "%	" + searchPatternLevel + "%" },
						MediaStore.Audio.Albums.ALBUM + " ASC");
				break;
			case SearchPatternLevel.SEARCH_CONTAINS_TEXT:
				cursor = mContentResolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, ALBUMS_PROJECTION, "LOWER("
						+ MediaStore.Audio.Albums.ALBUM + ") LIKE ?" + " AND LOWER(" + MediaStore.Audio.Albums.ALBUM
						+ ") NOT LIKE ?" + " AND LOWER(" + MediaStore.Audio.Albums.ALBUM + ") NOT LIKE ?", new String[] {
						"%" + searchText + "%", searchText + "%", "%	" + searchText + "%" }, MediaStore.Audio.Albums.ALBUM
						+ " ASC");
				break;
			case SearchPatternLevel.SEARCH_CONTAINS_EACH_CHAR:
				StringBuilder sb = new StringBuilder();
				for (char c : searchText.toCharArray()) {
					sb.append("%").append(c);
				}
				sb.append("%");
				cursor = mContentResolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, ALBUMS_PROJECTION, "LOWER("
						+ MediaStore.Audio.Albums.ALBUM + ") LIKE ?" + " AND LOWER(" + MediaStore.Audio.Albums.ALBUM
						+ ") NOT LIKE ?", new String[] { sb.toString(), "%" + searchText + "%" }, MediaStore.Audio.Albums.ALBUM
						+ " ASC");
				break;
			}
		} catch (Exception e) {

		}
		ArrayList<Suggestion> suggestions = new ArrayList<Suggestion>();
		if (cursor != null) {
			if (cursor.getCount() > offset) {
				cursor.moveToFirst();
				cursor.move(offset);
				int i = 0;
				while (!cursor.isAfterLast() && i++ < limit) {
					AlbumSuggestion albumSuggestion = new AlbumSuggestion(this, cursor.getInt(INDEX_COLUMN_ID), cursor
							.getString(INDEX_COLUMN_ALBUM), cursor.getString(INDEX_COLUMN_ARTIST));
					suggestions.add(albumSuggestion);
					cursor.moveToNext();
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
