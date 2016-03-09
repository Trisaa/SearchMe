package com.solo.search.suggestion;

import java.io.InputStream;
import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.solo.search.util.ImageUtils;
import com.solo.search.util.ResourceUtil;
import com.solo.search.util.SearchPatternLevel;

public class ContactSuggestions extends Suggestions {

	private static final String NAME = "ContactSuggestions";

	private static final Uri URI_CONTACTS = ContactsContract.Contacts.CONTENT_URI;

	private static final String COLUMN_NAME = ContactsContract.Contacts.DISPLAY_NAME;
	private static final String COLUMN_PRESENCE_STATUS = ContactsContract.Contacts.CONTACT_PRESENCE;
	private static final String COLUMN_LOOKUP_KEY = ContactsContract.Contacts.LOOKUP_KEY;
	private static final String COLUMN_VISIBILITY = ContactsContract.Contacts.IN_VISIBLE_GROUP;

	private static final int INDEX_COLUMN_ID = 0;
	private static final int INDEX_COLUMN_NAME = 1;
	private static final int INDEX_COLUMN_LOOKUP_KEY = 2;
	private static final int INDEX_COLUMN_VISIBILITY = 3;

	private static final String[] CONTACTS_PROJECTION = new String[] { ContactsContract.Contacts._ID, COLUMN_NAME,
			COLUMN_PRESENCE_STATUS, COLUMN_LOOKUP_KEY, COLUMN_VISIBILITY };

	private Context mContext;
	private ContentResolver mContentResolver;
	private Drawable mContactDefaultIcon;

	public ContactSuggestions(Context context) {
		mContext = context;
		mContentResolver = context.getContentResolver();
		mContactDefaultIcon = ContextCompat.getDrawable(mContext, ResourceUtil.getDrawableId(mContext, "ssearch_contact"));
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public Suggestion getSuggestion(int id) {
		Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);
		Cursor cursor = mContentResolver.query(uri, CONTACTS_PROJECTION, null, null, null);
		Suggestion suggestion = null;
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();

				String phoneNumber = null;

				int contactId = cursor.getInt(INDEX_COLUMN_ID);
				Cursor phone = mContentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
						ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId, null, null);
				while (phone.moveToNext()) {
					phoneNumber = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
					// 获取第一个号码就行
					if (phoneNumber != null)
						break;
				}
				phone.close();

				suggestion = new ContactSuggestion(this, contactId, phoneNumber, cursor.getString(INDEX_COLUMN_NAME),
						ContactsContract.Contacts.getLookupUri(cursor.getInt(INDEX_COLUMN_ID), cursor
								.getString(INDEX_COLUMN_LOOKUP_KEY)));
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
			cursor = mContentResolver.query(URI_CONTACTS, CONTACTS_PROJECTION, "LOWER("
					+ ContactsContract.Contacts.DISPLAY_NAME + ") LIKE ?", new String[] { searchText + "%" },
					ContactsContract.Contacts.DISPLAY_NAME + " ASC");
			break;
		case SearchPatternLevel.SEARCH_CONTAINS_WORD_STARTS_WITH_TEXT:
			cursor = mContentResolver.query(URI_CONTACTS, CONTACTS_PROJECTION, "LOWER("
					+ ContactsContract.Contacts.DISPLAY_NAME + ") LIKE ?", new String[] { "% " + searchText + "%" },
					ContactsContract.Contacts.DISPLAY_NAME + " ASC");
			break;
		case SearchPatternLevel.SEARCH_CONTAINS_TEXT:
			cursor = mContentResolver.query(URI_CONTACTS, CONTACTS_PROJECTION, "LOWER("
					+ ContactsContract.Contacts.DISPLAY_NAME + ") LIKE ?" + " AND LOWER("
					+ ContactsContract.Contacts.DISPLAY_NAME + ") NOT LIKE ?" + " AND LOWER("
					+ ContactsContract.Contacts.DISPLAY_NAME + ") NOT LIKE ?", new String[] { "%" + searchText + "%",
					searchText + "%", "% " + searchText + "%" }, ContactsContract.Contacts.DISPLAY_NAME + " ASC");
			break;
		case SearchPatternLevel.SEARCH_CONTAINS_EACH_CHAR:
			StringBuilder searchPattern = new StringBuilder();
			for (char c : searchText.toCharArray()) {
				searchPattern.append("%").append(c);
			}
			searchPattern.append("%");

			cursor = mContentResolver.query(URI_CONTACTS, CONTACTS_PROJECTION, "LOWER("
					+ ContactsContract.Contacts.DISPLAY_NAME + ") LIKE ?" + " AND LOWER("
					+ ContactsContract.Contacts.DISPLAY_NAME + ") NOT LIKE ?", new String[] { searchPattern.toString(),
					"%" + searchText + "%" }, ContactsContract.Contacts.DISPLAY_NAME + " ASC");
			break;
		}
		ArrayList<Suggestion> suggestions = new ArrayList<Suggestion>();
		if (cursor != null) {
			if (cursor.getCount() > offset) {
				cursor.moveToFirst();
				cursor.move(offset);
				int i = 0;
				while (!cursor.isAfterLast() && i < limit) {
					if (cursor.getInt(INDEX_COLUMN_VISIBILITY) != 0) {

						String phoneNumber = null;

						int contactId = cursor.getInt(INDEX_COLUMN_ID);
						Cursor phone = mContentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
								ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId, null, null);
						while (phone.moveToNext()) {
							phoneNumber = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
							// 获取第一个号码就行
							if (phoneNumber != null)
								break;
						}
						phone.close();

						ContactSuggestion suggestion = new ContactSuggestion(this, contactId, cursor
								.getString(INDEX_COLUMN_NAME), phoneNumber, ContactsContract.Contacts.getLookupUri(cursor
								.getInt(INDEX_COLUMN_ID), cursor.getString(INDEX_COLUMN_LOOKUP_KEY)));
						suggestions.add(suggestion);

						i++;

					}
					cursor.moveToNext();
				}
			}

			cursor.close();
		}

		return suggestions;
	}

	public Drawable getIcon(Suggestion suggestion) {
		Uri uri = ContentUris.withAppendedId(URI_CONTACTS, suggestion.getId());
		InputStream in = ContactsContract.Contacts.openContactPhotoInputStream(mContentResolver, uri);
		if (in == null) {
			return mContactDefaultIcon;
		} else {
			return ImageUtils.createThumbnail(BitmapFactory.decodeStream(in), mContext);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public Intent getIntent(Suggestion suggestion) {
		Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, suggestion.getId());
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		return intent;
	}

	public boolean smsTo(Suggestion suggestion) {

		if (suggestion instanceof ContactSuggestion) {
			try {
				Uri smsToUri = Uri.parse("smsto:" + suggestion.getInfoText());
				Intent smsIntent = new Intent(android.content.Intent.ACTION_SENDTO, smsToUri);

				mContext.startActivity(smsIntent);
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

	public boolean dial(Suggestion suggestion) {
		if (suggestion instanceof ContactSuggestion) {
			try {
				Uri uri = Uri.parse("tel:" + ((ContactSuggestion) suggestion).getInfoText());

				Intent phoneIntent = new Intent("android.intent.action.CALL", uri);
				mContext.startActivity(phoneIntent);
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
	public boolean launch(Suggestion suggestion) {
		if (suggestion instanceof ContactSuggestion) {
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
