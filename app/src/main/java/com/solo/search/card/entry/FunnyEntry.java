package com.solo.search.card.entry;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.solo.search.card.CardConfig;
import com.solo.search.card.FunnyCard;
import com.solo.search.card.model.CardItem;
import com.solo.search.card.model.FunnyItem;
import com.solo.search.util.ResourceUtil;

public class FunnyEntry extends CardEntry {

	private int mNativeAdsPosition = FunnyCard.ITEM_SIZE;

	public FunnyEntry(Context context, JSONObject jsonObject) {
		super(context, jsonObject);
		mCardTitle = context.getResources().getString(ResourceUtil.getStringId(context, "ssearch_solo_funny"));
		try {
			mNativeAdsPosition = jsonObject.getInt("native_ads_position");
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	public FunnyEntry(Context context, Cursor cursor) {
		super(context, cursor);
		mCardTitle = context.getResources().getString(ResourceUtil.getStringId(context, "ssearch_solo_funny"));
		if (cursor == null) {
			throw new RuntimeException("Cursor is null when parse data to FunnyEntry.");
		}
		try {
			if (mCardData != null) {
				mNativeAdsPosition = mCardData.getInt("native_ads_position");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void buildCardItems() {
		if (mCardData != null) {
			try {
				mCardItems = new ArrayList<CardItem>();
				JSONArray itemArray = mCardData.getJSONArray(CardConfig.CARD_ITEMS_DATA);
				for (int i = 0; i < itemArray.length(); i++) {
					JSONObject itemObj = itemArray.getJSONObject(i);
					FunnyItem item = new FunnyItem(itemObj);
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

	public int getNativeAdsPosition() {
		return mNativeAdsPosition;
	}

}
