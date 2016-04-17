package com.solo.search;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.solo.search.card.BaseCard;
import com.solo.search.card.CardConfig;
import com.solo.search.card.CardFactory;
import com.solo.search.card.CardManager;
import com.solo.search.card.entry.CardEntry;
import com.solo.search.util.AppLauncher;
import com.solo.search.util.IntentUtils;
import com.solo.search.util.ResourceUtil;
import com.solo.search.util.SearchConfig;
import com.solo.search.util.SharedPreferencesHelper;
import com.solo.search.widget.FloatingActionButton;
import com.solo.search.widget.HomePageLayout;

public class SearchActivity extends BaseTranslucentStatusActivity implements OnClickListener, AdListener,
		OnSharedPreferenceChangeListener {

	public static final int MSG_LOADING_CARDS = 0;// 卡片数据正在加载中
	public static final int MSG_LOAD_CARD_SUCC = 1;// 卡片加载成功
	public static final int MSG_LOAD_CARD_FAILED = 2;// 卡片加载失败
	public static final int MSG_LOAD_ALL_CARDS_FINISHED = 4;// 完成所有Card的加载
	public static final int MSG_REFRESH_CARD_VIEWS = 5;// 刷新卡片视图

	public static final int REQ_MANAGE_STOCKS = 0;
	public static final int REQ_REFRESH_CARDS = 1;

	private RelativeLayout mAdBannerContainer;
	private HomePageLayout mCardsContainer;
	private FloatingActionButton mCardManageBtn;
	private LinearLayout mNetworkInvalidLayout;
	private ProgressBar mLoadingBar;

	private LinearLayout mSearchEngineLayout;
	private ImageView mSearchEngineIcon;
	private EditText mSearchEditText;
	private LinearLayout mSearchIconLayout;
	private AdView mAdViewBanner;
	private boolean mShowCardManage = true;// 默认是显示卡片管理的

	private HashMap<String, BaseCard> mCards = new HashMap<String, BaseCard>();

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_LOADING_CARDS:
				setNetworkInvalidLayoutVisible(false);
				mLoadingBar.setVisibility(View.VISIBLE);
				break;
			case MSG_LOAD_CARD_SUCC:
				mLoadingBar.setVisibility(View.GONE);
				setNetworkInvalidLayoutVisible(false);
				Object data = msg.obj;
				if (data != null) {
					CardEntry cardEntry = (CardEntry) data;
					addCardView(cardEntry);
				}
				break;
			case MSG_LOAD_CARD_FAILED:
				mLoadingBar.setVisibility(View.GONE);
				setNetworkInvalidLayoutVisible(false);
				break;
			case MSG_REFRESH_CARD_VIEWS:
				refreshAllCardViews();
				break;
			case MSG_LOAD_ALL_CARDS_FINISHED:
				boolean isAllCardsLoadedFailed = (mCardsContainer.getChildCount() == 0);
				if (isAllCardsLoadedFailed) {
					mLoadingBar.setVisibility(View.GONE);
					setNetworkInvalidLayoutVisible(true);
				} else {
					if (mShowCardManage) {
						mCardManageBtn.setVisibility(View.VISIBLE);
					}
				}
				break;
			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);

		setContentView(ResourceUtil.getLayoutId(this, "ssearch_activity_search"));

		initView();
		initListener();
		initData();
	}

	private void setupSearchIcon() {
		int searchEngineType = Integer.valueOf(SharedPreferencesHelper.getString(this, SearchConfig.KEY_SEARCH_ENGINE,
				String.valueOf(getResources().getInteger(ResourceUtil.getIntegerId(this, "ssearch_config_search_engine")))));
		switch (searchEngineType) {
		case SearchConfig.SEARCH_ENGINE_SOLO:
			mSearchEngineIcon.setImageResource(ResourceUtil.getDrawableId(this, "ssearch_engine_solo"));
			break;
		case SearchConfig.SEARCH_ENGINE_GOOGLE:
			mSearchEngineIcon.setImageResource(ResourceUtil.getDrawableId(this, "ssearch_engine_google"));
			break;
		case SearchConfig.SEARCH_ENGINE_BING:
			mSearchEngineIcon.setImageResource(ResourceUtil.getDrawableId(this, "ssearch_engine_bing"));
			break;
		case SearchConfig.SEARCH_ENGINE_YAHOO:
			mSearchEngineIcon.setImageResource(ResourceUtil.getDrawableId(this, "ssearch_engine_yahoo"));
			break;
		case SearchConfig.SEARCH_ENGINE_BAIDU:
			mSearchEngineIcon.setImageResource(ResourceUtil.getDrawableId(this, "ssearch_engine_baidu"));
			break;
		case SearchConfig.SEARCH_ENGINE_DUCKDUCKGO:
			mSearchEngineIcon.setImageResource(ResourceUtil.getDrawableId(this, "ssearch_engine_duckduckgo"));
			break;
		case SearchConfig.SEARCH_ENGINE_AOL:
			mSearchEngineIcon.setImageResource(ResourceUtil.getDrawableId(this, "ssearch_engine_aol"));
			break;
		}
	}

	private void setNetworkInvalidLayoutVisible(boolean visible) {
		if (visible) {
			mCardsContainer.setVisibility(View.GONE);
			mNetworkInvalidLayout.setVisibility(View.VISIBLE);
		} else {
			mCardsContainer.setVisibility(View.VISIBLE);
			mNetworkInvalidLayout.setVisibility(View.GONE);
		}
	}

	private void initBanner() {
		String facebookBannerId = getResources().getString(ResourceUtil.getStringId(this, "ssearch_fb_banner_id"));
		if (facebookBannerId == null) {
			facebookBannerId = CardConfig.FACEBOOK_BANNER_PLACEMENT_ID;
		}
		mAdViewBanner = new AdView(this, facebookBannerId, AdSize.BANNER_HEIGHT_50);
		mAdViewBanner.setAdListener(this);
		mAdViewBanner.loadAd();
		mAdBannerContainer.addView(mAdViewBanner);
	}

	private void initView() {
		mShowCardManage = getResources().getBoolean(ResourceUtil.getBoolId(this, "ssearch_config_card_manage"));
		mSearchEngineLayout = (LinearLayout) findViewById(ResourceUtil.getId(this, "search_engine_layout"));
		mSearchEngineIcon = (ImageView) findViewById(ResourceUtil.getId(this, "search_engine_icon"));
		mSearchEditText = (EditText) findViewById(ResourceUtil.getId(this, "search_edit"));
		mSearchIconLayout = (LinearLayout) findViewById(ResourceUtil.getId(this, "search_icon_layout"));
		mSearchEditText.setFocusable(false);

		mAdBannerContainer = (RelativeLayout) findViewById(ResourceUtil.getId(this, "ad_banner_container"));
		mNetworkInvalidLayout = (LinearLayout) findViewById(ResourceUtil.getId(this, "no_data_layout"));
		mCardsContainer = (HomePageLayout) findViewById(ResourceUtil.getId(this, "card_container_ll"));
		mCardManageBtn = (FloatingActionButton) findViewById(ResourceUtil.getId(this, "card_manage_btn"));
		mLoadingBar = (ProgressBar) findViewById(ResourceUtil.getId(this, "loading_bar"));
		if (!mShowCardManage) {
			mCardManageBtn.setVisibility(View.GONE);
		}

		Intent intent = getIntent();
		if (intent != null) {
			String searchText = intent.getStringExtra(IntentUtils.EXTRA_SEARCH_HINT_TEXT);
			if (!TextUtils.isEmpty(searchText)) {
				mSearchEditText.setHint(searchText);
			}
		}

		setupSearchIcon();
		setNetworkInvalidLayoutVisible(false);
		initBanner();
	}

	private void initListener() {
		mSearchEngineLayout.setOnClickListener(this);
		mSearchEditText.setOnClickListener(this);
		mSearchIconLayout.setOnClickListener(this);
		mCardManageBtn.setOnClickListener(this);

		findViewById(ResourceUtil.getId(this, "connect_retry")).setOnClickListener(this);
		findViewById(ResourceUtil.getId(this, "funny_btn")).setOnClickListener(this);
		findViewById(ResourceUtil.getId(this, "game_center_btn")).setOnClickListener(this);
		findViewById(ResourceUtil.getId(this, "news_btn")).setOnClickListener(this);

		getSharedPreferences(SharedPreferencesHelper.DEFAULT_NAME, Context.MODE_PRIVATE)
				.registerOnSharedPreferenceChangeListener(this);
	}

	private void initData() {
		CardManager.getInstance(this).initCards(this, mHandler);
	}

	@Override
	protected void onDestroy() {
		if (mAdViewBanner != null) {
			mAdViewBanner.destroy();
			mAdViewBanner = null;
		}
		super.onDestroy();
	}

	private int findCardViewIndex(CardEntry cardEntry) {
		int cardViewCount = mCardsContainer.getChildCount();
		int cardOrder = cardEntry.getCardOrder();
		int cardIndex = 0;
		for (int i = 0; i < cardViewCount; i++) {
			View cardView = mCardsContainer.getChildAt(i);
			int curCardOrder = (int) cardView.getTag();
			if (cardOrder > curCardOrder) {
				cardIndex++;
			}
		}
		cardIndex = Math.min(cardIndex, cardViewCount);
		return cardIndex;
	}

	private void addCardView(CardEntry cardEntry) {
		mCardsContainer.setVisibility(View.VISIBLE);
		BaseCard card = CardFactory.makeCard(this, cardEntry);
		String cardId = card.getCardId();
		if (!mCards.containsKey(cardId)) {
			View view = card.getCardView();
			view.setTag(cardEntry.getCardOrder());
			mCardsContainer.addView(view, findCardViewIndex(cardEntry));
			mCards.put(card.getCardId(), card);
		}
	}

	/**
	 * 刷新卡片页面
	 */
	private void refreshAllCardViews() {
		if (mCards != null) {
			mCards.clear();
			mCardsContainer.removeAllViews();
			CardManager.getInstance(this).getCardEntries(this, mHandler);
			mCardManageBtn.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case REQ_MANAGE_STOCKS:
			case REQ_REFRESH_CARDS:
				refreshAllCardViews();
				break;
			case AppLauncher.VOICE_RECOGNITION_REQUEST_CODE:
				ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
				if (matches.size() > 0) {
					AppLauncher.launchSearch(SearchActivity.this, matches.get(0));
				}
				break;
			}
		}
	}

	private void launchSearchActivity() {
		Intent intent = new Intent(this, SearchLocalActivity.class);
		String searchText = null;
		if (!TextUtils.isEmpty(mSearchEditText.getHint())) {
			searchText = mSearchEditText.getHint().toString();
		}
		intent.putExtra(IntentUtils.EXTRA_SEARCH_HINT_TEXT, searchText);
		startActivity(intent);
		overridePendingTransition(0, 0);
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

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == ResourceUtil.getId(this, "search_edit")) {
			launchSearchActivity();
		} else if (id == ResourceUtil.getId(this, "search_icon_layout")) {
			searchOnline();
		} else if (id == ResourceUtil.getId(this, "connect_retry")) {
			setNetworkInvalidLayoutVisible(false);
			CardManager.getInstance(this).getCardEntries(this, mHandler);
		} else if (id == ResourceUtil.getId(this, "card_manage_btn")) {
			Intent intent = new Intent(SearchActivity.this, CardManageActivity.class);
			startActivityForResult(intent, REQ_REFRESH_CARDS);
		} else if (id == ResourceUtil.getId(this, "search_engine_layout")) {
			/*
			 * startActivity(new Intent(SearchActivity.this,
			 * SearchSettingsActivity.class));
			 */
		} else if (id == ResourceUtil.getId(this, "funny_btn")) {
			AppLauncher.launchBrowser(this, CardConfig.URL_SOLO_FUNNY_PICTURES_ICON);
		} else if (id == ResourceUtil.getId(this, "game_center_btn")) {
			AppLauncher.launchBrowser(this, CardConfig.URL_GAME_CENTER_ICON);
		} else if (id == ResourceUtil.getId(this, "news_btn")) {
			AppLauncher.launchBrowser(this, CardConfig.URL_SOLO_NEWS_ICON);
		}
	}

	@Override
	public void onAdClicked(Ad arg0) {
	}

	@Override
	public void onAdLoaded(Ad arg0) {
		mAdBannerContainer.setVisibility(View.VISIBLE);
	}

	@Override
	public void onError(Ad arg0, AdError arg1) {
		mAdBannerContainer.setVisibility(View.GONE);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(SearchConfig.KEY_SEARCH_ENGINE)) {
			setupSearchIcon();
		}
	}
}
