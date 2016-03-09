package com.solo.search.card.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

public class StockItem extends CardItem {

	public static final String UNKOWN = "N/A";

	private String mSymbol;
	private String mPrice;
	private String mChangeValue;
	private String mChangePercent;
	private boolean isRising;
	private String mUrl;

	public StockItem(JSONObject jsonObject) {
		super(jsonObject);
	}

	@Override
	public void parseCardItem() {
		try {
			mSymbol = mJsonObject.getString("symbol");
			mPrice = mJsonObject.getString("price");
			mChangeValue = mJsonObject.getString("changeValue");
			mChangePercent = mJsonObject.getString("changePercent");
			isRising = mJsonObject.getBoolean("rising");
			mUrl = mJsonObject.getString("url");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public String getSymbol() {
		return mSymbol;
	}

	public void setSymbol(String symbol) {
		mSymbol = symbol;
	}

	public String getPrice() {
		return mPrice;
	}

	public void setPrice(String price) {
		mPrice = price;
	}

	public String getChangeValue() {
		return mChangeValue;
	}

	public void setChangeValue(String changeValue) {
		mChangeValue = changeValue;
	}

	public String getChangePercent() {
		return mChangePercent;
	}

	public void setChangePercent(String changePercent) {
		mChangePercent = changePercent;
	}

	public String getUrl() {
		return mUrl;
	}

	public void setUrl(String url) {
		mUrl = url;
	}

	public boolean isRising() {
		return isRising;
	}

	public void setRising(boolean isRising) {
		this.isRising = isRising;
	}

	public boolean isValid() {
		if (TextUtils.isEmpty(mPrice) || mPrice.equalsIgnoreCase(UNKOWN)) {
			return false;
		}
		return true;
	}

	public JSONObject getContentJSONSObject() {
		JSONObject object = new JSONObject();
		try {
			object.put("title", mTitle);
			object.put("symbol", mSymbol);
			object.put("price", mPrice);
			object.put("change", mChangeValue);
			object.put("changePercent", mChangePercent);
			object.put("isRising", isRising);
			object.put("url", mUrl);
		} catch (JSONException e) {
			e.printStackTrace();
			object = null;
		}
		return object;
	}
}
