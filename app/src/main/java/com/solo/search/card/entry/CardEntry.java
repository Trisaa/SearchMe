package com.solo.search.card.entry;


import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.solo.search.card.CardConfig;
import com.solo.search.card.model.CardItem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

/**
 * 一个卡片集合的数据
 */
public abstract class CardEntry {

	protected String mCardId;
	protected String mCardTitle;
	protected int mCardEnable;
	protected int mCardOrder;
	protected long mUpdateInteval;
	protected long mUpdateTime;
	private String mMoreUrl;

	protected JSONObject mCardData;
	protected ArrayList<CardItem> mCardItems;

	public CardEntry() {
	}

	public CardEntry(Context context, Cursor cursor) {
		setCardData(cursor);
	}

	public CardEntry(Context context, JSONObject jsonObject) {
		try {
			mCardId = jsonObject.getString(CardConfig.CARD_ID);
			mUpdateInteval = jsonObject.getLong(CardConfig.CARD_UPDATE_INTERVAL);
			mCardData = jsonObject;
			try {
				mUpdateTime = jsonObject.getLong(CardConfig.CARD_UPDATE_TIME);
			} catch (JSONException e) {
				e.printStackTrace();
			}

			try {
				JSONObject menuObject = mCardData.getJSONObject(CardConfig.CARD_MENU_DATA);
				if (menuObject != null) {
					mMoreUrl = menuObject.getString("more");
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

			buildCardItems();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public String getCardId() {
		return mCardId;
	}

	public void setCardId(String cardId) {
		mCardId = cardId;
	}

	public String getCardTitle() {
		return mCardTitle;
	}

	public void setCardTitle(String cardTitle) {
		mCardTitle = cardTitle;
	}

	public int getCardEnable() {
		return mCardEnable;
	}

	public void setCardEnable(int cardEnable) {
		mCardEnable = cardEnable;
	}

	public int getCardOrder() {
		return mCardOrder;
	}

	public void setCardOrder(int cardOrder) {
		mCardOrder = cardOrder;
	}

	public long getUpdateInteval() {
		return mUpdateInteval;
	}

	public void setUpdateInteval(long updateInteval) {
		mUpdateInteval = updateInteval;
	}

	public long getUpdateTime() {
		return mUpdateTime;
	}

	public void setUpdateTime(long updateTime) {
		mUpdateTime = updateTime;
	}

	public String getMoreUrl() {
		return mMoreUrl;
	}

	public void setMoreUrl(String moreUrl) {
		mMoreUrl = moreUrl;
	}

	public JSONObject getCardData() {
		return mCardData;
	}

	public void setCardData(JSONObject cardData) {
		mCardData = cardData;
	}

	private void setCardData(Cursor cursor) {
		if (cursor == null) {
			throw new RuntimeException("Cursor is null when parse data to CardEntry.");
		}
		try {
			String data = cursor.getString(cursor.getColumnIndexOrThrow(CardConfig.CARD_DATA));
			if (!TextUtils.isEmpty(data)) {
				mCardData = new JSONObject(data);
			}

			mCardId = cursor.getString(cursor.getColumnIndexOrThrow(CardConfig.CARD_ID));
			// mCardTitle =
			// cursor.getString(cursor.getColumnIndexOrThrow(CardConfig.CARD_TITLE));
			mCardEnable = cursor.getInt(cursor.getColumnIndexOrThrow(CardConfig.CARD_ENABLE));
			mCardOrder = cursor.getInt(cursor.getColumnIndexOrThrow(CardConfig.CARD_ORDER));
			mUpdateInteval = cursor.getLong(cursor.getColumnIndexOrThrow(CardConfig.CARD_UPDATE_INTERVAL));
			mUpdateTime = cursor.getLong(cursor.getColumnIndexOrThrow(CardConfig.CARD_UPDATE_TIME));

			try {
				JSONObject menuObject = mCardData.getJSONObject(CardConfig.CARD_MENU_DATA);
				if (menuObject != null) {
					mMoreUrl = menuObject.getString("more");
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

			buildCardItems();
		} catch (Exception exception) {
		}
	}

	public void setCardItems(ArrayList<CardItem> cardItems) {
		mCardItems = cardItems;
	}

	public ArrayList<CardItem> getCardItems() {
		if (mCardItems == null || mCardItems.isEmpty()) {
			buildCardItems();
		}
		return mCardItems;
	}

	public ContentValues getContentValues() {
		ContentValues values = new ContentValues();
		values.put(CardConfig.CARD_ID, mCardId);
		values.put(CardConfig.CARD_TITLE, mCardTitle);
		values.put(CardConfig.CARD_ENABLE, mCardEnable);
		values.put(CardConfig.CARD_ORDER, mCardOrder);
		values.put(CardConfig.CARD_UPDATE_INTERVAL, mUpdateInteval);
		values.put(CardConfig.CARD_UPDATE_TIME, mUpdateTime);
		if (mCardData != null) {
			values.put(CardConfig.CARD_DATA, mCardData.toString());
		}

		setContentValues(values);

		return values;
	}

	public boolean isContentAvailable() {
		if (mCardData != null && mCardItems != null && mCardItems.size() > 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 其他字段若需要存取的需要填充相关数据。
	 * 
	 * @param values
	 */
	protected abstract void setContentValues(ContentValues values);

	/**
	 * 负责CardItem的解析。
	 */
	protected abstract void buildCardItems();

}
