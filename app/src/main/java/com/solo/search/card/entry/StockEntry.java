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
import com.solo.search.card.model.StockItem;
import com.solo.search.util.ResourceUtil;

public class StockEntry extends CardEntry {

	public StockEntry(Context context, JSONObject jsonObject) {
		super(context, jsonObject);
		mCardTitle = context.getResources().getString(ResourceUtil.getStringId(context, "ssearch_card_stock"));
	}

	public StockEntry(Context context, Cursor cursor) {
		super(context, cursor);
		mCardTitle = context.getResources().getString(ResourceUtil.getStringId(context, "ssearch_card_stock"));
	}

	@Override
	protected void buildCardItems() {
		if (mCardData != null) {
			try {
				mCardItems = new ArrayList<CardItem>();
				JSONArray itemArray = mCardData.getJSONArray(CardConfig.CARD_ITEMS_DATA);
				for (int i = 0; i < itemArray.length(); i++) {
					JSONObject itemObj = itemArray.getJSONObject(i);
					StockItem item = new StockItem(itemObj);
					mCardItems.add(item);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void setContentValues(ContentValues values) {
	}

}
