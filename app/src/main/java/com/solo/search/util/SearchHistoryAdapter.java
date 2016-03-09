package com.solo.search.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.solo.search.BaseSearchActivity;
import com.solo.search.suggestion.Suggestion;
import com.solo.search.suggestion.Suggestions;

public class SearchHistoryAdapter extends BaseAdapter {

	private static final String SEARCH_HISTORY_DB = "searchHistory";

	private static final int MSG_INIT_SEARCH_HISTORY = 1;
	private static final int MSG_ADD_LAUNCHABLE_TO_SEARCH_HISTORY = 2;
	private static final int MSG_REMOVE_LAUNCHABLE_FROM_SEARCH_HISTORY = 3;
	private static final int MSG_CLEAR_SEARCH_HISTORY = 4;
	private static final int MAX_SEARCH_HISTORY_SIZE = 10;

	private static HandlerThread sHandlerThread = null;

	private final BaseSearchActivity mSearchActivity;
	private final Context mContext;
	private final SearchHistoryWorker mSearchHistoryWorker;
	private final SuggestionObserver mSuggestionObserver;

	private final ArrayList<Suggestions> mSuggestionsSource;
	private Vector<Suggestion> mSuggestions = new Vector<Suggestion>();
	private final HashMap<Integer, Integer> mSuggestionsSourceIndexes;

	private final int mSuggestionsSourceNum;
	private boolean mSearchHistoryEnabled = true;
	private boolean mCancelInitSearchHistory = false;
	private boolean mPendingListUpdate = false;

	public SearchHistoryAdapter(BaseSearchActivity searchActivity) {
		mSearchActivity = searchActivity;
		mContext = searchActivity;
		mSearchHistoryWorker = new SearchHistoryWorker(mContext);
		mSuggestionsSource = mSearchActivity.getSuggestionsSource();
		mSuggestionsSourceNum = mSuggestionsSource.size();
		mSuggestionsSourceIndexes = new HashMap<Integer, Integer>(mSuggestionsSourceNum);
		mSuggestionObserver = new SuggestionObserver(new Handler());

		for (int i = 0; i < mSuggestionsSourceNum; i++) {
			mSuggestionsSourceIndexes.put(mSuggestionsSource.get(i).getId(), i);
			mSuggestionsSource.get(i).registerContentObserver(mSuggestionObserver);
		}

		mSearchHistoryEnabled = true;
		mSearchHistoryWorker.initSearchHistory(false);
	}

	public void onDestroy() {
		mCancelInitSearchHistory = true;
		for (int i = 0; i < mSuggestionsSourceNum; i++) {
			mSuggestionsSource.get(i).unregisterContentObserver(mSuggestionObserver);
		}
	}

	@Override
	public void notifyDataSetChanged() {
		mPendingListUpdate = false;
		super.notifyDataSetChanged();
	}

	public boolean isListUpdatePending() {
		return mPendingListUpdate;
	}

	public void addSuggestion(Suggestion suggestion, boolean topOfList, boolean updateList, boolean updateDatabase) {
		if (mSearchHistoryEnabled) {
			for (Suggestion l : mSuggestions) {
				if (suggestion.getId() == l.getId() && suggestion.getSuggestions().getId() == l.getSuggestions().getId()) {
					mSuggestions.remove(l);
					break;
				}
			}
			if (topOfList) {
				mSuggestions.add(0, suggestion);
			} else {
				mSuggestions.add(suggestion);
			}
			if (mSuggestions.size() > MAX_SEARCH_HISTORY_SIZE) {
				mSuggestions.setSize(MAX_SEARCH_HISTORY_SIZE);
			}
			if (updateList) {
				notifyDataSetChanged();
			} else {
				mPendingListUpdate = true;
			}

			mSearchActivity.setSearchHistoryVisible(mSuggestions.size() > 0 ? true : false);

			if (updateDatabase) {
				mSearchHistoryWorker.addSuggestion(suggestion);
			}
		}
	}

	public void removeSuggestion(Suggestion suggestion) {
		if (mSearchHistoryEnabled) {
			for (Suggestion item : mSuggestions) {
				if (suggestion.getId() == item.getId() && suggestion.getSuggestions().getId() == item.getSuggestions().getId()) {
					mSuggestions.remove(item);
					break;
				}
			}
			notifyDataSetChanged();
			mSearchHistoryWorker.removeSuggestion(suggestion);
		}
	}

	public void clearSearchHistory(boolean clearSearchHistoryDatabase) {
		mSuggestions.clear();
		notifyDataSetChanged();
		if (clearSearchHistoryDatabase) {
			mSearchHistoryWorker.clearSearchHistory();
		}
		mSearchActivity.setSearchHistoryVisible(false);
	}

	public int getSuggestionsCount() {
		return mSuggestions.size();
	}

	@Override
	public int getCount() {
		return mSuggestions.size();
	}

	@Override
	public Object getItem(int position) {
		if (position < mSuggestions.size()) {
			return mSuggestions.get(position);
		} else {
			return null;
		}
	}

	@Override
	public long getItemId(int position) {
		return mSuggestions.get(position).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(ResourceUtil.getLayoutId(mContext, "ssearch_app_list_item"),
					null);
			viewHolder = new ViewHolder();
			viewHolder.icon = (ImageView) convertView.findViewById(ResourceUtil.getId(mContext, "app_icon"));
			viewHolder.title = (TextView) convertView.findViewById(ResourceUtil.getId(mContext, "app_title"));
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		Suggestion suggestion = mSuggestions.get(position);

		if (suggestion.getIcon() != null) {
			viewHolder.icon.setImageDrawable(suggestion.getIcon());
		} else {
			viewHolder.icon.setVisibility(View.GONE);
		}

		viewHolder.title.setText(suggestion.getTitle());
		viewHolder.title
				.setTextColor(mContext.getResources().getColor(ResourceUtil.getColorId(mContext, "ssearch_card_title")));
		convertView.setBackgroundResource(ResourceUtil.getDrawableId(mContext, "ssearch_selector_list_item"));

		return convertView;
	}

	private class SearchHistoryWorker extends Handler {

		private final AsyncSearchHistoryWorker mAsyncSearchHistoryWorker;
		private final SearchHistoryDatabase mSearchHistoryDatabase;

		public SearchHistoryWorker(Context context) {
			synchronized (SearchHistoryWorker.class) {
				if (sHandlerThread == null) {
					sHandlerThread = new HandlerThread("SearchHistoryWorker");
					sHandlerThread.start();
				}
			}
			mAsyncSearchHistoryWorker = new AsyncSearchHistoryWorker(sHandlerThread.getLooper());
			mSearchHistoryDatabase = new SearchHistoryDatabase(context);
		}

		public void initSearchHistory(boolean clearSearchHistory) {
			Message msg = mAsyncSearchHistoryWorker.obtainMessage();
			msg.what = MSG_INIT_SEARCH_HISTORY;
			msg.arg1 = clearSearchHistory ? 1 : 0;
			msg.obj = this;
			mAsyncSearchHistoryWorker.sendMessage(msg);
		}

		public void addSuggestion(Suggestion suggestion) {
			Message msg = mAsyncSearchHistoryWorker.obtainMessage();
			msg.what = MSG_ADD_LAUNCHABLE_TO_SEARCH_HISTORY;
			msg.obj = suggestion;
			mAsyncSearchHistoryWorker.sendMessage(msg);
		}

		public void removeSuggestion(Suggestion suggestion) {
			Message msg = mAsyncSearchHistoryWorker.obtainMessage();
			msg.what = MSG_REMOVE_LAUNCHABLE_FROM_SEARCH_HISTORY;
			msg.obj = suggestion;
			mAsyncSearchHistoryWorker.sendMessage(msg);
		}

		public void clearSearchHistory() {
			Message msg = mAsyncSearchHistoryWorker.obtainMessage();
			msg.what = MSG_CLEAR_SEARCH_HISTORY;
			mAsyncSearchHistoryWorker.sendMessage(msg);
		}

		@Override
		public void handleMessage(Message msg) {
			int event = msg.what;
			switch (event) {
			case MSG_INIT_SEARCH_HISTORY: {
				Suggestion suggestion = (Suggestion) msg.obj;
				SearchHistoryAdapter.this.addSuggestion(suggestion, false, true, false);
			}
				break;
			case MSG_CLEAR_SEARCH_HISTORY: {
				SearchHistoryAdapter.this.clearSearchHistory(false);
			}
				break;
			}
		}

		private class AsyncSearchHistoryWorker extends Handler {
			public AsyncSearchHistoryWorker(Looper looper) {
				super(looper);
			}

			@Override
			public void handleMessage(Message msg) {
				int event = msg.what;
				switch (event) {
				case MSG_INIT_SEARCH_HISTORY: {
					Handler handler = (Handler) msg.obj;
					boolean clearSearchHistory = (msg.arg1 != 0) ? true : false;
					initSearchHistory(handler, clearSearchHistory);
				}
					break;
				case MSG_ADD_LAUNCHABLE_TO_SEARCH_HISTORY: {
					Suggestion suggestion = (Suggestion) msg.obj;
					addSuggestion(suggestion);
				}
					break;
				case MSG_REMOVE_LAUNCHABLE_FROM_SEARCH_HISTORY: {
					Suggestion suggestion = (Suggestion) msg.obj;
					removeSuggestion(suggestion);
				}
					break;
				case MSG_CLEAR_SEARCH_HISTORY: {
					clearSearchHistory();
				}
					break;
				}
			}

			private void initSearchHistory(Handler handler, boolean clearSearchHistory) {
				if (clearSearchHistory) {
					Message reply = handler.obtainMessage();
					reply.what = MSG_CLEAR_SEARCH_HISTORY;
					reply.sendToTarget();
				}
				SQLiteDatabase db;
				try {
					db = mSearchHistoryDatabase.getWritableDatabase();
				} catch (SQLiteException e) {
					db = null;
				}
				if (db != null) {
					Cursor cursor = db.rawQuery("SELECT " + COLUMN_ID + ", " + COLUMN_SUGGESTIONS_ID + ", "
							+ COLUMN_SUGGESTION_ID + " FROM " + SEARCH_HISTORY_DB + " ORDER BY " + COLUMN_ID + " DESC LIMIT "
							+ MAX_SEARCH_HISTORY_SIZE, null);
					if (cursor != null) {
						if (cursor.getCount() > 0) {
							cursor.moveToFirst();
							while (!cursor.isAfterLast() && !mCancelInitSearchHistory) {
								int suggestionsId = cursor.getInt(INDEX_COLUMN_SUGGESTIONS_ID);
								int suggestionId = cursor.getInt(INDEX_COLUMN_SUGGESTION_ID);
								Integer suggestionsIndex = mSuggestionsSourceIndexes.get(suggestionsId);
								if (suggestionsIndex != null) {
									Suggestion launchable = mSuggestionsSource.get(suggestionsIndex)
											.getSuggestion(suggestionId);
									if (launchable != null) {
										Message reply = handler.obtainMessage();
										reply.what = MSG_INIT_SEARCH_HISTORY;
										reply.obj = launchable;
										reply.sendToTarget();
									}
								}
								cursor.moveToNext();
							}
						}
						cursor.close();
					}
					db.close();
				}

			}

			private void addSuggestion(Suggestion suggestion) {
				SQLiteDatabase db;
				try {
					db = mSearchHistoryDatabase.getWritableDatabase();
				} catch (SQLiteException e) {
					db = null;
				}
				if (db != null) {
					db.execSQL("DELETE FROM " + SEARCH_HISTORY_DB + " WHERE " + COLUMN_SUGGESTIONS_ID + " = "
							+ suggestion.getSuggestions().getId() + " AND " + COLUMN_SUGGESTION_ID + " = " + suggestion.getId());
					db.execSQL("INSERT INTO " + SEARCH_HISTORY_DB + " (" + COLUMN_SUGGESTIONS_ID + ", " + COLUMN_SUGGESTION_ID
							+ ") VALUES " + "('" + suggestion.getSuggestions().getId() + "', '" + suggestion.getId() + "');");
					Cursor cursor = db.rawQuery("SELECT " + COLUMN_ID + " FROM " + SEARCH_HISTORY_DB + " ORDER BY " + COLUMN_ID
							+ " DESC LIMIT " + MAX_SEARCH_HISTORY_SIZE, null);
					if (cursor != null) {
						if (cursor.getCount() >= MAX_SEARCH_HISTORY_SIZE) {
							cursor.moveToLast();
							if (!cursor.isAfterLast()) {
								int id = cursor.getInt(INDEX_COLUMN_ID);
								db.execSQL("DELETE FROM " + SEARCH_HISTORY_DB + " WHERE " + COLUMN_ID + " < " + id);
							}
						}
						cursor.close();
					}
					db.close();
				}
			}

			private void removeSuggestion(Suggestion suggestion) {
				SQLiteDatabase db;
				try {
					db = mSearchHistoryDatabase.getWritableDatabase();
				} catch (SQLiteException e) {
					db = null;
				}
				if (db != null) {
					db.execSQL("DELETE FROM " + SEARCH_HISTORY_DB + " WHERE " + COLUMN_SUGGESTIONS_ID + " = "
							+ suggestion.getSuggestions().getId() + " AND " + COLUMN_SUGGESTION_ID + " = " + suggestion.getId());
					db.close();
				}
			}

			private void clearSearchHistory() {
				SQLiteDatabase db;
				try {
					db = mSearchHistoryDatabase.getWritableDatabase();
				} catch (SQLiteException e) {
					db = null;
				}
				if (db != null) {
					db.delete(SEARCH_HISTORY_DB, null, null);
					db.close();
				}
			}
		}
	}

	private class SuggestionObserver extends ContentObserver {
		public SuggestionObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			mSearchHistoryWorker.initSearchHistory(true);
		}
	}

	private static final int INDEX_COLUMN_ID = 0;
	private static final int INDEX_COLUMN_SUGGESTIONS_ID = 1;
	private static final int INDEX_COLUMN_SUGGESTION_ID = 2;

	private static final String COLUMN_ID = "_id";
	private static final String COLUMN_SUGGESTIONS_ID = "suggestions_id";
	private static final String COLUMN_SUGGESTION_ID = "suggestion_id";

	private static class SearchHistoryDatabase extends SQLiteOpenHelper {

		private static final String DB_NAME = "search_history.db";
		private static final int DB_VERSION = 1;

		public SearchHistoryDatabase(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			updateDatabase(db, 0, DB_VERSION);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			updateDatabase(db, oldVersion, newVersion);
		}

		private void updateDatabase(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("CREATE TABLE IF NOT EXISTS " + SEARCH_HISTORY_DB + " ( " + COLUMN_ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_SUGGESTIONS_ID + " INTEGER, " + COLUMN_SUGGESTION_ID
					+ " INTEGER " + ")");
		}
	}

}