package com.solo.search.suggestion;

import android.graphics.drawable.Drawable;

public class WebSuggestion extends Suggestion {

	private WebSuggestions mWebSuggestions;
	private String mUrl;

	public WebSuggestion(Suggestions suggestions, int id, String title,
			String url) {
		super(suggestions, id, title);
		mWebSuggestions = (WebSuggestions) suggestions;
		mUrl = url;
	}

	public Drawable getIcon() {
		return mWebSuggestions.getIcon();
	}

	public String getUrl() {
		return mUrl;
	}

	public int hashCode() {
		return super.hashCode();
	}

	public boolean equals(Object o) {
		return super.equals(o);
	}

}
