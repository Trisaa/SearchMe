package com.solo.search.card.entry;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.solo.search.card.CardConfig;
import com.solo.search.card.HotnewsCard;
import com.solo.search.card.model.CardItem;
import com.solo.search.card.model.HotnewsItem;
import com.solo.search.util.ResourceUtil;

public class HotnewsEntry extends CardEntry {

	private int mNativeAdsPosition = HotnewsCard.HOTNEWS_SIZE;

	public HotnewsEntry(Context context, JSONObject jsonObject) {
		super(context, jsonObject);
		mCardTitle = context.getResources().getString(ResourceUtil.getStringId(context, "ssearch_card_news"));
		try {
			mNativeAdsPosition = jsonObject.getInt("native_ads_position");
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	public HotnewsEntry(Context context, Cursor cursor) {
		super(context, cursor);
		mCardTitle = context.getResources().getString(ResourceUtil.getStringId(context, "ssearch_card_news"));
		if (cursor == null) {
			throw new RuntimeException("Cursor is null when parse data to HotnewsEntry.");
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
					HotnewsItem item = new HotnewsItem(itemObj);
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
