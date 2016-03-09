package com.solo.search.card.model;

import org.json.JSONException;
import org.json.JSONObject;

public class HotwordItem extends CardItem {

	public static final String DEFAULT_COLOR = "#F4F4F4";

	private String mUrl;
	private String mType;
	private String mImg;
	private String mColor;
	private boolean isHot;
	private boolean isNew;

	public HotwordItem(JSONObject jsonObject) {
		super(jsonObject);
	}

	@Override
	public void parseCardItem() {
		try {
			mUrl = mJsonObject.getString("url");
			mType = mJsonObject.getString("type");
			mImg = mJsonObject.getString("img");
			mColor = mJsonObject.optString("color");
			isHot = mJsonObject.getBoolean("isHot");
			isNew = mJsonObject.getBoolean("isNew");
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

	public String getColor() {
		return mColor;
	}

	public void setColor(String color) {
		mColor = color;
	}

	public void isHot(boolean isHot) {
		this.isHot = isHot;
	}

	public boolean isHot() {
		return isHot;
	}

	public void isNew(boolean isNew) {
		this.isNew = isNew;
	}

	public boolean isNew() {
		return isNew;
	}

}
