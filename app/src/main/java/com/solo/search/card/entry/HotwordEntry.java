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
import com.solo.search.card.model.HotwordItem;
import com.solo.search.util.ResourceUtil;

public class HotwordEntry extends CardEntry {

	public HotwordEntry(Context context, JSONObject jsonObject) {
		super(context, jsonObject);
		mCardTitle = context.getResources().getString(ResourceUtil.getStringId(context, "ssearch_card_hotwords"));
	}

	public HotwordEntry(Context context, Cursor cursor) {
		super(context, cursor);
		mCardTitle = context.getResources().getString(ResourceUtil.getStringId(context, "ssearch_card_hotwords"));
	}

	@Override
	protected void buildCardItems() {
		if (mCardData != null) {
			try {
				mCardItems = new ArrayList<CardItem>();
				JSONArray itemArray = mCardData.getJSONArray(CardConfig.CARD_ITEMS_DATA);
				for (int i = 0; i < itemArray.length(); i++) {
					JSONObject itemObj = itemArray.getJSONObject(i);
					HotwordItem item = new HotwordItem(itemObj);
					mCardItems.add(item);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void setContentValues(ContentValues values) {
		// TODO Auto-generated method stub

	}
}