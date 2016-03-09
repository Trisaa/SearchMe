package com.solo.search.card.model;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class CardItem {

	protected String mTitle;
	protected JSONObject mJsonObject;

	public CardItem() {
	}

	public CardItem(JSONObject jsonObject) {
		if (jsonObject != null) {
			mJsonObject = jsonObject;
			try {
				mTitle = mJsonObject.getString("title");
				parseCardItem();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String title) {
		mTitle = title;
	}

	public abstract void parseCardItem();

}
