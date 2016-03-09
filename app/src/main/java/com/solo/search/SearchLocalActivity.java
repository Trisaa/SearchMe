package com.solo.search;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.solo.search.base.OnAppLaunchListener;
import com.solo.search.card.CardConfig;
import com.solo.search.card.CardFactory;
import com.solo.search.card.CardManager;
import com.solo.search.card.HotwordCard;
import com.solo.search.card.entry.CardEntry;
import com.solo.search.card.entry.HotwordEntry;
import com.solo.search.db.CardDBHelper;
import com.solo.search.suggestion.AlbumSuggestions;
import com.solo.search.suggestion.AppSuggestions;
import com.solo.search.suggestion.ArtistSuggestions;
import com.solo.search.suggestion.ContactSuggestions;
import com.solo.search.suggestion.MessageSuggestions;
import com.solo.search.suggestion.MusicSuggestions;
import com.solo.search.suggestion.Suggestion;
import com.solo.search.suggestion.Suggestions;
import com.solo.search.suggestion.WebSuggestions;
import com.solo.search.util.AppLauncher;
import com.solo.search.util.AppSyncWorker;
import com.solo.search.util.IntentUtils;
import com.solo.search.util.ResourceUtil;
import com.solo.search.util.SearchConfig;
import com.solo.search.util.SearchHelper;
import com.solo.search.util.SearchHistoryAdapter;
import com.solo.search.util.SearchResultAdapter;
import com.solo.search.util.SharedPreferencesHelper;
import com.solo.search.util.SystemBarTintManager;
import com.solo.search.widget.InnerScrollGridView;
import com.solo.search.widget.SuggestionPanelView;

public class SearchLocalActivity extends BaseSearchActivity implements OnClickListener, OnAppLaunchListener,
		OnSharedPreferenceChangeListener {

	private static final int MSG_LOAD_LOCAL_HOTWORDS_SUCC = 0;
	private static final int MSG_LOAD_LOCAL_HOTWORDS_FAIL = 1;
	private static final int MSG_LOAD_ONLINE_HOTWORDS_SUCC = 2;
	private static final int MSG_LOAD_ONLINE_HOTWORDS_FAIL = 3;

	private LinearLayout mEngineLayout;
	private ImageView mEngineIcon;
	private EditText mSearchEditText;

	private LinearLayout mSuggestionHistoryLayout;
	private ImageView mCleanHistoryLayout;
	private LinearLayout mHotwordContainer;
	private SuggestionPanelView mSuggesPanelView;
	private InnerScrollGridView mHistoryGridView;
	private LinearLayout mClearBtn;
	private LinearLayout mSearchBtn;

	private ArrayList<Suggestions> mSuggestionsSource;

	private SearchResultAdapter mSearchResultAdapter;
	private SearchHistoryAdapter mSearchHistoryAdapter;

	private RequestQueue mRequestQueue;

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_LOAD_LOCAL_HOTWORDS_SUCC:
			case MSG_LOAD_ONLINE_HOTWORDS_SUCC:
				CardEntry cardEntry = (CardEntry) msg.obj;
				HotwordCard card = new HotwordCard(SearchLocalActivity.this, cardEntry);
				card.setHeaderVisivility(View.GONE);
				mHotwordContainer.addView(card.getCardView());
				mHotwordContainer.setVisibility(View.VISIBLE);
				break;
			case MSG_LOAD_LOCAL_HOTWORDS_FAIL:
				loadOnlineHotwords();
				break;
			case MSG_LOAD_ONLINE_HOTWORDS_FAIL:
				mHotwordContainer.setVisibility(View.GONE);
				break;
			}
		}

	};

	@TargetApi(19)
	private void setTranslucentStatus(boolean on) {
		Window win = getWindow();
		WindowManager.LayoutParams winParams = win.getAttributes();
		final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
		if (on) {
			winParams.flags |= bits;
		} else {
			winParams.flags &= ~bits;
		}
		win.setAttributes(winParams);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			setTranslucentStatus(true);
			SystemBarTintManager tintManager = new SystemBarTintManager(this);
			tintManager.setStatusBarTintEnabled(true);
			tintManager.setStatusBarTintColor(getResources().getColor(ResourceUtil.getColorId(this, "ssearch_theme_primary")));
		}

		setContentView(ResourceUtil.getLayoutId(this, "ssearch_activity_search_all"));

		initView();
		setListener();
		initData();

		AppSyncWorker.start(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mSearchResultAdapter.onDestroy();
		mSearchHistoryAdapter.onDestroy();
	}

	@Override
	protected void onStop() {
		super.onStop();
		mRequestQueue.cancelAll(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		overridePendingTransition(0, 0);

		if (mSearchHistoryAdapter.isListUpdatePending()) {
			mSearchHistoryAdapter.notifyDataSetChanged();
		}
		if (!TextUtils.isEmpty(mSearchEditText.getText().toString())) {
			showInputMethod();
		}
	}

	private InputMethodManager getInputMethodManager() {
		return (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	}

	private void showInputMethod() {
		InputMethodManager imm = getInputMethodManager();
		if (imm != null) {
			mSearchEditText.setFocusable(true);
			mSearchEditText.setFocusableInTouchMode(true);
			mSearchEditText.requestFocus();
			mHandler.postDelayed(mShowInputMethodTask, 0);
		}
	}

	private void initView() {
		mEngineLayout = (LinearLayout) findViewById(ResourceUtil.getId(this, "search_engine_layout"));
		mEngineIcon = (ImageView) findViewById(ResourceUtil.getId(this, "search_engine_icon"));
		mSearchEditText = (EditText) findViewById(ResourceUtil.getId(this, "search_edit"));

		mSuggesPanelView = (SuggestionPanelView) findViewById(ResourceUtil.getId(this, "search_sugges_panel"));
		mSuggesPanelView.setSearchActivity(this);

		mSuggestionHistoryLayout = (LinearLayout) findViewById(ResourceUtil.getId(this, "search_sugess_list_layout"));
		mHistoryGridView = (InnerScrollGridView) findViewById(ResourceUtil.getId(this, "search_sugess_list"));
		mCleanHistoryLayout = (ImageView) findViewById(ResourceUtil.getId(this, "search_clean_layout"));
		mHotwordContainer = (LinearLayout) findViewById(ResourceUtil.getId(this, "hotword_container"));

		mClearBtn = (LinearLayout) findViewById(ResourceUtil.getId(this, "search_clear_layout"));
		mSearchBtn = (LinearLayout) findViewById(ResourceUtil.getId(this, "search_icon_layout"));

		Intent intent = getIntent();
		if (intent != null) {
			String searchText = intent.getStringExtra(IntentUtils.EXTRA_SEARCH_TEXT);
			if (!TextUtils.isEmpty(searchText)) {
				mSearchEditText.setText(searchText);
			} else {
				String searchHintText = intent.getStringExtra(IntentUtils.EXTRA_SEARCH_HINT_TEXT);
				mSearchEditText.setHint(searchHintText);
			}
		}

		setupEngineIcon();

	}

	private void initSuggestions() {
		mSuggestionsSource = new ArrayList<Suggestions>();

		int maxSuggestionsSize = SearchHelper.getMaxSuggestionsSize();
		int searchPatternLevel = SearchHelper.getSearchPatternLevel(this);

		if (SearchHelper.isSearchAppActive(this)) {
			Suggestions appSuggestions = new AppSuggestions(this);
			appSuggestions.setMaxSuggestions(Integer.MAX_VALUE);
			appSuggestions.setSearchPatternLevel(searchPatternLevel);
			mSuggestionsSource.add(appSuggestions);
		}

		if (SearchHelper.isSearchContactActive(this)) {
			Suggestions contactSuggestions = new ContactSuggestions(this);
			contactSuggestions.setMaxSuggestions(maxSuggestionsSize);
			contactSuggestions.setSearchPatternLevel(searchPatternLevel);
			mSuggestionsSource.add(contactSuggestions);
		}

		if (SearchHelper.isSearchMessageActive(this)) {
			Suggestions smsSuggestions = new MessageSuggestions(this);
			smsSuggestions.setMaxSuggestions(maxSuggestionsSize);
			smsSuggestions.setSearchPatternLevel(searchPatternLevel);
			mSuggestionsSource.add(smsSuggestions);
		}

		if (SearchHelper.isSearchMusicActive(this)) {
			MusicSuggestions musicSuggestions = new MusicSuggestions(this);
			musicSuggestions.setMaxSuggestions(maxSuggestionsSize);
			musicSuggestions.setSearchPatternLevel(searchPatternLevel);
			mSuggestionsSource.add(musicSuggestions);

			AlbumSuggestions albumSuggestions = new AlbumSuggestions(this);
			albumSuggestions.setMaxSuggestions(maxSuggestionsSize);
			albumSuggestions.setSearchPatternLevel(searchPatternLevel);
			mSuggestionsSource.add(albumSuggestions);

			ArtistSuggestions artistSuggestions = new ArtistSuggestions(this);
			artistSuggestions.setMaxSuggestions(maxSuggestionsSize);
			artistSuggestions.setSearchPatternLevel(searchPatternLevel);
			mSuggestionsSource.add(artistSuggestions);

		}

		if (SearchHelper.isSearchWebActivie(this)) {
			WebSuggestions webSuggestions = new WebSuggestions(this, SearchHelper.getSearchSource(this));
			webSuggestions.setMaxSuggestions(SearchConfig.MAX_SUGGESTIONS_SIZE_WEB);
			webSuggestions.setSearchPatternLevel(searchPatternLevel);
			mSuggestionsSource.add(webSuggestions);
		}

		mSearchResultAdapter = new SearchResultAdapter(this, mSuggesPanelView);
		mSearchHistoryAdapter = new SearchHistoryAdapter(this);
		mHistoryGridView.setAdapter(mSearchHistoryAdapter);

	}

	private void initHotwords() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				Message msg = new Message();

				CardDBHelper dbHelper = new CardDBHelper(SearchLocalActivity.this);
				SQLiteDatabase db = dbHelper.getWritableDatabase();
				try {
					Cursor cursor = db.query(CardDBHelper.TABLE_NAME, CardConfig.CARD_DB_PROJECTION, CardConfig.CARD_ID
							+ " = ?", new String[] { String.valueOf(CardConfig.CARD_ID_HOTWORD) }, null, null,
							CardConfig.CARD_ORDER + " ASC");

					if (cursor != null) {
						while (cursor.moveToNext()) {
							CardEntry cardEntry = CardFactory.makeCardEntry(SearchLocalActivity.this, CardConfig.CARD_ID_HOTWORD,
									cursor);
							msg.obj = cardEntry;
							break;
						}
						cursor.close();
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				db.close();

				CardEntry cardEntry = (CardEntry) msg.obj;
				if (cardEntry != null && cardEntry.isContentAvailable()) {
					msg.what = MSG_LOAD_LOCAL_HOTWORDS_SUCC;
				} else {
					msg.what = MSG_LOAD_LOCAL_HOTWORDS_FAIL;
				}

				mHandler.sendMessage(msg);
			}
		}).start();
	}

	private void setupEngineIcon() {
		int searchEngineType = Integer.valueOf(SharedPreferencesHelper.getString(this, SearchConfig.KEY_SEARCH_ENGINE, String
				.valueOf(getResources().getInteger(ResourceUtil.getIntegerId(this, "ssearch_config_search_engine")))));
		switch (searchEngineType) {
		case SearchConfig.SEARCH_ENGINE_SOLO:
			mEngineIcon.setImageResource(ResourceUtil.getDrawableId(this, "ssearch_engine_solo"));
			break;
		case SearchConfig.SEARCH_ENGINE_GOOGLE:
			mEngineIcon.setImageResource(ResourceUtil.getDrawableId(this, "ssearch_engine_google"));
			break;
		case SearchConfig.SEARCH_ENGINE_BING:
			mEngineIcon.setImageResource(ResourceUtil.getDrawableId(this, "ssearch_engine_bing"));
			break;
		case SearchConfig.SEARCH_ENGINE_YAHOO:
			mEngineIcon.setImageResource(ResourceUtil.getDrawableId(this, "ssearch_engine_yahoo"));
			break;
		case SearchConfig.SEARCH_ENGINE_BAIDU:
			mEngineIcon.setImageResource(ResourceUtil.getDrawableId(this, "ssearch_engine_baidu"));
			break;
		case SearchConfig.SEARCH_ENGINE_DUCKDUCKGO:
			mEngineIcon.setImageResource(ResourceUtil.getDrawableId(this, "ssearch_engine_duckduckgo"));
			break;
		case SearchConfig.SEARCH_ENGINE_AOL:
			mEngineIcon.setImageResource(ResourceUtil.getDrawableId(this, "ssearch_engine_aol"));
			break;
		}

	}

	private void hideInputMethod() {
		InputMethodManager imm = getInputMethodManager();
		if (imm != null) {
			imm.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);
		}
	}

	private void setListener() {
		mEngineLayout.setOnClickListener(this);
		mHistoryGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				hideInputMethod();
				Suggestion suggestion = (Suggestion) mSearchHistoryAdapter.getItem(position);
				suggestion.launch();
			}
		});
		mClearBtn.setOnClickListener(this);
		mSearchBtn.setOnClickListener(this);
		mCleanHistoryLayout.setOnClickListener(this);
		mSearchEditText.addTextChangedListener(new SearchTextWatcher());
		mSearchEditText.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
					Suggestion suggestion = (Suggestion) mSearchResultAdapter.getItem(0);
					if (suggestion != null) {
						InputMethodManager imm = SearchLocalActivity.this.getInputMethodManager();
						if (imm != null) {
							imm.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);
						}
					}
					return true;
				}
				return false;
			}
		});
		mSearchEditText.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				searchOnline();
				return false;
			}
		});
		mSuggesPanelView.setOnAppLaunchListener(this);

		getSharedPreferences(SharedPreferencesHelper.DEFAULT_NAME, Context.MODE_PRIVATE)
				.registerOnSharedPreferenceChangeListener(this);

	}

	/**
	 * 获取在线热词
	 */
	private void loadOnlineHotwords() {
		final String url = CardConfig.getCardUrl(this, CardConfig.CARD_ID_HOTWORD);
		final Message msg = new Message();
		StringRequest request = new StringRequest(Method.GET, url, new Listener<String>() {

			@Override
			public void onResponse(String result) {
				if (!TextUtils.isEmpty(result)) {
					CardEntry cardEntry = null;
					try {
						JSONObject object = new JSONObject(result);
						cardEntry = new HotwordEntry(SearchLocalActivity.this, object);
						cardEntry.setCardTitle(SearchLocalActivity.this.getResources().getString(
								ResourceUtil.getStringId(SearchLocalActivity.this, "ssearch_card_hotwords")));
					} catch (JSONException e) {
						e.printStackTrace();
					}

					if (cardEntry != null && cardEntry.isContentAvailable()) {
						msg.obj = cardEntry;
						CardManager.getInstance(SearchLocalActivity.this).saveCardEntryToDB(cardEntry);
						msg.what = MSG_LOAD_ONLINE_HOTWORDS_SUCC;
					} else {
						msg.what = MSG_LOAD_ONLINE_HOTWORDS_FAIL;
					}
					mHandler.sendMessage(msg);
				}
			}
		}, new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError arg0) {
				arg0.printStackTrace();
				msg.what = MSG_LOAD_ONLINE_HOTWORDS_FAIL;
				mHandler.sendMessage(msg);
			}
		});
		mRequestQueue.add(request);

	}

	private void initData() {
		mRequestQueue = Volley.newRequestQueue(this);
		initSuggestions();
		initHotwords();
	}

	public ArrayList<Suggestions> getSuggestionsSource() {
		return mSuggestionsSource;
	}

	public void onSuggestionLaunch(Suggestion suggestion) {
		mSearchHistoryAdapter.addSuggestion(suggestion, true, false, true);
		hideInputMethod();
	}

	public void setSearchHistoryVisible(boolean visible) {
		mSuggestionHistoryLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
		mCleanHistoryLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
	}

	private final Runnable mShowInputMethodTask = new Runnable() {
		public void run() {
			InputMethodManager imm = getInputMethodManager();
			if (imm != null) {
				imm.showSoftInput(mSearchEditText, 0);
			}
		}
	};

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(SearchConfig.KEY_SEARCH_ENGINE)) {
			initSuggestions();
			setupEngineIcon();
		} else if (key.equals(SearchConfig.KEY_APP) || key.equals(SearchConfig.KEY_CONTACT)
				|| key.equals(SearchConfig.KEY_MUSIC) || key.equals(SearchConfig.KEY_WEB)) {
			initSuggestions();
		}
	}

	private void clearHistory() {
		mCleanHistoryLayout.setVisibility(View.GONE);
		mSearchHistoryAdapter.clearSearchHistory(true);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == ResourceUtil.getId(this, "search_clear_layout")) {
			mSearchEditText.setText("");
		} else if (id == ResourceUtil.getId(this, "search_icon_layout")) {
			searchOnline();
		} else if (id == ResourceUtil.getId(this, "search_clean_layout")) {
			clearHistory();
		} else if (id == ResourceUtil.getId(this, "search_engine_layout")) {
			// startActivity(new Intent(SearchAllActivity.this,
			// SearchSettingsActivity.class));
		}

	}

	private void searchOnline() {
		String searchText = mSearchEditText.getText().toString();
		if (TextUtils.isEmpty(searchText)) {
			String defaultHint = getString(ResourceUtil.getStringId(this, "ssearch_edit_hint"));
			CharSequence currHint = mSearchEditText.getHint();
			if (!TextUtils.isEmpty(currHint) && !defaultHint.equals(currHint.toString())) {
				searchText = mSearchEditText.getHint().toString();
				AppLauncher.launchSearch(this, searchText);
			}
		} else {
			AppLauncher.launchSearch(this, searchText);
		}
	}

	private void searchLocal(String keyword) {
		mSuggestionHistoryLayout.setVisibility(View.GONE);
		mCleanHistoryLayout.setVisibility(View.GONE);
		mHotwordContainer.setVisibility(View.GONE);
		mSuggesPanelView.setVisibility(View.VISIBLE);
		mClearBtn.setVisibility(View.VISIBLE);
		mSearchResultAdapter.search(keyword);
	}

	private class SearchTextWatcher implements TextWatcher {

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void afterTextChanged(Editable s) {
		}

		@Override
		public void onTextChanged(CharSequence searchText, int start, int before, int count) {
			if (searchText.length() > 0) {
				searchLocal(mSearchEditText.getText().toString());
			} else {
				mClearBtn.setVisibility(View.GONE);
				mHotwordContainer.setVisibility(View.VISIBLE);
				mSearchResultAdapter.search(null);
				if (mSearchHistoryAdapter.getSuggestionsCount() == 0) {
					mSuggestionHistoryLayout.setVisibility(View.GONE);
					mCleanHistoryLayout.setVisibility(View.GONE);
				} else {
					mCleanHistoryLayout.setVisibility(View.VISIBLE);
					mSuggestionHistoryLayout.setVisibility(View.VISIBLE);
				}
			}
		}

	}

	@Override
	public void setProgressBarIndeterminateVisibility() {
	}

	@Override
	public void onAppLaunch() {
		hideInputMethod();
	}

}
