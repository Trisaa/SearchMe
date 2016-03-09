package com.solo.search.card.entry;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.solo.search.card.CardConfig;
import com.solo.search.card.model.CardItem;
import com.solo.search.card.model.CurrencyItem;
import com.solo.search.util.ResourceUtil;

public class CurrencyEntry extends CardEntry {

	private static final String CARD_CREATE_TIME = "create_time";

	private String mCreateTime;

	public CurrencyEntry(Context context, JSONObject jsonObject) {
		super(context, jsonObject);
		mCardTitle = context.getResources().getString(ResourceUtil.getStringId(context, "ssearch_card_currency"));
		try {
			mCreateTime = jsonObject.getString(CARD_CREATE_TIME);
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	public CurrencyEntry(Context context, Cursor cursor) {
		super(context, cursor);
		mCardTitle = context.getResources().getString(ResourceUtil.getStringId(context, "ssearch_card_currency"));
	}

	@Override
	protected void buildCardItems() {
		if (mCardData != null) {
			try {
				mCardItems = new ArrayList<CardItem>();
				JSONArray itemArray = mCardData.getJSONArray(CardConfig.CARD_ITEMS_DATA);
				for (int i = 0; i < itemArray.length(); i++) {
					JSONObject itemObj = itemArray.getJSONObject(i);
					CurrencyItem item = new CurrencyItem(itemObj);
					mCardItems.add(item);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	public String getCreateTime() {
		return mCreateTime;
	}

	public void setCreateTime(String createTime) {
		mCreateTime = createTime;
	}

	@Override
	protected void setContentValues(ContentValues values) {
		// values.put(CARD_CREATE_TIME, mCreateTime);
	}
}
