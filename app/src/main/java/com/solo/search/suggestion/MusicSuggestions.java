package com.solo.search.suggestion;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.solo.search.util.ResourceUtil;
import com.solo.search.util.SearchPatternLevel;

public class MusicSuggestions extends Suggestions {

	public static final String NAME = "MusicSuggestions";
	private static final Uri URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;// Uri.parse("content://media/external/audio/search/fancy/");
	private static final String[] SONG_PROJECTION = new String[] { MediaStore.Audio.Media._ID, // 0
			MediaStore.Audio.Media.ARTIST, // 1
			MediaStore.Audio.Media.ALBUM, // 2
			MediaStore.Audio.Media.TITLE // 3
	};
	private static final int ID_COLUMN_INDEX = 0;
	private static final int ARTIST_COLUMN_INDEX = 1;
	private static final int ALBUM_COLUMN_INDEX = 2;
	private static final int TITLE_COLUMN_INDEX = 3;

	private Context mContext;
	private ContentResolver mContentResolver;
	private Drawable mIcon;

	public MusicSuggestions(Context context) {
		mContext = context;
		mContentResolver = context.getContentResolver();
		mIcon = ContextCompat.getDrawable(mContext, ResourceUtil.getDrawableId(mContext, "ssearch_music"));
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public ArrayList<Suggestion> getSuggestions(String searchText, int patternMatchingLevel, int offset, int limit) {
		Cursor cursor = null;
		try {
			switch (patternMatchingLevel) {
			case SearchPatternLevel.SEARCH_STARTS_WITH_TEXT:
				cursor = mContentResolver.query(URI, SONG_PROJECTION, MediaStore.Audio.Media.IS_MUSIC + " != 0" + " AND LOWER("
						+ MediaStore.Audio.Media.TITLE + ") LIKE ?", new String[] { searchText + "%" },
						MediaStore.Audio.Media.TITLE + " ASC");
				break;
			case SearchPatternLevel.SEARCH_CONTAINS_WORD_STARTS_WITH_TEXT:
				cursor = mContentResolver.query(URI, SONG_PROJECTION, MediaStore.Audio.Media.IS_MUSIC + " != 0" + " AND LOWER("
						+ MediaStore.Audio.Media.TITLE + ") LIKE ?", new String[] { "% " + searchText + "%" },
						MediaStore.Audio.Media.TITLE + " ASC");
				break;
			case SearchPatternLevel.SEARCH_CONTAINS_TEXT:
				cursor = mContentResolver.query(URI, SONG_PROJECTION, MediaStore.Audio.Media.IS_MUSIC + " != 0" + " AND LOWER("
						+ MediaStore.Audio.Media.TITLE + ") LIKE ?" + " AND LOWER(" + MediaStore.Audio.Media.TITLE
						+ ") NOT LIKE ?" + " AND LOWER(" + MediaStore.Audio.Media.TITLE + ") NOT LIKE ?", new String[] {
						"%" + searchText + "%", searchText + "%", "% " + searchText + "%" }, MediaStore.Audio.Media.TITLE
						+ " ASC");
				break;
			case SearchPatternLevel.SEARCH_CONTAINS_EACH_CHAR:
				String searchPattern = "";
				for (char c : searchText.toCharArray()) {
					searchPattern += "%" + c;
				}
				searchPattern += "%";
				cursor = mContentResolver.query(URI, SONG_PROJECTION, MediaStore.Audio.Media.IS_MUSIC + " != 0" + " AND LOWER("
						+ MediaStore.Audio.Media.TITLE + ") LIKE ?" + " AND LOWER(" + MediaStore.Audio.Media.TITLE
						+ ") NOT LIKE ?", new String[] { searchPattern, "%" + searchText + "%" }, MediaStore.Audio.Media.TITLE
						+ " ASC");
				break;
			}
		} catch (SQLiteException ex) {
			ex.printStackTrace();
		}

		ArrayList<Suggestion> suggestions = new ArrayList<Suggestion>();
		if (cursor != null) {
			if (cursor.getCount() > offset) {
				cursor.moveToFirst();
				cursor.move(offset);
				int i = 0;
				while (!cursor.isAfterLast() && i++ < limit) {
					MusicSuggestion launchable = new MusicSuggestion(this, cursor.getInt(ID_COLUMN_INDEX), cursor
							.getString(TITLE_COLUMN_INDEX), cursor.getString(ARTIST_COLUMN_INDEX) + " - "
							+ cursor.getString(ALBUM_COLUMN_INDEX));
					suggestions.add(launchable);
					cursor.moveToNext();
				}
			}
			cursor.close();
		}

		return suggestions;
	}

	@Override
	public Suggestion getSuggestion(int id) {
		Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
		Cursor cursor = mContentResolver.query(uri, SONG_PROJECTION, null, null, null);
		Suggestion launchable = null;
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				launchable = new MusicSuggestion(this, cursor.getInt(ID_COLUMN_INDEX), cursor.getString(TITLE_COLUMN_INDEX),
						cursor.getString(ARTIST_COLUMN_INDEX) + " - " + cursor.getString(ALBUM_COLUMN_INDEX));
			}
			cursor.close();
		}
		return launchable;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean launch(Suggestion suggestion) {
		if (suggestion instanceof MusicSuggestion) {
			Intent intent = new Intent(Intent.ACTION_VIEW, ContentUris.withAppendedId(
					MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, suggestion.getId()));
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			List<ResolveInfo> list = mContext.getPackageManager().queryIntentActivities(intent,
					PackageManager.MATCH_DEFAULT_ONLY);
			if (list.size() > 0) {
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
		return false;
	}

	public Drawable getIcon(Suggestion launchable) {
		return mIcon;
	}

	@SuppressWarnings("deprecation")
	@Override
	public Intent getIntent(Suggestion launchable) {
		Intent intent = new Intent(Intent.ACTION_VIEW, ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				launchable.getId()));
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		return intent;
	}
}
