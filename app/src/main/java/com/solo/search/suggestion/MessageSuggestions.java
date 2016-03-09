package com.solo.search.suggestion;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.Toast;

import com.solo.search.util.ImageUtils;
import com.solo.search.util.ResourceUtil;
import com.solo.search.util.SearchPatternLevel;

public class MessageSuggestions extends Suggestions {

	private static final String NAME = "MessageSuggestions";

	private static final Uri URI_SMS = Uri.parse("content://sms/");

	private static final String COLUMN_ID = "_id";
	private static final String COLUMN_ADDRESS = "address";
	private static final String COLUMN_BODY = "body";

	private static final String[] SMS_PROJECTION = new String[] { COLUMN_ID, COLUMN_ADDRESS, COLUMN_BODY };

	private Context mContext;
	private ContentResolver mContentResolver;

	public MessageSuggestions(Context context) {
		mContext = context;
		mContentResolver = context.getContentResolver();
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public Suggestion getSuggestion(int id) {
		Cursor cursor = mContentResolver.query(URI_SMS, SMS_PROJECTION, COLUMN_ID + "=" + id, null, null);
		Suggestion suggestion = null;
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				int smsId = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
				String address = cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS));
				String body = cursor.getString(cursor.getColumnIndex(COLUMN_BODY));
				suggestion = new MessageSuggestion(this, smsId, address, body);
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
			cursor = mContentResolver.query(URI_SMS, SMS_PROJECTION, "LOWER(" + COLUMN_BODY + ") LIKE ?",
					new String[] { searchText + "%" }, COLUMN_BODY + " ASC");
			break;
		case SearchPatternLevel.SEARCH_CONTAINS_WORD_STARTS_WITH_TEXT:
			cursor = mContentResolver.query(URI_SMS, SMS_PROJECTION, "LOWER(" + COLUMN_BODY + ") LIKE ?", new String[] { "% "
					+ searchText + "%" }, COLUMN_BODY + " ASC");
			break;
		case SearchPatternLevel.SEARCH_CONTAINS_TEXT:
			cursor = mContentResolver.query(URI_SMS, SMS_PROJECTION, "LOWER(" + COLUMN_BODY + ") LIKE ?" + " AND LOWER("
					+ COLUMN_BODY + ") NOT LIKE ?" + " AND LOWER(" + COLUMN_BODY + ") NOT LIKE ?", new String[] {
					"%" + searchText + "%", searchText + "%", "% " + searchText + "%" }, COLUMN_BODY + " ASC");
			break;
		case SearchPatternLevel.SEARCH_CONTAINS_EACH_CHAR:
			StringBuilder searchPattern = new StringBuilder();
			for (char c : searchText.toCharArray()) {
				searchPattern.append("%").append(c);
			}
			searchPattern.append("%");

			cursor = mContentResolver.query(URI_SMS, SMS_PROJECTION, "LOWER(" + COLUMN_BODY + ") LIKE ?" + " AND LOWER("
					+ COLUMN_BODY + ") NOT LIKE ?", new String[] { searchPattern.toString(), "%" + searchText + "%" },
					COLUMN_BODY + " ASC");
			break;
		}

		ArrayList<Suggestion> suggestions = new ArrayList<Suggestion>();
		if (cursor != null) {
			if (cursor.getCount() > offset) {
				cursor.moveToFirst();
				cursor.move(offset);
				int i = 0;
				while (!cursor.isAfterLast() && i < limit) {
					i++;
					int smsId = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
					String address = cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS));
					String body = cursor.getString(cursor.getColumnIndex(COLUMN_BODY));
					MessageSuggestion suggestion = new MessageSuggestion(this, smsId, address, body);
					suggestions.add(suggestion);
					cursor.moveToNext();
				}
			}

			cursor.close();
		}

		return suggestions;
	}

	public Drawable getIcon(Suggestion suggestion) {
		return ImageUtils.createThumbnail(BitmapFactory.decodeResource(mContext.getResources(), ResourceUtil.getDrawableId(
				mContext, "ssearch_sms")), mContext);
	}

	@Override
	public Intent getIntent(Suggestion suggestion) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		MessageSuggestion sSuggesion = (MessageSuggestion) suggestion;
		intent.putExtra("address", sSuggesion.getTitle());
		intent.setType("vnd.android-dir/mms-sms");
		return intent;
	}

	@Override
	public boolean launch(Suggestion suggestion) {
		if (suggestion instanceof MessageSuggestion) {
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
}
