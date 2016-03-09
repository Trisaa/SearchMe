package com.solo.search.suggestion;

import android.graphics.drawable.Drawable;

public class AlbumSuggestion extends Suggestion {

	private final AlbumSuggestions mAlbumSuggestions;

	public AlbumSuggestion(Suggestions suggestions, int id, String title,
			String infoText) {
		super(suggestions, id, title, infoText);
		mAlbumSuggestions = (AlbumSuggestions) suggestions;
	}

	@Override
	public Drawable getIcon() {
		return mAlbumSuggestions.getIcon();
	}

	public int hashCode() {
		return super.hashCode();
	}

	public boolean equals(Object o) {
		return super.equals(o);
	}

}
