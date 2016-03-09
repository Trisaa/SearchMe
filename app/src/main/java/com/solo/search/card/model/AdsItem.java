package com.solo.search.card.model;

import org.json.JSONException;
import org.json.JSONObject;

public class AdsItem extends CardItem {

	private String mUrl;
	private String mType;
	private String mImg;

	public AdsItem(JSONObject jsonObject) {
		super(jsonObject);
	}

	@Override
	public void parseCardItem() {
		try {
			mUrl = mJsonObject.getString("url");
			mType = mJsonObject.getString("type");
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

	public String getType() {
		return mType;
	}

	public void setType(String type) {
		mType = type;
	}

	public String getImg() {
		return mImg;
	}

	public void setImg(String img) {
		mImg = img;
	}

}
