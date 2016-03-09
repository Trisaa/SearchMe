package com.solo.search.card.model;

import org.json.JSONException;
import org.json.JSONObject;

public class GameItem extends CardItem {

	private String mUrl;
	private String mImg;

	public GameItem(JSONObject jsonObject) {
		super(jsonObject);
	}

	@Override
	public void parseCardItem() {
		try {
			mUrl = mJsonObject.getString("url");
			mImg = mJsonObject.getString("img");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public String getUrl() {
		return mUrl;
	}

	public void setUrl(String url) {
		mUrl = url;
	}

	public String getImg() {
		return mImg;
	}

	public void setImg(String img) {
		mImg = img;
	}

}
