package com.solo.search.card.model;

import org.json.JSONException;
import org.json.JSONObject;

public class HotnewsItem extends CardItem {

	private String mUrl;
	private String mType;
	private String mImg;
	private String mAuthor;

	public HotnewsItem(JSONObject jsonObject) {
		super(jsonObject);
	}

	@Override
	public void parseCardItem() {
		try {
			mUrl = mJsonObject.getString("url");
			mImg = mJsonObject.getString("img");
			mType = mJsonObject.optString("type");
			mAuthor = mJsonObject.optString("author");
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

	public void setAuthor(String author) {
		mAuthor = author;
	}

	public String getAuthor() {
		return mAuthor;
	}

}
