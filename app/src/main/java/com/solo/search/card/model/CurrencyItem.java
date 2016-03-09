package com.solo.search.card.model;

import org.json.JSONException;
import org.json.JSONObject;

public class CurrencyItem extends CardItem {

	private String mSymbol;
	private double mRate;

	public CurrencyItem(String title, double rate) {
		mTitle = title;
		mRate = rate;
	}

	public CurrencyItem(JSONObject jsonObject) {
		super(jsonObject);
	}

	@Override
	public void parseCardItem() {
		try {
			mSymbol = mJsonObject.getString("symbol");
			mRate = mJsonObject.getDouble("rate");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void setSymbol(String symbol) {
		mSymbol = symbol;
	}

	public String getSymbol() {
		return mSymbol;
	}

	public void setRate(double rate) {
		mRate = rate;
	}

	public double getRate() {
		return mRate;
	}

}
