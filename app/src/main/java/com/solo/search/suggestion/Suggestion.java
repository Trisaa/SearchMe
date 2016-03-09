package com.solo.search.suggestion;

import android.graphics.drawable.Drawable;

public class Suggestion {

	private Suggestions mSuggestions;

	private int mId;
	private String mTitle;	
	private String mInfoText;

	public Suggestion(Suggestions suggestions, int id, String title) {
		mSuggestions = suggestions;
		mId = id;
		mTitle = title;
	}

	public Suggestion(Suggestions suggestions, int id, String title,
			String infoText) {
		mSuggestions = suggestions;
		mId = id;
		mTitle = title;
		mInfoText = infoText;
	}

	public Suggestions getSuggestions() {
		return mSuggestions;
	}

	public void setSuggestions(Suggestions suggestion) {
		mSuggestions = suggestion;
	}

	public int getId() {
		return mId;
	}

	public void setId(int id) {
		mId = id;
	}

	public String getTitle() {
		return mTitle;
	}

	public void setmTitle(String title) {
		mTitle = title;
	}

	public String getInfoText() {
		return mInfoText;
	}

	public void setInfoText(String infoText) {
		mInfoText = infoText;
	}

	public Drawable getIcon() {
		return null;
	}

	public boolean launch() {
		return mSuggestions.launch(this);
	}

	public int hashCode() {
		int hashCode = 17;
		hashCode = hashCode * 31 + mId;
		hashCode = hashCode * 31
				+ (mSuggestions != null ? mSuggestions.getId() : 0);
		return hashCode;
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Suggestion launchable = (Suggestion) o;
		if (mSuggestions != null ? mSuggestions.getId() != launchable
				.getSuggestions().getId() : launchable.getSuggestions() != null)
			return false;
		if (mId != launchable.mId)
			return false;
		return true;
	}

}
