package com.solo.search.card.model;

import org.json.JSONException;
import org.json.JSONObject;

public class VideoItem extends CardItem {

	private String mUrl;
	private String mImg;
	private String mTime;

	public VideoItem(JSONObject jsonObject) {
		super(jsonObject);
	}

	@Override
	public void parseCardItem() {
		try {
			mUrl = mJsonObject.getString("url");
			mImg = mJsonObject.getString("img");
			mTime = mJsonObject.getString("time");
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

	public String getTime() {
		return mTime;
	}

	public void setTime(String time) {
		mTime = time;
	}

}
