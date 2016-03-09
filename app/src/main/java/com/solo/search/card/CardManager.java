package com.solo.search.card;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.solo.search.SearchActivity;
import com.solo.search.card.entry.AdsEntry;
import com.solo.search.card.entry.CardEntry;
import com.solo.search.card.entry.CurrencyEntry;
import com.solo.search.card.entry.FunnyEntry;
import com.solo.search.card.entry.GameEntry;
import com.solo.search.card.entry.HotnewsEntry;
import com.solo.search.card.entry.HotwordEntry;
import com.solo.search.card.entry.StockEntry;
import com.solo.search.card.entry.VideoEntry;
import com.solo.search.db.CardDBHelper;
import com.solo.search.util.DeviceUtils;
import com.solo.search.util.LogUtils;
import com.solo.search.util.PreferenceConstants;
import com.solo.search.util.ResourceUtil;
import com.solo.search.util.SearchConfig;
import com.solo.search.util.SharedPreferencesHelper;
import com.solo.search.util.VolleyBitmapCache;

public class CardManager {

	private static final String TAG = LogUtils.makeLogTag(CardManager.class);

	private Context mContext;
	private static CardManager sInstance;
	private CardCounter mCardCounter;
	private RequestQueue mRequestQueue;
	private ImageLoader mImageLoader;
	private RequestQueue mImageRequestQueue;
	private RequestQueue mDataRequestQueue;

	private CardManager(Context context) {
		mContext = context;
		mRequestQueue = Volley.newRequestQueue(context);
	}

	public static final CardManager getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new CardManager(context);
		}
		return sInstance;
	}

	public ImageLoader getImageLoader() {
		if (mImageLoader == null) {
			if (mImageRequestQueue == null) {
				mImageRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
			}
			mImageLoader = new ImageLoader(mImageRequestQueue, VolleyBitmapCache.getCache());
		}
		return mImageLoader;
	}

	public RequestQueue getRequestQueue() {
		if (mDataRequestQueue == null) {
			mDataRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
		}
		return mDataRequestQueue;
	}

	public void cancelPendingRequests(Object tag) {
		if (mDataRequestQueue != null) {
			mDataRequestQueue.cancelAll(tag);
			mDataRequestQueue.cancelAll(TAG);
		}
	}

	public <T> void addToRequestQueue(Request<T> req) {
		// set the default tag if tag is empty
		req.setTag(TAG);

		getRequestQueue().add(req);
	}

	private int[] getInitializedCardStates() {
		Resources res = mContext.getResources();
		int[] ids = new int[] { Integer.valueOf(CardConfig.CARD_ID_ADS), Integer.valueOf(CardConfig.CARD_ID_HOTWORD),
				res.getInteger(ResourceUtil.getIntegerId(mContext, "ssearch_hotnews_visibility")),
				res.getInteger(ResourceUtil.getIntegerId(mContext, "ssearch_funny_visibility")),
				res.getInteger(ResourceUtil.getIntegerId(mContext, "ssearch_game_visibility")),
				res.getInteger(ResourceUtil.getIntegerId(mContext, "ssearch_video_visibility")),
				res.getInteger(ResourceUtil.getIntegerId(mContext, "ssearch_stock_visibility")),
				res.getInteger(ResourceUtil.getIntegerId(mContext, "ssearch_currency_visibility")) };
		return ids;
	}

	private int[] getInitializedCardOrders() {
		Resources res = mContext.getResources();
		int[] ids = new int[] { CardConfig.CARD_ORDER_ADS, CardConfig.CARD_ORDER_HOTWORD,
				res.getInteger(ResourceUtil.getIntegerId(mContext, "ssearch_first_visible")),
				res.getInteger(ResourceUtil.getIntegerId(mContext, "ssearch_second_visible")),
				res.getInteger(ResourceUtil.getIntegerId(mContext, "ssearch_third_visible")),
				res.getInteger(ResourceUtil.getIntegerId(mContext, "ssearch_forth_visible")),
				res.getInteger(ResourceUtil.getIntegerId(mContext, "ssearch_fifth_visible")),
				res.getInteger(ResourceUtil.getIntegerId(mContext, "ssearch_sixth_visible")) };
		return ids;
	}

	private int[] getInitializedCardTitleIds() {
		int[] ids = new int[] { ResourceUtil.getStringId(mContext, "ssearch_card_ads"),
				ResourceUtil.getStringId(mContext, "ssearch_card_hotwords"),
				ResourceUtil.getStringId(mContext, "ssearch_card_news"),
				ResourceUtil.getStringId(mContext, "ssearch_solo_funny"),
				ResourceUtil.getStringId(mContext, "ssearch_card_game"),
				ResourceUtil.getStringId(mContext, "ssearch_card_video"),
				ResourceUtil.getStringId(mContext, "ssearch_card_stock"),
				ResourceUtil.getStringId(mContext, "ssearch_card_currency") };
		return ids;
	}

	/**
	 * 第一次初始化卡片数据，把卡片的基本数据存入数据库中。
	 * 
	 * @param context
	 * @param handler
	 */
	public void initCards(final Context context, final Handler handler) {
		boolean isInitializedCards = SharedPreferencesHelper.getBoolean(mContext, SearchConfig.KEY_INITILIZED_CARDS, false);
		if (!isInitializedCards) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					// 第一次初始化卡片数据
					CardDBHelper dbHelper = new CardDBHelper(mContext);
					SQLiteDatabase db = dbHelper.getWritableDatabase();
					// 初始化卡片状态和顺序
					int[] states = getInitializedCardStates();
					int[] orders = getInitializedCardOrders();
					int[] titles = getInitializedCardTitleIds();

					Resources res = mContext.getResources();

					for (int i = 0; i < CardConfig.DEFAULT_CARDS_ID.length; i++) {
						String cardId = CardConfig.DEFAULT_CARDS_ID[i];

						String title = res.getString(titles[i]);
						int cardEnable = states[i];
						int cardOrder = orders[i];

						Cursor cursor = db.query(CardDBHelper.TABLE_NAME, new String[] { CardConfig.CARD_ID },
								CardConfig.CARD_ID + " = ?", new String[] { cardId }, null, null, null);

						ContentValues values = new ContentValues();
						values.put(CardConfig.CARD_ID, cardId);
						values.put(CardConfig.CARD_TITLE, title);
						values.put(CardConfig.CARD_ENABLE, cardEnable);
						values.put(CardConfig.CARD_ORDER, cardOrder);

						LogUtils.d(TAG, TAG + " cardId:" + cardId);

						if (cursor == null || cursor.getCount() == 0) {
							db.insert(CardDBHelper.TABLE_NAME, null, values);
						} else {
							db.update(CardDBHelper.TABLE_NAME, values, CardConfig.CARD_ID + " = ?", new String[] { cardId });
						}

						if (cursor != null) {
							cursor.close();
						}

					}

					db.close();

					// 获取数据
					SharedPreferencesHelper.setBoolean(mContext, SearchConfig.KEY_INITILIZED_CARDS, true);
					CardManager.getInstance(context).getCardEntries(context, handler);
				}
			}).start();

		} else {
			CardManager.getInstance(context).getCardEntries(context, handler);
		}
	}

	/**
	 * 保存单个卡片数据到DB里。
	 * 
	 * @param cardEntry
	 */
	public void saveCardEntryToDB(CardEntry cardEntry) {
		String cardId = cardEntry.getCardId();

		CardDBHelper dbHelper = new CardDBHelper(mContext);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor cursor = db.query(CardDBHelper.TABLE_NAME, new String[] { CardConfig.CARD_ID }, CardConfig.CARD_ID + " = ?",
				new String[] { cardId }, null, null, null);

		ContentValues values = cardEntry.getContentValues();

		LogUtils.d(TAG, TAG + " saveCardEntryToDB cardId:" + cardId);

		if (cursor == null || cursor.getCount() == 0) {
			// mContentResolver.insert(CardDBHelper.CARD_URI, values);
			db.insert(CardDBHelper.TABLE_NAME, null, values);
		} else {
			db.update(CardDBHelper.TABLE_NAME, values, CardConfig.CARD_ID + " = ?", new String[] { cardId });
			// mContentResolver.update(CardDBHelper.CARD_URI, values,
			// CardConfig.CARD_ID + " = ?", new String[] { cardId });
		}

		if (cursor != null) {
			cursor.close();
		}

		db.close();

	}

	private void sendGetOnlineCardEntrySuccMsg(Handler handler, CardEntry cardEntry) {
		LogUtils.d(TAG, TAG + " sendGetOnlineCardEntrySuccMsg");

		if (mCardCounter != null) {
			mCardCounter.onSucceed();
		}

		Message msg = handler.obtainMessage();
		msg.what = SearchActivity.MSG_LOAD_CARD_SUCC;
		msg.obj = cardEntry;
		handler.sendMessage(msg);

		checkLoadedAllCards(handler);
	}

	private void checkLoadedAllCards(Handler handler) {
		if (mCardCounter != null) {
			LogUtils.d(TAG, TAG + " checkLoadedAllCards loadCardsFailedCount:" + mCardCounter.mLoadCardsFailedCount
					+ " loadCardsSucceedCount:" + mCardCounter.mLoadCardsSucceedCount + " cardCount:"
					+ mCardCounter.mCardsCount);
			if (mCardCounter.isFinishedLoadingAllCards()) {
				Message msg = handler.obtainMessage();
				msg.what = SearchActivity.MSG_LOAD_ALL_CARDS_FINISHED;
				handler.sendMessage(msg);
			}
		}
	}

	private void sendGetOnlineCardEntryFailedMsg(Handler handler, String cardId) {
		LogUtils.d(TAG, TAG + " sendGetOnlineCardEntryFailedMsg");

		if (mCardCounter != null) {
			mCardCounter.onFailed();
		}

		Message msg = handler.obtainMessage();
		msg.what = SearchActivity.MSG_LOAD_CARD_FAILED;
		msg.obj = cardId;
		handler.sendMessage(msg);

		checkLoadedAllCards(handler);
	}

	/**
	 * 获取在线卡片数据。
	 * 
	 * @param context
	 * @param cardId
	 * @param cardEnable
	 * @param cardOrder
	 * @param handler
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void getOnlineCardEntry(final Context context, final String cardId, final int cardEnable, final int cardOrder,
			final Handler handler) {
		final String url = CardConfig.getCardUrl(context, cardId);
		Request request = new StringRequest(Method.GET, url, new Listener<String>() {

			@Override
			public void onResponse(String result) {
				if (!TextUtils.isEmpty(result)) {
					CardEntry cardEntry = null;
					try {
						JSONObject object = new JSONObject(result);
						switch (cardId) {
						case CardConfig.CARD_ID_ADS:
							cardEntry = new AdsEntry(context, object);
							cardEntry.setCardTitle(context.getResources().getString(
									ResourceUtil.getStringId(mContext, "ssearch_card_ads")));
							break;
						case CardConfig.CARD_ID_HOTWORD:
							cardEntry = new HotwordEntry(context, object);
							cardEntry.setCardTitle(context.getResources().getString(
									ResourceUtil.getStringId(mContext, "ssearch_card_hotwords")));
							break;
						case CardConfig.CARD_ID_HOTNEWS:
							cardEntry = new HotnewsEntry(context, object);
							cardEntry.setCardTitle(context.getResources().getString(
									ResourceUtil.getStringId(mContext, "ssearch_card_news")));
							break;
						case CardConfig.CARD_ID_VIDEO:
							cardEntry = new VideoEntry(context, object);
							cardEntry.setCardTitle(context.getResources().getString(
									ResourceUtil.getStringId(mContext, "ssearch_card_video")));
							break;
						case CardConfig.CARD_ID_STOCK:
							cardEntry = new StockEntry(context, object);
							cardEntry.setCardTitle(context.getResources().getString(
									ResourceUtil.getStringId(mContext, "ssearch_card_stock")));
							break;
						case CardConfig.CARD_ID_CURRENCY:
							cardEntry = new CurrencyEntry(context, object);
							cardEntry.setCardTitle(context.getResources().getString(
									ResourceUtil.getStringId(mContext, "ssearch_card_currency")));
							break;
						case CardConfig.CARD_ID_GAME:
							cardEntry = new GameEntry(context, object);
							cardEntry.setCardTitle(context.getResources().getString(
									ResourceUtil.getStringId(mContext, "ssearch_card_game")));
							break;
						case CardConfig.CARD_ID_FUNNY:
							cardEntry = new FunnyEntry(context, object);
							cardEntry.setCardId(CardConfig.CARD_ID_FUNNY);
							cardEntry.setCardTitle(context.getResources().getString(
									ResourceUtil.getStringId(mContext, "ssearch_solo_funny")));
							break;
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
					if (cardEntry != null && cardEntry.isContentAvailable()) {
						cardEntry.setCardEnable(cardEnable);
						cardEntry.setCardOrder(cardOrder);
						saveCardEntryToDB(cardEntry);
						sendGetOnlineCardEntrySuccMsg(handler, cardEntry);
					} else {
						sendGetOnlineCardEntryFailedMsg(handler, cardId);
					}
				}
			}
		}, new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError arg0) {
				arg0.printStackTrace();
				sendGetOnlineCardEntryFailedMsg(handler, cardId);
			}
		});
		mRequestQueue.add(request);
	}

	/**
	 * 是否显示广告卡片，广告卡片关闭之后在一定时间内不显示，Facebook Banner显示的时候，广告卡片不显示。
	 * 
	 * @return
	 */
	private boolean showAdsCard() {
		long closeAdsTime = SharedPreferencesHelper.getLong(mContext, PreferenceConstants.KEY_SEARCH_ADS_CLOSE_TIME, 0);

		if (((System.currentTimeMillis() - closeAdsTime) > CardConfig.TIME_RESHOW_ADS)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 是否显示卡片。
	 * 
	 * @param cardId
	 * @param cardEnable
	 * @return
	 */
	private boolean isShowCard(String cardId, int cardEnable) {
		boolean showCard = true;

		if ((cardId.equals(CardConfig.CARD_ID_ADS) && !showAdsCard()) || cardEnable == CardConfig.CARD_STATE_DISABLE) {
			showCard = false;
		}

		return showCard;
	}

	/**
	 * 根据卡片的显隐状态加载卡片数据
	 * 
	 * @param context
	 * @param handler
	 */
	public void getCardEntries(Context context, final Handler handler) {
		// Cursor cursor = mContentResolver.query(CardDBHelper.CARD_URI,
		// CardConfig.CARD_DB_PROJECTION, CardConfig.CARD_ENABLE
		// + " = ?", new String[] { String.valueOf(CardConfig.CARD_STATE_ENABLE)
		// }, CardConfig.CARD_ORDER + " ASC");
		CardDBHelper dbHelper = new CardDBHelper(mContext);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor cursor = db.query(CardDBHelper.TABLE_NAME, CardConfig.CARD_DB_PROJECTION, CardConfig.CARD_ENABLE + " = ?",
				new String[] { String.valueOf(CardConfig.CARD_STATE_ENABLE) }, null, null, CardConfig.CARD_ORDER + " ASC");

		if (cursor != null) {
			// 对卡片加载个数做统计
			mCardCounter = new CardCounter(cursor.getCount());

			Message msg = handler.obtainMessage();
			msg.what = SearchActivity.MSG_LOADING_CARDS;
			handler.sendMessage(msg);

			boolean connected = DeviceUtils.isNetConnected(context);
			try {
				while (cursor.moveToNext()) {
					String cardId = cursor.getString(cursor.getColumnIndexOrThrow(CardConfig.CARD_ID));
					int cardEnable = cursor.getInt(cursor.getColumnIndexOrThrow(CardConfig.CARD_ENABLE));

					if (!isShowCard(cardId, cardEnable)) {
						mCardCounter.onFailed();
						continue;
					} else {
						int cardOrder = cursor.getInt(cursor.getColumnIndexOrThrow(CardConfig.CARD_ORDER));
						long updateInterval = cursor.getLong(cursor.getColumnIndexOrThrow(CardConfig.CARD_UPDATE_INTERVAL));
						long updateTime = cursor.getLong(cursor.getColumnIndexOrThrow(CardConfig.CARD_UPDATE_TIME));
						String cardData = cursor.getString(cursor.getColumnIndexOrThrow(CardConfig.CARD_DATA));

						boolean refreshData = true;
						if (((System.currentTimeMillis() - updateTime) < updateInterval) && !TextUtils.isEmpty(cardData)) {
							refreshData = false;
						}

						LogUtils.d(TAG, TAG + " getCardEntries cardId:" + cardId + " cardOrder:" + cardOrder + " updateTime:"
								+ updateTime + " updateInterval:" + updateInterval + " currTime:" + System.currentTimeMillis()
								+ " refreshData:" + refreshData);

						// 网络正常并且到了需要刷新数据的时间，才需要重新加载数据
						if (refreshData && connected) {
							getOnlineCardEntry(context, cardId, cardEnable, cardOrder, handler);
						} else {
							CardEntry cardEntry = CardFactory.makeCardEntry(context, cardId, cursor);
							if (cardEntry.isContentAvailable()) {
								sendGetOnlineCardEntrySuccMsg(handler, cardEntry);
							} else {
								sendGetOnlineCardEntryFailedMsg(handler, cardId);
							}
						}
					}
				}
			} finally {
				cursor.close();
			}
			db.close();
		}
	}

	private class CardCounter {

		private int mCardsCount;
		private int mLoadedCardsCount;
		private int mLoadCardsFailedCount;
		private int mLoadCardsSucceedCount;

		public CardCounter(int cardsCount) {
			mCardsCount = cardsCount;
			mLoadedCardsCount = 0;
			mLoadCardsFailedCount = 0;
			mLoadCardsFailedCount = 0;
		}

		public void onFailed() {
			mLoadCardsFailedCount += 1;
			mLoadedCardsCount += 1;
		}

		public void onSucceed() {
			mLoadCardsSucceedCount += 1;
			mLoadedCardsCount += 1;
		}

		public boolean isFinishedLoadingAllCards() {
			return mLoadedCardsCount >= mCardsCount;
		}
	}
}
