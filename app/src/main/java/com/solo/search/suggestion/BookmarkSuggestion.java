package com.solo.search.suggestion;

import android.graphics.drawable.Drawable;

public class BookmarkSuggestion extends Suggestion {

	private BookmarkSuggestions mBookmarkSuggestions;
	private String mUrl;

	public BookmarkSuggestion(Suggestions suggestions, int id, String title,
			String url) {
		super(suggestions, id, title, url);
		mBookmarkSuggestions = (BookmarkSuggestions) suggestions;
		mUrl = url;
	}

	public String getUrl() {
		return mUrl;
	}

	@Override
	public Drawable getIcon() {
		return mBookmarkSuggestions.getThumbnail(this);
	}

	public int hashCode() {
		return super.hashCode();
	}

	public boolean equals(Object object) {
		return super.equals(object);
	}

}
